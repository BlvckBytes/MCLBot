package me.blvckbytes.bottesting.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleRequest {

  private String url, method;

  /**
   * Generate a new simple POST or GET request
   * @param url Url to request from
   * @param method Method to use
   */
  public SimpleRequest( String url, String method ) {
    this.url = url;
    this.method = method;
  }

  /**
   * Call the url for a request with provided body
   * @param body Body to use in the request
   * @return String answer of url
   */
  public String call( JSONObject body ) {
    try {

      // Open connection
      URL url = new URL( this.url );
      HttpURLConnection con = ( HttpURLConnection ) url.openConnection();
      con.setRequestMethod( this.method );
      con.setDoOutput( true );
      con.setRequestProperty( "Content-Type", "application/json" );
      con.setConnectTimeout( 5000 );
      con.setReadTimeout( 5000 );

      // Write out request parameters
      if( body != null ) {
        DataOutputStream out = new DataOutputStream( con.getOutputStream() );
        out.writeBytes( body.toString() );
        out.flush();
        out.close();
      }

      // Read lines
      BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ( ( inputLine = in.readLine() ) != null ) {
        content.append( inputLine );
      }

      // End and return
      in.close();
      con.disconnect();
      return content.toString();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to do a " + method + " request!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
      return null;
    }
  }
}
