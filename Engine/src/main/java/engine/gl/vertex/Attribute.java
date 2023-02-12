package engine.gl.vertex;

import engine.gl.GLType;
import org.jetbrains.annotations.NotNull;

public record Attribute(@NotNull GLType type, int count, boolean normalized)
{
    public Attribute(@NotNull GLType type, int count)
    {
        this(type, count, false);
    }
    
    public Attribute(@NotNull GLType type, boolean normalized)
    {
        this(type, 1, normalized);
    }
    
    public Attribute(@NotNull GLType type)
    {
        this(type, 1, false);
    }
    
    @Override
    public String toString()
    {
        return this.type + "x" + this.count;
    }
    
    public int size()
    {
        return this.type.bytes * this.count;
    }
}
