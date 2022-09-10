package nl.basmens.game.levels;

import nl.basmens.game.meshes.Triangles;
import nl.basmens.renderer.Renderable;
import nl.basmens.util.Mesh;
import nl.basmens.util.MeshInstance;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel implements Renderable {
  private Triangles triangles;


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    triangles = new Triangles();
    getRenderer().register(this);
  }

  
  /** 
   * Update the level based on elapsed time.
   *
   * @param deltaTime Time sinse last call in seconds.
   * 
   */
  public void update(double deltaTime) {
    triangles.update(deltaTime);
  }

  @Override
  public Mesh[] getMeshes() {
    return new Mesh[]{triangles};
  }

  @Override
  public MeshInstance[] getMeshInstances() {
    return new MeshInstance[]{new MeshInstance(triangles)};
  }


  @Override
  public long getMaxMeshInstanceCount() {
    return 1;
  }

}
