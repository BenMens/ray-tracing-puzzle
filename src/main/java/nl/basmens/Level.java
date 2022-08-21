package nl.basmens;

import nl.basmens.renderer.Renderable;
import nl.basmens.renderer.Renderer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import org.joml.Vector3f;

public class Level {

    private Triangle triangle;

    // TODO replace ugly code for testing
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
    }

    // =================================================================================================================
    // Constructor
    // =================================================================================================================
    public Level(Renderer renderer) {
        triangle = new Triangle(renderer);

        PuzzleGame.get().windowEvents.register("open",
                (String event, int... args) -> System.out.println("Open window event received"));
        PuzzleGame.get().windowEvents.register("close",
                (String event, int... args) -> System.out.println("Close window event received"));

        PuzzleGame.get().keyEventListener.register(GLFW_KEY_W, (String event, int... args) -> {
            if (args[2] == GLFW_PRESS) {
                System.out.println("The 'w' key has been pressed");
            } else if (args[2] == GLFW_RELEASE) {
                System.out.println("The 'w' key has been released");
            } else {
                System.out.println("Unknown action with 'w'");
            }
        });

    }

    // =================================================================================================================
    // Update
    // =================================================================================================================
    public void update(double deltaTime) {
        triangle.update(deltaTime);
    }
}
