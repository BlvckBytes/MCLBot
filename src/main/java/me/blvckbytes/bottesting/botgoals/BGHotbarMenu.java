package me.blvckbytes.bottesting.botgoals;

import com.github.steveice10.mc.protocol.data.game.ItemStack;
import com.github.steveice10.mc.protocol.data.game.values.Face;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import me.blvckbytes.bottesting.*;
import me.blvckbytes.bottesting.utils.Utils;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;

import java.util.Timer;
import java.util.TimerTask;

public class BGHotbarMenu extends BotGoal {

  public BGHotbarMenu( Object... params ) {
    super(
      "hotbarclick",
      "Rightclick on an item in the hotbar to open a inv-menu",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {
    if( params.length != 1 ) {
      done.call( new GoalResult( "Goal " + this.name + " needs one paremeter: ItemName!" ) );
      return;
    }

    // Slot where the outer item can be found
    int targetSlot = -1;

    // Loop item by item with corresponding slot ID
    ItemStack[] targetInv = dispatcher.getCurrentItems().get( 0 );
    for( int i = 0; i < targetInv.length; i++ ) {
      ItemStack item = targetInv[ i ];

      // Only work with actual items, not NULL (air)
      if( item == null )
        continue;

      // Try to get the name of the item
      String itemName;
      try {
        CompoundTag ct = item.getNBT().get( "display" );
        itemName = Utils.stripColor( ( ( StringTag ) ct.get( "Name" ) ).getValue() );
      } catch ( Exception e ) {
        itemName = "";
      }

      // Found target item!
      if( itemName.equals( params[ 0 ] ) ) {
        targetSlot = i;
        break;
      }
    }

    // Nothing has been found
    if( targetSlot < 0 ) {
      done.call( new GoalResult( this.name + " did not find the correct outerItem!" ) );
      return;
    }

    // Not in reach of hotbar
    if( targetSlot < 36 || targetSlot > 44 ) {
      done.call( new GoalResult( this.name + " found outerItem outside of hotbar!" ) );
      return;
    }

    // Switch to that found slot
    dispatcher.getClient().getSession().send( new ClientChangeHeldItemPacket( targetSlot - 36 ) );
    dispatcher.sendPosition();

    // Start a timeout timer for inventory packet opening
    Timer t = new Timer();
    t.schedule( new TimerTask() {

      @Override
      public void run() {
        done.call( new GoalResult( "Timed out while waiting for opening inv on " + name ) );
      }

    }, 1000 * 3 );

    // Monitor for incoming window openings
    PacketMonitor openMonitor = new PacketMonitor( ServerWindowItemsPacket.class );
    openMonitor.setCallback( windowInfo -> {
      ServerWindowItemsPacket windowPacket = ( ServerWindowItemsPacket ) windowInfo;

      // Ignore inventory, I need new top inv
      if( windowPacket.getWindowId() == 0 )
        return;

      // Cancel timer and destroy monitors
      dispatcher.getCurrentItems().put( windowPacket.getWindowId(), windowPacket.getItems() );
      t.cancel();
      openMonitor.destroy();

      // Done, inv is open
      SimpleLogger.getInst().log( "Successfully opened inventory menu", SLLevel.INFO );
      done.call( new GoalResult( null, String.valueOf( windowPacket.getWindowId() ) ) );
    } );
    dispatcher.registerMonitor( openMonitor );

    // Right click the found item
    dispatcher.getClient().getSession().send( new ClientPlayerPlaceBlockPacket(
      dispatcher.getLastLoc().toPosition(), Face.INVALID, targetInv[ targetSlot ], 0F, 0F, 0F
    ) );
    dispatcher.sendPosition();
  }
}
