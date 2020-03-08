package me.blvckbytes.bottesting;

import me.blvckbytes.bottesting.mastercmds.ExecutionInformation;
import me.blvckbytes.bottesting.utils.SimpleCallback;

import java.util.Scanner;

public class ControlConsole {

  private SimpleCallback< ExecutionInformation > commandCallback;

  public ControlConsole( SimpleCallback< ExecutionInformation > command ) {
    this.commandCallback = command;

    new Thread( () -> {

      while( true ) {
        Scanner scanner = new Scanner( System.in );
        String response = scanner.nextLine();
        command.call( new ExecutionInformation( false, response ) );
      }

    } ).start();
  }

  /**
   * Simulates a command without the user having to enter it
   * @param input Command and args to execute
   */
  public void simulateCommand( String input ) {
    this.commandCallback.call( new ExecutionInformation( true, input ) );
  }
}
