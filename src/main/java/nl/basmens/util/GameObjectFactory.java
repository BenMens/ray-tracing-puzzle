package nl.basmens.util;

import com.github.cliftonlabs.json_simple.JsonArray;
import java.util.Map;
import nl.basmens.game.gameobjects.Chest;
import nl.basmens.game.gameobjects.GameObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public final class GameObjectFactory {
  private static final Logger LOGGER = LogManager.getLogger(GameObjectFactory.class);

  private static GameObjectFactory instance;


  // ===============================================================================================
  // Singleton
  // ===============================================================================================
  private GameObjectFactory() {}

  public static GameObjectFactory get() {
    if (instance == null) {
      instance = new GameObjectFactory();
    }

    return instance;
  }


  // ===============================================================================================
  // Get game object
  // ===============================================================================================
  public static GameObject getGameObject(String type, Vector3f position, String mesh, String texture,
      Map<String, Object> objectData) {
    switch (type) {
      case "game object":
        return new GameObject(position, mesh, texture);

      case "chest":
        JsonArray items = (JsonArray) objectData.get("items");
        return new Chest(position, mesh, texture, items.toArray(new String[0]));

      default:
        LOGGER.warn("Could not create game object with type '" + type + "'");
        return null;
    }
  }
}
