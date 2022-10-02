package nl.basmens.game.gameobjects;

import org.joml.Vector3f;

public class Chest extends GameObject {
  private String[] items;


  public Chest(Vector3f position, String mesh, String texture, String[] items) {
    super(position, mesh, texture);

    this.items = items.clone();
  }


  @Override
  public void printData() {
    System.out.println("    {");
    System.out.println("        type = chest");
    System.out.println("        position = " + getPosition().x + ", " + getPosition().y + ", " + getPosition().z);
    System.out.println("        mesh = '" + getMesh() + "'");
    System.out.println("        texture = '" + getTexture() + "'");
    System.out.println("        items = [");

    for (String s : items) {
      System.out.println("            " + s);
    }
    System.out.println("        ]");
    System.out.println("    }");
  }
}
