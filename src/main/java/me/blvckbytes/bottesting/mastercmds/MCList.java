package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCList extends MasterCommand {

  public MCList( BotMaster master ) {
    super(
      "list", master, false, false,
      "Display all registered bots"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    SimpleLogger.getInst().log( "The following bots are available:", SLLevel.MASTER );

    // List all bots
    int c = 0;
    for( MCBot bot : master.getBots() ) {
      String state = bot.isOnline() ? "online" : "offline";
      SimpleLogger.getInst().log( "> [" + c + "] " + bot.getName() + " " + state, SLLevel.MASTER );
      c++;
    }

    // No bots currently registered
    if( c == 0 )
      SimpleLogger.getInst().log( "> No bots registered.", SLLevel.MASTER );
  }
}
