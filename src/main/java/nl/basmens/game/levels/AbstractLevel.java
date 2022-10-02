package nl.basmens.game.levels;

import java.util.HashMap;
import java.util.Map;
import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.sources.LevelEventSource;
import nl.basmens.events.types.Event;
import nl.basmens.game.gameobjects.GameObject;
import nl.basmens.renderer.Camera;
import nl.basmens.renderer.Renderer;

/**
 * Abstract game level that is the basis for concrete level objects.
 */
public abstract class AbstractLevel implements LevelEventSource {

  public final EventDispatcher<Event> levelEventDispatcher = new EventDispatcher<>("win", "lose");

  private Renderer renderer;

  private HashMap<String, GameObject> gameObjects = new HashMap<>();

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
  // Getters, setters and adders
  // ===============================================================================================
  public EventDispatcher<Event> getLevelEventDispatcher() {
    return levelEventDispatcher;
  }

  public Renderer getRenderer() {
    return renderer;
  }

  @SuppressWarnings("unchecked")
  public Map<String, GameObject> getGameObjects() {
    return (Map<String, GameObject>) gameObjects.clone();
  }

  public Object getGameObjectById(String gameObjectId) {
    return gameObjects.get(gameObjectId);
  }

  public void addGameObject(String gameObjectId, GameObject gameObject) {
    gameObjects.put(gameObjectId, gameObject);
  }


  // TODO replace test code with proper implementation
  public abstract void printData();
}
