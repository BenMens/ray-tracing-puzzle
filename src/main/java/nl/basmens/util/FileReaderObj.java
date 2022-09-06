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

public final class FileReaderObj {
  private static final Pattern PATTERN = Pattern.compile("(v\\b|vt\\b|vn\\b|f\\b|-?[0-9.]+)");


  private FileReaderObj() {
    // Prevent initialization
  }


  public static void read(String path) throws IOException {
    ArrayList<Vector3f> vertices = new ArrayList<>();
    ArrayList<Vector2f> verticesT = new ArrayList<>();
    ArrayList<Vector3f> verticesN = new ArrayList<>();
    ArrayList<Vector3i> faces = new ArrayList<>();

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

    for (Vector3f x : vertices) {
      System.out.println(x.x + ", " + x.y + ", " + x.z);
    }
    System.out.println();
    for (Vector2f x : verticesT) {
      System.out.println(x.x + ", " + x.y);
    }
    System.out.println();
    for (Vector3f x : verticesN) {
      System.out.println(x.x + ", " + x.y + ", " + x.z);
    }
    System.out.println();
    for (int i = 0; i < f.length; i += 9) {
      System.out.println(f[i] + "/" + f[i + 1] + "/" + f[i + 2] + ", " + f[i + 3] + "/" + f[i + 4]
          + "/" + f[i + 5] + ", " + f[i + 6] + "/" + f[i + 7] + "/" + f[i + 8]);
    }
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
      numbers[i] = Integer.parseInt(m.group());
    }

    return new Vector3i(numbers);
  }


  public static void main(String[] args) {
    try {
      //read("obj-files/test.obj");
      read("obj-files/donut_low.obj");
      //read("obj-files/donut_medium.obj");
      //read("obj-files/donut_high.obj");
      //read("obj-files/donut_ultra.obj");
    } catch (IOException e) {
      System.out.println("AHHHHHHH");
    }
  }
}
