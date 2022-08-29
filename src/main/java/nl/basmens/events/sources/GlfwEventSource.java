package nl.basmens.events.sources;

import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.listeners.KeyEventDispatcher;
import nl.basmens.events.listeners.MouseEventDispatcher;
import nl.basmens.events.types.Event;

public interface GlfwEventSource {

  EventDispatcher<Event> getWindowEventDispatcher();
  
  KeyEventDispatcher getKeyEventDispatcher();
  
  MouseEventDispatcher getMouseEventDispatcher();
}
