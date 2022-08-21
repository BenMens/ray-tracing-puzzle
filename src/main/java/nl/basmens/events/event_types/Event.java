package nl.basmens.events.event_types;

public class Event {
    private final String eventType;

    public Event(String eventType) {
        this.eventType = eventType;
    }

    // =================================================================================================================
    // Getters
    // =================================================================================================================
    public String getEventType() {
        return eventType;
    }
}
