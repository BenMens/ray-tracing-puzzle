package nl.basmens.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Vector3f;

public interface Mesh {

  ShaderDataSource<FloatBuffer> getVerticesData();

  long getVerticesCount();

  ShaderDataSource<FloatBuffer> getNormalsData();

  long getNormalsCount();

  ShaderDataSource<FloatBuffer> getTextureCoordsData();

  long getTextureCoordsCount();

  ShaderDataSource<IntBuffer> getIndicesData();
  
  long getIndicesCount();

  Vector3f getCenter();

  float getRadius();
}
