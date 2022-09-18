package nl.basmens.renderer;

import nl.basmens.util.MeshInstance;
import nl.basmens.util.MeshInterface;

/**
 * Represends an object than can be rendered.
 */
public interface Renderable {
  MeshInterface[] getMeshes();

  MeshInstance[] getMeshInstances();

  int getMaxMeshInstanceCount();
}
