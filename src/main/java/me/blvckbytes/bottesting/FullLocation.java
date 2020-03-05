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
   * Append a vector to this location and return the result,
   * the vector itself does not get changed
   * @param vec Vector to append
   * @return Full location
   */
  public FullLocation append( Vec3D vec ) {
    FullLocation appended = duplicate();
    appended.setX( appended.getX() + vec.getX() );
    appended.setY( appended.getY() + vec.getY() );
    appended.setZ( appended.getZ() + vec.getZ() );
    appended.setYaw( vec.getYaw() );
    appended.setPitch( vec.getPitch() );
    return appended;
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
