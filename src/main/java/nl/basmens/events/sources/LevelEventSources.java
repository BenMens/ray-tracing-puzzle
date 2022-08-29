package nl.basmens.events.sources;

public final class LevelEventSources {
  private static LevelEventSources instance;

  private LevelEventSource eventSource;

  // ===============================================================================================
  // Singleton
  // ===============================================================================================
  private LevelEventSources() {}

  public static LevelEventSources get() {
    if (instance == null) {
      instance = new LevelEventSources();
    }

    return instance;
  }


  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public void setEventSource(LevelEventSource source) {
    eventSource = source;
  }

  public LevelEventSource getEventSource() {
    return eventSource;
  }
}
