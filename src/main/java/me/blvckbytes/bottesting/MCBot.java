package me.blvckbytes.bottesting;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.ItemStack;
import com.github.steveice10.mc.protocol.data.game.values.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.values.MessageType;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import lombok.Getter;
import lombok.Setter;
import me.blvckbytes.bottesting.proxies.TcpProxySessionFactory;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;
import org.spacehq.mc.auth.exception.request.RequestException;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.event.session.DisconnectedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.packet.Packet;

import java.net.Proxy;
import java.util.*;

public class MCBot {

  // Bot managing and commanding
  @Getter
  private Client client;
  private BotMaster master;
  private String token;
  private String password;

  private Proxy proxy = null;// new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "103.37.81.92", 42420 ) );

  // Target server details
  private String address;
  private int port;

  // Bot properties
  private List< PacketMonitor > monitors;
  private Map< Integer, FullLocation > players;

  @Getter
  private Map< Integer, ItemStack[] > currentItems;

  @Getter @Setter
  private FullLocation lastLoc;

  /**
   * Empty constructor is only used for common called stuff, should
   * never ever be accessible from outside
   */
  private MCBot() {
    this.monitors = new ArrayList<>();
    this.currentItems = new HashMap<>();
    this.players = new HashMap<>();
    createMonitors();
  }

  /**
   * Initialize a new minecraft bot for all further desires to be executed
   * @param using Either a mcleaks token (if auth is active), or the minecraft username / mail
   * @param password Either null (mcleaks) or a acutal account password
   * @param address Address of target server
   * @param port Port of target server
   * @param master Bot-Master controlling this bot, null if it's only a single instance
   */
  public MCBot( String using, String password, String address, int port, BotMaster master ) {
    this();
    this.token = using;
    this.address = address;
    this.port = port;
    this.master = master;
    this.password = password;
  }

  /**
   * Creates a new bot from an existing minecraftprotocol session
   * @param address Address of target server
   * @param port Port of target server
   * @param protocol Existing session
   * @param master Bot-Master controlling this bot, null if it's only a single instance
   */
  public MCBot( String address, int port, MinecraftProtocol protocol, BotMaster master ) {
    this();
    this.master = master;
    this.address = address;
    this.port = port;

    //this.proxy = master.getProxyManager().getProxy();
    this.client = new Client( this.address, this.port, protocol, new TcpProxySessionFactory( this.proxy ) );
    listen();
  }

  /**
   * Register a monitor to start listening for specified
   * packet type until destroyed
   * @param monitor PacketMonitor instance
   */
  public void registerMonitor( PacketMonitor monitor ) {
    this.monitors.add( monitor );
  }

  /**
   * Changes the target of
   * @param host New host address
   * @param port New port
   */
  public void changeTarget( String host, int port ) {
    this.address = host;
    this.port = port;

    // Disconnect from current target
    getClient().getSession().disconnect( "Master is changing the target." );

    // Connect again with updated socket
    reconnect();
  }

  /**
   * Register used monitors
   */
  private void createMonitors() {
    // Monitor for keeping track of the current items
    PacketMonitor itemMonitor = new PacketMonitor( ServerWindowItemsPacket.class );
    itemMonitor.setCallback( itemInfo -> {
      // Cache items corresponding to window id
      ServerWindowItemsPacket itemPacket = ( ServerWindowItemsPacket ) itemInfo;
      this.currentItems.put( itemPacket.getWindowId(), itemPacket.getItems() );
    } );
    registerMonitor( itemMonitor );

    // Monitor for spawning players (to cache entity ids)
    PacketMonitor playerMonitor = new PacketMonitor( ServerSpawnPlayerPacket.class );
    playerMonitor.setCallback( playerInfo -> {
      ServerSpawnPlayerPacket playerPacket = ( ServerSpawnPlayerPacket ) playerInfo;

      // Location and entity id to identify this player
      int entID = playerPacket.getEntityId();
      FullLocation position = new FullLocation(
        playerPacket.getX(), playerPacket.getY(), playerPacket.getZ(), playerPacket.getYaw(), playerPacket.getPitch()
      );

      // Write to cache
      this.players.put( entID, position );
    } );
    registerMonitor( playerMonitor );

    // Monitor for respawing on join to avoid issues executing commands
    PacketMonitor joinRespawnMonitor = new PacketMonitor( ServerPlayerPositionRotationPacket.class );
    joinRespawnMonitor.setCallback( posInfo -> {
      // Issue a respawn and destroy the monitor (only use once)
      Utils.delayPacket( this.client, new ClientRequestPacket( ClientRequest.RESPAWN ), 100 );
      joinRespawnMonitor.destroy();
    } );
    registerMonitor( joinRespawnMonitor );

    // Monitor for keeping track of player's location
    PacketMonitor locationMonitor = new PacketMonitor( ServerPlayerPositionRotationPacket.class );
    locationMonitor.setCallback( posInfo -> {
      ServerPlayerPositionRotationPacket posPacket = ( ServerPlayerPositionRotationPacket ) posInfo;
      this.lastLoc = new FullLocation( posPacket.getX(), posPacket.getY(), posPacket.getZ(), posPacket.getYaw(), posPacket.getPitch() );
    } );
    registerMonitor( locationMonitor );

    // Monitor for deaths in order to automatically respawn bot
    PacketMonitor deathMonitor = new PacketMonitor( ServerChatPacket.class );
    deathMonitor.setCallback( chatInfo -> {
      ServerChatPacket chatPacket = ( ServerChatPacket ) chatInfo;

      // Only listen for specific message structure signaling death
      if( !( chatPacket.getType() == MessageType.SYSTEM && chatPacket.getMessage().toString().startsWith( "death." ) ) )
        return;

      // Issue a delayed respawn
      Utils.delayPacket( this.client, new ClientRequestPacket( ClientRequest.RESPAWN ), 500 );

      // No printing if muted
      if( isMuted() )
        return;

      // Notify
      SimpleLogger.getInst().log( "Bot died, automatic respawn packet sent!", SLLevel.WARNING );
    } );
    registerMonitor( deathMonitor );
  }

  /**
   * Event method for incoming packets
   * @param event Packet event with informations
   */
  private void packetEvent( PacketReceivedEvent event ) {
    Packet packet = event.getPacket();

    // Iterate from behind since delete is needed
    for( int i = this.monitors.size() - 1; i >= 0; i-- ) {
      PacketMonitor monitor = monitors.get( i );

      // Delete destroyed monitors
      if( monitor.getCallback() == null )
        monitors.remove( i );

      // Skip if class doesn't match
      if( packet.getClass() != monitor.getTarget() )
        continue;

      // Invoke callback
      monitor.getCallback().call( packet );
    }

    // Join packet, notify in console
    if( packet instanceof ServerJoinGamePacket ) {
      SimpleLogger.getInst().log( "Connected to " + client.getHost() + ":" + client.getPort() + "!", SLLevel.INFO );
    }

    // Chat packet, notify in console
    else if( packet instanceof ServerChatPacket ) {
      ServerChatPacket chatPacket = event.getPacket();

      // Bot not selected
      if( isMuted() )
        return;

      // Don't care about action bar stuff
      if( chatPacket.getType() == MessageType.NOTIFICATION )
        return;

      // Log messages of all types
      SimpleLogger.getInst().log( "(" + chatPacket.getType() + ") " + chatPacket.getMessage(), SLLevel.GAME );
    }
  }

  /**
   * Get a players entity id by his location
   * @param loc Location searching for
   * @return Entity id of player
   */
  public int getPlayerByLoc( FullLocation loc ) {
    for( int entID : this.players.keySet() ) {
      FullLocation curr = this.players.get( entID );
      double dist = curr.distTo( loc );

      // Return if distance smaller or equal to one block
      if( dist <= 1 )
        return entID;
    }

    // Not found
    return -1;
  }

  /**
   * Send the current position to the server in order to update
   * all previous actions
   */
  public void sendPosition() {
    FullLocation loc = this.master.currSel.getLastLoc();
    getClient().getSession().send(
        new ClientPlayerPositionRotationPacket( true, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch() )
    );
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
    this.client = new Client( this.address, this.port, protocol, new TcpProxySessionFactory( this.proxy ) );
    listen();
  }

  /**
   * Reconnects to the target server
   */
  public void reconnect() {
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
      this.client = new Client( this.address, this.port, newProt, new TcpProxySessionFactory( this.proxy ) );
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

        // Print cause if exists aswell
        if( event.getCause() != null )
          event.getCause().printStackTrace();
      }

    } );
  }
}
