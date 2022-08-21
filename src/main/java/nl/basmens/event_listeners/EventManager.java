package nl.basmens.event_listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Guide: https://refactoring.guru/design-patterns/observer/java/example
public class EventManager {
    private Map<String, ArrayList<Observer>> listeners = new HashMap<>();


	// ====================================================================================================================
	// Constructor
	// ====================================================================================================================
    public EventManager(String... events) {
        for (String event : events) {
            listeners.put(event, new ArrayList<>());
        }
    }


	// ====================================================================================================================
	// Register
	// ====================================================================================================================
    public void register(String event, Observer observer) {
        ArrayList<Observer> users = listeners.get(event);

        if (!users.contains(observer)) {
            users.add(observer);
        }
    }


	// ====================================================================================================================
	// Unregister
	// ====================================================================================================================
    public void unregister(String event, Observer observer) {
        ArrayList<Observer> users = listeners.get(event);
        
        users.remove(observer);
    }


	// ====================================================================================================================
	// Notify
	// ====================================================================================================================
    public void notify(String event, int... args) {
        ArrayList<Observer> users = listeners.get(event);

        for (Observer u : users) {
            u.invoke(event, args);
        }
    }
}
