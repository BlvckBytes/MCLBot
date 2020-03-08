package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCJoin extends MasterCommand {

  public MCJoin( BotMaster master ) {
    super(
      "join", master, true, true,
      "Join onto the target server"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {

    if( ignoreSelect ) {
      for( MCBot bot : master.getBots() ) {
        // Skip bot, already connected
        if( bot.getClient().getSession().isConnected() )
          continue;

        // Connect bot
        bot.reconnect();
      }
      SimpleLogger.getInst().log( "Connected all bots to target server!", SLLevel.MASTER );
      return;
    }

    // Already connected
    if( master.currSel.getClient().getSession().isConnected() ) {
      SimpleLogger.getInst().log( "This bot is already connected!", SLLevel.INFO );
      return;
    }

    // Connect to target server
    master.currSel.reconnect();
  }
}
