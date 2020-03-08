package me.blvckbytes.bottesting.mastercmds;
import me.blvckbytes.bottesting.BotMaster;

public abstract class MasterCommand {

  public boolean isSubterminal, isDoAllCapable;
  public BotMaster master;
  public String command;
  public String description;

  public MasterCommand( String command, BotMaster master, boolean isSubterminal, boolean isDoAllCapable, String description ) {
    this.command = command;
    this.master = master;
    this.isSubterminal = isSubterminal;
    this.description = description;
    this.isDoAllCapable = isDoAllCapable;
  }

  public abstract void call( String[] args, boolean ignoreSelect );
}
