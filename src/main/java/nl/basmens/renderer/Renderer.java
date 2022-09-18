package nl.basmens.renderer;

import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import nl.basmens.util.IoUtil;
import nl.basmens.util.MeshInstance;
import nl.basmens.util.MeshInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

/**
 * Rendes all registerd renderables using one of the available shaders.
 */
public class Renderer {
  private static final Logger LOGGER = LogManager.getLogger(Renderer.class);

  private static final int VERTEX_POSITION_ATTRIBUTE_LOCATION = 0;
  private static final int VERTEX_COLOR_ATTRIBUTE_LOCATION = 1;

  private static final int VERTICES_POS_BUFFER_BINDING = 0;
  private static final int TEXTURE_COORDS_BUFFER_BINDING = 1;
  private static final int NORMALS_BUFFER_BINDING = 2;
  private static final int FACES_BUFFER_BINDING = 3;
  private static final int MESHES_BUFFER_BINDING = 4;
  
  private static final int CAMERA_FOV_UNIFORM_LOCATION = 2;
  private static final int CAMERA_POINT_MATRIX_UNIFORM_LOCATION = 3;
  private static final int CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION = 4;
  private static final int RESOLUTION_UNIFORM_LOCATION = 5;

  private static final int MESH_INSTANCE_SIZE = 176;

  private ArrayList<Renderable> renderables;

  private HashMap<String, Integer> shaderPrograms = new HashMap<>();

  private int vao;
  private int verticesPosBuffer;
  private int normalsBuffer;
  private int textureCoordsBuffer;
  private int facesBuffer;
  private int meshesBuffer;

  private IdentityHashMap<String, AugumentedMesh> meshRegistry = new IdentityHashMap<>();

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public Renderer() {
    renderables = new ArrayList<>();
  }

  // ===============================================================================================
  // Register
  // ===============================================================================================
  public void register(Renderable renderable) {
    if (!renderables.contains(renderable)) {
      renderables.add(renderable);
    } else {
      // TODO print warning: same object registered twice
    }
  }

  // ===============================================================================================
  // Unregister
  // ===============================================================================================
  public void unregister(Renderable renderable) {
    if (!renderables.contains(renderable)) {
      // TODO print warning: trying to remove an uregistered object
    } else {
      renderables.remove(renderable);
    }
  }

  // ===============================================================================================
  // Initialization
  // ===============================================================================================
  public void init() throws Exception {  // TODO make specific exeption
    LOGGER.trace("reading vertex shader");

    ByteBuffer vs = IoUtil.ioResourceToByteBuffer("shaders/vertex/vertex.vert", 4096);
    int v = glCreateShader(GL_VERTEX_SHADER);
    compileShader(v, vs);

    String fragmentShadersPath = "shaders/fragment";
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (
          InputStream in = classLoader.getResourceAsStream(fragmentShadersPath);
          BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {

      String resource;

      while ((resource = br.readLine()) != null) {
        LOGGER.trace("reading fragment shader {}", resource);

        ByteBuffer fs = IoUtil.ioResourceToByteBuffer(fragmentShadersPath + "/" + resource, 4096);
        int f = glCreateShader(GL_FRAGMENT_SHADER);
        compileShader(f, fs);

        int program = compileShaderProgram(v, f);

        @SuppressWarnings("squid:S4248")
        String baseName = resource.split("\\.(?=[^\\.]+$)")[0];

        shaderPrograms.put(baseName, program);
      }
    }

    LOGGER.trace("loaded shader programs \n{}", shaderPrograms.toString());

    createVao();
    createShaderBuffers();
    collectMeshes();
    collectMeshInstances();

    updateVerticesData();
    updateTextureCoordsData();
    updateNormalsData();
    updateFacesData();
    updateMeshesData();
  }

  // ===============================================================================================
  // Shader compilation
  // ===============================================================================================
  private static void printShaderInfoLog(int obj) {
    int infologLength = glGetShaderi(obj, GL_INFO_LOG_LENGTH);
    if (infologLength > 0) {
      LOGGER.trace("Shader info: {}", glGetShaderInfoLog(obj));
    }
  }

  private static void printProgramInfoLog(int obj) {
    int infologLength = glGetProgrami(obj, GL_INFO_LOG_LENGTH);
    if (infologLength > 0) {
      LOGGER.trace("Program info: {}", glGetProgramInfoLog(obj));
    }
  }

  private static void compileShader(int shader, ByteBuffer code) {
    try (MemoryStack stack = stackPush()) {
      glShaderSource(shader, stack.pointers(code), stack.ints(code.remaining()));
      glCompileShader(shader);
      printShaderInfoLog(shader);

      if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
        throw new IllegalStateException("Failed to compile shader.");
      }
    }
  }

  private static int compileShaderProgram(int v, int f) {
    int p = glCreateProgram();

    glAttachShader(p, v);
    glAttachShader(p, f);
    glLinkProgram(p);
    printProgramInfoLog(p);

    if (glGetProgrami(p, GL_LINK_STATUS) != GL_TRUE) {
      throw new IllegalStateException("Failed to link program.");
    }

    return p;
  }

  // ===============================================================================================
  // Create Vertex Arrays Object and buffers
  // ===============================================================================================

  void createVao() {
    FloatBuffer pb = BufferUtils.createFloatBuffer(6 * 3);
    pb.put(-1F).put(-1F).put(0F);
    pb.put(1F).put(-1F).put(0F);
    pb.put(-1).put(1F).put(0F);
    pb.put(1F).put(-1F).put(0F);
    pb.put(-1F).put(1F).put(0F);
    pb.put(1).put(1F).put(0F);
    pb.flip();

    FloatBuffer cb = BufferUtils.createFloatBuffer(6 * 4);
    cb.put(1F).put(0F).put(0F).put(1.0F);
    cb.put(0F).put(1F).put(0F).put(1.0F);
    cb.put(0F).put(0F).put(1F).put(1.0F);
    cb.put(0F).put(1F).put(0F).put(1.0F);
    cb.put(0F).put(0F).put(1F).put(1.0F);
    cb.put(1F).put(1F).put(1F).put(1.0F);
    cb.flip();

    vao = glGenVertexArrays();
    glBindVertexArray(vao);

    // setup vertex positions buffer
    int posVbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, posVbo);
    glBufferData(GL_ARRAY_BUFFER, pb, GL_STATIC_DRAW);
    glEnableVertexAttribArray(VERTEX_POSITION_ATTRIBUTE_LOCATION);
    glVertexAttribPointer(VERTEX_POSITION_ATTRIBUTE_LOCATION, 3, GL_FLOAT, false, 0, 0L);

    // setup vertex color buffer
    int colVbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, colVbo);
    glBufferData(GL_ARRAY_BUFFER, cb, GL_STATIC_DRAW);
    glEnableVertexAttribArray(VERTEX_COLOR_ATTRIBUTE_LOCATION);
    glVertexAttribPointer(VERTEX_COLOR_ATTRIBUTE_LOCATION, 4, GL_FLOAT, false, 0, 0L);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
  }

  void collectMeshes() {
    int verticesBufferSize = 0;
    int normalsBufferSize = 0;
    int textureCoordsBufferSize = 0;
    int facesBufferSize = 0;
    for (Renderable r : renderables) {
      for (MeshInterface m : r.getMeshes()) {
        if (meshRegistry.get(m.getName()) == null) {
          meshRegistry.put(m.getName(), 
            new AugumentedMesh(
                  m,
                  verticesBufferSize,
                  textureCoordsBufferSize,
                  normalsBufferSize,
                  facesBufferSize));

          verticesBufferSize += m.getVerticesCount();
          normalsBufferSize += m.getNormalsCount();
          textureCoordsBufferSize += m.getTextureCoordsCount();
          facesBufferSize += m.getfacesCount();          
        }
      }
    }

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, verticesPosBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, verticesBufferSize * 16L, GL_DYNAMIC_DRAW);

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, textureCoordsBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, textureCoordsBufferSize * 8L, GL_DYNAMIC_DRAW);

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalsBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, normalsBufferSize * 16L, GL_DYNAMIC_DRAW);

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, facesBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, facesBufferSize * 36L, GL_DYNAMIC_DRAW);
  }

  void collectMeshInstances() {
    long meshesBufferSize = 0;
    for (Renderable r : renderables) {
      meshesBufferSize += r.getMaxMeshInstanceCount();
    }

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, meshesBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, meshesBufferSize * MESH_INSTANCE_SIZE, GL_DYNAMIC_DRAW);
  }

  void createShaderBuffers() {

    verticesPosBuffer = glGenBuffers();
    textureCoordsBuffer = glGenBuffers();
    normalsBuffer = glGenBuffers();
    facesBuffer = glGenBuffers();
    meshesBuffer = glGenBuffers();
  }
  
  void updateVerticesData() {
    try (MemoryStack stack = stackPush()) {
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, verticesPosBuffer);

      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          AugumentedMesh am = meshRegistry.get(m.getName());

          glBufferSubData(GL_SHADER_STORAGE_BUFFER, am.verticesOffset * 16L, 
              m.getVerticesData().getData(stack));
        }
      }
    }
  }

  void updateNormalsData() {
    try (MemoryStack stack = stackPush()) {
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalsBuffer);
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          AugumentedMesh am = meshRegistry.get(m.getName());

          glBufferSubData(GL_SHADER_STORAGE_BUFFER, am.normalsOffset * 16L, 
              m.getNormalsData().getData(stack));
        }
      }
    }
  }


  void updateTextureCoordsData() {
    try (MemoryStack stack = stackPush()) {
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, textureCoordsBuffer);

      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          AugumentedMesh am = meshRegistry.get(m.getName());

          glBufferSubData(GL_SHADER_STORAGE_BUFFER, am.textureCoordOffset * 8L, 
              m.getTextureCoordsData().getData(stack));
        }
      }
    }
  }

  void updateFacesData() {
    try (MemoryStack stack = stackPush()) {
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, facesBuffer);
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          AugumentedMesh am = meshRegistry.get(m.getName());

          glBufferSubData(GL_SHADER_STORAGE_BUFFER, am.facesOffset * 36L, 
              m.getIndicesData().getData(stack));
        }
      }
    }
  }


  void updateMeshesData() {
    try (MemoryStack stack = stackPush()) {
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, meshesBuffer);
      long meshesOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInstance mi : r.getMeshInstances()) {
          ByteBuffer meshBufferData = stack.malloc(MESH_INSTANCE_SIZE);
          MeshInterface m = mi.mesh();
          AugumentedMesh am = meshRegistry.get(m.getName());

          mi.modelMatrix().get(meshBufferData);

          mi.modelMatrix().normal().get(64, meshBufferData);

          meshBufferData
              .position(128)
              .putFloat(m.getCenter().x).putFloat(m.getCenter().y).putFloat(m.getCenter().z).putFloat(1)
              .putFloat(m.getRadius2())
              .putInt(am.facesOffset)  // facesOffset
              .putInt((int) m.getfacesCount())  // facesCount
              .putInt(0)  // textureIndex
              .putInt(am.verticesOffset)  // verticesOffset
              .putInt(am.normalsOffset)  // normalsOffset
              .putInt(am.textureCoordOffset)  // verticesSTOffset
              .flip();
  
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, meshesOffset * MESH_INSTANCE_SIZE, meshBufferData);
          meshesOffset++;
        }
      }
    }
  }

  // ===============================================================================================
  // Render
  // ===============================================================================================
  public void render(Camera camera, int width, int height) {

    try (MemoryStack stack = stackPush()) {
      // updateVerticesData();
      // updateTextureCoordsData();
      // updateNormalsData();
      // updateFacesData();
      // updateMeshesData();


      glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, VERTICES_POS_BUFFER_BINDING, verticesPosBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, NORMALS_BUFFER_BINDING, normalsBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, TEXTURE_COORDS_BUFFER_BINDING, textureCoordsBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, FACES_BUFFER_BINDING, facesBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, MESHES_BUFFER_BINDING, meshesBuffer);

      glUseProgram(this.shaderPrograms.get("ray_tracer"));

      glUniform1f(CAMERA_FOV_UNIFORM_LOCATION, camera.getFov());

      FloatBuffer matBuffer = stack.mallocFloat(16);
      
      camera.getPointCameraMatrix().get(matBuffer);
      glUniformMatrix4fv(CAMERA_POINT_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      camera.getVectorCameraMatrix().get(matBuffer);
      glUniformMatrix3fv(CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      glBindVertexArray(vao);

      IntBuffer resolutionBuffer = stack.mallocInt(2);
      resolutionBuffer
          .put(width)
          .put(height)
          .flip();
      glUniform2iv(RESOLUTION_UNIFORM_LOCATION, resolutionBuffer);


      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      glDrawArrays(GL_TRIANGLES, 0, 6);

      glUseProgram(0);
      glBindVertexArray(0);
      
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, VERTICES_POS_BUFFER_BINDING, 0);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, NORMALS_BUFFER_BINDING, 0);
    }
  }

  private record AugumentedMesh(
      MeshInterface meshInstance, 
      int verticesOffset, 
      int textureCoordOffset, 
      int normalsOffset, 
      int facesOffset) {
  }
}
