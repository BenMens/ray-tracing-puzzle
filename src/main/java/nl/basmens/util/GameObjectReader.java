package nl.basmens.util;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public final class GameObjectReader implements AutoCloseable {
  private static final Logger LOGGER = LogManager.getLogger(GameObjectReader.class);

  // Generics
  private static final String TYPE_KEY = "type";
  private static final String POSITION_KEY = "position";
  private static final String MESH_KEY = "mesh";
  private static final String TEXTURE_KEY = "texture";
  private static final String OBJECT_DATA_KEY = "object data";

  // Object in different file
  private static final String OTHER_FILE_TYPE_VALUE = "json";
  private static final String OTHER_FILE_PATH_KEY = "path";

  private String type;
  private Vector3f position;
  private String mesh;
  private String texture;
  private HashMap<String, Object> objectData;

  private ArrayList<String> otherFilePathStack = new ArrayList<>();

  // ===============================================================================================
  // Read
  // ===============================================================================================
  @SuppressWarnings("unchecked") 
  public GameObjectReader read(Map<String, Object> object) throws IOException {
    try {
      type = (String) object.get(TYPE_KEY);

      if (OTHER_FILE_TYPE_VALUE.equals(type)) {
        String jsonPath = (String) object.get(OTHER_FILE_PATH_KEY);

        if (otherFilePathStack.contains(jsonPath)) {
          return null;
        }

        otherFilePathStack.add(jsonPath);
        read(jsonPath);
        otherFilePathStack.clear();
      }
      
      if (object.containsKey(POSITION_KEY)) {
        ArrayList<BigDecimal> vector = (ArrayList<BigDecimal>) object.get(POSITION_KEY);
        position = new Vector3f(vector.get(0).floatValue(), vector.get(1).floatValue(), vector.get(2).floatValue());
      }
      if (object.containsKey(MESH_KEY)) {
        mesh = (String) object.get(MESH_KEY);
      }
      if (object.containsKey(TEXTURE_KEY)) {
        texture = (String) object.get(TEXTURE_KEY);
      }
      if (object.containsKey(OBJECT_DATA_KEY)) {
        objectData = (HashMap<String, Object>) object.get(OBJECT_DATA_KEY);
      }
      
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid object", e);
    }

    return this;
  }


  @SuppressWarnings("unchecked")
  public GameObjectReader read(String path) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

      HashMap<String, Object> object = (HashMap<String, Object>) Jsoner.deserialize(br);

      type = (String) object.get(TYPE_KEY);

      if ("json".equals(type)) {
        String jsonPath = (String) object.get("path");

        if (otherFilePathStack.contains(jsonPath)) {
          return null;
        }

        otherFilePathStack.add(jsonPath);
        read(jsonPath);
        otherFilePathStack.clear();
      }

      if (object.containsKey(POSITION_KEY)) {
        ArrayList<BigDecimal> vector = (ArrayList<BigDecimal>) object.get(POSITION_KEY);
        position = new Vector3f(vector.get(0).floatValue(), vector.get(1).floatValue(), vector.get(2).floatValue());
      }
      if (object.containsKey(MESH_KEY)) {
        mesh = (String) object.get(MESH_KEY);
      }
      if (object.containsKey(TEXTURE_KEY)) {
        texture = (String) object.get(TEXTURE_KEY);
      }
      if (object.containsKey(OBJECT_DATA_KEY)) {
        objectData = (HashMap<String, Object>) object.get(OBJECT_DATA_KEY);
      }
      
    } catch (JsonException e) {
      LOGGER.warn("Could not read json '" + path + "', because the json is invalid", e);
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json '" + path + "', the json does not describe a valid object", e);
    }

    return this;
  }


  // ===============================================================================================
  // Getters
  // ===============================================================================================
  public String getType() {
    return type;
  }

  public Vector3f getPosition() {
    return position;
  }

  public String getMesh() {
    return mesh;
  }

  public String getTexture() {
    return texture;
  }

  public Map<String, Object> getObjectData() {
    return objectData;
  }


  // ===============================================================================================
  // Autoclosable
  // ===============================================================================================
  @Override
  public void close() throws Exception {
    // Auto closable
  }
}
