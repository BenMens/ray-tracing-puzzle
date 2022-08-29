package nl.basmens.events.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import nl.basmens.events.types.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generic event dispatcher.
 * <p>
 * Gui1de: https://refactoring.guru/design-patterns/observer/java/example
 * </p>
 */
public class EventDispatcher<E extends Event> {
  private static final Logger LOGGER = LogManager.getLogger(EventDispatcher.class);

  private Map<String, ArrayList<Observer<E>>> listeners = new HashMap<>();

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public EventDispatcher(String... eventTypes) {
    for (String event : eventTypes) {
      listeners.put(event, new ArrayList<>());
    }
  }

  // ===============================================================================================
  // Register
  // ===============================================================================================
  public Observer<E> register(String eventType, Observer<E> observer) {
    ArrayList<Observer<E>> users = listeners.get(eventType);

    if (users != null) {
      if (!users.contains(observer)) {
        users.add(observer);
      }
    } else {
      LOGGER.warn(
          "Unable to register client to event '" + eventType + "' because the type doesn't exist");
    }

    return observer;
  }

  // ===============================================================================================
  // Unregister
  // ===============================================================================================
  public void unregister(String eventType, Observer<E> observer) {
    ArrayList<Observer<E>> users = listeners.get(eventType);

    if (users != null) {
      users.remove(observer);
    } else {
      LOGGER.warn("Unable to unregister client to event '" + eventType
          + "' because the type doesn't exist");
    }
  }

  // ===============================================================================================
  // Notify
  // ===============================================================================================
  public void notify(E eventType) {
    ArrayList<Observer<E>> users = listeners.get(eventType.getEventType());

    if (users != null) {
      for (Observer<E> u : users) {
        u.invoke(eventType);
      }
    } else {
      LOGGER.warn("Unable to notify clients about event '" + eventType
          + "' because the type doesn't exist");
    }
  }
}
