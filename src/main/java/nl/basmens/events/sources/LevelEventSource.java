package nl.basmens.events.sources;

import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.types.Event;

public interface LevelEventSource {
  EventDispatcher<Event> getLevelEventDispatcher();
}
