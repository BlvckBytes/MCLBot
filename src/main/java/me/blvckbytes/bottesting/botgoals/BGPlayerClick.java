package me.blvckbytes.bottesting.botgoals;

import com.github.steveice10.mc.protocol.data.game.values.entity.player.InteractAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerInteractEntityPacket;
import me.blvckbytes.bottesting.FullLocation;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.utils.SimpleCallback;

public class BGPlayerClick extends BotGoal {

  public BGPlayerClick( Object... params ) {
    super(
      "playerclick",
      "Interact with a player, identified by his location",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {
    // Check that first parameter is given and is location
    if( params.length < 1 || !( params[ 0 ] instanceof FullLocation ) ) {
      done.call( new GoalResult( this.name + " needs one parameter - player's location" ) );
      return;
    }

    // Position of player
    FullLocation position = ( FullLocation ) params[ 0 ];
    int targetID = dispatcher.getPlayerByLoc( position );

    // Not found
    if( targetID == -1 ) {
      done.call( new GoalResult( this.name + " did not find the target entity based on location" ) );
      return;
    }

    // Send interact action
    dispatcher.getClient().getSession().send(
      new ClientPlayerInteractEntityPacket( targetID, InteractAction.INTERACT_AT )
    );
    done.call( null );
  }
}
