package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleLogger;

public class MCQuit extends MasterCommand {

  public MCQuit( BotMaster master ) {
    super(
      "quit", master, true,
      "Leave the target server"
    );
  }

  @Override
  public void call( String[] args ) {
    // Not connected to any server
    if( !master.currSel.getClient().getSession().isConnected() ) {
      SimpleLogger.getInst().log( "This bot is not connected!", SLLevel.MASTER );
      return;
    }

    // Dispatch disconnect
    master.currSel.getClient().getSession().disconnect( "Master disconnected me." );
  }
}
