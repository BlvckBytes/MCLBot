package me.blvckbytes.bottesting.mastercmds;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCCommand extends MasterCommand {

  public MCCommand( BotMaster master ) {
    super(
      "command", master, true,
      "Dispatch a command on the server"
    );
  }

  @Override
  public void call( String[] args ) {
    // No command provided
    if( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: command <cmd>", SLLevel.MASTER );
      return;
    }

    // Dispatch command
    String gameCommand = "/" + Utils.concatArr( args, 0, args.length - 1 );
    master.currSel.getClient().getSession().send( new ClientChatPacket( gameCommand ) );
    SimpleLogger.getInst().log( "Sent command " + gameCommand + "!", SLLevel.INFO );
  }
}
