package nl.basmens.events.event_listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.basmens.events.event_types.Event;

// Guide: https://refactoring.guru/design-patterns/observer/java/example
public class EventManager {
    private static final Logger LOGGER = LogManager.getLogger(EventManager.class);

    private Map<String, ArrayList<Observer>> listeners = new HashMap<>();

    // =================================================================================================================
    // Constructor
    // =================================================================================================================
    public EventManager(String... eventTypes) {
        for (String event : eventTypes) {
            listeners.put(event, new ArrayList<>());
        }
    }

    // =================================================================================================================
    // Register
    // =================================================================================================================
    public Observer register(String eventType, Observer observer) {
        ArrayList<Observer> users = listeners.get(eventType);

        if (users != null) {
            if (!users.contains(observer)) {
                users.add(observer);
            }
        } else {
            LOGGER.warn("Unable to register client to event '" + eventType + "' because the type doesn't exist");
        }

        return observer;
    }

    // =================================================================================================================
    // Unregister
    // =================================================================================================================
    public void unregister(String eventType, Observer observer) {
        ArrayList<Observer> users = listeners.get(eventType);

        if (users != null) {
            users.remove(observer);
        } else {
            LOGGER.warn("Unable to unregister client to event '" + eventType + "' because the type doesn't exist");
        }
    }

    // =================================================================================================================
    // Notify
    // =================================================================================================================
    public void notify(Event eventType) {
        ArrayList<Observer> users = listeners.get(eventType.getEventType());

        if (users != null) {
            for (Observer u : users) {
                u.invoke(eventType);
            }
        } else {
            LOGGER.warn("Unable to notify clients about event '" + eventType + "' because the type doesn't exist");
        }
    }
}
