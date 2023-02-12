package engine.gl.buffer;

import engine.gl.GLType;
import engine.util.Logger;
import engine.util.MemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferElementArray extends Buffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final BufferElementArray NULL = new Null();
    
    // -------------------- Instance -------------------- //
    
    public final GLType indexType;
    
    private BufferElementArray()
    {
        super(0, GL40.GL_ELEMENT_ARRAY_BUFFER, BufferUsage.STATIC_READ, 0);
        
        this.indexType = GLType.UNSIGNED_INT;
    }
    
    public BufferElementArray(@NotNull BufferUsage usage, int indexCount, @NotNull GLType indexType)
    {
        super(GL40.GL_ELEMENT_ARRAY_BUFFER, usage, MemoryUtil.NULL, Integer.toUnsignedLong(indexCount * indexType.bytes));
        
        this.indexType = indexType;
    }
    
    public BufferElementArray(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data, @NotNull GLType indexType)
    {
        super(GL40.GL_ELEMENT_ARRAY_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)));
        
        this.indexType = indexType;
    }
    
    public BufferElementArray(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data, @NotNull GLType indexType)
    {
        super(GL40.GL_ELEMENT_ARRAY_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * data.sizeof()));
        
        this.indexType = indexType;
    }
    
    /**
     * @return The number of elements in this buffer.
     */
    public long elementCount()
    {
        return this.size / this.indexType.bytes;
    }
    
    private static final class Null extends BufferElementArray
    {
        @Contract(pure = true)
        @Override
        public @NotNull String toString()
        {
            return "BufferElementArray.NULL";
        }
        
        @Override
        public void delete()
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.delete", this);
        }
        
        @Override
        public @Nullable ByteBuffer map(@NotNull BufferAccess access)
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.map", this);
            return null;
        }
        
        @Override
        public void unmap()
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.unmap", this);
        }
        
        @Override
        public Buffer get(long offset, @NotNull java.nio.Buffer buffer)
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer get(long offset, @NotNull CustomBuffer<?> buffer)
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull java.nio.Buffer data)
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull CustomBuffer<?> data)
        {
            BufferElementArray.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
    }
}
