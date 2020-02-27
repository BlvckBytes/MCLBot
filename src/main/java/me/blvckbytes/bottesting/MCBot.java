package me.blvckbytes.bottesting;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.values.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.values.MessageType;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import lombok.Getter;
import me.blvckbytes.bottesting.utils.Utils;
import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.UUID;

public class MCBot {

  // Bot managing and commanding
  @Getter
  private Client client;
  private BotMaster master;
  private String token;
  private String password;

  // 80.91.190.188:8181
  private Proxy proxy = Proxy.NO_PROXY; // new Proxy( Proxy.Type.SOCKS, new InetSocketAddress( "80.91.190.188", 8181 ) );

  // Target server details
  private String address;
  private int port;

  // Bot properties
  private int entID;
  private FullLocation lastPos;
  private boolean hasJoinRespawned;
  private boolean hasRejoined;

  /**
   * Initialize a new minecraft bot for all further desires to be executed
   * @param using Either a mcleaks token (if auth is active), or the minecraft username / mail
   * @param password Either null (mcleaks) or a acutal account password
   * @param address Address of target server
   * @param port Port of target server
   * @param master Bot-Master controlling this bot, null if it's only a single instance
   */
  public MCBot( String using, String password, String address, int port, BotMaster master ) {
    this.token = using;
    this.address = address;
    this.port = port;
    this.master = master;
    this.password = password;

    // this.proxy = master.getProxyManager().getProxy();
  }

  /**
   * Creates a new bot from an existing minecraftprotocol session
   * @param address Address of target server
   * @param port Port of target server
   * @param protocol Existing session
   * @param master Bot-Master controlling this bot, null if it's only a single instance
   */
  public MCBot( String address, int port, MinecraftProtocol protocol, BotMaster master ) {
    this.master = master;
    this.address = address;
    this.port = port;

    //this.proxy = master.getProxyManager().getProxy();
    this.client = new Client( this.address, this.port, protocol, new TcpSessionFactory( this.proxy ) );
    listen();
  }

  /**
   * Event method for incoming packets
   * @param event Packet event with informations
   */
  private void packetEvent( PacketReceivedEvent event ) {
    Packet packet = event.getPacket();
    //SimpleLogger.getInst().log( "Incoming: " + event.getPacket().getClass().getSimpleName(), SLLevel.INFO );

    if( packet instanceof ServerPlayerPositionRotationPacket ) {
      ServerPlayerPositionRotationPacket pos = ( ServerPlayerPositionRotationPacket ) packet;
      this.lastPos = new FullLocation( pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch() );

      if( !hasJoinRespawned ) {
        Utils.delayPacket( this.client, new ClientRequestPacket( ClientRequest.RESPAWN ), 100 );
        hasJoinRespawned = true;
        return;
      }
    }

    // Join packet
    if( packet instanceof ServerJoinGamePacket ) {
      this.entID = ( ( ServerJoinGamePacket ) packet ).getEntityId();
      SimpleLogger.getInst().log( "Connected to " + client.getHost() + ":" + client.getPort() + "!", SLLevel.INFO );
      return;
    }

    // Chat packet
    if( packet instanceof ServerChatPacket ) {
      ServerChatPacket chatPacket = event.getPacket();

      // Automatic respawn on death
      if( chatPacket.getType() == MessageType.SYSTEM && chatPacket.getMessage().toString().startsWith( "death." ) ) {
        Utils.delayPacket( this.client, new ClientRequestPacket( ClientRequest.RESPAWN ), 500 );
        Utils.delayPacket( this.client, new ClientPlayerPositionPacket(
          false, this.lastPos.getX() + 0.1, this.lastPos.getY(), this.lastPos.getZ()
        ), 600 );

        // No printing if muted
        if( isMuted() )
          return;

        SimpleLogger.getInst().log( "Bot died, automatic respawn packet sent!", SLLevel.WARNING );
        return;
      }

      // No printing if muted
      if( isMuted() )
        return;

      // Log chat
      if( chatPacket.getType() != MessageType.NOTIFICATION )
        SimpleLogger.getInst().log( "(" + chatPacket.getType() + ") " + chatPacket.getMessage(), SLLevel.GAME );
    }

  }

  /**
   * Checks wether this bot is muted (not selected) by the master
   * @return True if selected, false otherwise
   */
  private boolean isMuted() {
    // No master, thus not muteable
    if( this.master == null )
      return false;

    // No bot selected, all are muted
    if( this.master.currSel == null )
      return true;

    // If not selected, mute
    return !this.master.currSel.equals( this );
  }

  /**
   * Returns the name this bot would join with
   * @return Name
   */
  public String getName() {
    return ( ( MinecraftProtocol ) this.getClient().getSession().getPacketProtocol() ).getProfile().getName();
  }

  /**
   * Checks wether this bot is online
   * @return State of bot
   */
  public boolean isOnline() {
    return this.getClient().getSession().isConnected();
  }

  /**
   * Connect to the provided server
   */
  public void connect() {
    try {
      // Login
      this.client.getSession().connect();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to connect the bot " + getName() + "!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
    }
  }

  /**
   * Authenticates with MCLeaks and creates the client instance
   * @throws RequestException Exceptions for non possible authenticate tries
   */
  public void auth() throws RequestException {
    // MCLeaks does not need a password, thus just send random stuff
    if( this.password == null )
      this.password = UUID.randomUUID().toString();

    // Create minecraft protocol instance with credentials
    MinecraftProtocol protocol = new MinecraftProtocol( this.token, this.password, false );
    SimpleLogger.getInst().log( "Successfully authenticated with provided credentials!", SLLevel.INFO );

    // Create client
    this.client = new Client( this.address, this.port, protocol, new TcpSessionFactory( this.proxy ) );
    listen();
  }

  /**
   * Reconnects to the target server
   */
  public void reconnect() {
    this.hasRejoined = true;
    refresh( ( MinecraftProtocol ) getClient().getPacketProtocol() );
    getClient().getSession().connect();
  }

  /**
   * Refreshes the client (new socket-session, since one cannot be reused)
   * and also refreshes the minecraftprotocol so the subprotocol states get set
   * back to startout values. Thus, use this when rejoining
   * @param protocol Old MinecraftProtocol object from previous session
   */
  private void refresh( MinecraftProtocol protocol ) {
    try {
      MinecraftProtocol newProt = new MinecraftProtocol( protocol.getProfile(), protocol.getAccessToken(), protocol.getClientToken() );
      this.client = new Client( this.address, this.port, newProt, new TcpSessionFactory( this.proxy ) );
      listen();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to refresh MinecraftProtocol!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Begin listening for packets and redirect them to event method, also
   * listen for the disconnect packet to log the action
   */
  private void listen() {
    this.client.getSession().addListener( new SessionAdapter() {

      @Override
      public void packetReceived( PacketReceivedEvent event ) {
        packetEvent( event );
      }

      @Override
      public void disconnected( DisconnectedEvent event ) {
        String reason = Message.fromString( event.getReason() ).getFullText();
        SimpleLogger.getInst().log( "Disconnected: " + reason, SLLevel.WARNING );

        // Detect issues on the first join attempt and try to fix them by performing a rejoin
        if( reason.toLowerCase().contains( "exception" ) && !hasRejoined ) {
          SimpleLogger.getInst().log( "Detected bot firstjoin issue, performing rejoin delayed by 2s!", SLLevel.INFO );
          hasRejoined = true;

          // Perform delayed rejoin
          Utils.delayExec( func -> {
            reconnect();
          }, 2000 );
        }

        // Print cause if exists aswell
        if( event.getCause() != null )
          event.getCause().printStackTrace();
      }

    } );
  }
}
