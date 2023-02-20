package engine;

import engine.util.Logger;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
    }
    
    static void beforeDraw()
    {
    }
    
    static void afterDraw()
    {
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
    }
    
    private Renderer() {}
}
