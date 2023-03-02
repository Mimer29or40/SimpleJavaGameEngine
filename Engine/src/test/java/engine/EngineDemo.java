package engine;

import engine.gl.GL;
import engine.util.Logger;

public class EngineDemo extends Engine
{
    protected EngineDemo()
    {
        super("Engine Demo", 640, 400);
        
        this.updateFreq = 120;
        this.drawFreq   = 60;
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
        //GL.clearColor(0.2, 0.8, 0.4, 1.0);
        GL.clearBuffers();
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        
        Engine instance = new EngineDemo();
        start(instance);
    }
}
