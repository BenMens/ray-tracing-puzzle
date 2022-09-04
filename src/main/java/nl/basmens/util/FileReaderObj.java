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

public final class FileReaderObj {
  private static final Pattern PATTERN = Pattern.compile("(v\\b|vt\\b|vn\\b|-?[0-9.]+)");


  private FileReaderObj() {
    // Prevent initialization
  }


  public static void read(String path) throws IOException {
    ArrayList<Vector3f> v = new ArrayList<>();
    ArrayList<Vector2f> vt = new ArrayList<>();
    ArrayList<Vector3f> vn = new ArrayList<>();

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
            v.add(readVector3f(m));
            break;
          case "vt":
            vt.add(readVector2f(m));
            break;
          case "vn":
            vn.add(readVector3f(m));
            break;
          default:
            break;
        }
      }
    }

    for (Vector3f x : v) {
      System.out.println(x.x + ", " + x.y + ", " + x.z);
    }
    System.out.println();
    for (Vector2f x : vt) {
      System.out.println(x.x + ", " + x.y);
    }
    System.out.println();
    for (Vector3f x : vn) {
      System.out.println(x.x + ", " + x.y + ", " + x.z);
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


  public static void main(String[] args) {
    try {
      read("obj-files/test.obj");
    } catch(IOException e) {
      System.out.println("AHHHHHHH");
    }
  }
}
