package nl.basmens.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
  private static final Vector3f axisX = new Vector3f(1, 0, 0);
  private static final Vector3f axisY = new Vector3f(0, 1, 0);
  private static final Vector3f axisZ = new Vector3f(0, 0, 1);

  private Vector3f position;
  private Vector3f direction;
  private float fov;

  private Matrix4f pointCameraMatrix;
  private Matrix3f vectorCameraMatrix;

  // ===============================================================================================
  // Constructors
  // ===============================================================================================
  public Camera(float fov) {
    this(new Vector3f(0), new Vector3f(0), fov);
  }

  public Camera(Vector3f position, Vector3f direction, float fov) {
    this.position = position;
    this.direction = direction;
    this.fov = fov;

    pointCameraMatrix = Camera.generatePointMatrix(position, direction);
    vectorCameraMatrix = Camera.generateVectorMatrix(direction);
  }

  // ===============================================================================================
  // Generate matrices
  // ===============================================================================================

  // First rotate around z-axis (tilting head like a cute dog)
  // Then rotate around x-axis (looking up and down)
  // Finally rotate around y-axis (looking left to right)
  // Extra for points, translate

  public static Matrix4f generatePointMatrix(Vector3f position, Vector3f direction) {
    return new Matrix4f()
        .translate(position)
        .rotate(direction.y, axisY)
        .rotate(direction.x, axisX)
        .rotate(direction.z, axisZ);
  }

  public static Matrix3f generateVectorMatrix(Vector3f direction) {
    return new Matrix3f()
        .rotate(direction.y, axisY)
        .rotate(direction.x, axisX)
        .rotate(direction.z, axisZ);
  }

  // ===============================================================================================
  // Getters and Setters
  // ===============================================================================================
  public Vector3f getPosition() {
    return new Vector3f(position);
  }

  public void setPosition(Vector3f position) {
    this.position = new Vector3f(position);

    pointCameraMatrix = Camera.generatePointMatrix(position, direction);
    vectorCameraMatrix = Camera.generateVectorMatrix(direction);
  }

  public Vector3f getDirection() {
    return new Vector3f(direction);
  }

  public void setDirection(Vector3f direction) {
    this.direction = new Vector3f(direction);

    pointCameraMatrix = Camera.generatePointMatrix(position, direction);
    vectorCameraMatrix = Camera.generateVectorMatrix(direction);
  }

  public float getFov() {
    return fov;
  }

  public void setFov(float fov) {
    this.fov = fov;
  }

  // Get only
  public Matrix4f getPointCameraMatrix() {
    return new Matrix4f(pointCameraMatrix);
  }

  // Get only
  public Matrix3f getVectorCameraMatrix() {
    return new Matrix3f(vectorCameraMatrix);
  }
}
