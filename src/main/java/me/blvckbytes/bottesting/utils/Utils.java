package me.blvckbytes.bottesting.utils;

import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleCallback;
import me.blvckbytes.bottesting.SimpleLogger;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.packet.Packet;

public class Utils {

  public static void delayPacket( Client client, Packet packet, int delay ) {
    delayExec( func -> {
      client.getSession().send( packet );
    }, delay );
  }

  public static void delayExec( SimpleCallback< ? > func, int delay ) {
    new Thread( () -> {
      try {
        Thread.sleep( delay );
        func.call( null );
      } catch ( Exception e ) {
        SimpleLogger.getInst().log( "Error while delaying an execution!", SLLevel.ERROR );
        SimpleLogger.getInst().log( e, SLLevel.ERROR );
      }
    } ).start();
  }

  public static boolean isInt( String input ) {
    try {
      Integer.parseInt( input );
      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  public static String concatArr( String[] data, int from, int to, String separator ) {
    StringBuilder sb = new StringBuilder();

    for ( int i = from; i <= to; i++ )
      sb.append( data[ i ] + ( i == to ? "" : separator ) );

    return sb.toString();
  }

  public static String concatArr( String[] data, int from, int to ) {
    return concatArr( data, from, to, " " );
  }
}
