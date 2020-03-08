package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCQuit extends MasterCommand {

  public MCQuit( BotMaster master ) {
    super(
      "quit", master, true, true,
      "Leave the target server"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {

    if( ignoreSelect ) {
      for( MCBot bot : master.getBots() ) {
        // Skip non connecteds
        if( !bot.getClient().getSession().isConnected() )
          continue;

        // Disconnect bot
        bot.getClient().getSession().disconnect( "Master disconnected me." );
      }
      SimpleLogger.getInst().log( "Disconnected all bots from target server!", SLLevel.MASTER );
      return;
    }

    // Not connected to any server
    if( !master.currSel.getClient().getSession().isConnected() ) {
      SimpleLogger.getInst().log( "This bot is not connected!", SLLevel.INFO );
      return;
    }

    // Dispatch disconnect
    master.currSel.getClient().getSession().disconnect( "Master disconnected me." );
  }
}
