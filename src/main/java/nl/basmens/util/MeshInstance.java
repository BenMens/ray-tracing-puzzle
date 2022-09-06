package nl.basmens.util;

public record MeshInstance(
    int offset, 
    int count, 
    int textureIndex,
    float centerX,
    float centerY,
    float centerZ,
    float radius
    ) {
}
