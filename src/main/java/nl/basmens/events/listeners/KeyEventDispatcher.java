package nl.basmens.events.listeners;

import nl.basmens.events.types.KeyEvent;

public class KeyEventDispatcher {
  private static final int MAXKEYS = 350;

  private EventDispatcher<KeyEvent> eventDispatcher;

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public KeyEventDispatcher() {
    String[] eventTypes = new String[MAXKEYS];
    for (int i = 0; i < eventTypes.length; i++) {
      eventTypes[i] = Integer.toString(i);
    }
    eventDispatcher = new EventDispatcher<>(eventTypes);
  }

  // ===============================================================================================
  // KeyCallBack
  // ===============================================================================================
  public void keyCallBack(long window, int key, int scanCode, int action, int mods) {
    if (key < MAXKEYS) {
      String eventType = Integer.toString(key);
      eventDispatcher.notify(new KeyEvent(eventType, key, scanCode, action, mods));
    }
  }

  // ===============================================================================================
  // Register
  // ===============================================================================================
  public Observer<KeyEvent> register(int eventType, Observer<KeyEvent> observer) {
    return eventDispatcher.register(Integer.toString(eventType), observer);
  }

  // ===============================================================================================
  // Unregister
  // ===============================================================================================
  public void unregister(int eventType, Observer<KeyEvent> observer) {
    eventDispatcher.unregister(Integer.toString(eventType), observer);
  }
}
