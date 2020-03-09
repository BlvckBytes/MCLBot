package me.blvckbytes.bottesting.proxies;

import lombok.Getter;

public class HttpProxy {

  public boolean hasLogin;

  @Getter
  private String user, passwd, host;

  @Getter
  private int port;

  public HttpProxy( String host, int port ) {
    this.host = host;
    this.port = port;
    this.hasLogin = false;
  }

  public HttpProxy( String host, int port, String user, String passwd ) {
    this( host, port );
    this.user = user;
    this.passwd = passwd;
    this.hasLogin = true;
  }
}
