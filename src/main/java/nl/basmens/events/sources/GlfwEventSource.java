package nl.basmens.events.sources;

import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.listeners.KeyEventListener;
import nl.basmens.events.listeners.MouseEventListener;
import nl.basmens.events.types.Event;

public interface GlfwEventSource {

  EventDispatcher<Event> getWindowEventDispatcher();
  
  KeyEventListener getKeyEventListener();
  
  MouseEventListener getMouseEventListener();
}
