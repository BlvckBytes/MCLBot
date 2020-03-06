package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCHelp extends MasterCommand {

  public MCHelp( BotMaster master ) {
    super(
      "help", master, false,
      "Display all available commands"
    );
  }

  @Override
  public void call( String[] args ) {
    // Print out header
    SimpleLogger.getInst().log( "The following commands are registered:", SLLevel.MASTER );
    SimpleLogger.getInst().log( "> Command | Description | Subterminal", SLLevel.MASTER );

    // Loop all commands and build up help screen
    for( MasterCommand cmd : master.getCommands() ) {
      String subTerm = cmd.isSubterminal ? "Y" : "N";
      SimpleLogger.getInst().log( "> " + cmd.command + " | " + cmd.description + " | " + subTerm, SLLevel.MASTER );
    }
  }
}
