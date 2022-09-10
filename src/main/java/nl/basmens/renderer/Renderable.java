package nl.basmens.renderer;

import nl.basmens.util.MeshInterface;
import nl.basmens.util.MeshInstance;

/**
 * Represends an object than can be rendered.
 */
public interface Renderable {
  MeshInterface[] getMeshes();

  MeshInstance[] getMeshInstances();

  long getMaxMeshInstanceCount();
}
