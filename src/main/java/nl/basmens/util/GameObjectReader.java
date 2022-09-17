package nl.basmens.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public final class GameObjectReader implements AutoCloseable {
  private static final Logger LOGGER = LogManager.getLogger(GameObjectReader.class);

  private String type;
  private Vector3f position;
  private String mesh;
  private String texture;
  private HashMap<String, Object> objectData;

  // ===============================================================================================
  // Read
  // ===============================================================================================
  @SuppressWarnings("unchecked") 
  public GameObjectReader read(Map<String, Object> object) {
    try {
      type = (String) object.get("type");
      
      ArrayList<BigDecimal> vector = (ArrayList<BigDecimal>) object.get("position");
      position = new Vector3f(vector.get(0).floatValue(), vector.get(1).floatValue(), vector.get(2).floatValue());

      mesh = (String) object.get("mesh");
      texture = (String) object.get("texture");
      objectData = (HashMap<String, Object>) object.get("object data");
      
    } catch (ClassCastException e) {
      LOGGER.warn("Could not read json, the json does not describe a valid object", e);
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
