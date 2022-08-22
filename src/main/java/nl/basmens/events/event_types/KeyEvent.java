package nl.basmens.events.event_types;

// Near immutable class
public class KeyEvent extends Event {
    private final int key;
    private final int keyCode;
    private final int action;
    private final int mods;

    public KeyEvent(String eventType, int key, int keyCode, int action, int mods) {
        super(eventType);

        this.key = key;
        this.keyCode = keyCode;
        this.action = action;
        this.mods = mods;
    }

    // =================================================================================================================
    // Getters
    // =================================================================================================================
    public int getKey() {
        return key;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }
}
