package nl.basmens.util;

import org.joml.Matrix4f;

public record MeshInstance(MeshInterface mesh, int texture, Matrix4f modelMatrix) {}
