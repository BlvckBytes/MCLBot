package me.blvckbytes.bottesting.botgoals;

public class GoalResult {

  public String error;
  public String[] passedParams;

  public GoalResult( String error, String... passedParams ) {
    this.error = error;
    this.passedParams = passedParams;
  }
}
