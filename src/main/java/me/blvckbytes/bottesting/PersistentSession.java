package me.blvckbytes.bottesting;

import org.spacehq.mc.auth.data.GameProfile;

import java.io.Serializable;

public class PersistentSession implements Serializable {

  public GameProfile prof;
  public String accessToken, clientToken;

  public PersistentSession( GameProfile prof, String accessToken, String clientToken ) {
    this.prof = prof;
    this.accessToken = accessToken;
    this.clientToken = clientToken;
  }
}
