package nl.basmens.game.levels;

import nl.basmens.game.meshes.Triangles;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel {
  private static final Logger LOGGER = LogManager.getLogger(TestLevel.class);

  private Triangles triangles;


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    triangles = new Triangles();
    getRenderer().register(triangles);
  }

  
  /** 
   * Update the level based on elapsed time.
   *
   * @param deltaTime Time sinse last call in seconds.
   * 
   */
  public void update(double deltaTime) {
    triangles.update(deltaTime);
  }
}
