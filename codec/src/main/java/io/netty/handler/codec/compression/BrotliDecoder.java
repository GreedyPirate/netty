/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BrotliDecoder extends ByteToMessageDecoder {

  private enum State {
    DONE, NEEDS_MORE_INPUT, ERROR
  }

  private DecoderJNI.Wrapper decoder;
  private final AtomicBoolean destroyed = new AtomicBoolean();

  static {
    try {
      Brotli.ensureAvailability();
    } catch (Throwable throwable) {
      throw new ExceptionInInitializerError(throwable);
    }
  }

  private State decompress(ByteBuf input, List<Object> output) {
    while (true) {
      switch (decoder.getStatus()) {
        case DONE:
          return State.DONE;

        case OK:
          decoder.push(0);
          break;

        case NEEDS_MORE_INPUT:
          if (decoder.hasOutput()) {
            output.add(Unpooled.wrappedBuffer(decoder.pull()));
          }

          if (!input.isReadable()) {
            return State.NEEDS_MORE_INPUT;
          }

          ByteBuffer decoderInputBuffer = decoder.getInputBuffer();
          decoderInputBuffer.clear();
          int readBytes = readBytes(input, decoderInputBuffer);
          decoder.push(readBytes);
          break;

        case NEEDS_MORE_OUTPUT:
          output.add(Unpooled.wrappedBuffer(decoder.pull()));
          break;

        default:
          return State.ERROR;
      }
    }
  }

  private static int readBytes(ByteBuf in, ByteBuffer dest) {
    int limit = Math.min(in.readableBytes(), dest.remaining());
    ByteBuffer slice = dest.slice();
    slice.limit(limit);
    in.readBytes(slice);
    dest.position(dest.position() + limit);
    return limit;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() == 0) {
      out.add(in);
      return;
    }

    if (decoder == null) {
      decoder = new DecoderJNI.Wrapper(8 * 1024);
    }

    try {
      State state = decompress(in, out);
      if (state == State.DONE) {
        destroy();
      } else if (state == State.ERROR) {
        throw new DecompressionException("Brotli stream corrupted");
      }
    } catch (Exception e) {
      destroy();
      throw e;
    }
  }

  private void destroy() {
    if (destroyed.compareAndSet(false, true)) {
      decoder.destroy();
    }
  }

  @Override
  protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    try {
      destroy();
    } finally {
      super.handlerRemoved0(ctx);
    }
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    try {
      destroy();
    } finally {
      super.channelInactive(ctx);
    }
  }
}
