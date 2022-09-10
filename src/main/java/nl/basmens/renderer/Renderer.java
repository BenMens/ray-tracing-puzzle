package nl.basmens.renderer;

import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
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
  private static final int INDICES_BUFFER_BINDING = 3;
  private static final int MESHES_BUFFER_BINDING = 4;
  
  private static final int CAMERA_FOV_UNIFORM_LOCATION = 2;
  private static final int CAMERA_POINT_MATRIX_UNIFORM_LOCATION = 3;
  private static final int CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION = 4;

  private ArrayList<Renderable> renderables;

  private HashMap<String, Integer> shaderPrograms = new HashMap<>();

  private int vao;
  private int verticesPosBuffer;
  private int normalsBuffer;
  private int textureCoordsBuffer;
  private int indicesBuffer;
  private int meshesBuffer;


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
    try (InputStream in = classLoader.getResourceAsStream(fragmentShadersPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      String resource;

      while ((resource = br.readLine()) != null) {
        LOGGER.trace("reading fragment shader {}", resource);

        ByteBuffer fs = IoUtil.ioResourceToByteBuffer(fragmentShadersPath + "/" + resource, 4096);
        int f = glCreateShader(GL_FRAGMENT_SHADER);
        compileShader(f, fs);

        int program = compileShaderProgram(v, f);
        String baseName = resource.split("\\.(?=[^\\.]+$)")[0];
        shaderPrograms.put(baseName, program);
      }
    }

    LOGGER.trace("loaded shader programs \n{}", shaderPrograms.toString());

    createVao();
    createShaderBuffers();
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

  void createShaderBuffers() {

    long verticesBufferSize = 0;
    long normalsBufferSize = 0;
    long textureCoordsBufferSize = 0;
    long indicesBufferSize = 0;
    long meshesBufferSize = 0;
    for (Renderable r : renderables) {
      meshesBufferSize += r.getMaxMeshInstanceCount();

      for (MeshInterface m : r.getMeshes()) {
        verticesBufferSize += m.getVerticesCount();
        normalsBufferSize += m.getNormalsCount();
        textureCoordsBufferSize += m.getTextureCoordsCount();
        indicesBufferSize += m.getIndicesCount();
      }
    }

    verticesPosBuffer = glGenBuffers();
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, verticesPosBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, verticesBufferSize * 4, GL_DYNAMIC_DRAW);

    textureCoordsBuffer = glGenBuffers();
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, textureCoordsBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, textureCoordsBufferSize * 4, GL_DYNAMIC_DRAW);

    normalsBuffer = glGenBuffers();
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalsBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, normalsBufferSize * 4, GL_DYNAMIC_DRAW);

    indicesBuffer = glGenBuffers();
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, indicesBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, indicesBufferSize * 4, GL_DYNAMIC_DRAW);

    meshesBuffer = glGenBuffers();
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, meshesBuffer);
    glBufferData(GL_SHADER_STORAGE_BUFFER, meshesBufferSize * 32, GL_DYNAMIC_DRAW);
  }
  

  // ===============================================================================================
  // Render
  // ===============================================================================================
  public void render(Camera camera) {

    try (MemoryStack stack = stackPush()) {

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, verticesPosBuffer);
      long vertexOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, vertexOffset * 4, 
              m.getVerticesData().getData(stack));
          vertexOffset += m.getVerticesCount();
        }
      }

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, textureCoordsBuffer);
      long textureCoordsOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, textureCoordsOffset * 4, 
              m.getTextureCoordsData().getData(stack));
          textureCoordsOffset += m.getTextureCoordsCount();
        }
      }

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalsBuffer);
      long normalsOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, normalsOffset * 4, 
              m.getNormalsData().getData(stack));
          normalsOffset += m.getNormalsCount();
        }
      }

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, indicesBuffer);
      long indicesOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInterface m : r.getMeshes()) {
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, indicesOffset * 4, 
              m.getIndicesData().getData(stack));
          indicesOffset += m.getIndicesCount();
        }
      }

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, meshesBuffer);
      long meschesOffset = 0;
      for (Renderable r : renderables) {
        for (MeshInstance mi : r.getMeshInstances()) {
          ByteBuffer meshBufferData = stack.malloc(32);
          MeshInterface m = mi.mesh();
  
          meshBufferData
              .putFloat(m.getCenter().x)
              .putFloat(m.getCenter().y)
              .putFloat(m.getCenter().z)
              .putFloat(1)
              .putInt(0)  // Offset
              .putInt((int) Math.floorDiv(m.getIndicesCount(), 9))  // Count
              .putInt(0)  // Texture index
              .putFloat(m.getRadius2())
              .flip();
  
          glBufferSubData(GL_SHADER_STORAGE_BUFFER, meschesOffset * 32, meshBufferData);
          meschesOffset += r.getMaxMeshInstanceCount();
        }
      }

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, VERTICES_POS_BUFFER_BINDING, verticesPosBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, NORMALS_BUFFER_BINDING, normalsBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, TEXTURE_COORDS_BUFFER_BINDING, textureCoordsBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, INDICES_BUFFER_BINDING, indicesBuffer);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, MESHES_BUFFER_BINDING, meshesBuffer);

      glUseProgram(this.shaderPrograms.get("ray_tracer"));

      glUniform1f(CAMERA_FOV_UNIFORM_LOCATION, camera.getFov());

      FloatBuffer matBuffer = stack.mallocFloat(16);
      
      camera.getPointCameraMatrix().get(matBuffer);
      glUniformMatrix4fv(CAMERA_POINT_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      camera.getVectorCameraMatrix().get(matBuffer);
      glUniformMatrix3fv(CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      glBindVertexArray(vao);

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      glDrawArrays(GL_TRIANGLES, 0, 6);

      glUseProgram(0);
      glBindVertexArray(0);
      
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, VERTICES_POS_BUFFER_BINDING, 0);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, NORMALS_BUFFER_BINDING, 0);
    }
  }
}
