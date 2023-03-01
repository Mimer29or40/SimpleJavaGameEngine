package engine;

import engine.renderer.TextRenderer;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    private static TextRenderer textRenderer;
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
        
        Renderer.textRenderer = new TextRenderer();
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
    
        Renderer.textRenderer.delete();
    }
    
    static void beforeDraw()
    {
    }
    
    static void afterDraw()
    {
    }
    
    public static @NotNull TextRenderer textRenderer()
    {
        return Renderer.textRenderer;
    }
    
    private Renderer() {}
}
