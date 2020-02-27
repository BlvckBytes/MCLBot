package me.blvckbytes.bottesting;

import org.json.JSONObject;

public class MCLAuth {

  private String currIP;
  private static MCLAuth inst;

  private MCLAuth() {
    // Get current ip of auth redirect from mcleaks
    JSONObject baseApiResp = new JSONObject(
        new SimpleRequest( "https://api.mcleaks.net/v4/", "POST" ).call( null )
    );
    currIP = ( String ) baseApiResp.get( "authServer" );

    // Hook for shutdown, remove redirect
    Runtime.getRuntime().addShutdownHook( new Thread( () -> removeRedirect( false ) ) );
  }

  /**
   * Apply the redirect, but first remove the old lines silently
   */
  public void apply() {
    removeRedirect( true );
    applyRedirect();
    applyCertificates();
  }

  /**
   * Appand redirect to hosts file
   */
  private void applyRedirect() {
    execCommand( "echo \"" + this.currIP + " sessionserver.mojang.com\" | sudo tee -a /etc/hosts" );
    execCommand( "echo \"" + this.currIP + " authserver.mojang.com\" | sudo tee -a /etc/hosts" );
    SimpleLogger.getInst().log( "Applied mojang REST redirect to MCLeaks!", SLLevel.INFO );
  }

  /**
   * Apply certificates to cacerts cert store
   */
  private void applyCertificates() {
    execCommand( "echo \"" + MCLCerts.AUTHSERVER.content + "\" | sudo tee /tmp/" + MCLCerts.AUTHSERVER.name );
    execCommand( "echo \"" + MCLCerts.SESSIONSERVER.content + "\" | sudo tee /tmp/" + MCLCerts.SESSIONSERVER.name );
    execCommand( "sudo keytool -cacerts -storepass \"changeit\" -noprompt -import -alias " + MCLCerts.AUTHSERVER.name + " -file /tmp/" + MCLCerts.AUTHSERVER.name );
    execCommand( "sudo keytool -cacerts -storepass \"changeit\" -noprompt -import -alias " + MCLCerts.SESSIONSERVER.name + " -file /tmp/" + MCLCerts.SESSIONSERVER.name );
    SimpleLogger.getInst().log( "Applied MCLeaks custom certificates to cacerts!", SLLevel.INFO );
  }

  /**
   * Remove redirect from hosts file
   * @param silent If true, it's not logged
   */
  private void removeRedirect( boolean silent ) {
    execCommand( "echo \"$(cat /etc/hosts | sed \"/mojang.com/d\")\" | sudo tee /etc/hosts" );

    if( !silent )
      SimpleLogger.getInst().log( "Undone mojang REST redirect to MCLeaks!", SLLevel.INFO );
  }

  /**
   * Executes a given command as in bash shell
   * @param command Command to execute
   */
  private void execCommand( String command ) {
    try {
      ProcessBuilder process = new ProcessBuilder();
      process.command( "/bin/bash", "-c", command );
      process.start();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Could not execute sudo command in mcl auth process!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e.getMessage(), SLLevel.ERROR );
    }
  }

  /**
   * Singleton getter
   * @return Instance of class
   */
  public static MCLAuth getInst() {
    if( inst == null )
      inst = new MCLAuth();

    return inst;
  }
}
