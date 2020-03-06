package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCUnselect extends MasterCommand {

  public MCUnselect( BotMaster master ) {
    super(
      "unselect", master, false,
      "Leave a bot's subterminal"
    );
  }

  @Override
  public void call( String[] args ) {
    // No bot selected
    if( master.currSel == null ) {
      SimpleLogger.getInst().log( "No bot currently selected!", SLLevel.MASTER );
      return;
    }

    // Unselect current bot
    master.currSel = null;
    SimpleLogger.getInst().log( "Unselected bot, now back in master terminal.", SLLevel.MASTER );
  }
}
