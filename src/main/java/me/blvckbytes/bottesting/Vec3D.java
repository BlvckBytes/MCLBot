package me.blvckbytes.bottesting;

import lombok.Getter;

public class Vec3D {

  @Getter
  private double x, y, z;

  @Getter
  private float yaw, pitch;

  /**
   * Create a vector based of from and to points (for paths)
   * @param from From location
   * @param to Destination location
   */
  public Vec3D( FullLocation from, FullLocation to ) {
    this.x = to.getX() - from.getX();
    this.y = to.getY() - from.getY();
    this.z = to.getZ() - from.getZ();
    this.yaw = to.getYaw();
    this.pitch = to.getPitch();
  }

  /**
   * Create the vector based on coordinates
   * @param x X loc
   * @param y Y loc
   * @param z Z loc
   * @param yaw Head yaw
   * @param pitch Head pitch
   */
  public Vec3D( double x, double y, double z, float yaw, float pitch ) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }

  /**
   * Multiply this vector by a number and get the result
   * @param m Multiply value
   * @return Full location
   */
  public Vec3D multiply( double m ) {
    return new Vec3D( this.x * m, this.y * m, this.z * m, this.yaw, this.pitch );
  }

  /**
   * Divide this vector by a number and get the result
   * @param d Divide value
   * @return Full location
   */
  public Vec3D divide( double d ) {
    return new Vec3D( this.x / d, this.y / d, this.z / d, this.yaw, this.pitch );
  }

  /**
   * Get the length of this vector
   * @return Length value
   */
  public double getLength() {
    return Math.sqrt( Math.pow( this.x, 2 ) + Math.pow( this.y, 2 ) + Math.pow( this.z, 2 ) );
  }

  /**
   * Get the unit vector from this instance
   * @return Unit vector as Vec3D
   */
  public Vec3D getUnitvector() {
    double l = getLength();
    return new Vec3D( this.x / l, this.y / l, this.z / l, this.yaw, this.pitch );
  }

  /**
   * Convert this vector to a location
   * @return FullLocation
   */
  public FullLocation toLocation() {
    return new FullLocation( this.x, this.y, this.z, this.yaw, this.pitch );
  }
}
