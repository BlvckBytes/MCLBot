package me.blvckbytes.bottesting.utils;

import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class PropConfig {

  private final String path = "configuration.properties";
  private static PropConfig inst;

  @Getter
  private Properties props;

  /**
   * Create property map and read the file into
   * it if possible
   */
  private PropConfig() {
    // Create an empty property map
    inst = this;
    this.props = new Properties();

    try {
      // Get file as stream
      InputStream is = getClass().getClassLoader().getResourceAsStream( this.path );

      // Load file properties if file is existent
      if( is != null )
        props.load( is );

      // Close after reading
      if( is != null )
        is.close();
    } catch( Exception e ) {
      SimpleLogger.getInst().log( "Error while loading PropConfig! Is the file existent (it's part of gitignore)?", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
    }
  }

  /**
   * Save the properties from RAM into the file
   */
  public void save() {
    // Nothing has been done yet, abort
    if( this.props == null )
      return;

    try {
      // Create output stream to file
      File f = new File( this.path );
      OutputStream outS = new FileOutputStream( f );

      // Format header comment and store
      SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy, HH:mm" );
      this.props.store( outS, "Last wrote at:" + sdf.format( new Date() ) );

      // Close after writing
      outS.close();
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
   * Singleton instance getter, instantiates if non existent
   * @return Instance of PropConfig
   */
  public static PropConfig getInstance() {
    if( inst == null )
      inst = new PropConfig();

    return inst;
  }
}
