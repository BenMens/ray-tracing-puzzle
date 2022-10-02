package nl.basmens.game.gameobjects;

import org.joml.Vector3f;

public class GameObject {
  private Vector3f position;
  private String mesh;
  private String texture;


  public GameObject(Vector3f position, String mesh, String texture) {
    this.position = position;
    this.mesh = mesh;
    this.texture = texture;
  }


  public void printData() {
    System.out.println("    {");
    System.out.println("        type = game object");
    System.out.println("        position = " + position.x + ", " + position.y + ", " + position.z);
    System.out.println("        mesh = '" + mesh + "'");
    System.out.println("        texture = '" + texture + "'");
    System.out.println("    }");
  }


  // =============================================================================================
  // Getters
  // =============================================================================================
  public Vector3f getPosition() {
    return position;
  }


  public String getMesh() {
    return mesh;
  }


  public String getTexture() {
    return texture;
  }
}
