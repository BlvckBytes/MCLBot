package me.blvckbytes.bottesting.proxies;

import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyManager {

  private Map< String, Integer > proxies;
  private List< ProxyScanner > scanners;

  /**
   * Scraps all proxies off multiple webpages registered. When a proxy
   * object is requested, it cycles to the next one. Before outputting,
   * the proxy is checked so we can make sure sockets will be successful.
   */
  public ProxyManager() {
    this.proxies = new HashMap<>();
    this.scanners = new ArrayList<>();

    // Register all known scanners and collect their entries
    registerScanners();
    collectEntries();

    // Filter out unusable proxies
    filterUnusable();
  }

  /**
   * Register all various scanners to query from
   */
  private void registerScanners() {
    this.scanners.add( new PSFreeProxyListNET() );
  }

  /**
   * Collect all entries and generate a unique list,
   * existing address proxies get dropped
   */
  private void collectEntries() {
    // Get results from individual scanners
    for( ProxyScanner scanner : this.scanners ) {
      List< Proxy > newProxies = scanner.yieldResults();

      // Loop new proxies to add if non existent
      for( Proxy newProxy : newProxies ) {
        InetSocketAddress addrInfo = ( InetSocketAddress ) newProxy.address();
        String addr = addrInfo.getAddress().getHostAddress();

        // Already having this proxy
        if( this.proxies.containsKey( addr ) )
          continue;

        // Add to cache
        this.proxies.put( addr, addrInfo.getPort() );
      }
    }

    // Notify of success
    SimpleLogger.getInst().log( "Scrapped " + this.proxies.size() + " proxy entries!", SLLevel.MASTER );
  }

  /**
   * Filter out unusable proxies based on timeout and the
   * correct 200 OK response to CONNECT request
   */
  private void filterUnusable() {
    // Buffer variables to keep track of keys to remove, checked proxies aswell as percentages given
    List< String > remKeys = Collections.synchronizedList( new ArrayList<>() );
    AtomicInteger checkCounter = new AtomicInteger( 0 );
    AtomicInteger lastPercent = new AtomicInteger( 0 );

    // Begin progress tracker
    SimpleLogger.getInst().log( "Starting to eliminate bad proxies...", SLLevel.MASTER );
    SimpleLogger.getInst().logInlineBegin( "Status: ", SLLevel.MASTER );

    // No proxies to loop, thus done
    if( this.proxies.size() == 0 )
      SimpleLogger.getInst().logInline( "100%\n" );

    // Multithreaded proxy check, loop all proxy entries
    ExecutorService exec = Executors.newCachedThreadPool();
    for( String address : this.proxies.keySet() ) {
      int port = this.proxies.get( address );

      // Every socket check gets it's own thread
      exec.execute( () -> {
        Future< Boolean > usable = exec.submit( checkProxy( address, port ) );

        try {
          // Proxy usable, do nothing
          if( usable.get( 5, TimeUnit.SECONDS ) )
            return;

          // Proxy not usable, add to remove keys
          remKeys.add( address );
        } catch ( Exception e ) {
          // Exception -> Timeout of future or general error, remove
          remKeys.add( address );
        } finally {
          // Every proxy checked, counter is at size of proxy list
          if( checkCounter.incrementAndGet() == this.proxies.size() ) {

            // Remove proxies now
            for( String remKey : remKeys )
              this.proxies.remove( remKey );

            // Terminate percentage line
            SimpleLogger.getInst().logInline( "100%\n" );
            SimpleLogger.getInst().log( "Eliminated " + remKeys.size() + " unusable proxies!", SLLevel.MASTER );
          }

          else {
            // Tell percent of proxies checked
            int currPercent = ( int ) Math.floor( 100F * ( float ) checkCounter.get() / ( float ) proxies.size() );
            if( currPercent % 10 == 0 && currPercent != lastPercent.get() ) {
              // Set last percent to new value
              lastPercent.set( currPercent );

              // Log new percentage
              SimpleLogger.getInst().logInline( currPercent + "% " );
            }

            // Cancel socket checking thread
            usable.cancel( true );
          }
        }
      } );
    }
  }

  /**
   * Checks wether a proxy is working or not
   * @param address IP address of proxy server
   * @param port Port the socket service listens on
   * @return Callable with boolean as result, describing working state
   */
  private Callable< Boolean > checkProxy( String address, int port ) {
    return () -> {
      Socket sock = null;
      try {
        // Open socket and get command
        sock = new Socket( address, port );
        sock.setSoTimeout( 3500 );
        String proxyCommand = "CONNECT 127.0.0.1:80 HTTP/1.0\n\n";
        sock.getOutputStream().write( proxyCommand.getBytes() );

        // Read answer into byte buffer
        byte[] bytebuf = new byte[ 512 ];
        InputStream socketInput = sock.getInputStream();
        int respLength = socketInput.read( bytebuf, 0, bytebuf.length );
        String resp = new String( bytebuf, StandardCharsets.UTF_8 ).trim();
        sock.close();

        // True if reponded 200 OK, false otherwise
        return respLength != 0 && resp.contains( "200" );
      } catch ( Exception e ) {
        // Close socket if exists and is open
        if( sock != null && !sock.isClosed() )
          sock.close();

        // Exception -> dead / buggy proxy
        return false;
      }
    };
  }
}
