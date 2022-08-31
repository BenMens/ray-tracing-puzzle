package nl.basmens.game.levels;

import java.nio.ByteBuffer;
import nl.basmens.renderer.Renderable;
import nl.basmens.renderer.Renderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel {
  private static final Logger LOGGER = LogManager.getLogger(TestLevel.class);

  private Triangle triangle;

  static class Triangle implements Renderable {
    private double time;
    private Vector3f color = new Vector3f(0);

    public Triangle(Renderer renderer) {
      renderer.register(this);
    }

    public void update(double deltaTime) {
      time += deltaTime;

      float r = (float) (1.5F - Math.abs((time + 0) % 3 - 1.5));
      float g = (float) (1.5F - Math.abs((time + 1) % 3 - 1.5));
      float b = (float) (1.5F - Math.abs((time + 2) % 3 - 1.5));

      color = new Vector3f(r, g, b);
    }

    public Vector3f getColor() {
      return color;
    }

    public void getData(ByteBuffer data) {
      data.putFloat(color.x);
      data.putFloat(color.y);
      data.putFloat(color.z);
      data.putFloat(1F);
    }
  }

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    triangle = new Triangle(getRenderer());
  }

  
  /** 
   * Update the level based on elapsed time.
   *
   * @param deltaTime Time sinse last call in seconds.
   * 
   */
  public void update(double deltaTime) {
    triangle.update(deltaTime);
  }
}
