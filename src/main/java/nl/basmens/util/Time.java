package nl.basmens.util;

public final class Time {
  private static double timeStarted = System.nanoTime();

  private Time() {}

  public static double getTime() {
    return (System.nanoTime() - timeStarted) * 1E-9;
  }

  public static double getTimeStarted() {
    return timeStarted;
  }
}
