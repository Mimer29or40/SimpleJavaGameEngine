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

public class LOGL_125_HelloTriangleExercise3 extends Engine
{
    Program[] program = new Program[2];
    
    VertexArray[] VAO = new VertexArray[2];
    
    protected LOGL_125_HelloTriangleExercise3()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.2.5 - Hello Triangle Exercise 3");
        
        String vertCode = """
                          #version 440 core
                          layout (location = 0) in vec3 aPos;
                          void main()
                          {
                              gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                          }
                          """;
        String fragCode0 = """
                           #version 440 core
                           out vec4 FragColor;
                           void main()
                           {
                               FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
                           }
                           """;
        String fragCode1 = """
                           #version 440 core
                           out vec4 FragColor;
                           void main()
                           {
                               FragColor = vec4(1.0f, 1.0f, 0.0f, 1.0f);
                           }
                           """;
        
        Shader vertShader  = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader0 = new Shader(ShaderType.FRAGMENT, fragCode0);
        Shader fragShader1 = new Shader(ShaderType.FRAGMENT, fragCode1);
        
        program[0] = new Program(vertShader, fragShader0);
        program[1] = new Program(vertShader, fragShader1);
        
        vertShader.delete();
        fragShader0.delete();
        fragShader1.delete();
        
        float[] firstTriangle = {
                -0.9f, -0.5f, 0.0f,  // left
                -0.0f, -0.5f, 0.0f,  // right
                -0.45f, 0.5f, 0.0f,  // top
        };
        float[] secondTriangle = {
                0.0f, -0.5f, 0.0f,  // left
                0.9f, -0.5f, 0.0f,  // right
                0.45f, 0.5f, 0.0f   // top
        };
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer firstBuffer  = stack.floats(firstTriangle);
            FloatBuffer secondBuffer = stack.floats(secondTriangle);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            
            VAO[0] = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, firstBuffer, position).build();
            VAO[1] = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, secondBuffer, position).build();
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
        
        Program.bind(program[0]);
        VAO[0].draw(DrawMode.TRIANGLES, 3);
        
        Program.bind(program[1]);
        VAO[1].draw(DrawMode.TRIANGLES, 3);
    }
    
    @Override
    protected void destroy()
    {
        VAO[0].delete();
        VAO[1].delete();
        
        program[0].delete();
        program[1].delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_125_HelloTriangleExercise3();
        
        start(instance);
    }
}
