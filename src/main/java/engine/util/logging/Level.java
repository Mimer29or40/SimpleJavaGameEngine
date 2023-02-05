package engine.util.logging;

public enum Level
{
    OFF(Integer.MAX_VALUE),
    SEVERE(600),
    WARNING(500),
    INFO(400),
    DEBUG(300),
    TRACE(100),
    ALL(Integer.MIN_VALUE),
    ;
    
    public final int value;
    
    Level(int value)
    {
        this.value = value;
    }
}
