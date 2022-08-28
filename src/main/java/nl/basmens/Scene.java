package nl.basmens;

import nl.basmens.renderer.Renderer;

/**
 * TODO: describe the goal of this class.
 */
public class Scene {

  private Renderer renderer;
  private Level level;


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public Scene() {
    renderer = new Renderer();
    level = new Level(renderer);
  }


  // ===============================================================================================
  // Init
  // ===============================================================================================
  public void init() throws Exception {
    renderer.init();
  }


  // ===============================================================================================
  // Update
  // ===============================================================================================
  public void update(double deltaTime) {
    level.update(deltaTime);
    renderer.render();
  }
}
