package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleLogger;

public class MCJoin extends MasterCommand {

  public MCJoin( BotMaster master ) {
    super(
      "join", master, true,
      "Join onto the target server"
    );
  }

  @Override
  public void call( String[] args ) {
    // Already connected
    if( master.currSel.getClient().getSession().isConnected() ) {
      SimpleLogger.getInst().log( "This bot is already connected!", SLLevel.MASTER );
      return;
    }

    // Connect to target server
    master.currSel.reconnect();
  }
}
