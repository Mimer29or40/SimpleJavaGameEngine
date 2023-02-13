package engine.gl.vertex;

import engine.gl.GLType;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferArray;
import engine.gl.buffer.BufferElementArray;
import engine.gl.buffer.BufferUsage;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.CustomBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VertexArray
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static void bind(@NotNull VertexArray vertexArray)
    {
        VertexArray.LOGGER.trace("Binding:", vertexArray);
        
        GL44.glBindVertexArray(vertexArray.id());
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    protected BufferElementArray indexBuffer;
    
    protected final List<BufferArray> buffers = new ArrayList<>();
    
    protected final List<VertexAttribute> attributes = new ArrayList<>();
    
    protected int vertexCount;
    
    private VertexArray()
    {
        this.id = 0;
        
        this.indexBuffer = BufferElementArray.NULL;
    }
    
    private VertexArray(@NotNull BufferElementArray indexBuffer, @NotNull List<BufferArray> buffers, List<VertexAttribute[]> vertexAttributes)
    {
        this.id = GL44.glGenVertexArrays();
        
        bind(this);
        
        this.indexBuffer = indexBuffer;
        Buffer.bind(this.indexBuffer);
        
        for (int i = 0, n = buffers.size(); i < n; i++)
        {
            BufferArray       buffer     = buffers.get(i);
            VertexAttribute[] attributes = vertexAttributes.get(i);
            
            int stride = 0;
            for (VertexAttribute attribute : attributes)
            {
                stride += attribute.size();
            }
            
            VertexArray.LOGGER.trace("Adding VBO %s of structure %s to", buffer, attributes, this);
            
            this.vertexCount = Math.min(this.vertexCount > 0 ? this.vertexCount : Integer.MAX_VALUE, (int) (buffer.size() / stride));
            
            Buffer.bind(buffer);
            this.buffers.add(buffer);
            for (int j = 0, m = attributes.length, attributeCount = attributeCount(), offset = 0; j < m; j++)
            {
                VertexAttribute attribute = attributes[j];
                
                GL44.glVertexAttribPointer(attributeCount, attribute.count(), attribute.type().ref, attribute.normalized(), stride, offset);
                GL44.glEnableVertexAttribArray(attributeCount++);
                offset += attribute.size();
                
                this.attributes.add(attribute);
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
               this.attributes +
               ", vertexCount=" +
               this.vertexCount +
               (indexCount() > 0 ? ", indexCount=" + indexCount() : "") +
               '}';
    }
    
    // -------------------- Properties -------------------- //
    
    public int id()
    {
        return this.id;
    }
    
    public int attributeCount()
    {
        return this.attributes.size();
    }
    
    public int vertexSize()
    {
        int size = 0;
        for (VertexAttribute attribute : this.attributes) size += attribute.size();
        return size;
    }
    
    public int vertexCount()
    {
        return this.vertexCount;
    }
    
    public int indexCount()
    {
        return this.indexBuffer != null ? (int) this.indexBuffer.elementCount() : 0;
    }
    
    public @NotNull BufferElementArray indexBuffer()
    {
        return this.indexBuffer;
    }
    
    public @NotNull BufferArray buffer(int index)
    {
        return this.buffers.get(index);
    }
    
    // -------------------- Functions -------------------- //
    
    public void delete()
    {
        VertexArray.LOGGER.debug("Deleting", this);
    
        if (this.indexBuffer != BufferElementArray.NULL) this.indexBuffer.delete();
        this.indexBuffer = null;
    
        for (Buffer vbo : this.buffers) vbo.delete();
        this.buffers.clear();
        
        this.attributes.clear();
        this.vertexCount = 0;
        
        GL44.glDeleteVertexArrays(this.id);
        
        this.id = 0;
    }
    
    // -------------------- Draw Functions -------------------- //
    
    public VertexArray draw(@NotNull DrawMode mode, int offset, int count)
    {
        bind(this);
        
        VertexArray.LOGGER.trace("Drawing Arrays size=%s from %s", count, this);
        
        GL44.glDrawArrays(mode.ref, offset, count);
        // glDrawArraysInstanced(int mode, int first, int count, int primcount) // TODO
        
        return this;
    }
    
    public VertexArray draw(@NotNull DrawMode mode, int count)
    {
        return draw(mode, 0, count);
    }
    
    public VertexArray draw(@NotNull DrawMode mode)
    {
        return draw(mode, 0, this.vertexCount);
    }
    
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
    
    public VertexArray drawElements(@NotNull DrawMode mode, int count)
    {
        return drawElements(mode, 0L, count);
    }
    
    public VertexArray drawElements(@NotNull DrawMode mode)
    {
        return drawElements(mode, 0L, indexCount());
    }
    
    // -------------------- Builder -------------------- //
    
    private static final Builder BUILDER = new Builder();
    
    public static Builder builder()
    {
        return VertexArray.BUILDER.reset();
    }
    
    public static final class Builder
    {
        private       BufferElementArray      indexBuffer;
        private final List<BufferArray>       buffers    = new ArrayList<>();
        private final List<VertexAttribute[]> attributes = new ArrayList<>();
        
        private Builder reset()
        {
            this.indexBuffer = BufferElementArray.NULL;
            this.buffers.clear();
            this.attributes.clear();
            return this;
        }
        
        public Builder buffer(@NotNull BufferArray buffer, VertexAttribute @NotNull ... attributes)
        {
            this.buffers.add(buffer);
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, int count, VertexAttribute @NotNull ... attributes)
        {
            int attributeSize = 0;
            for (VertexAttribute attribute : attributes) attributeSize += attribute.size();
            long size = Integer.toUnsignedLong(attributeSize * count);
            this.buffers.add(new BufferArray(usage, size));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, @NotNull java.nio.Buffer data, VertexAttribute @NotNull ... attributes)
        {
            this.buffers.add(new BufferArray(usage, data));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull BufferUsage usage, @NotNull CustomBuffer<?> data, VertexAttribute @NotNull ... attributes)
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
    
    
    public static final VertexArray NULL = new VertexArray()
    {
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
    };
}
