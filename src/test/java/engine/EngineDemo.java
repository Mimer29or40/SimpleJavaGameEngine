package engine;

import engine.util.logging.Level;
import engine.util.logging.Logger;

public class EngineDemo extends Engine
{
    protected EngineDemo()
    {
        super(640, 400);
    }
    
    @Override
    protected void setup()
    {
    
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Logger.GLOBAL_LEVEL = Level.DEBUG;
        
        Engine instance = new EngineDemo();
        start(instance);
    }
}
