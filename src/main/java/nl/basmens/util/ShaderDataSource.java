package nl.basmens.util;

import java.nio.Buffer;
import org.lwjgl.system.MemoryStack;

/**
 * A shader data source.
 */
public interface ShaderDataSource<T extends Buffer> {
  /**
   * Returns the maximum number of instances used by this buffer.
   *
   * @return The maximum number of instances.
   */
  long getMaxBufSize();


  /**
   * Returns a buffer that contains the data.
   *
   * @param stack A stack object that can be used to allocate temporal data. If data is 
   *              not allocated using the stack object, the implementing class is responsible 
   *              to free the allocated data.
   * 
   * @return The buffer containing the data.
   */
  T getData(MemoryStack stack);    
}
