package nl.basmens.util;

/**
 * Models the time since the userprogram is started. The starttime is captured
 * using System.nanoTime() and converted to seconds.
 */
public final class Time {
  private static double timeStarted = System.nanoTime();

  private Time() {}

  /**
   * Calculates the time since the start of the program.
   *
   * @return Time since the start of the program in seconds.
   */
  public static double getTimeSinceProgramStart() {
    return (System.nanoTime() - timeStarted) * 1E-9;
  }

  /**
   * Returns the time at which the program is started.
   *
   * @return Time since program start in seconds.
   */  
  public static double getTimeStarted() {
    return timeStarted;
  }
}
