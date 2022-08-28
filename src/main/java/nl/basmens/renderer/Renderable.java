package nl.basmens.renderer;

import java.nio.ByteBuffer;

/**
 * Represends an object than can be rendered.
 */
public interface Renderable {
  void getData(ByteBuffer data);
}
