package nl.basmens.events.sources;

/**
 * Event sources.
 */
public final class GlfwEventSources {
  private static GlfwEventSources instance;

  private GlfwEventSource eventSource;

  // ===============================================================================================
  // Singleton
  // ===============================================================================================
  private GlfwEventSources() {
  }

  public static GlfwEventSources get() {
    if (instance == null) {
      instance = new GlfwEventSources();
    }

    return instance;
  }

  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public void setEventSource(GlfwEventSource source) {
    eventSource = source;
  }

  public GlfwEventSource getEventSource() {
    return eventSource;
  }

}
