package engine.gl.vertex;

import engine.gl.GLType;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferUsage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.CustomBuffer;
import engine.gl.buffer.BufferArray;
import engine.gl.buffer.BufferElementArray;
import engine.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static engine.Renderer.bind;

public class VertexArray
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final VertexArray NULL = new Null();
    
    // -------------------- Creation -------------------- //
    
    private static final Builder BUILDER = new Builder();
    
    public static Builder builder()
    {
        return VertexArray.BUILDER.reset();
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    public final BufferElementArray indexBuffer;
    
    protected final List<BufferArray> _buffers = new ArrayList<>();
    public final    List<BufferArray> buffers  = Collections.unmodifiableList(this._buffers);
    
    protected final List<Attribute> _attributes = new ArrayList<>();
    public final    List<Attribute> attributes  = Collections.unmodifiableList(this._attributes);
    
    protected int vertexCount;
    
    private VertexArray()
    {
        this.id = 0;
        
        this.indexBuffer = BufferElementArray.NULL;
    }
    
    private VertexArray(@NotNull BufferElementArray indexBuffer, @NotNull List<BufferArray> buffers, List<Attribute[]> vertexAttributes)
    {
        this.id = GL44.glGenVertexArrays();
        
        bind(this);
        
        this.indexBuffer = indexBuffer;
        bind(this.indexBuffer);
        
        for (int i = 0, n = buffers.size(); i < n; i++)
        {
            BufferArray buffer     = buffers.get(i);
            Attribute[] attributes = vertexAttributes.get(i);
            
            int stride = 0;
            for (Attribute attribute : attributes)
            {
                stride += attribute.size();
            }
            
            VertexArray.LOGGER.trace("Adding VBO %s of structure %s to", buffer, attributes, this);
            
            this.vertexCount = Math.min(this.vertexCount > 0 ? this.vertexCount : Integer.MAX_VALUE, (int) (buffer.size() / stride));
            
            bind(buffer);
            this._buffers.add(buffer);
            for (int j = 0, m = attributes.length, attributeCount = attributeCount(), offset = 0; j < m; j++)
            {
                Attribute attribute = attributes[j];
                
                GL44.glVertexAttribPointer(attributeCount, attribute.count(), attribute.type().ref, attribute.normalized(), stride, offset);
                GL44.glEnableVertexAttribArray(attributeCount++);
                offset += attribute.size();
                
                this._attributes.add(attribute);
            }
        }
        
        VertexArray.LOGGER.debug("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexArray that = (VertexArray) o;
        return this.id == that.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return "VertexArray{" +
               "id=" +
               this.id +
               ", vertex=" +
               this._attributes +
               ", vertexCount=" +
               this.vertexCount +
               (indexCount() > 0 ? ", indexCount=" + indexCount() : "") +
               '}';
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * @return The count of elements in a vertex.
     */
    public int attributeCount()
    {
        return this._attributes.size();
    }
    
    /**
     * @return The size in bytes of a vertex.
     */
    public int vertexSize()
    {
        int size = 0;
        for (Attribute attribute : this._attributes) size += attribute.size();
        return size;
    }
    
    /**
     * @return The number of vertices in the vertex array.
     */
    public int vertexCount()
    {
        return this.vertexCount;
    }
    
    /**
     * @return The number of indices in the vertex array.
     */
    public int indexCount()
    {
        return this.indexBuffer != null ? (int) this.indexBuffer.elementCount() : 0;
    }
    
    /**
     * @return The object id.
     */
    public int id()
    {
        return this.id;
    }
    
    // -------------------- Functions -------------------- //
    
    /**
     * Deletes the VertexArray and Buffers.
     */
    public void delete()
    {
        VertexArray.LOGGER.debug("Deleting", this);
        
        for (Buffer vbo : this._buffers) vbo.delete();
        this._buffers.clear();
        if (this.indexBuffer != null) this.indexBuffer.delete();
        
        this._attributes.clear();
        this.vertexCount = 0;
        
        GL44.glDeleteVertexArrays(this.id);
        
        this.id = 0;
    }
    
    // -------------------- Draw Functions -------------------- //
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     *
     * @param mode   The primitive type.
     * @param offset The offset into the array.
     * @param count  the number of vertices to draw.
     *
     * @return This instance for call chaining.
     */
    public VertexArray draw(@NotNull DrawMode mode, int offset, int count)
    {
        bind(this);
        
        VertexArray.LOGGER.trace("Drawing Arrays size=%s from %s", count, this);
        
        GL44.glDrawArrays(mode.ref, offset, count);
        // glDrawArraysInstanced(int mode, int first, int count, int primcount) // TODO
        
        return this;
    }
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     *
     * @param mode  The primitive type.
     * @param count The size of the buffer to draw.
     *
     * @return This instance for call chaining.
     */
    public VertexArray draw(@NotNull DrawMode mode, int count)
    {
        return draw(mode, 0, count);
    }
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     *
     * @param mode The primitive type.
     *
     * @return This instance for call chaining.
     */
    public VertexArray draw(@NotNull DrawMode mode)
    {
        return draw(mode, 0, this.vertexCount);
    }
    
    /**
     * Draws the array with elements in the specified mode.
     *
     * @param mode   The primitive type.
     * @param offset The offset into the array.
     * @param count  the number of vertices to draw.
     *
     * @return This instance for call chaining.
     */
    public VertexArray drawElements(@NotNull DrawMode mode, long offset, int count)
    {
        bind(this);
        
        if (this.indexBuffer == null) throw new IllegalStateException("Cannot draw elements when non are provided.");
        
        VertexArray.LOGGER.trace("Drawing Elements size=%s from %s", count, this);
        
        GLType indexType = this.indexBuffer.indexType;
        
        GL44.glDrawElements(mode.ref, count, indexType.ref, offset * indexType.bytes);
        // GL44.glDrawElementsInstanced(int mode, int count, int type, long indices, int primcount); // TODO
        
        return this;
    }
    
    /**
     * Draws the array with elements in the specified mode.
     *
     * @param mode  The primitive type.
     * @param count the number of vertices to draw.
     *
     * @return This instance for call chaining.
     */
    public VertexArray drawElements(@NotNull DrawMode mode, int count)
    {
        return drawElements(mode, 0L, count);
    }
    
    /**
     * Draws the array with elements in the specified mode.
     *
     * @param mode The primitive type.
     *
     * @return This instance for call chaining.
     */
    public VertexArray drawElements(@NotNull DrawMode mode)
    {
        return drawElements(mode, 0L, indexCount());
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public static final class Builder
    {
        private       BufferElementArray indexBuffer;
        private final List<BufferArray>  buffers    = new ArrayList<>();
        private final List<Attribute[]>  attributes = new ArrayList<>();
        
        private Builder reset()
        {
            this.indexBuffer = BufferElementArray.NULL;
            this.buffers.clear();
            this.attributes.clear();
            return this;
        }
        
        public Builder buffer(@NotNull BufferArray buffer, Attribute @NotNull ... attributes)
        {
            this.buffers.add(buffer);
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, int count, Attribute @NotNull ... attributes)
        {
            int attributeSize = 0;
            for (Attribute attribute : attributes) attributeSize += attribute.size();
            long size = Integer.toUnsignedLong(attributeSize * count);
            this.buffers.add(new BufferArray(usage, size));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data, Attribute @NotNull ... attributes)
        {
            this.buffers.add(new BufferArray(usage, data));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data, Attribute @NotNull ... attributes)
        {
            this.buffers.add(new BufferArray(usage, data));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder indexBuffer(@NotNull BufferUsage usage, int indexCount, @NotNull GLType indexType)
        {
            this.indexBuffer = new BufferElementArray(usage, indexCount, indexType);
            return this;
        }
        
        public Builder indexBuffer(int indexCount, @NotNull BufferUsage usage)
        {
            this.indexBuffer = new BufferElementArray(usage, indexCount, GLType.UNSIGNED_INT);
            return this;
        }
        
        public Builder indexBuffer(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data, @NotNull GLType indexType)
        {
            this.indexBuffer = new BufferElementArray(usage, data, indexType);
            return this;
        }
        
        public Builder indexBuffer(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data)
        {
            this.indexBuffer = new BufferElementArray(usage, data, GLType.UNSIGNED_INT);
            return this;
        }
        
        public Builder indexBuffer(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data, @NotNull GLType indexType)
        {
            this.indexBuffer = new BufferElementArray(usage, data, indexType);
            return this;
        }
        
        public Builder indexBuffer(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data)
        {
            this.indexBuffer = new BufferElementArray(usage, data, GLType.UNSIGNED_INT);
            return this;
        }
        
        public @NotNull VertexArray build()
        {
            return new VertexArray(this.indexBuffer, this.buffers, this.attributes);
        }
    }
    
    private static final class Null extends VertexArray
    {
        @Contract(pure = true)
        @Override
        public @NotNull String toString()
        {
            return "VertexArray.NULL";
        }
        
        @Override
        public void delete()
        {
            VertexArray.LOGGER.warning("Cannot call %s.delete", this);
        }
        
        @Override
        public VertexArray draw(@NotNull DrawMode mode, int offset, int count)
        {
            VertexArray.LOGGER.warning("Cannot call %s.draw", this);
            return this;
        }
        
        @Override
        public VertexArray drawElements(@NotNull DrawMode mode, long offset, int count)
        {
            VertexArray.LOGGER.warning("Cannot call %s.drawElements", this);
            return this;
        }
    }
}
