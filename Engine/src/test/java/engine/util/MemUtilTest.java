package engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.*;

class MemUtilTest
{
    @Test
    void elementSize()
    {
        Buffer buffer;
        
        buffer = ByteBuffer.allocateDirect(1);
        Assertions.assertEquals(Byte.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(4).asIntBuffer();
        Assertions.assertEquals(Integer.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(4).asFloatBuffer();
        Assertions.assertEquals(Float.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(1).asCharBuffer();
        Assertions.assertEquals(Character.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(2).asShortBuffer();
        Assertions.assertEquals(Short.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(8).asLongBuffer();
        Assertions.assertEquals(Long.BYTES, MemUtil.elementSize(buffer));
        
        buffer = ByteBuffer.allocateDirect(8).asDoubleBuffer();
        Assertions.assertEquals(Double.BYTES, MemUtil.elementSize(buffer));
    }
    
    @Test
    void memCopyByteArrayToByteBuffer()
    {
        int length = 16;
        
        byte[]     src = new byte[length];
        ByteBuffer dst = ByteBuffer.allocateDirect(length);
        
        for (int i = 0; i < length; i++) src[i] = (byte) i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy((byte[]) null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyIntArrayToByteBuffer()
    {
        int length = 16;
        
        int[]      src = new int[length];
        ByteBuffer dst = ByteBuffer.wrap(new byte[length]);
        
        for (int i = 0; i < length; i++) src[i] = i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy((int[]) null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, (ByteBuffer) null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyShortArrayToShortBuffer()
    {
        int length = 16;
        
        short[]     src = new short[length];
        ShortBuffer dst = ByteBuffer.allocateDirect(length * Short.BYTES).asShortBuffer();
        
        for (int i = 0; i < length; i++) src[i] = (short) i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyIntArrayToIntBuffer()
    {
        int length = 16;
        
        int[]     src = new int[length];
        IntBuffer dst = ByteBuffer.allocateDirect(length * Integer.BYTES).asIntBuffer();
        
        for (int i = 0; i < length; i++) src[i] = i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, (IntBuffer) null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyLongArrayToLongBuffer()
    {
        int length = 16;
        
        long[]     src = new long[length];
        LongBuffer dst = ByteBuffer.allocateDirect(length * Long.BYTES).asLongBuffer();
        
        for (int i = 0; i < length; i++) src[i] = i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyFloatArrayToFloatBuffer()
    {
        int length = 16;
        
        float[]     src = new float[length];
        FloatBuffer dst = ByteBuffer.allocateDirect(length * Float.BYTES).asFloatBuffer();
        
        for (int i = 0; i < length; i++) src[i] = (float) i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyDoubleArrayToDoubleBuffer()
    {
        int length = 16;
        
        double[]     src = new double[length];
        DoubleBuffer dst = ByteBuffer.allocateDirect(length * Double.BYTES).asDoubleBuffer();
        
        for (int i = 0; i < length; i++) src[i] = i;
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src[i], dst.get(i));
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyByteBufferToByteArray()
    {
        int length = 16;
        
        ByteBuffer src = ByteBuffer.allocateDirect(length);
        byte[]     dst = new byte[length];
        
        for (int i = 0; i < length; i++) src.put(i, (byte) i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, (byte[]) null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyByteBufferToIntArray()
    {
        int length = 16;
        
        ByteBuffer src = ByteBuffer.allocateDirect(length);
        int[]      dst = new int[length];
        
        for (int i = 0; i < length; i++) src.put(i, (byte) i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy((ByteBuffer) null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, (int[]) null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyShortBufferToShortArray()
    {
        int length = 16;
        
        ShortBuffer src = ByteBuffer.allocateDirect(length * Short.BYTES).asShortBuffer();
        short[]     dst = new short[length];
        
        for (int i = 0; i < length; i++) src.put(i, (short) i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyIntBufferToIntArray()
    {
        int length = 16;
        
        IntBuffer src = ByteBuffer.allocateDirect(length * Integer.BYTES).asIntBuffer();
        int[]     dst = new int[length];
        
        for (int i = 0; i < length; i++) src.put(i, i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy((IntBuffer) null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyLongBufferToLongArray()
    {
        int length = 16;
        
        LongBuffer src = ByteBuffer.allocateDirect(length * Long.BYTES).asLongBuffer();
        long[]     dst = new long[length];
        
        for (int i = 0; i < length; i++) src.put(i, i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyFloatBufferToFloatArray()
    {
        int length = 16;
        
        FloatBuffer src = ByteBuffer.allocateDirect(length * Float.BYTES).asFloatBuffer();
        float[]     dst = new float[length];
        
        for (int i = 0; i < length; i++) src.put(i, (float) i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
    
    @Test
    void memCopyDoubleBufferToDoubleArray()
    {
        int length = 16;
        
        DoubleBuffer src = ByteBuffer.allocateDirect(length * Double.BYTES).asDoubleBuffer();
        double[]     dst = new double[length];
        
        for (int i = 0; i < length; i++) src.put(i, i);
        
        MemUtil.memCopy(src, dst);
        
        for (int i = 0; i < length; i++) Assertions.assertEquals(src.get(i), dst[i]);
        
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, -1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, -1, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 0, -1));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(null, 0, dst, 0, length));
        Assertions.assertThrows(NullPointerException.class, () -> MemUtil.memCopy(src, 0, null, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 1, dst, 0, length));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> MemUtil.memCopy(src, 0, dst, 1, length));
    }
}