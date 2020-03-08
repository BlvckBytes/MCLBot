package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCHelp extends MasterCommand {

  public MCHelp( BotMaster master ) {
    super(
      "help", master, false, false,
      "Display all available commands"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // Print out header
    SimpleLogger.getInst().log( "The following commands are registered:", SLLevel.MASTER );
    SimpleLogger.getInst().log( "> Command | Description | Subterminal | DoAll", SLLevel.MASTER );

    // Loop all commands and build up help screen
    for( MasterCommand cmd : master.getCommands() ) {
      String subTerm = cmd.isSubterminal ? "Y" : "N";
      String doAll = cmd.isDoAllCapable ? "Y" : "N";
      SimpleLogger.getInst().log( "> " + cmd.command + " | " + cmd.description + " | " + subTerm + " | " + doAll, SLLevel.MASTER );
    }
  }
}
