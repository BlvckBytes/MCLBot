package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.SimpleCallback;

public class BGVectorWalk extends BotGoal {

  public BGVectorWalk( String... params ) {
    super(
      "vectorwalk",
      "Walk an array of vectors with constant speed",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {

  }
}
