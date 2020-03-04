package me.blvckbytes.bottesting;

import com.github.steveice10.mc.protocol.data.game.Position;
import lombok.Getter;
import lombok.Setter;

public class FullLocation {

  @Getter @Setter
  private double x, y, z;

  @Getter @Setter
  private float yaw, pitch;

  public FullLocation( double x, double y, double z, float yaw, float pitch ) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }

  public FullLocation duplicate() {
    return new FullLocation( this.x, this.y, this.z, this.yaw, this.pitch );
  }

  /**
   * Convert this full location to a position consisting of
   * x, y and z only
   * @return Position object
   */
  public Position toPosition() {
    return new Position( ( int ) this.x, ( int ) this.y, ( int ) this.z );
  }
}
