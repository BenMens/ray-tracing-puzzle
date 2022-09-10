package nl.basmens.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Mesh implements MeshInterface {
  private FloatArray vertices;
  private FloatArray textureCoords;
  private FloatArray normals;
  private IntArray indices;
  private Vector3f centre;
  private float radius2;


  // TODO make setters
  public Mesh(List<Vector3f> initVertices, List<Vector2f> initTexCoords,
      List<Vector3f> initNormals, List<Vector3i> initIndices) {

    vertices = new FloatArray(initVertices.size() * 4);
    for (Vector3f v : initVertices) {
      vertices.getBuffer().put(v.x).put(v.y).put(v.z).put(1);
    }
    vertices.getBuffer().flip();

    textureCoords = new FloatArray(initTexCoords.size() * 2);
    for (Vector2f st : initTexCoords) {
      textureCoords.getBuffer().put(st.x).put(st.y);
    }
    textureCoords.getBuffer().flip();

    normals = new FloatArray(initNormals.size() * 4);
    for (Vector3f n : initNormals) {
      normals.getBuffer().put(n.x).put(n.y).put(n.z).put(1);
    }
    normals.getBuffer().flip();

    indices = new IntArray(initIndices.size() * 3);
    for (Vector3i face : initIndices) {
      indices.getBuffer().put(face.x).put(face.y).put(face.z);
    }
    indices.getBuffer().flip();

    centre = new Vector3f(0);
    radius2 = 100;
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
    // TODO calculate centre based on vertices
    return centre;
  }

  @Override
  public float getRadius2() {
    // TODO calculate radius based on vertices
    return radius2;
  }
}
