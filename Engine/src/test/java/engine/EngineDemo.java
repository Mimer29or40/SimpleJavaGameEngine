package engine;

import engine.color.Color;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.renderer.LineRenderer;
import engine.renderer.TextRenderer;
import engine.util.Logger;

import static engine.IO.mousePos;

public class EngineDemo extends Engine
{
    TextRenderer textRenderer;
    LineRenderer lineRenderer;
    
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
        this.lineRenderer = new LineRenderer();
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
        this.textRenderer.color.set(Color.DARK_GREEN);
        this.textRenderer.size = mousePos().x();
        this.textRenderer.drawText("ABCDEF\nGHIJK\nLMNOP\nQRSTU\nVWXYZ", 10, 10);
        
        this.lineRenderer.drawLines(10, 10, 10, 20, 20, 20, 20, 10);
    }
    
    @Override
    protected void destroy()
    {
        this.textRenderer.delete();
        this.lineRenderer.delete();
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        
        Engine instance = new EngineDemo();
        start(instance);
    }
}
