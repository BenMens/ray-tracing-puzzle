package nl.basmens.util;

import org.joml.Matrix4f;

public record MeshInstance(MeshInterface mesh, Matrix4f modelMatrix) {}
