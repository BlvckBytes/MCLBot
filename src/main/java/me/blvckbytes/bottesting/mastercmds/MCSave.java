package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;

public class MCSave extends MasterCommand {

  public MCSave( BotMaster master ) {
    super(
      "save", master, false,
      "Write all sessions to file"
    );
  }

  @Override
  public void call( String[] args ) {
    master.saveSessions();
  }
}
