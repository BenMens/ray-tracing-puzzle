package nl.basmens.events.listeners;

import nl.basmens.events.types.Event;

public interface Observer<E extends Event> {
  void invoke(E event);
}