package nl.basmens.events.event_listeners;

import nl.basmens.events.event_types.KeyEvent;

public class KeyEventListener {
    private static final int MAXKEYS = 350;

    private EventManager eventManager;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================
    public KeyEventListener() {
        String[] events = new String[MAXKEYS];
        for (int i = 0; i < events.length; i++) {
            events[i] = Integer.toString(i);
        }
        eventManager = new EventManager(events);
    }

    // =================================================================================================================
    // KeyCallBack
    // =================================================================================================================
    public void keyCallBack(long window, int key, int scanCode, int action, int mods) {
        if (key < MAXKEYS) {
            String eventType = Integer.toString(key);
            eventManager.notify(new KeyEvent(eventType, key, scanCode, action, mods));
        }
    }

    // =================================================================================================================
    // Register
    // =================================================================================================================
    public Observer register(int event, Observer observer) {
        return eventManager.register(Integer.toString(event), observer);
    }

    // =================================================================================================================
    // Unregister
    // =================================================================================================================
    public void unregister(int event, Observer observer) {
        eventManager.unregister(Integer.toString(event), observer);
    }
}