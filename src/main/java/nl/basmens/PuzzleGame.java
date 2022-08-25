package nl.basmens;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nl.basmens.util.Time;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class PuzzleGame {
    private static final Logger LOGGER = LogManager.getLogger(PuzzleGame.class);

    private long window;

    private Scene scene;

    // =================================================================================================================
    // Run
    // =================================================================================================================
    public void run() {
        try {
            LOGGER.info("Main method");
            init();
            loop();

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
            private Map<Integer, String> ERROR_CODES = APIUtil
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
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }

        // Close the window when 'esc' is pressed
        glfwSetKeyCallback(window, (long callBackWindow, int key, int scancode, int action, int mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(callBackWindow, true);
            }
        });

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

    // =================================================================================================================
    // Main
    // =================================================================================================================
    public static void main(String[] args) {
        new PuzzleGame().run();
    }
}
