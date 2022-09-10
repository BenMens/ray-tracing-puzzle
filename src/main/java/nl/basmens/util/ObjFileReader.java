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
import org.joml.Vector3i;

public final class ObjFileReader implements AutoCloseable {
  private static final Pattern PATTERN = Pattern.compile("(^[a-z]+\\b|-?[0-9.]+)");

  private ArrayList<Vector3f> vertices = new ArrayList<>();
  private ArrayList<Vector2f> verticesT = new ArrayList<>();
  private ArrayList<Vector3f> verticesN = new ArrayList<>();
  private ArrayList<Vector3i> faces = new ArrayList<>();


  public ObjFileReader read(String path) throws IOException {
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
            vertices.add(readVector3f(m));
            break;
          case "vt":
            verticesT.add(readVector2f(m));
            break;
          case "vn":
            verticesN.add(readVector3f(m));
            break;
          case "f":
            faces.add(readVector3i(m));
            faces.add(readVector3i(m));
            faces.add(readVector3i(m));
            break;
          default:
            break;
        }
      }
    }

    int[] f = new int[faces.size() * 3];
    for (int i = 0; i < f.length; i += 3) {
      f[i + 0] = faces.get(i / 3).x;
      f[i + 1] = faces.get(i / 3).y;
      f[i + 2] = faces.get(i / 3).z;
    }

    return this;
  }


  private static Vector2f readVector2f(Matcher m) {
    float[] numbers = new float[2];

    for (int i = 0; i < numbers.length; i++) {
      m.find();
      numbers[i] = Float.parseFloat(m.group());
    }

    return new Vector2f(numbers);
  }

  private static Vector3f readVector3f(Matcher m) {
    float[] numbers = new float[3];

    for (int i = 0; i < numbers.length; i++) {
      m.find();
      numbers[i] = Float.parseFloat(m.group());
    }

    return new Vector3f(numbers);
  }

  private static Vector3i readVector3i(Matcher m) {
    int[] numbers = new int[3];

    for (int i = 0; i < numbers.length; i++) {
      m.find();
      numbers[i] = Integer.parseInt(m.group()) - 1;
    }

    return new Vector3i(numbers);
  }


  public Mesh getMesh() {
    return new Mesh(vertices, verticesT, verticesN, faces);
  }


  @Override
  public void close() throws Exception {
    // Auto closable
  }
}
