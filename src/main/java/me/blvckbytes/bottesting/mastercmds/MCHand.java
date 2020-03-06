package me.blvckbytes.bottesting.mastercmds;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientChangeHeldItemPacket;
import me.blvckbytes.bottesting.BotMaster;
import me.blvckbytes.bottesting.utils.SLLevel;
import me.blvckbytes.bottesting.utils.SimpleLogger;
import me.blvckbytes.bottesting.utils.Utils;

public class MCHand extends MasterCommand {

  public MCHand( BotMaster master ) {
    super(
      "hand", master, true,
      "Change the selected slot in hotbar"
    );
  }

  @Override
  public void call( String[] args ) {
    // No slot provided
    if ( args.length == 0 ) {
      SimpleLogger.getInst().log( "Usage: hand <slot>", SLLevel.MASTER );
      return;
    }

    // Slot is not an integer
    if( !Utils.isInt( args[ 0 ] ) ) {
      SimpleLogger.getInst().log( "Slot must be a integer value!", SLLevel.MASTER );
      return;
    }

    // Parse and check if in range
    int slot = Integer.parseInt( args[ 0 ] );
    if( slot < 0 || slot > 8 ) {
      SimpleLogger.getInst().log( "Slot must be a value between 0 and 8 (including)!", SLLevel.MASTER );
      return;
    }

    // Send packet and update location for update
    this.master.currSel.getClient().getSession().send( new ClientChangeHeldItemPacket( slot ) );
    this.master.currSel.sendPosition();
    SimpleLogger.getInst().log( "Changed slot to " + slot + "!", SLLevel.INFO );
  }

}
