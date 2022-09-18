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
import java.util.List;
import java.util.Map;
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
  private static final String GAME_OBJECTS_KEY = "game objects";
  private static final String MESHES_KEY = "meshes";
  private static final String TEXTUES_KEY = "textures";
  private static final String GAME_OBJECTS_TYPE_KEY = "type";
  private static final String GAME_OBJECTS_POSITION_KEY = "position";
  private static final String GAME_OBJECTS_MESH_KEY = "mesh";
  private static final String GAME_OBJECTS_TEXTURE_KEY = "texture";
  private static final String GAME_OBJECTS_OBJECT_DATA_KEY = "object data";
  private static final String GAME_OBJECTS_REFERENCE_TYPE_VALUE = "json";
  private static final String GAME_OBJECTS_REFERENCE_PATH_KEY = "path";
  private static final String MESHES_TYPE_KEY = "type";
  private static final String MESHES_NAME_KEY = "name";
  private static final String MESHES_OBJ_TYPE_VALUE = "obj";
  private static final String MESHES_OBJ_PATH_KEY = "path";
  private static final String MESHES_REFERENCE_TYPE_VALUE = "json";
  private static final String MESHES_REFERENCE_PATH_KEY = "path";
  private static final String TEXTURES_TYPE_KEY = "type";
  private static final String TEXTURES_NAME_KEY = "name";
  private static final String TEXTURES_PNG_TYPE_VALUE = "png";
  private static final String TEXTURES_PNG_PATH_KEY = "path";
  private static final String TEXTURES_REFERENCE_TYPE_VALUE = "json";
  private static final String TEXTURES_REFERENCE_PATH_KEY = "path";

  private static final ObjFileReader objFileReader = new ObjFileReader();

  private AbstractLevel level;

  private HashMap<String, MeshInterface> meshes = new HashMap<>();
  private HashMap<String, Object> textures = new HashMap<>();


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

  // ===================================================================================================================
  // Clear
  // ===================================================================================================================
  public void clear() {
    level = null;

    meshes.clear();
    textures.clear();
  }


  // ===================================================================================================================
  // Get json
  // ===================================================================================================================
  @SuppressWarnings("unchecked")
  private static HashMap<String, Object> getJson(String path) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

      return (HashMap<String, Object>) Jsoner.deserialize(br);
    } catch (JsonException e) {
      LOGGER.warn("Could not read json file '" + path + "' because the json is invalid", e);
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json file '" + path + "' because file is not a json object", e);
    }

    return new HashMap<>();
  }

  // ===================================================================================================================
  // Parse json reference
  // ===================================================================================================================
  private static Map<String, Object> parseJsonReference(Map<String, Object> json, String typeKey, String referenceValue,
      String pathKey) throws IOException {

    ArrayList<String> referencePathHistory = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();

    json.forEach(result::put);

    while (referenceValue.equals(json.get(typeKey))) {
      String path = (String) json.get(pathKey);
      if (referencePathHistory.contains(path)) {
        break;
      }
      referencePathHistory.add(path);
      json = getJson(path);

      json.forEach((String key, Object value) -> {
        if (typeKey.equals(key) || !result.containsKey(key)) {
          result.put(key, value);
        }
      });
    }

    return result;
  }


  // ===================================================================================================================
  // Read level
  // ===================================================================================================================
  public void readLevel(String path) throws IOException {
    readLevel(getJson(path));
  }

  @SuppressWarnings("unchecked")
  public void readLevel(Map<String, Object> json) throws IOException {
    clear();

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

      for (HashMap<String, Object> mesh : (ArrayList<HashMap<String, Object>>) json.get(MESHES_KEY)) {
        addMesh(mesh);
      }

      for (HashMap<String, Object> texture : (ArrayList<HashMap<String, Object>>) json.get(TEXTUES_KEY)) {
        addTexture(texture);
      }

      for (HashMap<String, Object> object : (ArrayList<HashMap<String, Object>>) json.get(GAME_OBJECTS_KEY)) {
        addGameObject(object);
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, because the json does not describe a valid level", e);
    }
  }


  // ===================================================================================================================
  // Add game object
  // ===================================================================================================================
  @SuppressWarnings("unchecked")
  public void addGameObject(Map<String, Object> json) throws IOException {
    try {
      json = parseJsonReference(json, GAME_OBJECTS_TYPE_KEY, GAME_OBJECTS_REFERENCE_TYPE_VALUE,
          GAME_OBJECTS_REFERENCE_PATH_KEY);

      String type = (String) json.get(GAME_OBJECTS_TYPE_KEY);

      ArrayList<BigDecimal> vector = (ArrayList<BigDecimal>) json.get(GAME_OBJECTS_POSITION_KEY);
      Vector3f position =
          new Vector3f(vector.get(0).floatValue(), vector.get(1).floatValue(), vector.get(2).floatValue());

      String mesh = (String) json.get(GAME_OBJECTS_MESH_KEY);

      String texture = (String) json.get(GAME_OBJECTS_TEXTURE_KEY);

      HashMap<String, Object> data = (HashMap<String, Object>) json.get(GAME_OBJECTS_OBJECT_DATA_KEY);

      switch (type) {
        case "game object":
          level.addGameObject(new GameObject(position, mesh, texture));
          break;

        case "chest":
          JsonArray items = (JsonArray) data.get("items");
          level.addGameObject(new Chest(position, mesh, texture, items.toArray(new String[0])));
          break;

        default:
          LOGGER.warn("Could not create game object with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid object", e);
    }
  }


  // ===================================================================================================================
  // Add mesh
  // ===================================================================================================================
  public void addMesh(Map<String, Object> json) throws IOException {
    try {
      json = parseJsonReference(json, MESHES_TYPE_KEY, MESHES_REFERENCE_TYPE_VALUE, MESHES_REFERENCE_PATH_KEY);

      String name = (String) json.get(MESHES_NAME_KEY);
      if (meshes.containsKey(name)) {
        LOGGER.warn("Could not add mesh '" + name + "' because there is already a know mesh with that name");
        return;
      }

      String type = (String) json.get(MESHES_TYPE_KEY);
      switch (type) {
        case MESHES_OBJ_TYPE_VALUE:
          String path = (String) json.get(MESHES_OBJ_PATH_KEY);
          meshes.put(name, objFileReader.read(path).getMesh());
          break;

        case "cube":
          meshes.put(name, new Triangles());
          break;

        default:
          LOGGER.warn("Could not create mesh with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid mesh", e);
    }
  }


  // ===================================================================================================================
  // Add texture
  // ===================================================================================================================
  public void addTexture(Map<String, Object> json) throws IOException {
    try {
      json = parseJsonReference(json, TEXTURES_TYPE_KEY, TEXTURES_REFERENCE_TYPE_VALUE, TEXTURES_REFERENCE_PATH_KEY);

      String name = (String) json.get(TEXTURES_NAME_KEY);
      if (textures.containsKey(name)) {
        LOGGER.warn("Could not add texture '" + name + "' because there is already a know texture with that name");
        return;
      }

      String type = (String) json.get(TEXTURES_TYPE_KEY);
      switch (type) {
        case TEXTURES_PNG_TYPE_VALUE:
          String path = (String) json.get(TEXTURES_PNG_PATH_KEY);
          // TODO read png
          //textures.put(name, dosomething);
          break;

        default:
          LOGGER.warn("Could not create texture with type '" + type + "'");
      }
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid texture", e);
    }
  }


  // ===================================================================================================================
  // Getters
  // ===================================================================================================================
  public AbstractLevel getLevel() {
    return level;
  }

  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getMeshes() {
    return (List<HashMap<String, Object>>) meshes.clone();
  }

  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getTextures() {
    return (List<HashMap<String, Object>>) textures.clone();
  }
}
