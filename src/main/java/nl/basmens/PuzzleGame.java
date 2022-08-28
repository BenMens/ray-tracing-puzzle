package nl.basmens;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;
import java.util.Map;
import nl.basmens.events.listeners.EventManager;
import nl.basmens.events.listeners.KeyEventListener;
import nl.basmens.events.listeners.MouseEventListener;
import nl.basmens.events.types.Event;
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

public class PuzzleGame {
  private static final Logger LOGGER = LogManager.getLogger(PuzzleGame.class);

  private long window;

  private static PuzzleGame puzzleGame;

  public final EventManager<Event> windowEvents = new EventManager<>("open", "close");
  public final KeyEventListener keyEventListener = new KeyEventListener();
  public final MouseEventListener mouseEventListener = new MouseEventListener();

  private Scene scene;

  // =============================================================================================
  // Singleton
  // =============================================================================================
  private PuzzleGame() {}

  public static PuzzleGame get() {
    if (puzzleGame == null) {
      puzzleGame = new PuzzleGame();
    }

    return puzzleGame;
  }

  // =============================================================================================
  // Run
  // =============================================================================================
  public void run() {
    try {
      LOGGER.info("Main method");
      init();
      windowEvents.notify(new Event("open"));

      loop();
      windowEvents.notify(new Event("close"));

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
  private void init() {
    // ============================================================
    // Set log4j error callback
    // ============================================================
    glfwSetErrorCallback(new GLFWErrorCallback() {
      private static Map<Integer, String> ERROR_CODES = APIUtil
          .apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

      @Override
      public void invoke(int error, long description) {
        StringBuilder errMsg = new StringBuilder();

        String msg = getDescription(description);

        errMsg.append("[LWJGL] " + ERROR_CODES.get(error) + " error\n");
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

    window = glfwCreateWindow(800, 800, "Ray tracing puzzle", NULL, NULL);
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
    glfwSetKeyCallback(window, keyEventListener::keyCallBack);
    glfwSetCursorPosCallback(window, mouseEventListener::mousePosCallBack);
    glfwSetMouseButtonCallback(window, mouseEventListener::mouseButtonCallback);
    glfwSetScrollCallback(window, mouseEventListener::mouseScrollCallback);
    glfwMakeContextCurrent(window);

    // Enable v-sync
    glfwSwapInterval(1);

    glfwShowWindow(window);

    // ============================================================
    // Scene creation
    // ============================================================
    scene = new Scene();
  }

  // =============================================================================================
  // Loop
  // ===============================================================================================
  private void loop() throws Exception {
    GL.createCapabilities();

    LOGGER.trace("GL version is {}", glGetString(GL_VERSION));

    scene.init();

    double beginTime = Time.getTime();
    double endTime;
    double deltaTime = -1;

    glClearColor(0, 0, 0, 1);

    while (!glfwWindowShouldClose(window)) {
      // Poll events
      glfwPollEvents();

      IntBuffer w = BufferUtils.createIntBuffer(4);
      IntBuffer h = BufferUtils.createIntBuffer(4);
      glfwGetWindowSize(window, w, h);
      int width = w.get(0);
      int height = h.get(0);

      glViewport(0, 0, width, height);

      if (deltaTime >= 0) {
        scene.update(deltaTime);
      }

      glfwSwapBuffers(window);

      endTime = Time.getTime();
      deltaTime = endTime - beginTime;
      beginTime = endTime;
    }
  }

  // ===============================================================================================
  // Main
  // ===============================================================================================
  public static void main(String[] args) {
    PuzzleGame.get().run();
  }
}
