package me.blvckbytes.bottesting.proxies;

import io.netty.channel.ChannelFactory;
import io.netty.channel.socket.oio.OioSocketChannel;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.*;

public class HttpProxyOioChannelFactory implements ChannelFactory< OioSocketChannel > {

  private HttpProxy proxy;
  private String host;
  private int port;
  private static ExecutorService exec;

  static {
    exec = Executors.newCachedThreadPool();
  }

  /**
   * nstantiates a new oio channel factory for http proxies
   * @param proxy Proxy to create socket from
   * @param host Host of target server
   * @param port Port of target server
   */
  public HttpProxyOioChannelFactory( HttpProxy proxy, String host, int port ) {
    this.proxy = proxy;
    this.host = host;
    this.port = port;
  }

  /**
   * Create a new channel with the given proxy applied onto it's
   * socket used for connection
   * @return IONetty socket channel
   */
  public OioSocketChannel newChannel() {
    Future< Socket > usable = null;
    try {
      usable = exec.submit( createSock() );
      Socket sock = usable.get( 5, TimeUnit.SECONDS );

      // Didn't work in createSocket internally
      if( sock == null ) {
        usable.cancel( true );
        throw new Exception( "Socket was null" );
      }

      return new OioSocketChannel( sock );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error with proxy, grabbing a new one!", SLLevel.MASTER );
      SimpleLogger.getInst().log( e, SLLevel.MASTER );

      // Cancel if existing
      if( usable != null )
        usable.cancel( true );

      // Get new proxy and call again
      this.proxy = ProxyManager.getInst().getProxy();
      return newChannel();
    }
  }

  /**
   * Helper method for newChannel(), is used to create a socket within a
   * reasonable timespan. If this wont work, it gets called again with a new proxy
   * set to the instance of this factory.
   */
  private Callable< Socket > createSock() {
    return () -> {
      try {
        // Create socket object and write out http connect request
        Socket sock = new Socket( proxy.getHost(), proxy.getPort() );
        String proxyConnect = "CONNECT " + this.host + ":" + this.port + " HTTP/1.1";

        if( this.proxy.hasLogin ) {
          String loginC = new String( Base64.getEncoder().encode( ( proxy.getUser() + ":" + proxy.getPasswd() ).getBytes() ) );
          proxyConnect += "\nProxy-Authorization: basic " + loginC;
        }

        proxyConnect += "\n\n";

        sock.getOutputStream().write( proxyConnect.getBytes() );

        // Create buffer and read server's answer into it
        byte[] bytebuf = new byte[ 512 ];
        InputStream socketInput = sock.getInputStream();
        int len = socketInput.read( bytebuf, 0, bytebuf.length );

        // Server didn't respond at all
        if ( len == 0 )
          throw new SocketException( "Invalid response from proxy" );

        String resp = new String( bytebuf, StandardCharsets.UTF_8 ).trim();

        // Expecting HTTP/1.x 200 OK, otherwise socket creation failed
        if ( !resp.contains( "200" ) )
          throw new Exception( "Failed to create http socket -> " + resp );

        // Everything okay, notify of response
        SimpleLogger.getInst().log( "Socket successfully opened: " + resp.replaceAll( "\n", "," ), SLLevel.INFO );

        // Flush any outstanding message in buffer
        if ( socketInput.available() > 0 )
          socketInput.skip( socketInput.available() );

        return sock;
      } catch ( Exception e ) {
        SimpleLogger.getInst().log( "Creation of HTTP-Proxy socket failed!", SLLevel.ERROR );
        return null;
      }
    };
  }
}
