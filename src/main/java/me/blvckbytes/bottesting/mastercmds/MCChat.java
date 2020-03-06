package me.blvckbytes.bottesting.mastercmds;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCChat extends MasterCommand {

  public MCChat( BotMaster master ) {
    super(
      "chat", master, true,
      "Send a message to server chat"
    );
  }

  @Override
  public void call( String[] args ) {
    // No message provided
    if( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: chat <msg>", SLLevel.MASTER );
      return;
    }

    // Dispatch chat packet
    master.currSel.getClient().getSession().send( new ClientChatPacket( Utils.concatArr( args, 0, args.length - 1 ) ) );
  }
}
