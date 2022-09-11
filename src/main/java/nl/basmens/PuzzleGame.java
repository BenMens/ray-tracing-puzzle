package nl.basmens;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;
import java.util.Map;
import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.listeners.KeyEventDispatcher;
import nl.basmens.events.listeners.MouseEventDispatcher;
import nl.basmens.events.sources.GlfwEventSource;
import nl.basmens.events.sources.GlfwEventSources;
import nl.basmens.events.types.Event;
import nl.basmens.game.Player;
import nl.basmens.game.levels.AbstractLevel;
import nl.basmens.game.levels.TestLevel;
import nl.basmens.util.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;

/**
 * A puzzle game with puzzels that are based on shaders that use ray-tracing.
 */
public class PuzzleGame implements GlfwEventSource {
  private static final Logger LOGGER = LogManager.getLogger(PuzzleGame.class);

  private long window;

  public final EventDispatcher<Event> windowEventsDispatcher =
      new EventDispatcher<>("open", "close");
  public final KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher();
  public final MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher();

  private AbstractLevel level;
  private Player player;

  // =============================================================================================
  // Constructor
  // =============================================================================================
  public PuzzleGame() {
    GlfwEventSources.get().setEventSource(this);

    level = new TestLevel();
    player = new Player();
  }

  // =============================================================================================
  // Run
  // =============================================================================================
  public void run() {
    try {
      LOGGER.info("Main method");
      init();
      windowEventsDispatcher.notify(new Event("open"));

      loop();
      windowEventsDispatcher.notify(new Event("close"));

      glfwFreeCallbacks(window);
      glfwDestroyWindow(window);

      glfwTerminate();
      glfwSetErrorCallback(null).free();
    } catch (Exception e) {
      LOGGER.error("Program terminated because of an exception", e);
    }
  }

  // =============================================================================================
  // Init
  // =============================================================================================
  private void init() throws Exception { // TODO throw specific exeption
    // ============================================================
    // Set log4j error callback
    // ============================================================
    glfwSetErrorCallback(new GLFWErrorCallback() {
      private static Map<Integer, String> errorCodes = APIUtil
          .apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

      @Override
      public void invoke(int error, long description) {
        StringBuilder errMsg = new StringBuilder();

        String msg = getDescription(description);

        errMsg.append("[LWJGL] " + errorCodes.get(error) + " error\n");
        errMsg.append("Description : " + msg + "\n");
        errMsg.append("Stacktrace  : ");
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // Start at stack[4] to skip internal stack frames.
        for (int i = 4; i < stack.length; i++) {
          if (i > 4) {
            errMsg.append("\tat ");
          }
          errMsg.append(stack[i].toString() + "\n");
        }

        LOGGER.error(errMsg);
      }
    });

    // ============================================================
    // Window creation
    // ============================================================
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

    window = glfwCreateWindow(1600, 900, "Ray tracing puzzle", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    // ============================================================
    // Set the window position to the centre of the screen
    // ============================================================
    try (MemoryStack stack = stackPush()) {
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);

      glfwGetWindowSize(window, width, height);

      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      glfwSetWindowPos(window, (vidmode.width() - width.get(0)) / 2,
          (vidmode.height() - height.get(0)) / 2);
    }

    // ============================================================
    // Set event callbacks
    // ============================================================
    glfwSetKeyCallback(window, keyEventDispatcher::keyCallBack);
    glfwSetCursorPosCallback(window, mouseEventDispatcher::mousePosCallBack);
    glfwSetMouseButtonCallback(window, mouseEventDispatcher::mouseButtonCallback);
    glfwSetScrollCallback(window, mouseEventDispatcher::mouseScrollCallback);
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    glfwMakeContextCurrent(window);

    // Enable v-sync
    glfwSwapInterval(1);

    glfwShowWindow(window);


    // ============================================================
    // Create capabilities
    // ============================================================
    GL.createCapabilities();

    LOGGER.trace("GL version is {}", glGetString(GL_VERSION));

    // ============================================================
    // Initiation of game objects
    // ============================================================
    level.init();
    player.init();
  }

  // =============================================================================================
  // Loop
  // ===============================================================================================
  private void loop() {
    double beginTime = Time.getTimeStarted();
    double endTime;
    double deltaTime = -1;

    glClearColor(0, 0, 0, 1);

    while (!glfwWindowShouldClose(window)) {
      // Poll events
      glfwPollEvents();

      if (deltaTime >= 0) {
        level.update(deltaTime);
        player.update(deltaTime);
      }

      IntBuffer w = BufferUtils.createIntBuffer(4);
      IntBuffer h = BufferUtils.createIntBuffer(4);
      glfwGetWindowSize(window, w, h);
      int width = w.get(0);
      int height = h.get(0);

      glViewport(0, 0, width, height);

      level.render(player.getCamera());
      glfwSwapBuffers(window);

      endTime = Time.getTimeSinceProgramStart();
      deltaTime = endTime - beginTime;
      beginTime = endTime;
    }
  }

  // ===============================================================================================
  // Getters and setters
  // ===============================================================================================
  public EventDispatcher<Event> getWindowEventDispatcher() {
    return windowEventsDispatcher;
  }

  public KeyEventDispatcher getKeyEventDispatcher() {
    return keyEventDispatcher;
  }

  public MouseEventDispatcher getMouseEventDispatcher() {
    return mouseEventDispatcher;
  }

  // ===============================================================================================
  // Main
  // ===============================================================================================
  public static void main(String[] args) {
    new PuzzleGame().run();
  }
}
