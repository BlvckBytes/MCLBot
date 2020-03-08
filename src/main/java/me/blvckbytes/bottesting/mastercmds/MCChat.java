package me.blvckbytes.bottesting.mastercmds;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCChat extends MasterCommand {

  public MCChat( BotMaster master ) {
    super(
      "chat", master, true, true,
      "Send a message to server chat"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // No message provided
    if( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: chat <msg>", SLLevel.MASTER );
      return;
    }

    ClientChatPacket chatPacket = new ClientChatPacket( Utils.concatArr( args, 0, args.length - 1 ) );

    // Ignore select, dispatch on all
    if( ignoreSelect ) {
      for( MCBot bot : master.getBots() )
        bot.getClient().getSession().send( chatPacket );
      SimpleLogger.getInst().log( "Sent message to all bots!", SLLevel.MASTER );
      return;
    }

    // Dispatch chat packet on current
    master.currSel.getClient().getSession().send( chatPacket );
  }
}
