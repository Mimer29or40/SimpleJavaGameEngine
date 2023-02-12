package engine;

import engine.util.Logger;
import org.lwjgl.opengl.GL44;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    static float[] vertices;
    static int VBO;
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
    
        vertices = new float[] {
                -0.5f, -0.5f, +0.0f, // Vertex 0
                +0.5f, -0.5f, +0.0f, // Vertex 1
                +0.0f, +0.5f, +0.0f  // Vertex 2
        };
        VBO = GL44.glGenBuffers();
        GL44.glBindBuffer(GL44.GL_ARRAY_BUFFER, VBO);
        GL44.glBufferData(GL44.GL_ARRAY_BUFFER, vertices, GL44.GL_STATIC_DRAW);
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
