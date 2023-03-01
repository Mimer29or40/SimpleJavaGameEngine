package LearnOpenGL.LOGL_01_GettingStarted;

import engine.Engine;
import engine.Key;
import engine.gl.GL;
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

import static engine.IO.keyboardKeyDown;
import static engine.IO.windowTitle;

public class LOGL_131_ShadersUniform extends Engine
{
    Program program;
    
    VertexArray VAO;
    
    protected LOGL_131_ShadersUniform()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.3.1 - Shaders Uniform");
        
        String vertCode = """
                          #version 440 core
                          layout (location = 0) in vec3 aPos;
                          void main()
                          {
                              gl_Position = vec4(aPos, 1.0);
                          }
                          """;
        String fragCode = """
                          #version 440 core
                          out vec4 FragColor;
                          uniform vec4 ourColor;
                          void main()
                          {
                              FragColor = ourColor;
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        program = new Program(vertShader, fragShader);
        
        vertShader.delete();
        fragShader.delete();
        
        float[] vertices = {
                +0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                +0.0f, +0.5f, 0.0f   // top
        };
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats(vertices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, position).build();
        }
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
    
        Program.bind(program);
        
        double green = Math.sin(time) * 0.5 + 0.5;
        Program.uniformFloat4("ourColor", 0.0, green, 0.0, 1.0);
        
        VAO.draw(DrawMode.TRIANGLES, 3);
    }
    
    @Override
    protected void destroy()
    {
        VAO.delete();
        
        program.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_131_ShadersUniform();
        
        start(instance);
    }
}
