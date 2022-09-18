package nl.basmens.game.levels;

import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.sources.LevelEventSource;
import nl.basmens.events.types.Event;
import nl.basmens.renderer.Camera;
import nl.basmens.renderer.Renderer;

/**
 * Abstract game level that is the basis for concrete level objects.
 */
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
  public void render(Camera camera, int width, int height) {
    renderer.render(camera, width, height);
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
