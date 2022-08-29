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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

public class Renderer {
  private static final Logger LOGGER = LogManager.getLogger(Renderer.class);

  private static final int VERTEX_POSITION_ATTRIBUTE_LOCATION = 0;
  private static final int VERTEX_COLOR_ATTRIBUTE_LOCATION = 1;
  private static final int SHADER_BUFFER_BINDING = 0;
  private static final int CAMERA_POSITION_UNIFORM_LOCATION = 0;
  private static final int CAMERA_DIRECTION_UNIFORM_LOCATION = 1;
  private static final int CAMERA_FOV_UNIFORM_LOCATION = 2;
  private static final int CAMERA_POINT_MATRIX_UNIFORM_LOCATION = 3;
  private static final int CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION = 4;

  private ArrayList<Renderable> renderables;

  private HashMap<String, Integer> shaderPrograms = new HashMap<>();

  private int vao;
  private int shaderBuffer;

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
    createShaderBuffer();
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

  void createShaderBuffer() {
    shaderBuffer = glGenBuffers();
  }

  // ===============================================================================================
  // Render
  // ===============================================================================================
  public void render(Camera camera) {

    try (MemoryStack stack = stackPush()) {
      ByteBuffer sb = stack.malloc(1 * 4 * 4);
      for (Renderable r : renderables) {
        r.getData(sb);
      }
      sb.flip();

      glBindBuffer(GL_SHADER_STORAGE_BUFFER, shaderBuffer);
      glBufferData(GL_SHADER_STORAGE_BUFFER, sb, GL_DYNAMIC_DRAW);
      glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

      glUseProgram(this.shaderPrograms.get("shader1"));

      Vector3f position = camera.getPosition();
      glUniform3f(CAMERA_POSITION_UNIFORM_LOCATION, position.x, position.y, position.z);

      Vector3f direction = camera.getDirection();
      glUniform3f(CAMERA_DIRECTION_UNIFORM_LOCATION, direction.x, direction.y, direction.z);

      glUniform1f(CAMERA_FOV_UNIFORM_LOCATION, camera.getFov());

      FloatBuffer matBuffer = stack.mallocFloat(16);

      camera.getPointCameraMatrix().get(matBuffer);
      glUniformMatrix4fv(CAMERA_POINT_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      camera.getVectorCameraMatrix().get(matBuffer);
      glUniformMatrix4fv(CAMERA_VECTOR_MATRIX_UNIFORM_LOCATION, false, matBuffer);

      glBindVertexArray(vao);
      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, SHADER_BUFFER_BINDING, shaderBuffer);

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      glDrawArrays(GL_TRIANGLES, 0, 6);

      glBindBufferBase(GL_SHADER_STORAGE_BUFFER, SHADER_BUFFER_BINDING, 0);
      glBindVertexArray(0);
      glUseProgram(0);
    }
  }

}
