package nl.basmens.event_listeners;

public interface Observer {
    void invoke(String event, int... args);
}
