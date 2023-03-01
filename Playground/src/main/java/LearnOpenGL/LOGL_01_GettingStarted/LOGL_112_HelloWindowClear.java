package LearnOpenGL.LOGL_01_GettingStarted;

import engine.Engine;
import engine.Key;
import engine.gl.GL;
import engine.gl.ScreenBuffer;

import static engine.IO.keyboardKeyDown;
import static engine.IO.windowTitle;

public class LOGL_112_HelloWindowClear extends Engine
{
    protected LOGL_112_HelloWindowClear()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.1.2 - Hello Window Clear");
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.2, 0.3, 0.3, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_112_HelloWindowClear();
        
        start(instance);
    }
}
