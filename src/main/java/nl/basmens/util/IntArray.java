package nl.basmens.util;

import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class IntArray implements ShaderDataSource<IntBuffer> {

  private IntBuffer buffer;
  
  public IntArray(int size) {
    buffer = MemoryUtil.memAllocInt(size);
  } 

  public IntBuffer getBuffer() {
    return buffer;
  }

  @Override
  public IntBuffer getData(MemoryStack stack) {
    return buffer;
  }

  @Override
  public int getMaxBufSize() {
    return buffer.capacity();
  }

  @Override
  protected void finalize() throws Throwable {
    if (buffer != null) {
      MemoryUtil.memFree(buffer);
    }
    super.finalize();
  }

}
