/*
 * Copyright LWJGL. All rights reserved. License terms: https://www.lwjgl.org/license
 */

package nl.basmens.util;

import static org.lwjgl.system.MemoryUtil.memSlice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

public final class IoUtil {
  private static final Logger LOGGER = LogManager.getLogger(IoUtil.class);


  private IoUtil() {}

  private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
    ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
    buffer.flip();
    newBuffer.put(buffer);
    return newBuffer;
  }

  /**
   * Reads the specified resource and returns the raw data as a ByteBuffer.
   *
   * @param resource the resource to read
   * @param bufferSize the initial buffer size
   *
   * @return the resource data
   *
   * @throws IOException if an IO error occurs
   */
  public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize)
      throws IOException {
    ByteBuffer buffer;

    LOGGER.trace("Reading resource {}", resource);
    Path path = Paths.get(resource);

    try {
      if (Files.isReadable(path)) {
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
          buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
          while (fc.read(buffer) != -1) {
            // Wait
          }
        }
      } else {
        try (InputStream source = 
            Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            ReadableByteChannel rbc = Channels.newChannel(source)) {
          buffer = BufferUtils.createByteBuffer(bufferSize);

          while (true) {
            int bytes = rbc.read(buffer);
            if (bytes == -1) {
              break;
            }
            if (buffer.remaining() == 0) {
              buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
            }
          }
        }
      }
      buffer.flip();
      return memSlice(buffer);
    } catch (Exception e) {
      LOGGER.error("Error while loading resource {}", resource);

      throw e;
    }

  }
  
}
