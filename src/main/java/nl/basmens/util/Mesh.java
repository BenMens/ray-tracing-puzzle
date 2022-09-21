package nl.basmens.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import nl.basmens.events.listeners.EventDispatcher;
import nl.basmens.events.types.Event;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Mesh implements MeshInterface {
  private String name;
  private FloatArray vertices;
  private FloatArray textureCoords;
  private FloatArray normals;
  private IntArray indices;
  private Vector3f centre;
  private float radius2;

  private EventDispatcher<Event> dispatcher = new EventDispatcher<>("vertices changed",
      "vertices modified", "texture coords changed", "texture coords modified", "normals changed",
      "normals modified", "indices changed", "indices modified");


  public Mesh(String name, Collection<Vector3f> initVertices, Collection<Vector2f> initTextureCoords,
      Collection<Vector3f> initNormals, Collection<Integer> initIndices) {
    this.name = name;
    
    setVerticesData(initVertices);
    setTextureCoordsData(initTextureCoords);
    setNormalsData(initNormals);
    setIndicesData(initIndices);

    centre = new Vector3f(0);
    radius2 = 10000;
  }


  // ===============================================================================================
  // Vertices
  // ===============================================================================================
  public final void setVerticesData(Collection<Vector3f> initVertices) {
    final long oldSize = (vertices == null) ? -1 : vertices.getMaxBufSize();
    final long newSize = initVertices.size() * 4L;

    if (vertices == null || oldSize <= newSize) {
      vertices = new FloatArray(initVertices.size() * 4);
    }
    for (Vector3f v : initVertices) {
      vertices.getBuffer().put(v.x).put(v.y).put(v.z).put(1);
    }
    vertices.getBuffer().flip();

    if (oldSize > newSize) {
      dispatcher.notify(new Event("vertices modified"));
    } else {
      dispatcher.notify(new Event("vertices changed"));
    }
  }

  @Override
  public ShaderDataSource<FloatBuffer> getVerticesData() {
    return vertices;
  }

  @Override
  public long getVerticesCount() {
    return vertices.getMaxBufSize() / 4;
  }

  // ===============================================================================================
  // Textures
  // ===============================================================================================
  public final void setTextureCoordsData(Collection<Vector2f> initTextureCoords) {
    final long oldSize = (textureCoords == null) ? -1 : textureCoords.getMaxBufSize();
    final long newSize = initTextureCoords.size() * 2L;

    if (textureCoords == null || oldSize <= newSize) {
      textureCoords = new FloatArray(initTextureCoords.size() * 2);
    }
    for (Vector2f st : initTextureCoords) {
      textureCoords.getBuffer().put(st.x).put(st.y);
    }
    textureCoords.getBuffer().flip();

    if (oldSize > newSize) {
      dispatcher.notify(new Event("texture coords modified"));
    } else {
      dispatcher.notify(new Event("texture coords changed"));
    }
  }

  @Override
  public ShaderDataSource<FloatBuffer> getTextureCoordsData() {
    return textureCoords;
  }

  @Override
  public long getTextureCoordsCount() {
    return textureCoords.getMaxBufSize() / 2;
  }

  // ===============================================================================================
  // Normals
  // ===============================================================================================
  public final void setNormalsData(Collection<Vector3f> initNormals) {
    final long oldSize = (normals == null) ? -1 : normals.getMaxBufSize();
    final long newSize = initNormals.size() * 2L;

    if (normals == null || oldSize <= newSize) {
      normals = new FloatArray(initNormals.size() * 4);
    }
    for (Vector3f n : initNormals) {
      normals.getBuffer().put(n.x).put(n.y).put(n.z).put(1);
    }
    normals.getBuffer().flip();

    if (oldSize > newSize) {
      dispatcher.notify(new Event("normals modified"));
    } else {
      dispatcher.notify(new Event("normals changed"));
    }
  }

  @Override
  public ShaderDataSource<FloatBuffer> getNormalsData() {
    return normals;
  }

  @Override
  public long getNormalsCount() {
    return normals.getMaxBufSize() / 4;
  }


  @Override
  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  // ===============================================================================================
  // Indices
  // ===============================================================================================
  public final void setIndicesData(Collection<Integer> initIndices) {
    int oldSize = (indices == null) ? -1 : indices.getMaxBufSize();
    int newSize = initIndices.size();

    if (indices == null || oldSize <= newSize) {
      indices = new IntArray(newSize);
    }
    for (Integer face : initIndices) {
      indices.getBuffer().put(face);
    }
    indices.getBuffer().flip();

    if (oldSize > newSize) {
      dispatcher.notify(new Event("indices modified"));
    } else {
      dispatcher.notify(new Event("indices changed"));
    }
  }

  @Override
  public ShaderDataSource<IntBuffer> getIndicesData() {
    return indices;
  }

  @Override
  public long getfacesCount() {
    long result = indices.getMaxBufSize() / 9;

    return result;
  }

  

  // ===============================================================================================
  // Encompassing sphere
  // ===============================================================================================
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
