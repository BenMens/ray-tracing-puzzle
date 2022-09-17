package nl.basmens.util;

import java.util.Map;
import nl.basmens.game.levels.AbstractLevel;
import nl.basmens.game.levels.TestLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LevelFactory {
  private static final Logger LOGGER = LogManager.getLogger(LevelFactory.class);

  private static LevelFactory instance;


  // ===============================================================================================
  // Singleton
  // ===============================================================================================
  private LevelFactory() {}

  public static LevelFactory get() {
    if (instance == null) {
      instance = new LevelFactory();
    }

    return instance;
  }


  // ===============================================================================================
  // Get game object
  // ===============================================================================================
  public static AbstractLevel getLevel(String type, Map<String, Object> levelData) {
    switch (type) {
      case "type1":
        return new TestLevel();

      case "type2":
        return new TestLevel(levelData);

      default:
        LOGGER.warn("Could not create level with type '" + type + "'");
        return null;
    }
  }
}
