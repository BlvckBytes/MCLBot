package me.blvckbytes.bottesting;

import lombok.Getter;
import me.blvckbytes.bottesting.utils.SimpleCallback;
import org.spacehq.packetlib.packet.Packet;

public class PacketMonitor {

  @Getter
  private Class< ? extends Packet > target;

  @Getter
  private SimpleCallback< Packet > callback;

  /**
   * Create a new monitor that listens for a specific packet type
   * @param target Type of the needed packet
   */
  public PacketMonitor( Class< ? extends Packet > target ) {
    this.target = target;
  }

  /**
   * Set the callback (probably lambda function) for this monitor
   * @param callback Callback on receiving occurances
   */
  public void setCallback( SimpleCallback< Packet > callback ) {
    this.callback = callback;
  }

  /**
   * Destroys this monitor. This means it will get removed from
   * monitor list in the bot on next call, that call will be suppressed
   */
  public void destroy() {
    this.callback = null;
  }
}
