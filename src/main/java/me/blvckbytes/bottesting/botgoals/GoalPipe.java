package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleCallback;
import me.blvckbytes.bottesting.utils.SimpleLogger;

import java.util.ArrayList;
import java.util.List;

public class GoalPipe {

  private List< BotGoal > goals;
  public MCBot dispatcher;
  private String name;

  private String[] passedParams;
  private int state;

  /**
   * Generates a new goal pipeline to execute one after the
   * other after registering all
   * @param dispatcher Bot to dispatch goals
   */
  public GoalPipe( MCBot dispatcher, String name ) {
    this.goals = new ArrayList<>();
    this.dispatcher = dispatcher;
    this.passedParams = new String[ 0 ];
    this.name = name;
  }

  /**
   * Register goal in pipeline
   * @param goal Goal to register
   */
  public void registerGoal( BotGoal goal ) {
    this.goals.add( goal );
  }

  /**
   * Execute pipeline recursively
   */
  public void execute( SimpleCallback< ? > complete ) {
    try {
      // Empty pipe, stop
      if( goals.size() == 0 )
        return;

      // Done with pipeline
      if( state == goals.size() ) {
        SimpleLogger.getInst().log( "Successfully completed the pipeline " + name + " on " + dispatcher.getName() + "!", SLLevel.INFO );

        if( complete != null )
          complete.call( null );

        return;
      }

      new Thread( () -> {
        // Call recursively through pipeline
        goals.get( state ).appendParams( passedParams ).call( this.dispatcher, done -> {

          // Cache params for next goal
          passedParams = done.passedParams;

          // Stop recursion on errors in a pipe goal
          if( done.error != null ) {
            SimpleLogger.getInst().log( "Error in pipeline: " + done.error, SLLevel.ERROR );

            if( complete != null )
              complete.call( null );

            return;
          }

          // Print out stage completeness
          String goalName = goals.get( state ).name;
          SimpleLogger.getInst().log( "Stage " + goalName + " done on " + this.dispatcher.getName() + "!", SLLevel.INFO );

          // Launch next delayed
          try {
            Thread.sleep( 600 );
            state++;
            execute( complete );
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        } );

      } ).start();
    } catch ( Exception e ) {
      SimpleLogger.getInst().log( "Error while executing pipeline!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }
}
