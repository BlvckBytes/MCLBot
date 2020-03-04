package me.blvckbytes.bottesting;

public class Main {

  public static void main( String[] args ) {

    // Initialize a master
    BotMaster master = new BotMaster( "bausucht.net", 25565, false );
    master.begin();

  }
}
