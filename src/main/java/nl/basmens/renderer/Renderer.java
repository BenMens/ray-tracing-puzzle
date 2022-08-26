package nl.basmens.renderer;

import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform3f;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import nl.basmens.util.IOUtil;
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
  private Camera camera;

  // ===============================================================================================
  // Constructor
  // ===============================================================================================
  public Renderer() {
    renderables = new ArrayList<>();
    camera = new Camera(0.7f);
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
  public void init() throws Exception {
    LOGGER.trace("reading vertex shader");

    ByteBuffer vs = IOUtil.ioResourceToByteBuffer("shaders/vertex/vertex.vert", 4096);
    int v = glCreateShader(GL_VERTEX_SHADER);
    compileShader(v, vs);

    String fragmentShadersPath = "shaders/fragment";
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(fragmentShadersPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      String resource;

      while ((resource = br.readLine()) != null) {
        LOGGER.trace("reading fragment shader {}", resource);

        ByteBuffer fs = IOUtil.ioResourceToByteBuffer(fragmentShadersPath + "/" + resource, 4096);
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
  private void printShaderInfoLog(int obj) {
    int infologLength = glGetShaderi(obj, GL_INFO_LOG_LENGTH);
    if (infologLength > 0) {
      LOGGER.trace("Shader info: {}", glGetShaderInfoLog(obj));
    }
  }

  private void printProgramInfoLog(int obj) {
    int infologLength = glGetProgrami(obj, GL_INFO_LOG_LENGTH);
    if (infologLength > 0) {
      LOGGER.trace("Program info: {}", glGetProgramInfoLog(obj));
    }
  }

  private void compileShader(int shader, ByteBuffer code) {
    try (MemoryStack stack = stackPush()) {
      glShaderSource(shader, stack.pointers(code), stack.ints(code.remaining()));
      glCompileShader(shader);
      printShaderInfoLog(shader);

      if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
        throw new IllegalStateException("Failed to compile shader.");
      }
    }
  }

  private int compileShaderProgram(int v, int f) {
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
    pb.put(-1f).put(-1f).put(0f);
    pb.put(1f).put(-1f).put(0f);
    pb.put(-1).put(1f).put(0f);
    pb.put(1f).put(-1f).put(0f);
    pb.put(-1f).put(1f).put(0f);
    pb.put(1).put(1f).put(0f);
    pb.flip();

    FloatBuffer cb = BufferUtils.createFloatBuffer(6 * 4);
    cb.put(1f).put(0f).put(0f).put(1.0f);
    cb.put(0f).put(1f).put(0f).put(1.0f);
    cb.put(0f).put(0f).put(1f).put(1.0f);
    cb.put(0f).put(1f).put(0f).put(1.0f);
    cb.put(0f).put(0f).put(1f).put(1.0f);
    cb.put(1f).put(1f).put(1f).put(1.0f);
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
  public void render() {

    ByteBuffer sb = BufferUtils.createByteBuffer(1 * 4 * 4);
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

    FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);

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

  public Camera getCamera() {
    return camera;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }


}
