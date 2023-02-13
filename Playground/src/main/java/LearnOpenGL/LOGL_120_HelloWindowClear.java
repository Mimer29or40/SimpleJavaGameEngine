package LearnOpenGL;

import engine.Engine;
import engine.gl.Framebuffer;
import engine.gl.ScreenBuffer;

import static engine.IO.windowTitle;
import static engine.Renderer.clearColor;
import static engine.Renderer.clearBuffers;

public class LOGL_120_HelloWindowClear extends Engine
{
    protected LOGL_120_HelloWindowClear()
    {
        super(640, 400);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.2 - Hello Window Clear");
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        Framebuffer.bind(Framebuffer.NULL);
        
        clearColor(0.2, 0.3, 0.3, 1.0);
        clearBuffers(ScreenBuffer.COLOR);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_120_HelloWindowClear();
        
        start(instance);
    }
}
