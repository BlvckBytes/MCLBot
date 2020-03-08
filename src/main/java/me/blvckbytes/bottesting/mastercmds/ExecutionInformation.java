package me.blvckbytes.bottesting.mastercmds;

import lombok.Getter;

public class ExecutionInformation {

  @Getter
  public boolean ignoreSelect;

  @Getter
  public String command;

  public ExecutionInformation( boolean ignoreSelect, String command ) {
    this.ignoreSelect = ignoreSelect;
    this.command = command;
  }

}
