package engine.gl.buffer;

import engine.util.Logger;
import engine.util.MemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferArray extends Buffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final BufferArray NULL = new Null();
    
    // -------------------- Instance -------------------- //
    
    private BufferArray()
    {
        super(0, GL40.GL_ARRAY_BUFFER, BufferUsage.STATIC_READ, 0);
    }
    
    public BufferArray(@NotNull BufferUsage usage, long size)
    {
        super(GL40.GL_ARRAY_BUFFER, usage, MemoryUtil.NULL, size);
    }
    
    public BufferArray(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data)
    {
        super(GL40.GL_ARRAY_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)));
    }
    
    public BufferArray(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data)
    {
        super(GL40.GL_ARRAY_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * data.sizeof()));
    }
    
    private static final class Null extends BufferArray
    {
        @Contract(pure = true)
        @Override
        public @NotNull String toString()
        {
            return "BufferArray.NULL";
        }
        
        @Override
        public void delete()
        {
            BufferArray.LOGGER.warning("Cannot call %s.delete", this);
        }
        
        @Override
        public @Nullable ByteBuffer map(@NotNull BufferAccess access)
        {
            BufferArray.LOGGER.warning("Cannot call %s.map", this);
            return null;
        }
        
        @Override
        public void unmap()
        {
            BufferArray.LOGGER.warning("Cannot call %s.unmap", this);
        }
        
        @Override
        public Buffer get(long offset, @NotNull java.nio.Buffer buffer)
        {
            BufferArray.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer get(long offset, @NotNull CustomBuffer<?> buffer)
        {
            BufferArray.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull java.nio.Buffer data)
        {
            BufferArray.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull CustomBuffer<?> data)
        {
            BufferArray.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
    }
}
