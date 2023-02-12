package engine.gl.buffer;

import engine.util.Logger;
import engine.util.MemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static engine.Renderer.bind;

public class BufferUniform extends Buffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final BufferUniform NULL = new Null();
    
    // -------------------- Instance -------------------- //
    
    private BufferUniform()
    {
        super(0, GL44.GL_UNIFORM_BUFFER, BufferUsage.STATIC_READ, 0);
    }
    
    public BufferUniform(@NotNull BufferUsage usage, long size)
    {
        super(GL44.GL_UNIFORM_BUFFER, usage, MemoryUtil.NULL, size);
    }
    
    public BufferUniform(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data)
    {
        super(GL44.GL_UNIFORM_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)));
    }
    
    public BufferUniform(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data)
    {
        super(GL44.GL_UNIFORM_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * data.sizeof()));
    }
    
    /**
     * Binds the buffer for reading/writing.
     * <p>
     * Make sure to bind the buffer.
     *
     * @return This instance for call chaining.
     */
    public BufferUniform base(int index)
    {
        bind(this);
        
        BufferUniform.LOGGER.trace("%s: Binding to Base: %s", this, index);
        
        GL44.glBindBufferBase(this.type, index, this.id);
        
        return this;
    }
    
    private static final class Null extends BufferUniform
    {
        @Contract(pure = true)
        @Override
        public @NotNull String toString()
        {
            return "BufferUniform.NULL";
        }
        
        @Override
        public void delete()
        {
            BufferUniform.LOGGER.warning("Cannot call %s.get", this);
        }
        
        @Override
        public @Nullable ByteBuffer map(@NotNull BufferAccess access)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.map", this);
            return null;
        }
        
        @Override
        public void unmap()
        {
            BufferUniform.LOGGER.warning("Cannot call %s.unmap", this);
        }
        
        @Override
        public Buffer get(long offset, @NotNull java.nio.Buffer buffer)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer get(long offset, @NotNull CustomBuffer<?> buffer)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.get", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull java.nio.Buffer data)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
        
        @Override
        public Buffer set(long offset, @NotNull CustomBuffer<?> data)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.set", this);
            return this;
        }
        
        @Override
        public BufferUniform base(int index)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.base", this);
            return this;
        }
    }
}
