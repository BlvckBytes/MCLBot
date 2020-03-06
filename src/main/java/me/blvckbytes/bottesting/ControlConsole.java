package me.blvckbytes.bottesting;

import me.blvckbytes.bottesting.utils.SimpleCallback;

import java.util.Scanner;

public class ControlConsole {

  public ControlConsole( SimpleCallback< String > command ) {
    new Thread( () -> {

      while( true ) {
        Scanner scanner = new Scanner( System.in );
        String response = scanner.nextLine();
        command.call( response );
      }

    } ).start();
  }

}
