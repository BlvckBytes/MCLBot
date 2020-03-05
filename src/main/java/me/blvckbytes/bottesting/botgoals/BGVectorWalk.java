package me.blvckbytes.bottesting.botgoals;

import me.blvckbytes.bottesting.*;

public class BGVectorWalk extends BotGoal {

  public BGVectorWalk( Object... params ) {
    super(
      "vectorwalk",
      "Walk to a point based on a straight vector with const. speed",
      params
    );
  }

  @Override
  public void call( MCBot dispatcher, SimpleCallback< GoalResult > done ) {
    // Check that first parameter is given and is location
    if( params.length < 1 || !( params[ 0 ] instanceof FullLocation  ) ) {
      done.call( new GoalResult( this.name + " needs one parameter - destination location" ) );
      return;
    }

    // Create path vector
    FullLocation to = ( FullLocation ) params[ 0 ];
    FullLocation from = dispatcher.getLastLoc().duplicate();
    Vec3D pathVec = new Vec3D( dispatcher.getLastLoc(), to );

    // Calculate unit vector based on step size
    double stepSize = 1;
    Vec3D pathUnit = pathVec.getUnitvector().divide( stepSize );

    // Iterate over steps with delays between
    new Thread( () -> {
      for( double len = 0; len <= pathVec.getLength() * stepSize; len++ ) {
        // Send new location to server
        Vec3D newPos = pathUnit.multiply( len );
        dispatcher.setLastLoc( from.append( newPos ) );
        dispatcher.sendPosition();

        // Delay next iteration
        try {
          Thread.sleep( 450 );
        } catch ( Exception e ) {
        e.printStackTrace();
        }
      }
      done.call( new GoalResult( null ) );
    } ).start();
  }
}
