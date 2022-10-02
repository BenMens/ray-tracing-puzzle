package nl.basmens.util;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MeshReader implements AutoCloseable {
  private static final Logger LOGGER = LogManager.getLogger(MeshReader.class);

  // Generics
  private static final String TYPE_KEY = "type";
  private static final String NAME_KEY = "name";

  // Object in different file
  private static final String OTHER_FILE_TYPE_VALUE = "json";
  private static final String OTHER_FILE_PATH_KEY = "path";

  // Mesh is in obj file
  private static final String OBJ_FILE_TYPE_VALUE = "obj";
  private static final String OBJ_FILE_PATH_KEY = "path";

  private String type;
  private String name;
  private String objFilePath;

  private ArrayList<String> otherFilePathStack = new ArrayList<>();

  // ===============================================================================================
  // Read
  // ===============================================================================================
  public MeshReader read(Map<String, Object> object) throws IOException {
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
      
      if (object.containsKey(NAME_KEY)) {
        name = (String) object.get(NAME_KEY);
      }
      if (OBJ_FILE_TYPE_VALUE.equals(type) && object.containsKey(OBJ_FILE_PATH_KEY)) {
        objFilePath = (String) object.get(OBJ_FILE_PATH_KEY);
      }
      
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid object", e);
    }

    return this;
  }


  @SuppressWarnings("unchecked")
  public MeshReader read(String path) throws IOException {
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

      if (object.containsKey(NAME_KEY)) {
        name = (String) object.get(NAME_KEY);
      }
      if (OBJ_FILE_TYPE_VALUE.equals(type) && object.containsKey(OBJ_FILE_PATH_KEY)) {
        objFilePath = (String) object.get(OBJ_FILE_PATH_KEY);
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

  public String getName() {
    return name;
  }

  public String getObjFilePath() {
    return objFilePath;
  }


  // ===============================================================================================
  // Autoclosable
  // ===============================================================================================
  @Override
  public void close() throws Exception {
    // Auto closable
  }
}
