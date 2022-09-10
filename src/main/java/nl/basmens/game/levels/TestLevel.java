package nl.basmens.game.levels;

import nl.basmens.game.meshes.Triangles;
import nl.basmens.renderer.Renderable;
import nl.basmens.util.Mesh;
import nl.basmens.util.MeshInstance;
import nl.basmens.util.MeshInterface;
import nl.basmens.util.ObjFileReader;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel implements Renderable {
  private Triangles triangles;
  private Mesh mesh;


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    triangles = new Triangles();
    getRenderer().register(this);

    try (ObjFileReader reader = new ObjFileReader()) {
      mesh = reader.read("obj-files/cube.obj").getMesh();
      //mesh = reader.read("obj-files/donut_low.obj").getMesh();
      //mesh = reader.read("obj-files/monkey.obj").getMesh();
    } catch (Exception e) {
      // TODO: handle exception
    }
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
  public MeshInterface[] getMeshes() {
    // return new MeshInterface[] {triangles};
    return new MeshInterface[] {mesh};
  }

  @Override
  public MeshInstance[] getMeshInstances() {
    // return new MeshInstance[] {new MeshInstance(triangles)};
    return new MeshInstance[] {new MeshInstance(mesh)};
  }


  @Override
  public long getMaxMeshInstanceCount() {
    return 1;
  }

}
