package me.blvckbytes.bottesting.proxies;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.concurrent.Future;
import me.blvckbytes.bottesting.utils.RUtils;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.packet.PacketProtocol;
import org.spacehq.packetlib.tcp.TcpPacketCodec;
import org.spacehq.packetlib.tcp.TcpPacketEncryptor;
import org.spacehq.packetlib.tcp.TcpPacketSizer;
import org.spacehq.packetlib.tcp.TcpSession;

public class TcpClientProxySession extends TcpSession {

  private TcpClientProxySession inst;
  private Client client;
  private HttpProxy proxy;
  private EventLoopGroup group;

  /**
   * Create a new tcp session for clients using a proxy
   * @param host Host of server
   * @param port Port of server
   * @param protocol MinecraftProtocol to use
   * @param client Client to use
   * @param proxy Proxy to apply
   */
  public TcpClientProxySession( String host, int port, PacketProtocol protocol, Client client, HttpProxy proxy ) {
    super( host, port, protocol );

    this.client = client;
    this.proxy = proxy;
    inst = this;
  }

  /**
   * Not really connect but initialize everything that interfaces with
   * the socket already created and opened by custom factory
   * @param wait Wether to wait or not, ignored in this context (nothing to wait for)
   */
  public void connect( boolean wait ) {
    // Already disconnected
    if ( this.disconnected )
      throw new IllegalStateException( "Session has already been disconnected." );

    // Already connected
    if( this.group != null )
      return;

    try {
      final Bootstrap bootstrap = new Bootstrap();

      // Proxy should always be given
      if( this.proxy == null )
        throw new Exception( "Proxy needs to be valid, this session type depends on the proxy!" );

      // Pass custom channel factory
      this.group = new OioEventLoopGroup();
      bootstrap.channelFactory( new HttpProxyOioChannelFactory( this.proxy, this.getHost(), this.getPort() ) );

      // Set channel initializer
      ( bootstrap.handler( new ChannelInitializer< Channel > () {

        public void initChannel( Channel channel ) {
          getPacketProtocol().newClientSession( client, inst );
          channel.config().setOption( ChannelOption.IP_TOS, 24 );
          channel.config().setOption( ChannelOption.TCP_NODELAY, false );
          ChannelPipeline pipeline = channel.pipeline();

          refreshReadTimeoutHandler( channel );
          refreshWriteTimeoutHandler( channel );

          pipeline.addLast( "encryption", new TcpPacketEncryptor( inst ) );
          pipeline.addLast( "sizer", new TcpPacketSizer( inst ) );
          pipeline.addLast( "codec", new TcpPacketCodec( inst ) );
          pipeline.addLast( "manager", inst );
        }

      } ).group( this.group ) ).option( ChannelOption.CONNECT_TIMEOUT_MILLIS, this.getConnectTimeout() * 1000 );

      // Invoke protected method initAndRegister (to init i/o on already open socket)
      bootstrap.remoteAddress( this.getHost(), this.getPort() );
      RUtils.findMethod( bootstrap.getClass(), "initAndRegister" ).invoke( bootstrap );
    } catch ( Throwable thr ) {
      this.exceptionCaught( null, thr );
    }
  }

  /**
   * Disconnect session from target server
   * @param reason Disconnect reason
   * @param cause Cause
   * @param wait Await disconnect
   */
  public void disconnect( String reason, Throwable cause, boolean wait ) {
    super.disconnect( reason, cause, wait );

    // No active connection
    if( this.group == null )
      return;

    Future<?> future = this.group.shutdownGracefully();

    // No waiting, thus don't call await
    if( !wait )
      return;

    try {
      future.await();
    } catch (InterruptedException e) {}
  }
}
