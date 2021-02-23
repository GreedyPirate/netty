package com.sankuai.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

public class ByteBufTests {

    @Test
    public void testInit() {
        ByteBuf buffer = Unpooled.buffer(32, 512);
        int capacity = buffer.capacity();

        assertTrue(capacity == 32);
    }

    @Test
    public void testRead() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        // 一个字节8位，int只能写 -128～127
        buffer.writeByte(127);
        buffer.writeByte(2);
        buffer.writeByte(3);
        buffer.writeBoolean(true);
        buffer.writeZero(3);

        System.out.println("write index = " + buffer.writerIndex());
        while (buffer.isReadable()) {
            byte b = buffer.readByte();
            System.out.println("b = " + b);
        }


        // 剩余的可写字节数
        int maxWritableBytes = buffer.maxWritableBytes();
        System.out.println("maxWritableBytes = " + maxWritableBytes);

        // clear之后数据还在，指针重置为0
        buffer.clear();
        System.out.println("read index = " + buffer.readerIndex());
        System.out.println("write index = " + buffer.writerIndex());
    }

    @Test
    public void testDiscard() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);

        byte b = buffer.readByte();
        buffer.discardReadBytes();

    }

    @Test
    public void testSearch() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(5);
        buffer.writeByte(10);
        buffer.writeByte(15);
        buffer.writeByte(10);

        byte target = 10;
        int index = buffer.indexOf(0, buffer.writerIndex(), target);
        System.out.println("index = " + index);
        assertTrue(index == 1);

        int before = buffer.bytesBefore(target);
        System.out.println("before = " + before);
        assertTrue(before == 1);

    }

    @Test
    public void testMarkReset() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);

        buffer.markReaderIndex();

        byte b = buffer.readByte();
        System.out.println("b = " + b + ", index = " + buffer.readerIndex());
        assertTrue(buffer.readerIndex() == 1);

        b = buffer.readByte();
        System.out.println("b = " + b + ", index = " + buffer.readerIndex());
        assertTrue(buffer.readerIndex() == 2);

        buffer.resetReaderIndex();

        System.out.println("buffer = " + buffer.readerIndex());
        assertTrue(buffer.readerIndex() == 0);

    }

    /**
     * 派生的buffer和原buffer，2个index互相独立
     */
    @Test
    public void testDerived() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);

        ByteBuf duplicate = buffer.duplicate();

        System.out.println("buffer readerIndex = " + buffer.readerIndex());
        print(duplicate);
        System.out.println("buffer readerIndex = " + buffer.readerIndex());

        System.out.println("buffer writerIndex = " + buffer.writerIndex());
        duplicate.writeByte(4);
        System.out.println("buffer writerIndex = " + buffer.writerIndex());

        print(buffer);

        buffer.writeByte(5);
        print(duplicate);
    }

    @Test
    public void testCopy() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);

        ByteBuf copy = buffer.copy();
        copy.writeByte(4);

        print(buffer);
        print(copy);
    }

    @Test
    public void testToNio() {
        ByteBuf buffer = Unpooled.buffer(32, 64);
        buffer.writeByte(1);
        buffer.writeByte(2);
        buffer.writeByte(3);

        ByteBuffer nioBuffer = buffer.nioBuffer();

        while (nioBuffer.hasRemaining()) {
            byte b = nioBuffer.get();
            System.out.println("b = " + b);
        }
    }


    private void print(ByteBuf byteBuf) {
        System.out.println("\n*******start printing*******");
        while (byteBuf.isReadable()) {
            byte b = byteBuf.readByte();
            System.out.println("b = " + b);
        }
    }
}
