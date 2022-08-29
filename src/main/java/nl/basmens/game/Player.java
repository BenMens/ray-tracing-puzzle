package nl.basmens.game;

import nl.basmens.renderer.Camera;
import org.joml.Vector3f;

/**
 * TODO: describe the goal of this class.
 */
public class Player {
  private Camera camera;



  /**
   * Constructs a player object that is the bridge between the human player and
   * the level.
   */
  public Player() {
    camera = new Camera(new Vector3f(0), new Vector3f(0), 3F);
  }


  /**
   * Initialize the player object.
   */
  public void init() {
    // Currently not used.
  }

  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public Camera getCamera() {
    return camera;
  }
}
