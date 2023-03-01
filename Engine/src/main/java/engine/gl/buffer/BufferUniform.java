package engine.gl.buffer;

import engine.util.Logger;
import engine.util.MemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL44.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL44.glBindBufferBase;

public class BufferUniform extends Buffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    private BufferUniform()
    {
        super(0, GL_UNIFORM_BUFFER, BufferUsage.STATIC_READ, 0);
    }
    
    public BufferUniform(@NotNull BufferUsage usage, long size)
    {
        super(GL_UNIFORM_BUFFER, usage, MemoryUtil.NULL, size);
    }
    
    public BufferUniform(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data)
    {
        super(GL_UNIFORM_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)));
    }
    
    public BufferUniform(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data)
    {
        super(GL_UNIFORM_BUFFER, usage, MemoryUtil.memAddress(data), Integer.toUnsignedLong(data.remaining() * data.sizeof()));
    }
    
    public @NotNull BufferUniform base(int index)
    {
        bind(this);
        
        BufferUniform.LOGGER.trace("%s: Binding to Base: %s", this, index);
        
        glBindBufferBase(this.type, index, this.id);
        
        return this;
    }
    
    public @NotNull BufferUniform range(int index, int offset, long size)
    {
        bind(this);
    
        BufferUniform.LOGGER.trace("%s: Binding to Range: index=%s offset=%s size=%s", this, index, offset, size);
    
        glBindBufferRange(this.type, index, this.id, offset, size);
    
        return this;
    }
    
    public @NotNull BufferUniform range(int index, int size)
    {
        return range(index, 0, size);
    }
    
    public @NotNull BufferUniform range(int index)
    {
        return range(index, 0, this.size);
    }
    
    public static final BufferUniform NULL = new BufferUniform()
    {
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
        public @NotNull BufferUniform base(int index)
        {
            BufferUniform.LOGGER.warning("Cannot call %s.base", this);
            return this;
        }
    };
}
