package engine;

import engine.util.Logger;

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
        Logger.LEVEL = Logger.Level.TRACE;
        
        Engine instance = new EngineDemo();
        start(instance);
    }
}
