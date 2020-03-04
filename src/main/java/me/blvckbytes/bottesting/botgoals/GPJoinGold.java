package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.MCBot;

public class GPJoinGold extends GoalPipe {

  /**
   * Tries to connect this bot onto the "gold" subserver
   * of "bausucht.net"
   *
   * @param dispatcher Bot to dispatch goals
   */
  public GPJoinGold( MCBot dispatcher ) {
    super( dispatcher, "joingold" );
    createGoals();
  }

  /**
   * Register all goals in correct order
   */
  private void createGoals() {
    registerGoal( new BGHotbarMenu( "Teleporter" ) );
    registerGoal( new BGInvClick( "13" ) );
  }
}
