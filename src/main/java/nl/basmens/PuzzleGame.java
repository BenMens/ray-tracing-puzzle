package nl.basmens;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import nl.basmens.events.event_listeners.EventManager;
import nl.basmens.events.event_listeners.KeyEventListener;
import nl.basmens.events.event_types.Event;
import nl.basmens.util.Time;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class PuzzleGame {
	private static PuzzleGame puzzleGame;

	private long window;

	public final EventManager windowEvents = new EventManager("open", "close");
	public final KeyEventListener keyEventListener = new KeyEventListener();

	private Scene scene;

	// ====================================================================================================================
	// Singleton
	// ====================================================================================================================
	private PuzzleGame() {
	}

	public static PuzzleGame get() {
		if (puzzleGame == null) {
			puzzleGame = new PuzzleGame();
		}

		return puzzleGame;
	}

	// ====================================================================================================================
	// Run
	// ====================================================================================================================
	public void run() {
		init();
		windowEvents.notify(new Event("open"));

		loop();
		windowEvents.notify(new Event("close"));

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	// ====================================================================================================================
	// Init
	// ====================================================================================================================
	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();

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

		// Set event callbacks
		glfwSetKeyCallback(window, keyEventListener::keyCallBack);

		glfwMakeContextCurrent(window);

		// Enable v-sync
		glfwSwapInterval(1);

		glfwShowWindow(window);

		// ============================================================
		// Scene creation
		// ============================================================
		scene = new Scene();
	}

	// ====================================================================================================================
	// Loop
	// ====================================================================================================================
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

	// ====================================================================================================================
	// Main
	// ====================================================================================================================
	public static void main(String[] args) {
		PuzzleGame.get().run();
	}
}
