package nl.basmens.events.event_listeners;

import nl.basmens.events.event_types.Event;

public interface Observer<E extends Event> {
    void invoke(E event);
}
