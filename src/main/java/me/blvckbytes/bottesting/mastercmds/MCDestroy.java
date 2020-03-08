package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCDestroy extends MasterCommand {

  public MCDestroy( BotMaster master ) {
    super(
      "destroy", master, true, true,
      "Disconnect and dispose the bot"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    if( ignoreSelect ) {
      for( int i = master.getBots().size() - 1; i >= 0; i-- ) {
        MCBot curr = master.getBots().get( i );

        // Disconnect if connected
        if( curr.getClient().getSession().isConnected() )
          curr.getClient().getSession().disconnect( "Master destroyed me." );

        // Remove
        master.getBots().remove( i );
      }
      SimpleLogger.getInst().log( "All bots successfully removed!", SLLevel.MASTER );
      return;
    }

    SimpleLogger.getInst().log( "Current bot successfully removed!", SLLevel.MASTER );
    if( master.currSel.getClient().getSession().isConnected() )
      master.currSel.getClient().getSession().disconnect( "Master destroyed me." );

    // Remove from management pool
    master.getBots().remove( master.currSel );
    master.currSel = null;
  }
}
