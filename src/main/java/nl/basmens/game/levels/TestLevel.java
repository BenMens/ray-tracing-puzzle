package nl.basmens.game.levels;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.nio.ByteBuffer;
import nl.basmens.events.sources.GlfwEventSource;
import nl.basmens.events.sources.GlfwEventSources;
import nl.basmens.events.types.Event;
import nl.basmens.events.types.KeyEvent;
import nl.basmens.events.types.MouseEvent;
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

    GlfwEventSource source = GlfwEventSources.get().getEventSource();

    source.getWindowEventDispatcher().register("open",
        (Event event) -> LOGGER.info("Open window event received"));
    source.getWindowEventDispatcher().register("close",
        (Event event) -> LOGGER.info("Close window event received"));

    source.getKeyEventDispatcher().register(GLFW_KEY_W, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        LOGGER.info("The 'w' key has been pressed");
      } else if (event.getAction() == GLFW_RELEASE) {
        LOGGER.info("The 'w' key has been released");
      } else {
        LOGGER.info("Unknown action with 'w'");
      }
    });
    source.getMouseEventDispatcher().register("move", (MouseEvent event) -> {
      LOGGER.info("MouseX moved from " + event.getPrevX() + " to " + event.getPosX());
      LOGGER.info("MouseY moved from " + event.getPrevY() + " to " + event.getPosY());
    });
    source.getMouseEventDispatcher().register("click", (MouseEvent event) -> LOGGER.info(
        "Mouse click " + event.getButton() + " at " + event.getPosX() + ", " + event.getPosY()));
    source.getMouseEventDispatcher().register("scroll", (MouseEvent event) -> LOGGER
        .info("Mouse scroll " + event.getScrollX() + ", " + event.getScrollY()));
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
