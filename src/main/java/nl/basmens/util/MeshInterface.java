package nl.basmens.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Vector3f;

public interface MeshInterface {

  ShaderDataSource<FloatBuffer> getVerticesData();

  long getVerticesCount();

  ShaderDataSource<FloatBuffer> getTextureCoordsData();

  long getTextureCoordsCount();

  ShaderDataSource<FloatBuffer> getNormalsData();

  long getNormalsCount();

  ShaderDataSource<IntBuffer> getIndicesData();
  
  long getfacesCount();

  Vector3f getCenter();

  float getRadius2();

  String getName();
}
