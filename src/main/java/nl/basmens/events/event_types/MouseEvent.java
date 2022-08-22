package nl.basmens.events.event_types;

// Near immutable class
public class MouseEvent extends Event {
    private final double posX;
    private final double posY;
    private final double prevX;
    private final double prevY;

    private final double scrollX;
    private final double scrollY;

    private final int button;
    private final int action;
    private final int mods;

    private final boolean isDragging;

    // Move event
    public MouseEvent(String eventType,
            double posX, double posY, double prevX, double prevY, boolean isDragging) {
        super(eventType);

        this.posX = posX;
        this.posY = posY;
        this.prevX = prevX;
        this.prevY = prevY;

        this.scrollX = 0;
        this.scrollY = 0;

        this.button = 0;
        this.action = 0;
        this.mods = 0;

        this.isDragging = isDragging;
    }

    // Click event
    public MouseEvent(String eventType,
            double posX, double posY, int button, int action, int mods) {
        super(eventType);

        this.posX = posX;
        this.posY = posY;
        this.prevX = 0;
        this.prevY = 0;

        this.scrollX = 0;
        this.scrollY = 0;

        this.button = button;
        this.action = action;
        this.mods = mods;

        this.isDragging = false;
    }

    // Scroll event
    public MouseEvent(String eventType,
            double scrollX, double scrollY) {
        super(eventType);

        this.posX = 0;
        this.posY = 0;
        this.prevX = 0;
        this.prevY = 0;

        this.scrollX = scrollX;
        this.scrollY = scrollY;

        this.button = 0;
        this.action = 0;
        this.mods = 0;

        this.isDragging = false;
    }

    // =================================================================================================================
    // Getters
    // =================================================================================================================
    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPrevX() {
        return prevX;
    }

    public double getPrevY() {
        return prevY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }

    public boolean isDragging() {
        return isDragging;
    }
}
