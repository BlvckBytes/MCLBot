package me.blvckbytes.bottesting.botgoals;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SimpleCallback;

public class BGExecuteCommand extends BotGoal {

  public BGExecuteCommand( Object... params ) {
    super(
      "executecommand",
      "Execute a command as the player on the server",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {
    if( params.length != 1 || !( params[ 0 ] instanceof String ) ) {
      done.call( new GoalResult( this.name + " needs one parameter - command to exec" ) );
      return;
    }

    // Execute command
    String command = ( String ) params[ 0 ];
    dispatcher.getClient().getSession().send( new ClientChatPacket( "/" + command ) );
    done.call( new GoalResult( null ) );
  }
}
