package engine;

import engine.color.Colorc;
import engine.gl.ScreenBuffer;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;

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
    
    public static void clearColor(double r, double g, double b, double a)
    {
        Renderer.LOGGER.trace("clearColor(%s, %s, %s, %s)", r, g, b, a);
        
        GL44.glClearColor((float) r, (float) g, (float) b, (float) a);
    }
    
    public static void clearColor(@NotNull Colorc color)
    {
        clearColor(color.rf(), color.gf(), color.bf(), color.af());
    }
    
    public static void clearDepth(double depth)
    {
        Renderer.LOGGER.trace("clearDepth(%s)", depth);
        
        GL44.glClearDepth(depth);
    }
    
    public static void clearStencil(int stencil)
    {
        Renderer.LOGGER.trace("clearStencil(%s)", stencil);
        
        GL44.glClearStencil(stencil);
    }
    
    public static void clearBuffers()
    {
        Renderer.LOGGER.trace("Clearing Screen Buffers");
        
        GL44.glClear(GL44.GL_COLOR_BUFFER_BIT | GL44.GL_DEPTH_BUFFER_BIT | GL44.GL_STENCIL_BUFFER_BIT);
    }
    
    public static void clearBuffers(@NotNull ScreenBuffer buffer, @NotNull ScreenBuffer @NotNull ... others)
    {
        if (others.length == 0)
        {
            Renderer.LOGGER.trace("Clearing Screen Buffer:", buffer);
            GL44.glClear(buffer.ref);
            return;
        }
    
        Renderer.LOGGER.trace("Clearing Screen Buffer: %s %s", buffer, others);
        int mask = buffer.ref;
        for (ScreenBuffer b : others) mask |= b.ref;
        GL44.glClear(mask);
    }
    
    private Renderer() {}
}
