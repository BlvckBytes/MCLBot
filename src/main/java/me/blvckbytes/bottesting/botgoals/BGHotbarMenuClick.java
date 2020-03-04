package me.blvckbytes.bottesting.botgoals;

import com.github.steveice10.mc.protocol.data.game.ItemStack;
import com.github.steveice10.mc.protocol.data.game.values.Face;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import me.blvckbytes.bottesting.*;
import me.blvckbytes.bottesting.utils.Utils;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import org.spacehq.opennbt.tag.builtin.Tag;

public class BGHotbarMenuClick extends BotGoal {

  public BGHotbarMenuClick( String... params ) {
    super(
      "hotbarmenuclick",
      "Right- or leftclick a item on a certain slot in the hotbar",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< String > done ) {
    if( params.length != 2 ) {
      SimpleLogger.getInst().log( "BotGoal " + this.name + " needs two parameters, outer- and innerItemName!", SLLevel.ERROR );
      return;
    }

    // Slot where the outer item can be found
    int outerSlot = -1;

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
        outerSlot = i;
        break;
      }
    }

    // Nothing has been found
    if( outerSlot < 0 ) {
      done.call( this.name + " did not find the correct outerItem!" );
      return;
    }

    // Not in reach of hotbar
    if( outerSlot < 36 || outerSlot > 44 ) {
      done.call( this.name + " found outerItem outside of hotbar!" );
      return;
    }

    // Switch to that found slot
    dispatcher.getClient().getSession().send( new ClientChangeHeldItemPacket( outerSlot - 36 ) );

    ClientPlayerPlaceBlockPacket clickPacket = new ClientPlayerPlaceBlockPacket(
      dispatcher.getLastLoc().toPosition(), Face.TOP, targetInv[ outerSlot ], 0F, 0F, 0F
    );

    // TODO: Find out why this won't fire, the server doesn't seem to open the GUI... :/

    PacketMonitor openMonitor = new PacketMonitor( ServerOpenWindowPacket.class );
    openMonitor.setCallback( openInfo -> {
      ServerOpenWindowPacket openPacket = ( ServerOpenWindowPacket ) openInfo;
      System.out.println( openPacket );
      openMonitor.destroy();
    } );
    dispatcher.registerMonitor( openMonitor );

    Utils.delayExec( func -> {
      // Dispatch right click
      dispatcher.getClient().getSession().send( clickPacket );

      // No errors, completed successfully
      done.call( null );
    }, 300 );
  }
}
