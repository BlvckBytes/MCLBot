package me.blvckbytes.bottesting.utils;

import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.packet.Packet;

import java.util.TimerTask;

public class Utils {

  /**
   * Send a packet delayed
   * @param client Client to send from
   * @param packet Packet to send
   * @param delay Delay in ms
   */
  public static void delayPacket( Client client, Packet packet, int delay ) {
    delayExec( func -> {
      client.getSession().send( packet );
    }, delay );
  }

  /**
   * Delay something by a given amount of time
   * @param func Callback to daly
   * @param delay Delay in ms
   */
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

  /**
   * Checks wether the given string represents a parsable integer
   * @param input Input to verify
   * @return Parsable state
   */
  public static boolean isInt( String input ) {
    try {
      Integer.parseInt( input );
      return true;
    } catch ( Exception e ) {
      return false;
    }
  }

  /**
   * Check multiple strings for integers
   * @param input Multiple strings to check
   * @return Parsable state
   */
  public static boolean isInt( String... input ) {
    boolean allInts = true;

    // Loop all parameters
    for( String curr : input ) {
      if( isInt( curr ) )
        continue;

      // False if one is not an int
      allInts = false;
      break;
    }

    return allInts;
  }

  /**
   * Combine two string arrays
   * @param a First
   * @param b Second
   * @return Merged array
   */
  public static Object[] combineArrays( Object[] a, Object[] b ){
    int length = a.length + b.length;
    Object[] result = new Object[ length ];
    System.arraycopy( a, 0, result, 0, a.length );
    System.arraycopy( b, 0, result, a.length, b.length );
    return result;
  }

  /**
   * Concat an array with space as separator
   * @param data String parts
   * @param from From index
   * @param to To index
   * @param separator String to separate parts
   * @return Concatenated string
   */
  public static String concatArr( String[] data, int from, int to, String separator ) {
    StringBuilder sb = new StringBuilder();

    for ( int i = from; i <= to; i++ )
      sb.append( data[ i ] ).append( i == to ? "" : separator );

    return sb.toString();
  }

  /**
   * Concat an array with space as separator
   * @param data String parts
   * @param from From index
   * @param to To index
   * @return Concatenated string
   */
  public static String concatArr( String[] data, int from, int to ) {
    return concatArr( data, from, to, " " );
  }

  /**
   * Strips all color codes off from the given input
   * @param input Input to strip
   * @return Colorcode free string
   */
  public static String stripColor( String input ) {
    return input.replaceAll( "§.", "" );
  }

  /**
   * Wrapper for a lambda expression to be used with timer task
   * @param r Runnable (lambda function)
   * @return TimerTask
   */
  public static TimerTask wrapTimer( Runnable r ) {
    return new TimerTask() {

      @Override
      public void run() {
        r.run();
      }
    };
  }
}
