package nl.basmens.events.types;

import nl.basmens.game.gameobjects.GameObject;
import nl.basmens.util.MeshInterface;

// Near immutable class
public class GameDataServiceEvent extends Event {
  private final GameObject gameObject;
  private final MeshInterface meshInterface;

  // General
  public GameDataServiceEvent(String eventType) {
    super(eventType);

    this.gameObject = null;
    this.meshInterface = null;
  }

  // Game object added event
  public GameDataServiceEvent(String eventType, GameObject gameObject) {
    super(eventType);

    this.gameObject = gameObject;
    this.meshInterface = null;
  }

  // Mesh added event
  public GameDataServiceEvent(String eventType, MeshInterface meshInterface) {
    super(eventType);

    this.gameObject = null;
    this.meshInterface = meshInterface;
  }

  // Texture added event
  // TODO add texture

  // ===============================================================================================
  // Getters
  // ===============================================================================================
  public GameObject getGameObject() {
    return gameObject;
  }

  public MeshInterface getMeshInterface() {
    return meshInterface;
  }
}
