package me.blvckbytes.bottesting.proxies;

import io.netty.channel.ChannelFactory;
import io.netty.channel.socket.oio.OioSocketChannel;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class HTTPProxyOioChannelFactory implements ChannelFactory< OioSocketChannel > {

  private Proxy proxy;
  private String host;
  private int port;

  /**
   * nstantiates a new oio channel factory for http proxies
   * @param proxy Proxy to create socket from
   * @param host Host of target server
   * @param port Port of target server
   */
  public HTTPProxyOioChannelFactory( Proxy proxy, String host, int port ) {
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
    try {
      // Create socket object and write out http connect request
      InetSocketAddress sockAddr = ( InetSocketAddress ) this.proxy.address();
      Socket sock = new Socket( sockAddr.getHostName(), sockAddr.getPort() );
      String proxyConnect = "CONNECT " + this.host + ":" + this.port + " HTTP/1.0\n\n";
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

      return new OioSocketChannel( sock );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Creation of HTTP-Proxy socket failed!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
      return null;
    }
  }
}
