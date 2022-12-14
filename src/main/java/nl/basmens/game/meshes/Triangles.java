package nl.basmens.game.meshes;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import nl.basmens.util.FloatArray;
import nl.basmens.util.IntArray;
import nl.basmens.util.MeshInterface;
import nl.basmens.util.ShaderDataSource;
import org.joml.Vector3f;


public class Triangles implements MeshInterface {

  private double time;
  private Vector3f color = new Vector3f(0);
  private FloatArray vertices;
  private FloatArray normals;
  private FloatArray textureCoords;
  private IntArray indices;

  public Triangles() {
    vertices = new FloatArray(5 * 4);
    vertices.getBuffer()
        .put(0).put(0).put(0).put(1)
        .put(3).put(0).put(0).put(1)
        .put(0).put(0).put(3).put(1)
        .put(0).put(3).put(-1).put(1)
        .put(3).put(-1).put(3).put(1)
        .flip();

    normals = new FloatArray(5 * 4);
    normals.getBuffer()
        .put(0).put(1).put(0).put(1)
        .put(3).put(9).put(3).put(1)
        .put(0).put(3).put(9).put(1)
        .flip();

    textureCoords = new FloatArray(4 * 2);
    textureCoords.getBuffer()
        .put(0).put(0)
        .put(1).put(0)
        .put(0).put(1)
        .put(1).put(1)
        .flip();

    indices = new IntArray(27);
    indices.getBuffer()
        .put(0).put(0).put(0).put(2).put(2).put(0).put(1).put(1).put(0)
        .put(1).put(1).put(1).put(2).put(2).put(1).put(4).put(3).put(1)
        .put(0).put(0).put(2).put(1).put(1).put(2).put(3).put(2).put(2)
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

  @Override
  public Vector3f getCenter() {
    return new Vector3f(0, 0, 0);
  }

  @Override
  public float getRadius2() {
    return 19;
  }
}
