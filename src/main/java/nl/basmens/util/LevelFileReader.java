package nl.basmens.util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LevelFileReader implements AutoCloseable {
  private static final Logger LOGGER = LogManager.getLogger(LevelFileReader.class);

  // Generics
  private static final String TYPE_KEY = "type";
  private static final String LEVEL_DATA_KEY = "position";
  private static final String GAME_OBJECTS_KEY = "game objects";
  private static final String MESHES_KEY = "meshes";
  private static final String TEXTUES_KEY = "textures";

  private String type;
  private Map<String, Object> levelData;

  private ArrayList<HashMap<String, Object>> gameObjects;
  private ArrayList<HashMap<String, Object>> meshes;
  private ArrayList<HashMap<String, Object>> textures;

  // ===============================================================================================
  // Read
  // ===============================================================================================
  @SuppressWarnings("unchecked") 
  public LevelFileReader read(String path) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

      HashMap<String, Object> level = (HashMap<String, Object>) Jsoner.deserialize(br);

      type = (String) level.get(TYPE_KEY);
      levelData = (Map<String, Object>) level.get(LEVEL_DATA_KEY);

      gameObjects = (ArrayList<HashMap<String, Object>>) level.get(GAME_OBJECTS_KEY);
      meshes = (ArrayList<HashMap<String, Object>>) level.get(MESHES_KEY);
      textures = (ArrayList<HashMap<String, Object>>) level.get(TEXTUES_KEY);
      
    } catch (JsonException e) {
      LOGGER.warn("Could not read level file '" + path + "', because the json is invalid", e);
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read level file '" + path + "', because the json does not describe a valid level", e);
    }

    return this;
  }

  // ===============================================================================================
  // Getters
  // ===============================================================================================
  public String getType() {
    return type;
  }

  public Map<String, Object> getLevelData() {
    return levelData;
  }

  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getGameObjects() {
    return (List<HashMap<String, Object>>) gameObjects.clone();
  }

  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getMeshes() {
    return (List<HashMap<String, Object>>) meshes.clone();
  }

  @SuppressWarnings("unchecked")
  public List<HashMap<String, Object>> getTextures() {
    return (List<HashMap<String, Object>>) textures.clone();
  }


  // ===============================================================================================
  // Autoclosable
  // ===============================================================================================
  @Override
  public void close() throws Exception {
    // Auto closable
  }
}
