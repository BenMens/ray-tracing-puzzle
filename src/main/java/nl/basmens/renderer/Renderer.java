package nl.basmens.renderer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;

import org.joml.Vector3f;

public class Renderer {

    private ArrayList<Renderable> renderables;


	// ====================================================================================================================
	// Constructor
	// ====================================================================================================================
    public Renderer() {
        renderables = new ArrayList<>();
    }



	// ====================================================================================================================
	// Register
	// ====================================================================================================================
    public void register(Renderable renderable) {
        if (!renderables.contains(renderable)) {
            renderables.add(renderable);
        } else {
            // TODO print warning: same object registered twice
        }
    }


	// ====================================================================================================================
	// Unregister
	// ====================================================================================================================
    public void unregister(Renderable renderable) {
        if (!renderables.contains(renderable)) {
            // TODO print warning: trying to remove an uregistered object
        } else {
            renderables.remove(renderable);
        }
    }


	// ====================================================================================================================
	// Render
	// ====================================================================================================================
    public void render() {
        for(Renderable r : renderables) {
            Vector3f color = r.getColor();
            glColor3f(color.x, color.y, color.z);
            glBegin(GL_TRIANGLES);
            glVertex2f(-0.5F, -0.5F);
            glVertex2f(0.5F, -0.5F);
            glVertex2f(0F, 0.5F);
            glEnd();
        }
    }
}
