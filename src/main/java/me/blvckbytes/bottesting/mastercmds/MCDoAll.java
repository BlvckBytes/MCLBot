package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCDoAll extends MasterCommand {

  public MCDoAll( BotMaster master ) {
    super(
      "doall", master, false, false,
      "Execute a command on all available bots"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // No command provided
    if( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: doall <command> [args]", SLLevel.MASTER );
      return;
    }

    // Run simulated command
    master.getConsole().simulateCommand( Utils.concatArr( args, 0, args.length - 1 ) );
  }
}
