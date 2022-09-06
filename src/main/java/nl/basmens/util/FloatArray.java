package nl.basmens.util;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class FloatArray implements ShaderDataSource<FloatBuffer> {

  private FloatBuffer buffer;
  
  public FloatArray(int size) {
    buffer = MemoryUtil.memAllocFloat(size);
  } 

  public FloatBuffer getBuffer() {
    return buffer;
  }

  @Override
  public FloatBuffer getData(MemoryStack stack) {
    return buffer;
  }

  @Override
  public long getMaxBufSize() {
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
