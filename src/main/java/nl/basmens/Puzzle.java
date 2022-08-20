package nl.basmens;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.PrintStream;
import java.nio.*;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Puzzle {

	private long window;
	private static final Logger logger = LogManager.getLogger(Puzzle.class);


	public void run() {
		logger.info("Main method");
		init();
		loop();

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {

		glfwSetErrorCallback(new GLFWErrorCallback() {
            private Map<Integer, String> ERROR_CODES = APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

            @Override
            public void invoke(int error, long description) {
                String msg = getDescription(description);

                logger.error("[LWJGL] %s error\n", ERROR_CODES.get(error));
                logger.error("\tDescription : " + msg);
                logger.error("\tStacktrace  :");
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for ( int i = 4; i < stack.length; i++ ) {
                    logger.error("\t\t");
                    logger.error(stack[i].toString());
                }
            }
        });


		// if (!glfwInit())
		// 	throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		window = glfwCreateWindow(800, 800, "Ray tracing puzzle", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetKeyCallback(window, (callBackWindow, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(callBackWindow, true);
		});

		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(window, pWidth, pHeight);

			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			glfwSetWindowPos(
					window,
					(vidmode.width() -pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);

		glfwSwapInterval(1);

		glfwShowWindow(window);
	}

	private void loop() {
		GL.createCapabilities();

		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glfwSwapBuffers(window);

			glfwPollEvents();
		}
	}

	public static void main(String[] args) {
		new Puzzle().run();
	}

}