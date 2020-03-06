package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SimpleCallback;
import me.blvckbytes.bottesting.utils.Utils;

public abstract class BotGoal {

  public String name;
  public String description;
  public Object[] params;

  /**
   * Create a new goal to be executed
   * @param name Name of goal
   * @param description Description of goal
   * @param params Parameters for execution
   */
  public BotGoal( String name, String description, Object... params ) {
    this.name = name;
    this.description = description;
    this.params = params;
  }

  /**
   * Call the goal
   * @param dispatcher Bot to dispatch
   * @param done Callback after finish
   */
  public abstract void call( MCBot dispatcher, SimpleCallback< GoalResult > done );

  /**
   * Appends new parameters to already contained ones
   * @param newParams Params to add
   */
  public BotGoal appendParams( Object[] newParams ) {
    this.params = Utils.combineArrays( this.params, newParams );
    return this;
  }
}
