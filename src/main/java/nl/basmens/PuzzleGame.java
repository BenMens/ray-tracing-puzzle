package nl.basmens;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.basmens.events.event_listeners.EventManager;
import nl.basmens.events.event_listeners.KeyEventListener;
import nl.basmens.events.event_listeners.MouseEventListener;
import nl.basmens.events.event_types.Event;
import nl.basmens.util.Time;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class PuzzleGame {
    private static PuzzleGame puzzleGame;
    private static final Logger LOGGER = LogManager.getLogger(PuzzleGame.class);

    private long window;

    public final EventManager windowEvents = new EventManager("open", "close");
    public final KeyEventListener keyEventListener = new KeyEventListener();
    public final MouseEventListener mouseEventListener = new MouseEventListener();

    private Scene scene;

    // =================================================================================================================
    // Singleton
    // =================================================================================================================
    private PuzzleGame() {
    }

    public static PuzzleGame get() {
        if (puzzleGame == null) {
            puzzleGame = new PuzzleGame();
        }

        return puzzleGame;
    }

    // =================================================================================================================
    // Run
    // =================================================================================================================
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

    // =================================================================================================================
    // Init
    // =================================================================================================================
    private void init() {
        // ============================================================
        // Set log4j error callback
        // ============================================================
        glfwSetErrorCallback(new GLFWErrorCallback() {
            private static final Map<Integer, String> ERRORCODES = 
                APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

            @Override
            public void invoke(int error, long description) {
                StringBuilder errMsg = new StringBuilder();

                String msg = getDescription(description);

                errMsg.append("[LWJGL] " + ERRORCODES.get(error) + " error\n");
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

        window = glfwCreateWindow(800, 800, "Ray tracing puzzle", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // ============================================================
        // Set the window position to the centre of the screen
        // ============================================================
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
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

    // =================================================================================================================
    // Loop
    // =================================================================================================================
    private void loop() {
        GL.createCapabilities();

        double beginTime = Time.getTime();
        double endTime;
        double deltaTime = -1;

        glClearColor(0, 0, 0, 1);

        while (!glfwWindowShouldClose(window)) {
            // Poll events
            glfwPollEvents();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (deltaTime >= 0) {
                scene.update(deltaTime);
            }

            glfwSwapBuffers(window);

            endTime = Time.getTime();
            deltaTime = endTime - beginTime;
            beginTime = endTime;
        }
    }

    // =================================================================================================================
    // Main
    // =================================================================================================================
    public static void main(String[] args) {
        PuzzleGame.get().run();
    }
}
