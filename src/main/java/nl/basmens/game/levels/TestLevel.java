package nl.basmens.game.levels;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import nl.basmens.renderer.Renderable;
import nl.basmens.renderer.Renderer;
import nl.basmens.util.Mesh;
import nl.basmens.util.ShaderDataSource;
import nl.basmens.util.FloatArray;
import nl.basmens.util.IntArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

/**
 * Represends a level in the game.
 */
public class TestLevel extends AbstractLevel {
  private static final Logger LOGGER = LogManager.getLogger(TestLevel.class);

  private Triangle triangle;

  static class Triangle implements Renderable, Mesh {
    private double time;
    private Vector3f color = new Vector3f(0);
    private FloatArray vertices;
    private FloatArray normals;
    private FloatArray textureCoords;
    private IntArray indices;
  
    public Triangle(Renderer renderer) {
      renderer.register(this);

      vertices = new FloatArray(5 * 4);
      vertices.getBuffer()
        .put(0).put(0).put(0).put(0)
        .put(3).put(0).put(0).put(0)
        .put(0).put(0).put(3).put(0)
        .put(0).put(3).put(-1).put(0)
        .put(3).put(-1).put(3).put(0)
        .flip();

      normals = new FloatArray(5 * 4);
      normals.getBuffer()
        .put(0).put(1).put(0).put(0)
        .put(3).put(9).put(3).put(0)
        .put(0).put(3).put(9).put(0)
        .flip();
  
      textureCoords = new FloatArray(4 * 2);
      textureCoords.getBuffer()
        .put(0).put(0)
        .put(1).put(0)
        .put(0).put(1)
        .put(1).put(1)
        .flip();


//   0, 2, 1, 0, 2, 1, 0, 0, 0,  // 0
//   1, 2, 4, 1, 2, 3, 1, 1, 1,  // 1
//   0, 1, 3, 0, 1, 2, 2, 2, 2   // 2


      indices = new IntArray(27);
      indices.getBuffer()
        .put(0).put(2).put(1).put(0).put(2).put(1).put(0).put(0).put(0)
        .put(1).put(2).put(4).put(1).put(2).put(3).put(1).put(1).put(1)
        .put(0).put(1).put(3).put(0).put(1).put(2).put(2).put(2).put(2)
        .flip();
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


    @Override
    public Mesh getMesh() {
      return this;
    }

    @Override
    public ShaderDataSource<FloatBuffer> getVerticesData() {
      return vertices;
    }

    @Override
    public long getVerticesCount() {
      return vertices.getMaxBufSize();
    }

    @Override
    public ShaderDataSource<FloatBuffer> getNormalsData() {
      return normals;
    }

    @Override
    public long getNormalsCount() {
      return normals.getMaxBufSize();
    }

    @Override
    public ShaderDataSource<FloatBuffer> getTextureCoordsData() {
      return textureCoords;
    }

    @Override
    public long getTextureCoordsCount() {
      return textureCoords.getMaxBufSize();
    }

    @Override
    public ShaderDataSource<IntBuffer> getIndicesData() {
      return indices;
    }

    @Override
    public long getIndicesCount() {
      return indices.getMaxBufSize();
    }
  }

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public TestLevel() {
    triangle = new Triangle(getRenderer());
  }

  
  /** 
   * Update the level based on elapsed time.
   *
   * @param deltaTime Time sinse last call in seconds.
   * 
   */
  public void update(double deltaTime) {
    triangle.update(deltaTime);
  }
}
