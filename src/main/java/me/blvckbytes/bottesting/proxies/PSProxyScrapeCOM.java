package me.blvckbytes.bottesting.proxies;

import me.blvckbytes.bottesting.utils.Utils;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PSProxyScrapeCOM implements ProxyScanner {

  @Override
  public List< Proxy > yieldResults() {
    List< Proxy > buffer = new ArrayList<>();

    try {
      // Initialize scanner onto url
      URL page = new URL( "https://api.proxyscrape.com/?request=getproxies&proxytype=http&timeout=5500&country=all&ssl=all&anonymity=all" );
      Scanner scanner = new Scanner( new InputStreamReader( page.openStream() ) );

      // Scan page line by line
      while( scanner.hasNextLine() ) {
        String[] lineData = scanner.nextLine().trim().split( ":" );

        // Bad format inputting
        if( lineData.length != 2 )
          continue;

        // Get ip and port
        String proxyIP = lineData[ 0 ];
        String proxyPort = lineData[ 1 ];

        // Port is no integer, this input is unneeded
        if( !Utils.isInt( proxyPort ) )
          continue;

        // Add to pool
        buffer.add( new Proxy( Proxy.Type.HTTP, new InetSocketAddress( proxyIP, Integer.parseInt( proxyPort ) ) ) );
      }

    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return buffer;
  }
}
