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

  private HttpProxy proxy;

  /**
   * Creates a new session factory for use with http proxies
   * @param clientProxy HTTP proxy to use
   */
  public TcpProxySessionFactory( HttpProxy clientProxy ) {
    this.proxy = clientProxy;
  }

  /**
   * Overridden creation method in order to apply my modified proxy session class
   * @param client Client to create the session for
   * @return Session instance
   */
  @Override
  public Session createClientSession( Client client ) {
    // No proxy specified
    if( proxy == null )
      return new TcpClientSession( client.getHost(), client.getPort(), client.getPacketProtocol(), client, null );

    // Open HTTP proxy
    return new TcpClientProxySession( client.getHost(), client.getPort(), client.getPacketProtocol(), client, proxy );
  }
}
