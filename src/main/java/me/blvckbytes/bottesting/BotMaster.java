package me.blvckbytes.bottesting;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import lombok.Getter;
import me.blvckbytes.bottesting.mastercmds.*;
import me.blvckbytes.bottesting.mcleaks.MCLAuth;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import org.spacehq.mc.auth.data.GameProfile;
import org.spacehq.mc.auth.exception.request.RequestException;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class BotMaster {

  // Bot management
  @Getter
  private List< MCBot > bots;
  public MCBot currSel;
  private boolean isMCL;
  private String sessPath;

  // Proxy management
  @Getter
  private ProxyManager proxyManager;

  // Registered commands
  @Getter
  private List< MasterCommand > commands;

  // Target server details
  private String server;
  private int port;

  /**
   * Initialize a new bot master to command multiple bots targetting
   * a specific server address with it's corresponding port. Also acts
   * as a built-in mcleaks authenticator, if wished
   * @param server Server to target
   * @param port Port of the target server
   * @param isMCL If mcleaks accounts will get used
   */
  public BotMaster( String server, int port, boolean isMCL ) {
    this.bots = new ArrayList<>();
    this.commands = new ArrayList<>();
    this.port = port;
    this.server = server;
    this.isMCL = isMCL;
    this.proxyManager = new ProxyManager( "https://www.socks-proxy.net/" );

    // Get location for file storage
    try {
      URI loc = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
      this.sessPath = new File( loc ).getAbsolutePath() + "/" + "persistent_sessions.ser";
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to get the path for sess-file!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }

    // Register all OOP commands
    registerCommands();
  }

  /**
   * Initializes the mcleaks authenticator and launches the
   * command prompt for bot controls
   */
  public void begin() {
    // Create authentication redirect if needed
    if( this.isMCL )
      MCLAuth.getInst().apply();

    // Hide those ugly reflect warnings from java
    System.err.close();

    // Open the console
    beginConsole();
    loadSessions();
  }

  /**
   * Re-aims the whole network onto a new target server
   * @param host New target host address
   * @param port New target port
   */
  public void reaim( String host, int port ) {
    this.server = host;
    this.port = port;

    for( MCBot bot : this.bots )
      bot.changeTarget( this.server, this.port );
  }

  /**
   * Write all current managed sessions into a file for later use
   */
  public void saveSessions() {
    List< PersistentSession > writeableSessions = new ArrayList<>();

    // Loop all current available bots and append them to buffer
    for( MCBot bot : this.bots ) {
      // Extract needed data from bot
      MinecraftProtocol prot = ( MinecraftProtocol ) bot.getClient().getPacketProtocol();
      GameProfile prof = prot.getProfile();
      String accessToken = prot.getAccessToken();
      String clientToken = prot.getClientToken();

      // Add to write buffer in PersistentSession container
      writeableSessions.add( new PersistentSession( prof, accessToken, clientToken ) );
    }

    try {
      // Open object stream
      FileOutputStream fileOut = new FileOutputStream( sessPath );
      ObjectOutputStream out = new ObjectOutputStream( fileOut );

      // Write out list
      out.writeObject( writeableSessions );

      // Close streams
      out.close();
      fileOut.close();
    } catch ( IOException e ) {
      SimpleLogger.getInst().log( "Could not serialize sessions to file!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
    }

    SimpleLogger.getInst().log( "Wrote all current sessions to file!", SLLevel.ERROR );
  }

  /**
   * Load sessions from session file and register them into
   * the bot master's management pool
   */
  private void loadSessions() {
    try {
      File f = new File( sessPath );

      // Create if non existent
      if( !f.exists() && !f.createNewFile() ) {
        SimpleLogger.getInst().log( "It seems like session file path is not writable! Cancelling...", SLLevel.ERROR );
        return;
      }

      // Open object stream
      FileInputStream fileIn = new FileInputStream( f.getAbsolutePath() );
      ObjectInputStream in = new ObjectInputStream( fileIn );

      // Read sessions
      List< PersistentSession > sessions = ( List< PersistentSession > ) in.readObject();

      // Iterate sessions
      int loaded = 0;
      for( PersistentSession session : sessions ) {
        try {
          // Open new bot and register in master
          MinecraftProtocol protocol = new MinecraftProtocol( session.prof, session.accessToken, session.clientToken );
          MCBot bot = new MCBot( this.server, this.port, protocol, this );
          this.bots.add( bot );

          // Increase counter
          loaded++;
        } catch ( Exception e ) {
          SimpleLogger.getInst().log( "Could not load a bot from file!", SLLevel.ERROR );
          SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
        }
      }

      SimpleLogger.getInst().log( "Loaded " + loaded + " bots from file!", SLLevel.MASTER );

      // Close streams
      in.close();
      fileIn.close();
    } catch ( EOFException eof ) {
      SimpleLogger.getInst().log( "Sessions file has never been written to!", SLLevel.WARNING );
    }
    catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not deserialize sessions from file!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Add a bot to this bot's management pool
   * @param using Either a username/email or a mcleaks token
   * @param password Either null (mcleaks) or a actual password
   * @return True if successful, false otherwise
   */
  public boolean addBot( String using, String password ) {
    // Initialize bot object
    MCBot bot = new MCBot( using, password, this.server, this.port, this );

    // Try to authenticate
    try {
      bot.auth();
    } catch ( RequestException e ) {
      SimpleLogger.getInst().log( "Could not authenticate with provided credentials!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
      return false;
    }

    // Connect to the target server and add to management pool
    bot.connect();
    this.bots.add( bot );
    return true;
  }

  /**
   * Register all commands for the console
   */
  private void registerCommands() {
    this.commands.add( new MCHelp( this ) );
    this.commands.add( new MCList( this ) );
    this.commands.add( new MCSelect( this ) );
    this.commands.add( new MCUnselect( this ) );
    this.commands.add( new MCNew( this ) );
    this.commands.add( new MCChat( this ) );
    this.commands.add( new MCJoin( this ) );
    this.commands.add( new MCQuit( this ) );
    this.commands.add( new MCCommand( this ) );
    this.commands.add( new MCDestroy( this ) );
    this.commands.add( new MCSave( this ) );
    this.commands.add( new MCTarget( this ) );
    this.commands.add( new MCRunPipe( this ) );
    this.commands.add( new MCHand( this ) );
  }

  /**
   * Begin to listen for commands and dispatch them
   */
  private void beginConsole() {
    new ControlConsole( input -> {

      try {

        String[] parts = input.split( " " );

        // Search for applicable command
        boolean found = false;
        for( MasterCommand mc : this.commands ) {
          if( !mc.command.equalsIgnoreCase( parts[ 0 ].toLowerCase() ) )
            continue;

          // Don't allow subterminal commands to be executed in main prompt
          if( mc.isSubterminal && this.currSel == null ) {
            SimpleLogger.getInst().log( "The " + mc.command + " command is only usable in the subterminal!", SLLevel.WARNING );
          }

          // Call command with args array
          else {
            // Strip off first element in array (so first arg is 0th elem)
            String[] args = new String[ parts.length - 1 ];
            for( int i = 0; i < parts.length; i++ ) {
              if( i == 0 ) continue;
              args[ i - 1 ] = parts[ i ];
            }

            // Pass args to command
            mc.call( args );
          }

          // Proper command found, stop
          found = true;
          break;
        }

        // Command not found
        if( !found )
          SimpleLogger.getInst().log( "Unknown command, type help for help!", SLLevel.WARNING );

      } catch ( Exception e ) {
        SimpleLogger.getInst().log( "Error while handling command prompt!", SLLevel.ERROR );
        SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
      }

    } );

    SimpleLogger.getInst().log( "Master console up and running!", SLLevel.MASTER );
  }
}
