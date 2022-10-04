package nl.basmens.game.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.basmens.game.gameobjects.GameObject;
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
  private MeshInterface testScene;

  private Map<String, Object> levelData;

  private List<MeshInterface> meshes = new ArrayList<>();
  private List<MeshInstance> meshInstances = new ArrayList<>();


  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    this(new HashMap<>());
  }

  public TestLevel(Map<String, Object> levelData) {
    getRenderer().register(this);

    try (ObjFileReader reader = new ObjFileReader()) {
      triangles = new Triangles();
      cube = reader.read("cube", "obj-files/cube.obj").getMesh();
      donut = reader.read("donut", "obj-files/donut_low.obj").getMesh();
      monkey = reader.read("monkey", "obj-files/monkey.obj").getMesh();
      testScene = reader.read("test scene", "obj-files/test_scene.obj").getMesh();

      meshes.add(triangles);
      meshes.add(cube);
      meshes.add(donut);
      meshes.add(monkey);
      meshes.add(testScene);

      meshInstances.add(new MeshInstance(monkey, 0, (new Matrix4f()).translate(-1.5F, 0, 0)));
      meshInstances.add(new MeshInstance(cube, 1, (new Matrix4f()).translate(1.5F, 0, 0)));
      meshInstances.add(new MeshInstance(triangles, 1, (new Matrix4f()).translate(0, 1.5F, 0)));
      meshInstances.add(new MeshInstance(donut, 1, (new Matrix4f()).scale(0.2F).translate(0, -5F, 0)));
      meshInstances.add(new MeshInstance(testScene, 3, (new Matrix4f()).scale(1F).translate(0, 0, 0)));


    } catch (Exception e) {
      String message = "Exception raised while reading meshes: " + e.getMessage();

      LOGGER.warn(message);
      // TODO: handle exception
    }

    this.levelData = levelData;
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
  public List<MeshInterface> getMeshes() {
    return meshes.subList(0, meshes.size());
  }

  @Override
  public List<MeshInstance> getMeshInstances() {
    return meshInstances.subList(0, meshInstances.size());
  }


  @Override
  public int getMaxMeshInstanceCount() {
    return meshInstances.size();
  }



  public void printData() {
    System.out.println("Level data: {");
    levelData.forEach((String key, Object value) -> System.out.println("    " + key + ": " + value.toString()));
    System.out.println("}");

    System.out.println("game objects: [");
    getGameObjects().forEach((String id, GameObject value) -> value.printData());
    System.out.println("]");
  }

}
