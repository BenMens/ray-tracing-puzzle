package nl.basmens.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class ObjFileReader implements AutoCloseable {
  private static final Pattern PATTERN = Pattern.compile("((^\\p{IsAlphabetic}+\\b)|-?[0-9.]+)");

  private ArrayList<Vector3f> vertices;
  private ArrayList<Vector2f> verticesT;
  private ArrayList<Vector3f> verticesN;
  private ArrayList<Integer> faces;
  private String name;


  public ObjFileReader read(String name, String path) throws IOException {
    this.name = name;
    this.vertices = new ArrayList<>();
    this.verticesT = new ArrayList<>();
    this.verticesN = new ArrayList<>();
    this.faces = new ArrayList<>();
  

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream in = classLoader.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

      String line;

      while ((line = br.readLine()) != null) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) {
          continue;
        }
        String type = m.group();

        switch (type) {
          case "v":
            vertices.add(new Vector3f(readFloat(m), readFloat(m), readFloat(m)));
            break;
          case "vt":
            verticesT.add(new Vector2f(readFloat(m), readFloat(m)));
            break;
          case "vn":
            verticesN.add(new Vector3f(readFloat(m), readFloat(m), readFloat(m)));
            break;
          case "f":
            for (int i = 0; i < 9; i++) {
              faces.add(readInt(m) - 1);
            }
            break;
          default:
            break;
        }
      }
    }

    return this;
  }


  private static float readFloat(Matcher m) {
    m.find();
    return Float.parseFloat(m.group());
  }

  private static int readInt(Matcher m) {
    m.find();
    return Integer.parseInt(m.group());
  }


  public Mesh getMesh() {
    return new Mesh(name, vertices, verticesT, verticesN, faces);
  }


  @Override
  public void close() throws Exception {
    // Auto closable
  }
}
