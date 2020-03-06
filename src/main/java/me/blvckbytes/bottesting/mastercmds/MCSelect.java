package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCSelect extends MasterCommand {

  public MCSelect( BotMaster master ) {
    super(
      "select", master, false,
      "Select a bot from the list"
    );
  }

  @Override
  public void call( String[] args ) {
    // ID parameter missing
    if( args.length != 1 ) {
      SimpleLogger.getInst().log( "Usage: select <id>", SLLevel.MASTER );
      return;
    }

    // ID is not an integer
    if( !Utils.isInt( args[ 0 ] ) ) {
      SimpleLogger.getInst().log( "ID must be numeric!", SLLevel.MASTER );
      return;
    }

    // Parse id
    int id = Integer.parseInt( args[ 0 ] );

    // Out of range
    if( id > master.getBots().size() - 1 || id < 0 ) {
      SimpleLogger.getInst().log( "ID out of range!", SLLevel.MASTER );
      return;
    }

    // Select the bot
    master.currSel = master.getBots().get( id );
    SimpleLogger.getInst().log( "Selected the bot " + master.currSel.getName() + "!", SLLevel.MASTER );
  }
}
