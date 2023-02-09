package CodingTrain;

import engine.Engine;
import static engine.IO.*;

public class CT_000_Template extends Engine
{
    protected CT_000_Template()
    {
        super(640, 400);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("Coding Train - 000 - Template");
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
        Engine instance = new CT_000_Template();
        
        start(instance);
    }
}
