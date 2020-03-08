package me.blvckbytes.bottesting.mastercmds;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCCommand extends MasterCommand {

  public MCCommand( BotMaster master ) {
    super(
      "command", master, true, true,
      "Dispatch a command on the server"
    );
  }

  @Override
  public void call( String[] args, boolean ignoreSelect ) {
    // No command provided
    if( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: command <cmd>", SLLevel.MASTER );
      return;
    }

    String gameCommand = "/" + Utils.concatArr( args, 0, args.length - 1 );
    ClientChatPacket commandPacket = new ClientChatPacket( gameCommand );

    // Ignore select, dispatch on all
    if( ignoreSelect ) {
      for( MCBot bot : master.getBots() )
        bot.getClient().getSession().send( commandPacket );
      SimpleLogger.getInst().log( "Sent command " + gameCommand + " to all bots!", SLLevel.MASTER );
      return;
    }

    // Dispatch command
    master.currSel.getClient().getSession().send( commandPacket );
    SimpleLogger.getInst().log( "Sent command " + gameCommand + "!", SLLevel.INFO );
  }
}
