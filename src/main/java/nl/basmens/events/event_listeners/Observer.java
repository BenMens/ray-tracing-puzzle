package nl.basmens.events.event_listeners;

import nl.basmens.events.event_types.Event;

public interface Observer {
    void invoke(Event event);
}
