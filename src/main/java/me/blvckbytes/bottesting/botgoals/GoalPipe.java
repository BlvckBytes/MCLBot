package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleLogger;

import java.util.ArrayList;
import java.util.List;

public class GoalPipe {

  private List< BotGoal > goals;
  private MCBot dispatcher;
  private int state;
  private String name;

  /**
   * Generates a new goal pipeline to execute one after the
   * other after registering all
   * @param dispatcher Bot to dispatch goals
   */
  public GoalPipe( MCBot dispatcher, String name ) {
    this.goals = new ArrayList<>();
    this.dispatcher = dispatcher;
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
  public void execute() {
    // Empty pipe, stop
    if( goals.size() == 0 )
      return;

    // Done with pipeline
    if( state == goals.size() ) {
      SimpleLogger.getInst().log( "Successfully completed the pipeline " + name + "!", SLLevel.INFO );
      return;
    }

    // Call recursively through pipeline
    goals.get( state ).call( this.dispatcher, done -> {

      // Stop recursion on errors in a pipe goal
      if( done != null ) {
        SimpleLogger.getInst().log( "Error in pipeline: " + done, SLLevel.ERROR );
        return;
      }

      // Launch next
      state++;
      execute();
    } );
  }
}
