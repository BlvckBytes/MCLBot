package me.blvckbytes.bottesting.mastercmds;

import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCTarget extends MasterCommand {

  public MCTarget( BotMaster master ) {
    super(
      "target", master, false,
      "Change target server of the master"
    );
  }

  @Override
  public void call( String[] args ) {
    // Either one or two argumentts
    if( !( args.length <= 2 && args.length != 0 ) ) {
      SimpleLogger.getInst().log( "Usage: target <ip/domain> [port]", SLLevel.MASTER );
      return;
    }

    // Change data in master
    String ip = args[ 0 ];
    int port = args.length == 2 && Utils.isInt( args[ 1 ] ) ? Integer.parseInt( args[ 1 ] ) : 25565;
    master.reaim( ip, port );

    SimpleLogger.getInst().log( "Successfully changed the target server!", SLLevel.MASTER );
  }
}
