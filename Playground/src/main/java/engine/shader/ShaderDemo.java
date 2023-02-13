package engine.shader;

import engine.Engine;
import engine.util.IOUtil;
import engine.util.Logger;

public class ShaderDemo extends Engine
{
    final ShaderToy shaderToy;
    
    protected ShaderDemo()
    {
        super(640, 400);
        
        this.shaderToy = new ShaderToy(IOUtil.getPath("shader_toy/shader.frag"));
    }
    
    @Override
    protected void setup()
    {
        this.shaderToy.setup();
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        this.shaderToy.update(frame, time, deltaTime);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        this.shaderToy.draw(frame, time, deltaTime);
    }
    
    @Override
    protected void destroy()
    {
        this.shaderToy.destroy();
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        Engine instance = new ShaderDemo();
        
        start(instance);
    }
}
