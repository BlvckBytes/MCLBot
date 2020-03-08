package me.blvckbytes.bottesting.proxies;

import me.blvckbytes.bottesting.utils.RUtils;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.tcp.TcpClientSession;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

import java.net.Proxy;

public class TcpProxySessionFactory extends TcpSessionFactory {

  /**
   * Creates a new session factory for use with http proxies
   * @param clientProxy HTTP proxy to use
   */
  public TcpProxySessionFactory( Proxy clientProxy ) {
    super( clientProxy );

    // Notify of wrong proxy type
    if( clientProxy != null && !clientProxy.type().equals( Proxy.Type.HTTP ) )
      SimpleLogger.getInst().log( "Tried to use a non-http proxy with factory, this won't work!", SLLevel.ERROR );
  }

  /**
   * Overridden creation method in order to apply my modified proxy session class
   * @param client Client to create the session for
   * @return Session instance
   */
  @Override
  public Session createClientSession( Client client ) {
    // Find proxy field from superclass
    Proxy clientProxy = ( Proxy ) RUtils.findValue( this.getClass(), "clientProxy", this );

    // No proxy specified
    if( clientProxy == null )
      return new TcpClientSession( client.getHost(), client.getPort(), client.getPacketProtocol(), client, null );

    // Open HTTP proxy
    return new TcpClientProxySession( client.getHost(), client.getPort(), client.getPacketProtocol(), client, clientProxy );
  }
}
