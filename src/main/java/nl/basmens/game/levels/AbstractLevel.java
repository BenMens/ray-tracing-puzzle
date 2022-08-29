package nl.basmens.game.levels;

import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.sources.LevelEventSource;
import nl.basmens.events.types.Event;
import nl.basmens.renderer.Camera;
import nl.basmens.renderer.Renderer;

public abstract class AbstractLevel implements LevelEventSource {

  public final EventDispatcher<Event> levelEventDispatcher = new EventDispatcher<>("win", "lose");

  private Renderer renderer;

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  protected AbstractLevel() {
    renderer = new Renderer();
  }


  // ===============================================================================================
  // Init
  // ===============================================================================================
  public void init() throws Exception {  // TODO throw specific exeption
    renderer.init();
  }


  // ===============================================================================================
  // Update
  // ===============================================================================================
  public abstract void update(double dt);

  // ===============================================================================================
  // Render
  // ===============================================================================================
  public void render(Camera camera) {
    renderer.render(camera);
  }


  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public EventDispatcher<Event> getLevelEventDispatcher() {
    return levelEventDispatcher;
  }

  public Renderer getRenderer() {
    return renderer;
  }
}
