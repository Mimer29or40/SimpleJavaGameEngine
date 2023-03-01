package engine.gl.vertex;

import engine.gl.GLType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VertexAttribute(@NotNull GLType type, int count, boolean normalized, @Nullable Integer divisor)
{
    public VertexAttribute(@NotNull GLType type, int count, boolean normalized)
    {
        this(type, count, normalized, null);
    }
    
    public VertexAttribute(@NotNull GLType type, int count)
    {
        this(type, count, false, null);
    }
    
    public VertexAttribute(@NotNull GLType type, boolean normalized)
    {
        this(type, 1, normalized, null);
    }
    
    public VertexAttribute(@NotNull GLType type)
    {
        this(type, 1, false, null);
    }
    
    @Override
    public @NotNull String toString()
    {
        return this.type + "x" + this.count;
    }
    
    public int size()
    {
        return this.type.bytes * this.count;
    }
}
