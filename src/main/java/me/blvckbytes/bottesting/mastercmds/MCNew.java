package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;

public class MCNew extends MasterCommand {

  public MCNew( BotMaster master ) {
    super(
      "new", master, false, false,
      "Register a new bot in the master"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // No message provided
    if( args.length != 1 ) {
      SimpleLogger.getInst().log( "Usage: new <credentials>", SLLevel.MASTER );
      return;
    }

    // Split up credentials on ':'
    String[] credentials = args[ 0 ].split( ":" );
    String using = credentials[ 0 ];
    String password = credentials.length == 1 ? null : credentials[ 1 ];

    // Try registering the new bot
    if( !master.addBot( using, password ) )
      SimpleLogger.getInst().log( "Bot could not be added, canceling...", SLLevel.MASTER );
    else
      SimpleLogger.getInst().log( "Bot added successfully!", SLLevel.MASTER );
  }
}
