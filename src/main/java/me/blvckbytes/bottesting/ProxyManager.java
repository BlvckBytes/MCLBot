package me.blvckbytes.bottesting;

import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.SimpleRequest;
import me.blvckbytes.bottesting.utils.Utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyManager {

  private List< ProxyEntry > proxies;
  private int currEntryID;
  private String address;

  /**
   * Scraps all proxies off the given link and manage outputting. When a
   * proxy object is requested, it cycles to the next one. Before outputting,
   * the proxy is checked so we can make sure sockets will be successful.
   * @param address Address of page to scrap
   */
  public ProxyManager( String address ) {
    this.proxies = new ArrayList<>();
    this.address = address;
    this.currEntryID = 0;

    scanTable();
  }

  /**
   * Get a proxy for use, it will cycle by itself and validate
   * before outputting
   * @return A working proxy or null if non existent
   */
  public Proxy getProxy() {
    // Initial get
    Proxy result = getProxyHelper();

    // Get next proxy until a valid one is found
    while( !isValid( result ) && this.currEntryID != this.proxies.size() - 1 )
      result = getProxyHelper();

    // Never return a non valid proxy, since this can lead to huge errors
    // Rather return null
    if( !isValid( result ) )
      return null;

    // Return valid proxy
    return result;
  }

  /**
   * Check if a proxy is able to actually transmit data
   * @param input Proxy to check
   * @return True if working, false otherwise
   */
  private boolean isValid( Proxy input ) {
    return true;
  }

  /**
   * Generate proxy object from ip and port
   * @return Proxy object
   */
  private Proxy getProxyHelper() {

    // No proxies available
    if( this.proxies.size() == 0 )
      return null;

    // Create proxy data objects
    ProxyEntry currEntry = this.proxies.get( this.currEntryID );
    SocketAddress endpoint = new InetSocketAddress( currEntry.getIp(), currEntry.getPort() );

    // Next proxy entry
    this.currEntryID++;
    if( this.currEntryID == this.proxies.size() )
      this.currEntryID = 0;

    // Return proxy object
    return new Proxy( Proxy.Type.SOCKS, endpoint );
  }

  /**
   * Scrap all proxies off the given website
   */
  public void scanTable() {
    try {
      // Fetch website data
      SimpleRequest req = new SimpleRequest( this.address, "GET" );
      String content = req.call( null );

      // Compile needed patterns and match all tr lines
      Pattern targetTag = Pattern.compile( "<tr>((?!</tr>).)+</tr>" );
      Pattern ipPart = Pattern.compile( "<td>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}</td>" );
      Pattern portPart = Pattern.compile( "<td>[0-9]{1,5}</td>" );
      Matcher trLines = targetTag.matcher( content );

      // Loop all tr line occurences
      while ( trLines.find() ) {
        String line = trLines.group();

        // Match regex on current line
        Matcher ipMatcher = ipPart.matcher( line );
        Matcher portMatcher = portPart.matcher( line );

        // If not ip and port have been found in line, jump to next
        if( !ipMatcher.find() || !portMatcher.find() )
          continue;

        // Strip td tags
        String proxyIP = ipMatcher.group().replaceAll( "</?td>", "" ).trim();
        String proxyPort = portMatcher.group().replaceAll( "</?td>", "" ).trim();

        // Port is no integer, this input is unneeded
        if( !Utils.isInt( proxyPort ) )
          continue;

        // Add to pool
        this.proxies.add( new ProxyEntry( proxyIP, Integer.parseInt( proxyPort ) ) );
      }

      SimpleLogger.getInst().log( "Fetched " + this.proxies.size() + " proxies from webservice!", SLLevel.MASTER );
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not fetch proxy list from page!", SLLevel.ERROR );
    }
  }
}
