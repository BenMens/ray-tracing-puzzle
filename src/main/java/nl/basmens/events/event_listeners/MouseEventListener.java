package nl.basmens.events.event_listeners;

import nl.basmens.events.event_types.MouseEvent;

public class MouseEventListener {
    private EventManager<MouseEvent> eventManager;

    private double prevX;
    private double prevY;
    private boolean isDragging;
    private boolean[] buttonsPressed = new boolean[3];

    private boolean hasInitialMousePos;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================
    public MouseEventListener() {
        eventManager = new EventManager<>("click", "scroll", "move");

        prevX = 0;
        prevY = 0;
    }

    // =================================================================================================================
    // MousePosCallback
    // =================================================================================================================
    public void mousePosCallBack(long window, double posX, double posY) {
        if (hasInitialMousePos) {
            eventManager.notify(new MouseEvent("move", posX, posY, prevX, prevY, isDragging));
        }

        prevX = posX;
        prevY = posY;
        hasInitialMousePos = true;

        isDragging = buttonsPressed[0] || buttonsPressed[1] || buttonsPressed[2];
    }

    // =================================================================================================================
    // MouseButtonCallback
    // =================================================================================================================
    public void mouseButtonCallback(long window, int button, int action, int mods) {
        if (hasInitialMousePos && button < buttonsPressed.length) {
            eventManager.notify(new MouseEvent("click", prevX, prevY, button, action, mods));
        }
    }

    // =================================================================================================================
    // MouseScrollCallback
    // =================================================================================================================
    public void mouseScrollCallback(long window, double scrollX, double scrollY) {
        eventManager.notify(new MouseEvent("scroll", scrollX, scrollY));
    }

    // =================================================================================================================
    // Register
    // =================================================================================================================
    public Observer<MouseEvent> register(String eventType, Observer<MouseEvent> observer) {
        return eventManager.register(eventType, observer);
    }

    // =================================================================================================================
    // Unregister
    // =================================================================================================================
    public void unregister(String eventType, Observer<MouseEvent> observer) {
        eventManager.unregister(eventType, observer);
    }
}
