package me.blvckbytes.bottesting.proxies;

import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.SimpleRequest;
import me.blvckbytes.bottesting.utils.Utils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PSFreeProxyListNET implements ProxyScanner {

  @Override
  public List< Proxy > yieldResults() {
    List< Proxy > buffer = new ArrayList<>();

    try {
      // Fetch website data
      SimpleRequest req = new SimpleRequest( "https://www.free-proxy-list.net/", "GET" );
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
        buffer.add( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( proxyIP, Integer.parseInt( proxyPort ) ) ) );
      }
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not fetch proxy list from " + getClass().getName() + " page!", SLLevel.ERROR );
    }

    return buffer;
  }
}
