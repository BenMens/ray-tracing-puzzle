package nl.basmens.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import nl.basmens.events.listeners.KeyEventDispatcher;
import nl.basmens.events.listeners.MouseEventDispatcher;
import nl.basmens.events.listeners.Observer;
import nl.basmens.events.sources.GlfwEventSources;
import nl.basmens.events.types.KeyEvent;
import nl.basmens.events.types.MouseEvent;
import nl.basmens.renderer.Camera;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/**
 * TODO: describe the goal of this class.
 */
public class Player {
  // private static final Logger LOGGER = LogManager.getLogger(Player.class);

  private static int moveForwardKey = GLFW_KEY_W;
  private static int moveBackwardKey = GLFW_KEY_S;
  private static int moveLeftKey = GLFW_KEY_A;
  private static int moveRightKey = GLFW_KEY_D;
  private static int moveUpKey = GLFW_KEY_SPACE;
  private static int moveDownKey = GLFW_KEY_LEFT_SHIFT;

  private Camera camera;

  private Observer<KeyEvent> moveForwardObserver;
  private Observer<KeyEvent> moveBackwardObserver;
  private Observer<KeyEvent> moveLeftObserver;
  private Observer<KeyEvent> moveRightObserver;
  private Observer<KeyEvent> moveUpObserver;
  private Observer<KeyEvent> moveDownObserver;

  private boolean moveForward;
  private boolean moveBackward;
  private boolean moveLeft;
  private boolean moveRight;
  private boolean moveUp;
  private boolean moveDown;
  private double turnX;
  private double turnY;

  private double cameraSpeed = 3;
  private double cameraSensitivityX = 0.003F;
  private double cameraSensitivityY = 0.003F;
  

  /**
   * Constructs a player object that is the bridge between the human player and
   * the level.
   */
  public Player() {
    camera = new Camera(new Vector3f(0, 0, 5), new Vector3f(0), 1F);
  }


  /**
   * Initialize the player object.
   */
  public void init() {
    registerMoveForward(moveForwardKey);
    registerMoveBackward(moveBackwardKey);
    registerMoveLeft(moveLeftKey);
    registerMoveRight(moveRightKey);
    registerMoveUp(moveUpKey);
    registerMoveDown(moveDownKey);
    registerTurn();
  }

  // ===============================================================================================
  // Update
  // ===============================================================================================
  public void update(double deltaTime) {
    updateMovement(deltaTime);
  }

  // ===============================================================================================
  // Update movement
  // ===============================================================================================
  public void updateMovement(double deltaTime) {
    Vector3f pos = camera.getPosition();
    Vector3f dir = camera.getDirection();

    if (moveForward) {
      pos.x -= Math.sin(dir.y) * cameraSpeed * deltaTime;
      pos.z -= Math.cos(dir.y) * cameraSpeed * deltaTime;
    }
    if (moveBackward) {
      pos.x += Math.sin(dir.y) * cameraSpeed * deltaTime;
      pos.z += Math.cos(dir.y) * cameraSpeed * deltaTime;
    }
    if (moveLeft) {
      pos.x -= Math.cos(dir.y) * cameraSpeed * deltaTime;
      pos.z += Math.sin(dir.y) * cameraSpeed * deltaTime;
    }
    if (moveRight) {
      pos.x += Math.cos(dir.y) * cameraSpeed * deltaTime;
      pos.z -= Math.sin(dir.y) * cameraSpeed * deltaTime;
    }
    if (moveUp) {
      pos.y += cameraSpeed * deltaTime;
    }
    if (moveDown) {
      pos.y -= cameraSpeed * deltaTime;
    }

    dir.y += turnX * cameraSensitivityX;
    dir.x += turnY * cameraSensitivityY;
    dir.y %= Math.PI * 2;
    if (dir.y < 0) {
      dir.y += Math.PI * 2;
    }
    dir.x = (float) Math.min(Math.max(dir.x, -Math.PI / 2), Math.PI / 2);
    turnX = 0;
    turnY = 0;

    camera.setPosition(pos);
    camera.setDirection(dir);
  }

  // ===============================================================================================
  // Register movement
  // ===============================================================================================
  public void registerMoveForward(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveForwardKey, moveForwardObserver);

    moveForwardObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveForward = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveForward = false;
      }
    });

    moveForwardKey = key;
  }

  public void registerMoveBackward(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveBackwardKey, moveBackwardObserver);

    moveBackwardObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveBackward = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveBackward = false;
      }
    });

    moveBackwardKey = key;
  }

  public void registerMoveLeft(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveLeftKey, moveLeftObserver);

    moveLeftObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveLeft = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveLeft = false;
      }
    });

    moveLeftKey = key;
  }

  public void registerMoveRight(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveRightKey, moveRightObserver);

    moveRightObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveRight = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveRight = false;
      }
    });

    moveForwardKey = key;
  }

  public void registerMoveUp(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveUpKey, moveUpObserver);

    moveUpObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveUp = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveUp = false;
      }
    });

    moveUpKey = key;
  }

  public void registerMoveDown(int key) {
    KeyEventDispatcher source = GlfwEventSources.get().getEventSource().getKeyEventDispatcher();

    source.unregister(moveDownKey, moveDownObserver);

    moveDownObserver = source.register(key, (KeyEvent event) -> {
      if (event.getAction() == GLFW_PRESS) {
        moveDown = true;
      } else if (event.getAction() == GLFW_RELEASE) {
        moveDown = false;
      }
    });

    moveDownKey = key;
  }

  public void registerTurn() {
    MouseEventDispatcher source = GlfwEventSources.get().getEventSource().getMouseEventDispatcher();

    source.register("move", (MouseEvent event) -> {
      turnY += event.getPrevY() - event.getPosY();
      turnX += event.getPrevX() - event.getPosX();
    });
  }

  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public Camera getCamera() {
    return camera;
  }
}
