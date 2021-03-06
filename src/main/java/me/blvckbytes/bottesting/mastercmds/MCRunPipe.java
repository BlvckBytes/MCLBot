package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleCallback;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.botgoals.GoalPipe;

import java.util.concurrent.atomic.AtomicInteger;

public class MCRunPipe extends MasterCommand {

  public MCRunPipe( BotMaster master ) {
    super(
      "runpipe", master, true, true,
      "Tries to instantiate the goal and execute it"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // Argument mismatch
    if( args.length != 1 ) {
      SimpleLogger.getInst().log( "Usage: runpipe <ClassName>", SLLevel.MASTER );
      return;
    }

    String className = args[ 0 ];
    try {
      // Load class of pipe
      Class< ? > target = Class.forName( "me.blvckbytes.bottesting.botgoals." + className );

      if( ignoreSelect ) {

        // TODO: Think about a way to delay the loop based on done callback
        for( MCBot bot : master.getBots() ) {
          GoalPipe pipe = ( GoalPipe ) target.getConstructor( MCBot.class ).newInstance( bot );

          pipe.execute( done -> {

          } );
        }

        SimpleLogger.getInst().log( "Sent pipe instruction " + className + " to all bots!", SLLevel.MASTER );
        return;
      }

      // Instantiate pipe and execute
      GoalPipe pipe = ( GoalPipe ) target.getConstructor( MCBot.class ).newInstance( master.currSel );
      pipe.execute( null );
    }

    // Class not found, thus pipe not existent
    catch ( ClassNotFoundException e ) {
      SimpleLogger.getInst().log( className + " is not a valid pipe!", SLLevel.ERROR );
    }

    // All other exceptions
    catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while trying to execute a pipe!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }
}
