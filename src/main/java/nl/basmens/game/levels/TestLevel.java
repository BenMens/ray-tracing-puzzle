package nl.basmens.game.levels;

import nl.basmens.game.meshes.Triangles;
import nl.basmens.renderer.Renderable;
import nl.basmens.util.MeshInstance;
import nl.basmens.util.MeshInterface;
import nl.basmens.util.ObjFileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel implements Renderable {
  private static final Logger LOGGER = LogManager.getLogger(TestLevel.class);

  private MeshInterface triangles;
  private MeshInterface donut;
  private MeshInterface cube;
  private MeshInterface monkey;


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    getRenderer().register(this);

    try (ObjFileReader reader = new ObjFileReader()) {
      triangles = new Triangles();
      cube = reader.read("cube", "obj-files/cube.obj").getMesh();
      donut = reader.read("donut", "obj-files/donut_low.obj").getMesh();
      monkey = reader.read("monkey", "obj-files/monkey.obj").getMesh();
    } catch (Exception e) {
      String message = "Exception raised while reading meshes: " + e.getMessage();

      LOGGER.warn(message);
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
    // TODO: implement this
  }

  @Override
  public MeshInterface[] getMeshes() {
    return new MeshInterface[] {cube, monkey, triangles, donut};
  }

  @Override
  public MeshInstance[] getMeshInstances() {
    return new MeshInstance[] {
      new MeshInstance(monkey, (new Matrix4f()).translate(-1.5F, 0, 0)),
      new MeshInstance(cube, (new Matrix4f()).translate(1.5F, 0, 0)),
      new MeshInstance(triangles, (new Matrix4f()).translate(0, 1.5F, 0)),
      //new MeshInstance(donut, (new Matrix4f()).scale(0.2F).translate(0, -5F, 0))
    };
  }


  @Override
  public int getMaxMeshInstanceCount() {
    return 3;
  }

}
