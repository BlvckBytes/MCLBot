package me.blvckbytes.bottesting.mastercmds;
import me.blvckbytes.bottesting.BotMaster;

public abstract class MasterCommand {

  public boolean isSubterminal;
  public BotMaster master;
  public String command;
  public String description;

  public MasterCommand( String command, BotMaster master, boolean isSubterminal, String description ) {
    this.command = command;
    this.master = master;
    this.isSubterminal = isSubterminal;
    this.description = description;
  }

  public abstract void call( String[] args );
}
