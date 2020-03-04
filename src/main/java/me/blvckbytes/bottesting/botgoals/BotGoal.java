package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.SimpleCallback;

public abstract class BotGoal {

  public String name;
  public String description;
  public String[] params;

  public BotGoal( String name, String description, String... params ) {
    this.name = name;
    this.description = description;
    this.params = params;
  }

  public abstract void call( MCBot dispatcher, SimpleCallback< String > done );
}
