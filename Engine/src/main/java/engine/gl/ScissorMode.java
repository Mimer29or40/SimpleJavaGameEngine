package engine.gl;

public record ScissorMode(int x, int y, int width, int height)
{
    public static final ScissorMode NONE = new ScissorMode(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    public static final ScissorMode DEFAULT = NONE;
    
    public boolean test(int x, int y)
    {
        return this.x <= x && x < this.x + this.width && this.y <= y && y < this.y + this.height;
    }
}
