package engine.color;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

class ColorBufferTest
{
    @Test
    void create()
    {
        ColorBuffer buffer;
        ByteBuffer  byteBuffer;
        
        buffer = ColorBuffer.create(256);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        buffer = ColorBuffer.create(ColorFormat.RGB, 256);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.DEFAULT.sizeof);
        buffer     = ColorBuffer.create(MemoryUtil.memAddress(byteBuffer), 256);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.RGB.sizeof);
        buffer     = ColorBuffer.create(ColorFormat.RGB, MemoryUtil.memAddress(byteBuffer), 256);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.create(ColorFormat.UNKNOWN, 1));
    }
    
    @Test
    void createSafe()
    {
        ColorBuffer buffer;
        ByteBuffer  byteBuffer;
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.DEFAULT.sizeof);
        buffer     = ColorBuffer.createSafe(MemoryUtil.memAddress(byteBuffer), 256);
        Assertions.assertNotNull(buffer);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.RGB.sizeof);
        buffer     = ColorBuffer.createSafe(ColorFormat.RGB, MemoryUtil.memAddress(byteBuffer), 256);
        Assertions.assertNotNull(buffer);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        buffer = ColorBuffer.createSafe(MemoryUtil.NULL, 256);
        Assertions.assertNull(buffer);
        
        buffer = ColorBuffer.createSafe(ColorFormat.RGB, MemoryUtil.NULL, 256);
        Assertions.assertNull(buffer);
        
        final ByteBuffer b = MemoryUtil.memAlloc(256 * ColorFormat.RGB.sizeof);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.createSafe(ColorFormat.UNKNOWN, MemoryUtil.memAddress(b), 1));
        MemoryUtil.memFree(b);
    }
    
    @Test
    void wrap()
    {
        ColorBuffer buffer;
        ByteBuffer  byteBuffer;
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.DEFAULT.sizeof);
        buffer     = ColorBuffer.wrap(byteBuffer);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.RGB.sizeof);
        buffer     = ColorBuffer.wrap(ColorFormat.RGB, byteBuffer);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        final ByteBuffer b = MemoryUtil.memAlloc(4);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.wrap(ColorFormat.UNKNOWN, b));
        MemoryUtil.memFree(b);
    }
    
    @SuppressWarnings({"ConstantValue", "DataFlowIssue"})
    @Test
    void wrapSafe()
    {
        ColorBuffer buffer;
        ByteBuffer  byteBuffer;
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.DEFAULT.sizeof);
        buffer     = ColorBuffer.wrapSafe(byteBuffer);
        Assertions.assertNotNull(buffer);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        byteBuffer = MemoryUtil.memAlloc(256 * ColorFormat.RGB.sizeof);
        buffer     = ColorBuffer.wrapSafe(ColorFormat.RGB, byteBuffer);
        Assertions.assertNotNull(buffer);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        buffer = ColorBuffer.wrapSafe(null);
        Assertions.assertNull(buffer);
        
        buffer = ColorBuffer.wrapSafe(ColorFormat.RGB, null);
        Assertions.assertNull(buffer);
        
        final ByteBuffer b = MemoryUtil.memAlloc(4);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.wrapSafe(ColorFormat.UNKNOWN, b));
        MemoryUtil.memFree(b);
    }
    
    @Test
    void malloc()
    {
        ColorBuffer buffer;
        
        buffer = ColorBuffer.malloc(256);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        buffer = ColorBuffer.malloc(ColorFormat.RGB, 256);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            buffer = ColorBuffer.malloc(256, stack);
            Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
            Assertions.assertEquals(0, buffer.position());
            Assertions.assertEquals(256, buffer.capacity());
            Assertions.assertEquals(256, buffer.remaining());
            
            buffer = ColorBuffer.malloc(ColorFormat.RGB, 256, stack);
            Assertions.assertEquals(ColorFormat.RGB, buffer.format);
            Assertions.assertEquals(0, buffer.position());
            Assertions.assertEquals(256, buffer.capacity());
            Assertions.assertEquals(256, buffer.remaining());
        }
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.malloc(ColorFormat.UNKNOWN, 256));
    }
    
    @Test
    void calloc()
    {
        ColorBuffer buffer;
        
        buffer = ColorBuffer.calloc(256);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        buffer = ColorBuffer.calloc(ColorFormat.RGB, 256);
        Assertions.assertEquals(ColorFormat.RGB, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(256, buffer.capacity());
        Assertions.assertEquals(256, buffer.remaining());
        buffer.free();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            buffer = ColorBuffer.calloc(256, stack);
            Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
            Assertions.assertEquals(0, buffer.position());
            Assertions.assertEquals(256, buffer.capacity());
            Assertions.assertEquals(256, buffer.remaining());
            
            buffer = ColorBuffer.calloc(ColorFormat.RGB, 256, stack);
            Assertions.assertEquals(ColorFormat.RGB, buffer.format);
            Assertions.assertEquals(0, buffer.position());
            Assertions.assertEquals(256, buffer.capacity());
            Assertions.assertEquals(256, buffer.remaining());
        }
        
        Assertions.assertThrows(IllegalArgumentException.class, () -> ColorBuffer.calloc(ColorFormat.UNKNOWN, 256));
    }
    
    @Test
    void realloc()
    {
        ColorBuffer buffer;
        
        buffer = ColorBuffer.malloc(256);
        buffer = ColorBuffer.realloc(buffer, 512);
        Assertions.assertEquals(ColorFormat.DEFAULT, buffer.format);
        Assertions.assertEquals(0, buffer.position());
        Assertions.assertEquals(512, buffer.capacity());
        Assertions.assertEquals(512, buffer.remaining());
        buffer.free();
    }
    
    @Test
    void sizeof()
    {
        ColorBuffer buffer;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            buffer = ColorBuffer.malloc(256, stack);
            Assertions.assertEquals(ColorFormat.DEFAULT.sizeof, buffer.sizeof());
            
            buffer = ColorBuffer.malloc(ColorFormat.RGB, 256, stack);
            Assertions.assertEquals(ColorFormat.RGB.sizeof, buffer.sizeof());
        }
    }
    
    @Test
    void copy()
    {
        ColorBuffer buffer1, buffer2;
        
        buffer1 = ColorBuffer.malloc(256);
        buffer2 = buffer1.copy();
        
        Assertions.assertNotNull(buffer2);
        Assertions.assertNotSame(buffer1, buffer2);
        Assertions.assertEquals(buffer1.format, buffer2.format);
        Assertions.assertEquals(buffer1.capacity(), buffer2.capacity());
        Assertions.assertEquals(0, buffer2.position());
        
        buffer1.free();
        buffer2.free();
    }
    
    @Test
    void getAndPut()
    {
        ColorBuffer buffer, b;
        Color       color = new Color(0, 0, 0, 255);
        
        buffer = ColorBuffer.malloc(256);
        Assertions.assertEquals(0, buffer.position());
        
        b = buffer.put(color.set(0, 0, 0, 255));
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(1, buffer.position());
        
        b = buffer.put(color.set(1, 1, 1, 255));
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(2, buffer.position());
        
        b = buffer.put(16, color.set(16, 16, 16, 255));
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(2, buffer.position());
        
        b = buffer.put(32, 32, 32, 32, 255);
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(2, buffer.position());
        
        buffer.clear();
        Assertions.assertEquals(0, buffer.position());
        
        color = buffer.get();
        Assertions.assertNotNull(color);
        Assertions.assertTrue(color.equals(0, 0, 0, 255));
        Assertions.assertEquals(1, buffer.position());
        
        b = buffer.get(color);
        Assertions.assertSame(buffer, b);
        Assertions.assertNotNull(color);
        Assertions.assertTrue(color.equals(1, 1, 1, 255));
        Assertions.assertEquals(2, buffer.position());
        
        color = buffer.get(16);
        Assertions.assertNotNull(color);
        Assertions.assertTrue(color.equals(16, 16, 16, 255));
        Assertions.assertEquals(2, buffer.position());
        
        b = buffer.get(32, color);
        Assertions.assertSame(buffer, b);
        Assertions.assertNotNull(color);
        Assertions.assertTrue(color.equals(32, 32, 32, 255));
        Assertions.assertEquals(2, buffer.position());
        
        buffer.free();
    }
    
    @Test
    void apply()
    {
        ColorBuffer buffer, b;
        Color       color = new Color(0, 0, 0, 255);
        
        buffer = ColorBuffer.malloc(256);
        Assertions.assertEquals(0, buffer.position());
        
        b = buffer.apply(c -> c.set(1, 2, 3, 4));
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(1, buffer.position());
        buffer.get(0, color);
        Assertions.assertTrue(color.equals(1, 2, 3, 4));
        
        b = buffer.apply(16, c -> c.set(1, 2, 3, 4));
        Assertions.assertSame(buffer, b);
        Assertions.assertEquals(1, buffer.position());
        buffer.get(16, color);
        Assertions.assertTrue(color.equals(1, 2, 3, 4));
        
        buffer.free();
    }
    
    @Test
    void forEach()
    {
        ColorBuffer buffer;
        
        buffer = ColorBuffer.malloc(256);
        Assertions.assertEquals(0, buffer.position());
        
        final int[] i = {0};
        buffer.forEach(c -> c.set(i[0], i[0], i[0], i[0]++));
        Assertions.assertEquals(256, i[0]);
        
        i[0] = 0;
        buffer.forEach(c -> Assertions.assertTrue(c.equals(i[0], i[0], i[0], i[0]++)));
        Assertions.assertEquals(256, i[0]);
        
        buffer.free();
    }
}