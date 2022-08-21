package nl.basmens;

import nl.basmens.renderer.Renderer;

public class Scene {
    
    private Renderer renderer;
    private Level level;


	// ====================================================================================================================
	// Constructor
	// ====================================================================================================================
    public Scene() {
        renderer = new Renderer();
        level = new Level(renderer);
    }


	// ====================================================================================================================
	// Update
	// ====================================================================================================================
	public void update(double deltaTime) {
        level.update(deltaTime);
        renderer.render();
	}
}
