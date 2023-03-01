package LearnOpenGL.LOGL_01_GettingStarted;

import engine.Engine;
import engine.Key;

import static engine.IO.keyboardKeyDown;
import static engine.IO.windowTitle;

public class LOGL_111_HelloWindow extends Engine
{
    protected LOGL_111_HelloWindow()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.1.1 - Hello Window");
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
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
        Engine instance = new LOGL_111_HelloWindow();
        
        start(instance);
    }
}
