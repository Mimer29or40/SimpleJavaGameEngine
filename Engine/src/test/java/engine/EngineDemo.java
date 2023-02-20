package engine;

import engine.renderer.TextRenderer;
import engine.util.Logger;

import static engine.IO.mousePos;

public class EngineDemo extends Engine
{
    TextRenderer textRenderer;
    
    protected EngineDemo()
    {
        super("Engine Demo", 640, 400);
        
        this.updateFreq = 120;
        this.drawFreq   = 60;
    }
    
    @Override
    protected void setup()
    {
        this.textRenderer = new TextRenderer();
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        this.textRenderer.drawText("TEXT", 50, 50);
    }
    
    @Override
    protected void destroy()
    {
        this.textRenderer.delete();
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        
        Engine instance = new EngineDemo();
        start(instance);
    }
}
