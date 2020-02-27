package me.blvckbytes.bottesting.utils;

import lombok.Getter;

public class ProxyEntry {

  @Getter
  private String ip;

  @Getter
  private int port;

  /**
   * Wrapper for a IP and a port, represents a proxy server
   * @param ip Address of service
   * @param port Port of service
   */
  public ProxyEntry( String ip, int port ) {
    this.ip = ip;
    this.port = port;
  }
}
