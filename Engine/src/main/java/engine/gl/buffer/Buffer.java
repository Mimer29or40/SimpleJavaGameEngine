package engine.gl.buffer;

import engine.util.Logger;
import engine.util.MemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Objects;

import static engine.Renderer.bind;

public abstract class Buffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    public final int         type;
    public final BufferUsage usage;
    
    protected long size;
    
    protected ByteBuffer mapped;
    
    protected Buffer(int id, int type, @NotNull BufferUsage usage, long size)
    {
        this.id = id;
        
        this.type  = type;
        this.usage = usage;
        
        this.size = size;
    }
    
    protected Buffer(int type, @NotNull BufferUsage usage, long address, long size)
    {
        this(GL44.glGenBuffers(), type, usage, size);
        
        bind(this);
    
        Buffer.LOGGER.trace("Uploaded Data with usage:", usage);
        GL44.nglBufferData(this.type, this.size, address, usage.ref);
        
        Buffer.LOGGER.debug("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Buffer that = (Buffer) o;
        return this.id > 0 && this.id == that.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", size=" + this.size + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * @return The object id.
     */
    public int id()
    {
        return this.id;
    }
    
    /**
     * @return The size in bytes of the buffer.
     */
    public long size()
    {
        return this.size;
    }
    
    // -------------------- Functions -------------------- //
    
    /**
     * Deletes the contents of the buffer and free's its memory.
     */
    public void delete()
    {
        Buffer.LOGGER.debug("Deleting", this);
        
        GL44.glDeleteBuffers(this.id);
        
        this.id   = -1;
        this.size = 0;
    }
    
    public @Nullable ByteBuffer map(@NotNull BufferAccess access)
    {
        bind(this);
        
        Buffer.LOGGER.trace("Mapping %s as %s", this, access);
        
        return this.mapped = GL44.glMapBuffer(this.type, access.ref, this.size, this.mapped);
    }
    
    public void unmap()
    {
        bind(this);
        
        Buffer.LOGGER.trace("Unmapping %s", this);
        
        if (!GL44.glUnmapBuffer(this.type)) Buffer.LOGGER.warning("Could not unmap", this);
    }
    
    /**
     * Gets the data in the buffer.
     *
     * @param offset The offset into the buffer.
     * @param buffer The destination buffer.
     *
     * @return The data in the buffer.
     */
    public Buffer get(long offset, @NotNull java.nio.Buffer buffer)
    {
        bind(this);
        
        Buffer.LOGGER.trace("Getting Contents of", this);
        
        GL44.nglGetBufferSubData(this.type, offset, Integer.toUnsignedLong(buffer.remaining() * MemUtil.elementSize(buffer)), MemoryUtil.memAddress(buffer));
        return this;
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param buffer The destination buffer.
     *
     * @return The data in the buffer.
     */
    public Buffer get(@NotNull java.nio.Buffer buffer)
    {
        return get(0, buffer);
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param buffer The destination buffer.
     *
     * @return The data in the buffer.
     */
    public Buffer get(long offset, @NotNull CustomBuffer<?> buffer)
    {
        bind(this);
        
        Buffer.LOGGER.trace("Getting Contents of", this);
        
        GL44.nglGetBufferSubData(this.type, offset, Integer.toUnsignedLong(buffer.remaining() * buffer.sizeof()), MemoryUtil.memAddress(buffer));
        return this;
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param buffer The destination buffer.
     *
     * @return The data in the buffer.
     */
    public Buffer get(@NotNull CustomBuffer<?> buffer)
    {
        return get(0, buffer);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param data   The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(long offset, @NotNull java.nio.Buffer data)
    {
        bind(this);
        
        Buffer.LOGGER.trace("Setting Contents of", this);
        
        GL44.nglBufferSubData(this.type, offset, Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)), MemoryUtil.memAddress(data));
        return this;
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(@NotNull java.nio.Buffer data)
    {
        return set(0, data);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param data   The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(long offset, @NotNull CustomBuffer<?> data)
    {
        bind(this);
        
        Buffer.LOGGER.trace("Setting Contents of", this);
        
        GL44.nglBufferSubData(this.type, offset, Integer.toUnsignedLong(data.remaining() * data.sizeof()), MemoryUtil.memAddress(data));
        return this;
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(@NotNull CustomBuffer<?> data)
    {
        return set(0, data);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(byte... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.bytes(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(short... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.shorts(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(int... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.ints(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(long... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.longs(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(float... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.floats(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     *
     * @return This instance for call chaining.
     */
    public Buffer set(double... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.doubles(data));
        }
    }
}
