package LearnOpenGL;

import engine.Engine;
import engine.gl.Framebuffer;
import engine.gl.GLType;
import engine.gl.ScreenBuffer;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.windowTitle;
import static engine.Renderer.clearColor;
import static engine.Renderer.clearBuffers;

public class LOGL_210_HelloTriangle extends Engine
{
    Shader  vertShader;
    Shader  fragShader;
    Program program;
    
    VertexArray VAO;
    
    protected LOGL_210_HelloTriangle()
    {
        super(640, 400);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.2 - Hello Window Clear");
        
        String vertCode = """
                          #version 440 core
                          layout (location = 0) in vec3 aPos;
                          void main()
                          {
                              gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                          }
                          """;
        String fragCode = """
                          #version 440 core
                          out vec4 FragColor;
                          void main()
                          {
                              FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
                          }
                          """;
        
        vertShader = new Shader(ShaderType.VERTEX, vertCode);
        fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        program = Program.builder().shader(vertShader).shader(fragShader).build();
        
        float[] vertices = {
                -0.5f, -0.5f, +0.0f, // Vertex 0
                +0.5f, -0.5f, +0.0f, // Vertex 1
                +0.0f, +0.5f, +0.0f  // Vertex 2
        };
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats(vertices);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, new VertexAttribute(GLType.FLOAT, 3, false)).build();
        }
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
        
        Program.bind(program);
        VertexArray.bind(VAO); // seeing as we only have a single VAO there's no need to bind it every time, but we'll do so to keep things a bit more organized
        VAO.draw(DrawMode.TRIANGLES);
    }
    
    @Override
    protected void destroy()
    {
        VAO.delete();
        
        program.delete();
        
        vertShader.delete();
        fragShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_210_HelloTriangle();
        
        start(instance);
    }
}
