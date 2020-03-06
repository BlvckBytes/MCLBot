package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCDestroy extends MasterCommand {

  public MCDestroy( BotMaster master ) {
    super(
      "destroy", master, true,
      "Disconnect and dispose the bot"
    );
  }

  @Override
  public void call( String[] args ) {
    // Disconnect
    if( master.currSel.getClient().getSession().isConnected() )
      master.currSel.getClient().getSession().disconnect( "Master destroyed me." );

    // Remove from management pool
    master.getBots().remove( master.currSel );
    master.currSel = null;
    SimpleLogger.getInst().log( "Bot successfully removed!", SLLevel.MASTER );
  }
}
