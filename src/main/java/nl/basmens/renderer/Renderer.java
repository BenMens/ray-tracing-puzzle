package nl.basmens.renderer;

import nl.basmens.util.IOUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryStack.*;


public class Renderer {
    private static final Logger LOGGER = LogManager.getLogger(Renderer.class);
    private static final int VERTEX_POSITION_ATTRIBUTE_LOCATION = 0;
    private static final int VERTEX_COLOR_ATTRIBUTE_LOCATION = 1;
    private static final int SHADER_BUFFER_BINDING = 3;

    private ArrayList<Renderable> renderables;

    private HashMap<String, Integer> shaderPrograms = new HashMap<>();

    private int vao;
    private int shaderBuffer;

    // ====================================================================================================================
    // Constructor
    // ====================================================================================================================
    public Renderer() {
        renderables = new ArrayList<>();
    }

    // ====================================================================================================================
    // Register
    // ====================================================================================================================
    public void register(Renderable renderable) {
        if (!renderables.contains(renderable)) {
            renderables.add(renderable);
        } else {
            // TODO print warning: same object registered twice
        }
    }

    // ====================================================================================================================
    // Unregister
    // ====================================================================================================================
    public void unregister(Renderable renderable) {
        if (!renderables.contains(renderable)) {
            // TODO print warning: trying to remove an uregistered object
        } else {
            renderables.remove(renderable);
        }
    }

    // ====================================================================================================================
    // Initialization
    // ====================================================================================================================
    public void init() throws Exception {
        int version;
        GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL41) {
            version = 430;
        } else if (caps.OpenGL33) {
            version = 330;
        } else if (caps.OpenGL21) {
            version = 120;
        } else {
            version = 110;
        }
        LOGGER.trace("GL version is {}", version);

        LOGGER.trace("reading vertex shader");
        ByteBuffer vs = IOUtil.ioResourceToByteBuffer("shaders/vertex/vertex.vert", 4096);
        int v = glCreateShader(GL_VERTEX_SHADER);
        compileShader(version, v, vs);

        String fragmentShadersPath = "shaders/fragment";

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (
                InputStream in = classLoader.getResourceAsStream(fragmentShadersPath);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                LOGGER.trace("reading fragment shader {}", resource);
                ByteBuffer fs = IOUtil.ioResourceToByteBuffer(fragmentShadersPath + "/" + resource, 4096);
                int f = glCreateShader(GL_FRAGMENT_SHADER);
                compileShader(version, f, fs);

                int program = compileShaderProgram(version, v, f);

                String baseName = resource.split("\\.(?=[^\\.]+$)")[0];

                shaderPrograms.put(baseName, program);
            }
        }

        LOGGER.trace("loaded shader programs \n{}", shaderPrograms.toString());

        createVao();
        createShaderBuffer();
    }

    // ====================================================================================================================
    // Shader compilation
    // ====================================================================================================================
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

    private void compileShader(int version, int shader, ByteBuffer code) {
        try (MemoryStack stack = stackPush()) {
            ByteBuffer header = stack.ASCII("#version " + version + "\n#line 0\n", false);

            glShaderSource(
                    shader,
                    stack.pointers(header, code),
                    stack.ints(header.remaining(), code.remaining()));

            glCompileShader(shader);
            printShaderInfoLog(shader);

            if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
                throw new IllegalStateException("Failed to compile shader.");
            }
        }
    }

    private int compileShaderProgram(int version, int v, int f) {
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


    // ====================================================================================================================
    // Create Vertex Arrays Object
    // ====================================================================================================================

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

    // ====================================================================================================================
    // Render
    // ====================================================================================================================
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
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, SHADER_BUFFER_BINDING, shaderBuffer);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, SHADER_BUFFER_BINDING, 0);
        glBindVertexArray(0);
        glUseProgram(0);
    }
}
