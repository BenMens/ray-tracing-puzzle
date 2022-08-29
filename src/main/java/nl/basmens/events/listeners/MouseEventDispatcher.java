package nl.basmens.events.listeners;

import nl.basmens.events.types.MouseEvent;

public class MouseEventDispatcher {
  private EventDispatcher<MouseEvent> eventDispatcher;

  private double prevX;
  private double prevY;
  private boolean isDragging;
  private boolean[] buttonsPressed = new boolean[3];

  private boolean hasInitialMousePos;

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public MouseEventDispatcher() {
    eventDispatcher = new EventDispatcher<>("click", "scroll", "move");

    prevX = 0;
    prevY = 0;
  }

  // ===============================================================================================
  // MousePosCallback
  // ===============================================================================================
  public void mousePosCallBack(long window, double posX, double posY) {
    if (hasInitialMousePos) {
      eventDispatcher.notify(new MouseEvent("move", posX, posY, prevX, prevY, isDragging));
    }

    prevX = posX;
    prevY = posY;
    hasInitialMousePos = true;

    isDragging = buttonsPressed[0] || buttonsPressed[1] || buttonsPressed[2];
  }

  // ===============================================================================================
  // MouseButtonCallback
  // ===============================================================================================
  public void mouseButtonCallback(long window, int button, int action, int mods) {
    if (hasInitialMousePos && button < buttonsPressed.length) {
      eventDispatcher.notify(new MouseEvent("click", prevX, prevY, button, action, mods));
    }
  }

  // ===============================================================================================
  // MouseScrollCallback
  // ===============================================================================================
  public void mouseScrollCallback(long window, double scrollX, double scrollY) {
    eventDispatcher.notify(new MouseEvent("scroll", scrollX, scrollY));
  }

  // ===============================================================================================
  // Register
  // ===============================================================================================
  public Observer<MouseEvent> register(String eventType, Observer<MouseEvent> observer) {
    return eventDispatcher.register(eventType, observer);
  }

  // ===============================================================================================
  // Unregister
  // ===============================================================================================
  public void unregister(String eventType, Observer<MouseEvent> observer) {
    eventDispatcher.unregister(eventType, observer);
  }
}
