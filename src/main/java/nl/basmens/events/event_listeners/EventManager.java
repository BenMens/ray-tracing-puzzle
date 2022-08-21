package nl.basmens.events.event_listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.basmens.events.event_types.Event;

// Guide: https://refactoring.guru/design-patterns/observer/java/example
public class EventManager {
    private Map<String, ArrayList<Observer>> listeners = new HashMap<>();


	// ====================================================================================================================
	// Constructor
	// ====================================================================================================================
    public EventManager(String... eventTypes) {
        for (String event : eventTypes) {
            listeners.put(event, new ArrayList<>());
        }
    }


	// ====================================================================================================================
	// Register
	// ====================================================================================================================
    public Observer register(String eventType, Observer observer) {
        ArrayList<Observer> users = listeners.get(eventType);

        if (users != null) {
            if (!users.contains(observer)) {
                users.add(observer);
            }
        } else {
            // TODO print warning: invalid event type
        }

        return observer;
    }


	// ====================================================================================================================
	// Unregister
	// ====================================================================================================================
    public void unregister(String eventType, Observer observer) {
        ArrayList<Observer> users = listeners.get(eventType);
        
        if (users != null) {
            users.remove(observer);
        } else {
            // TODO print warning: invalid event type
        }
    }


	// ====================================================================================================================
	// Notify
	// ====================================================================================================================
    public void notify(Event event) {
        ArrayList<Observer> users = listeners.get(event.getEventType());

        if (users != null) {
            for (Observer u : users) {
                u.invoke( event);
            }
        } else {
            // TODO print warning: invalid event type
        }
    }
}
