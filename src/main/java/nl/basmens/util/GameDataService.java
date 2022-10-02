package nl.basmens.util;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.listeners.Observer;
import nl.basmens.events.types.GameDataServiceEvent;
import nl.basmens.game.gameobjects.Chest;
import nl.basmens.game.gameobjects.GameObject;
import nl.basmens.game.levels.AbstractLevel;
import nl.basmens.game.levels.TestLevel;
import nl.basmens.game.meshes.Triangles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public final class GameDataService {
  private static final Logger LOGGER = LogManager.getLogger(GameDataService.class);

  private static GameDataService instance;

  private static final String TYPE_KEY = "type";
  private static final String LEVEL_DATA_KEY = "position";
  private static final String GAME_OBJECTS_KEY = "gameObjects";
  private static final String MESHES_KEY = "meshes";
  private static final String TEXTURES_KEY = "textures";
  private static final String GAME_OBJECTS_TYPE_KEY = "type";
  private static final String GAME_OBJECTS_POSITION_KEY = "position";
  private static final String GAME_OBJECTS_MESH_KEY = "mesh";
  private static final String GAME_OBJECTS_TEXTURE_KEY = "texture";
  private static final String GAME_OBJECTS_OBJECT_DATA_KEY = "objectData";
  private static final String MESHES_TYPE_KEY = "type";
  private static final String MESHES_OBJ_TYPE_VALUE = "obj";
  private static final String MESHES_OBJ_PATH_KEY = "path";
  private static final String TEXTURES_TYPE_KEY = "type";
  private static final String TEXTURES_PNG_TYPE_VALUE = "png";
  private static final String TEXTURES_PNG_PATH_KEY = "path";

  public static final String CLEAR_EVENT = "clear";
  public static final String START_LOADING_LEVEL_EVENT = "start loading level";
  public static final String FINISH_LOADING_LEVEL_EVENT = "finish loading level";
  public static final String GAME_OBJECT_ADDED_EVENT = "object added";
  public static final String MESH_ADDED_EVENT = "mesh added";
  public static final String TEXTURE_ADDED_EVENT = "texture added";

  private EventDispatcher<GameDataServiceEvent> eventDispatcher =
      new EventDispatcher<>(CLEAR_EVENT, START_LOADING_LEVEL_EVENT, FINISH_LOADING_LEVEL_EVENT, GAME_OBJECT_ADDED_EVENT,
          MESH_ADDED_EVENT, TEXTURE_ADDED_EVENT);

  private ObjFileReader objFileReader = new ObjFileReader();

  private AbstractLevel level;

  private HashMap<String, MeshInterface> meshes = new HashMap<>();
  private HashMap<String, Object> textures = new HashMap<>(); // TODO add textures


  // ===================================================================================================================
  // Singleton
  // ===================================================================================================================
  private GameDataService() {}

  public static GameDataService get() {
    if (instance == null) {
      instance = new GameDataService();
    }

    return instance;
  }

  // ===============================================================================================
  // Register
  // ===============================================================================================
  public Observer<GameDataServiceEvent> register(String eventType, Observer<GameDataServiceEvent> observer) {
    return eventDispatcher.register(eventType, observer);
  }

  // ===============================================================================================
  // Unregister
  // ===============================================================================================
  public void unregister(String eventType, Observer<GameDataServiceEvent> observer) {
    eventDispatcher.unregister(eventType, observer);
  }

  // ===================================================================================================================
  // Clear
  // ===================================================================================================================
  public void clear() {
    level = null;

    meshes.clear();
    textures.clear();

    eventDispatcher.notify(new GameDataServiceEvent(CLEAR_EVENT));
  }


  // ===================================================================================================================
  // Get json
  // ===================================================================================================================
  @SuppressWarnings("unchecked")
  private static HashMap<String, Object> parseJsonFile(String path) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

      return (HashMap<String, Object>) Jsoner.deserialize(br);
    } catch (JsonException e) {
      LOGGER.warn("Could not parse json file '" + path + "' because the json is invalid", e);
    } catch (ClassCastException e) {
      LOGGER.warn("Could not parse json file '" + path + "' because file is not a json object", e);
    }

    return new HashMap<>();
  }


  // ===================================================================================================================
  // Read level
  // ===================================================================================================================
  public void loadLevel(String path) throws IOException {
    loadLevel(parseJsonFile(path));
  }

  @SuppressWarnings("unchecked")
  public void loadLevel(Map<String, Object> json) throws IOException {
    clear();

    eventDispatcher.notify(new GameDataServiceEvent(START_LOADING_LEVEL_EVENT));
    try {
      String type = (String) json.get(TYPE_KEY);
      Map<String, Object> data = (Map<String, Object>) json.get(LEVEL_DATA_KEY);

      switch (type) {
        case "type1":
          level = new TestLevel();
          break;

        case "type2":
          level = new TestLevel(data);
          break;

        default:
          LOGGER.warn("Could not create level with type '" + type + "'");
      }

      HashMap<String, Object> levelMeshes = (HashMap<String, Object>) json.get(MESHES_KEY);
      for (Entry<String, Object> mesh : levelMeshes.entrySet()) {
        addMesh(mesh.getKey(), (HashMap<String, Object>) mesh.getValue());
      }

      HashMap<String, Object> levelTextures = (HashMap<String, Object>) json.get(TEXTURES_KEY);
      for (Entry<String, Object> texture : levelTextures.entrySet()) {
        addTexture(texture.getKey(), (HashMap<String, Object>) texture.getValue());
      }

      HashMap<String, Object> levelGameObjects = (HashMap<String, Object>) json.get(GAME_OBJECTS_KEY);
      for (Entry<String, Object> gameObject : levelGameObjects.entrySet()) {
        addGameObject(gameObject.getKey(), (HashMap<String, Object>) gameObject.getValue());
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not load json, because the json does not describe a valid level", e);
    }

    eventDispatcher.notify(new GameDataServiceEvent(FINISH_LOADING_LEVEL_EVENT));
  }


  // ===================================================================================================================
  // Add game object
  // ===================================================================================================================
  @SuppressWarnings("unchecked")
  public void addGameObject(String gameObjectId, Map<String, Object> json) {
    if (level.getGameObjectById(gameObjectId) != null) {
      LOGGER.warn("Failed to add GameObject because there is already one known with id '" + gameObjectId + "'");
      return;
    }

    try {
      String type = (String) json.get(GAME_OBJECTS_TYPE_KEY);

      ArrayList<BigDecimal> vector = (ArrayList<BigDecimal>) json.get(GAME_OBJECTS_POSITION_KEY);
      Vector3f position =
          new Vector3f(vector.get(0).floatValue(), vector.get(1).floatValue(), vector.get(2).floatValue());

      String mesh = (String) json.get(GAME_OBJECTS_MESH_KEY);

      String texture = (String) json.get(GAME_OBJECTS_TEXTURE_KEY);

      HashMap<String, Object> data = (HashMap<String, Object>) json.get(GAME_OBJECTS_OBJECT_DATA_KEY);

      switch (type) {
        case "gameObject":
          addGameObjectAndNotify(gameObjectId, new GameObject(position, mesh, texture));
          break;

        case "chest":
          JsonArray items = (JsonArray) data.get("items");
          addGameObjectAndNotify(gameObjectId, new Chest(position, mesh, texture, items.toArray(new String[0])));
          break;

        default:
          LOGGER.warn("Could not create game object with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid object", e);
    }
  }


  private void addGameObjectAndNotify(String gameObjectId, GameObject gameObject) {
    level.addGameObject(gameObjectId, gameObject);
    eventDispatcher.notify(new GameDataServiceEvent(GAME_OBJECT_ADDED_EVENT, gameObject));
  }


  // ===================================================================================================================
  // Add mesh
  // ===================================================================================================================
  public void addMesh(String meshId, Map<String, Object> json) throws IOException {
    if (meshes.containsKey(meshId)) {
      LOGGER.warn("Failed to add MeshInteface because there is already one known with id '" + meshId + "'");
      return;
    }

    try {
      String type = (String) json.get(MESHES_TYPE_KEY);

      switch (type) {
        case MESHES_OBJ_TYPE_VALUE:
          String path = (String) json.get(MESHES_OBJ_PATH_KEY);
          addMeshAndNotify(meshId, objFileReader.read(path).getMesh());
          break;

        case "cube":
          addMeshAndNotify(meshId, new Triangles());
          break;

        default:
          LOGGER.warn("Could not create mesh with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid mesh", e);
    }
  }

  private void addMeshAndNotify(String meshId, MeshInterface mesh) {
    meshes.put(meshId, mesh);
    eventDispatcher.notify(new GameDataServiceEvent(MESH_ADDED_EVENT, mesh));
  }


  // ===================================================================================================================
  // Add texture
  // ===================================================================================================================
  public void addTexture(String textureId, Map<String, Object> json) {
    if (textures.containsKey(textureId)) {
      LOGGER.warn("Failed to add Texture because there is already one known with id '" + textureId + "'");
      return;
    }

    try {
      String type = (String) json.get(TEXTURES_TYPE_KEY);

      switch (type) {
        case TEXTURES_PNG_TYPE_VALUE:
          String path = (String) json.get(TEXTURES_PNG_PATH_KEY);
          // TODO read png
          // addTextureAndNotify();
          break;

        default:
          LOGGER.warn("Could not create texture with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid texture", e);
    }
  }

  private void addTextureAndNotify(String textureId, Object texture) {
    textures.put(textureId, texture);
    // eventDispatcher.notify(new GameDataServiceEvent(TEXTURE_ADDED_EVENT, texture));
  }


  // ===================================================================================================================
  // Getters
  // ===================================================================================================================
  public AbstractLevel getLevel() {
    return level;
  }

  @SuppressWarnings("unchecked")
  public Map<String, MeshInterface> getMeshes() {
    return (Map<String, MeshInterface>) meshes.clone();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getTextures() {
    return (Map<String, Object>) textures.clone();
  }
}
