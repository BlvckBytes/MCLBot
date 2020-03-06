package me.blvckbytes.bottesting.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RUtils {

  /**
   * Searches for a defined method in the given class, automagically goes into
   * upper classes aswell, when nothing is available in current class
   * @param target Target class
   * @param name Name of the method
   * @param params Types of parameters (in case of overloading)
   * @return Method if found, else null
   */
  public static Method findMethod( Class< ? > target, String name, Class< ? >... params ) {
    try {

      // Try to get method if exists, else jump into catch
      Method buf = target.getDeclaredMethod( name, params );
      buf.setAccessible( true );
      return buf;

    } catch( NoSuchMethodException e ) {

      // recursive search in upper classes
      if( target.getSuperclass() != null )
        return findMethod( target.getSuperclass(), name, params );

      // at the end, if nothing found, return null
      SimpleLogger.getInst().log( "Error while trying to find a method using reflect recursively!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
      return null;
    }
  }

  /**
   * Searches for a defined field in the given class, automagically goes into
   * upper classes aswell, when nothing is available in current class
   * @param target Target class
   * @param name Name of the field
   * @return Field if found, else null
   */
  public static Field findField( Class< ? > target, String name ) {
    try {

      // Try to get field if exists, else jump into catch
      Field buf = target.getDeclaredField( name );
      buf.setAccessible( true );
      return buf;

    } catch( Exception e ) {

      // recursive search in upper classes
      if( target.getSuperclass() != null )
        return findField( target.getSuperclass(), name );

      // at the end, if nothing found, return null
      SimpleLogger.getInst().log( "Error while trying to find a field using reflect recursively!", SLLevel.ERROR );
      SimpleLogger.getInst().log( e, SLLevel.ERROR );
      return null;
    }
  }
}
