package me.blvckbytes.bottesting;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import org.spacehq.packetlib.Client;

import java.util.ArrayList;
import java.util.List;

public class SimplePath {

  // TODO: This is a huge pile of shit, either clean it up and make it work or dispose of it...

  private FullLocation from, to;
  private List< FullLocation > steps;
  private double stepSize;

  public SimplePath( FullLocation from, FullLocation to, double stepSize ) {
    this.steps = new ArrayList<>();
    this.from = from;
    this.to = to;
    this.stepSize = stepSize;

    calculateSteps();
  }

  private void calculateSteps() {
    for( double x = this.from.getX(); x <= this.to.getX(); x += this.stepSize ) {
      for( double z = this.from.getZ(); z <= this.to.getZ(); z += this.stepSize ) {
        for( double y = this.from.getY(); y <= this.to.getY(); y += this.stepSize ) {
          this.steps.add( new FullLocation( x, y, z, this.to.getYaw(), this.to.getPitch() ) );
        }
      }
    }

    this.steps.add( to );
  }

  public void execute( Client handle, SimpleCallback< FullLocation > done ) {
    new Thread( () -> {

      for( FullLocation step : this.steps ) {
        ClientPlayerPositionRotationPacket packet = new ClientPlayerPositionRotationPacket(
          true, step.getX(), step.getY(), step.getZ(), step.getYaw(), step.getPitch()
        );

        handle.getSession().send( packet );

        try {
          Thread.sleep( 70 );
        } catch ( Exception e ) {
          SimpleLogger.getInst().log( "Error while trying to sleep between steps in a path!", SLLevel.ERROR );
          SimpleLogger.getInst().log( e, SLLevel.ERROR );
        }
      }

      done.call( this.steps.get( this.steps.size() - 1 ) );

    } ).start();
  }
}
