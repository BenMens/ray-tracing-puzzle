package nl.basmens.renderer;

import java.util.List;
import nl.basmens.util.MeshInstance;
import nl.basmens.util.MeshInterface;

/**
 * Represends an object than can be rendered.
 */
public interface Renderable {
  List<MeshInterface> getMeshes();

  List<MeshInstance> getMeshInstances();

  int getMaxMeshInstanceCount();
}
