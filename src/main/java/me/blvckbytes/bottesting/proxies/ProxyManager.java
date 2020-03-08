package me.blvckbytes.bottesting.proxies;

import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    List< String > remKeys = new ArrayList<>();
    List< String > keptKeys = new ArrayList<>();

    ExecutorService exec = Executors.newCachedThreadPool();
    AtomicInteger counter = new AtomicInteger( 0 );

    for( String address : this.proxies.keySet() ) {
      int port = this.proxies.get( address );

      // Every socket check gets it's own thread
      exec.execute( () -> {
        Future< Boolean > usable = exec.submit( checkProxy( address, port ) );

        try {
          // Check if proxy is working with a given timeout
          boolean working = usable.get( 5, TimeUnit.SECONDS );

          // Already timed out
          if( remKeys.contains( address ) || keptKeys.contains( address ) )
            return;

          // Add to right list, based on working state
          ( working ? keptKeys : remKeys ).add( address );
        } catch ( Exception e ) {
          // Exception -> Timeout of future or general error, remove
          remKeys.add( address );
        } finally {
          usable.cancel( true );
          System.out.println( "STATUS: " + counter.incrementAndGet() + ", " + remKeys.size() + ", " + keptKeys.size() );
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
