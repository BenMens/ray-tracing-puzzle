package nl.basmens.renderer;

import nl.basmens.util.Mesh;
import nl.basmens.util.MeshInstance;

/**
 * Represends an object than can be rendered.
 */
public interface Renderable {
  Mesh[] getMeshes();

  MeshInstance[] getMeshInstances();

  long getMaxMeshInstanceCount();
}
