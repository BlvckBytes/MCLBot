package me.blvckbytes.bottesting.botgoals;

import com.github.steveice10.mc.protocol.data.game.ItemStack;
import com.github.steveice10.mc.protocol.data.game.values.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.values.window.WindowAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import me.blvckbytes.bottesting.MCBot;
import me.blvckbytes.bottesting.SLLevel;
import me.blvckbytes.bottesting.SimpleCallback;
import me.blvckbytes.bottesting.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class BGInvClick extends BotGoal {

  public BGInvClick( Object... params ) {
    super(
      "invclick",
      "Click on an item in the currently open inventory",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {
    if( params.length != 2 ) {
      done.call( new GoalResult( "Goal " + this.name + " needs two paremeter: WindowID and InventorySlot!" ) );
      return;
    }

    // Check both params to be integers
    if( !Utils.isInt( ( String ) params[ 0 ], ( String ) params[ 1 ] ) ) {
      done.call( new GoalResult( "BoatGoal " + this.name + " needs both parameters to be an integer!" ) );
      return;
    }

    // Slot where the outer item can be found
    int slot = Integer.parseInt( ( String ) params[ 0 ] );
    int windowID = Integer.parseInt( ( String ) params[ 1 ] );

    // No open inventory
    if( !dispatcher.getCurrentItems().containsKey( windowID ) ) {
      done.call( new GoalResult( "Goal " + this.name + " expected windowID inv to be open, but found none!" ) );
      return;
    }

    // Check if in range
    ItemStack[] targetInv = dispatcher.getCurrentItems().get( windowID );
    if( slot > targetInv.length - 1 ) {
      done.call( new GoalResult( "Slot id out of range for " + this.name + "!" ) );
      return;
    }

    // Click item
    ItemStack targetStack = targetInv[ slot ];
    ClientWindowActionPacket clickPacket = new ClientWindowActionPacket(
      windowID, 0, slot, targetStack, WindowAction.CLICK_ITEM, ClickItemParam.LEFT_CLICK
    );

    // Click item
    dispatcher.getClient().getSession().send( clickPacket );
    dispatcher.sendPosition();
    SimpleLogger.getInst().log( "Successfully clicked item!", SLLevel.INFO );
    done.call( new GoalResult( null ) );
  }
}
