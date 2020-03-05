package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.FullLocation;
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
    // Open inventory, click spawn - and thus teleport
    registerGoal( new BGHotbarMenu( "Teleporter" ) );
    registerGoal( new BGInvClick( "13" ) );

    // Walk up to the gold entity
    registerGoal( new BGVectorWalk( new FullLocation( 166.5, 99, 200.6, 0, 0 ) ) );
    registerGoal( new BGVectorWalk( new FullLocation( 166.5, 98.5, 203.5, 0, 0 ) ) );
    registerGoal( new BGVectorWalk( new FullLocation( 162.2, 98.5, 203.5, 90, 5 ) ) );
  }
}
