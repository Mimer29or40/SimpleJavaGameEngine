package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Key;
import engine.gl.DepthMode;
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
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public class LOGL_4111_AntiAliasingMSAA extends Engine
{
    Program shader;
    
    VertexArray cubeVAO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    protected LOGL_4111_AntiAliasingMSAA()
    {
        super(800, 600);
    
        // TODO - Need a way to set window hints here; glfwWindowHint(GLFW_SAMPLES, 4);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.11.1 - Anti-Aliasing MSAA");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            
                            uniform mat4 model;
                            uniform mat4 view;
                            uniform mat4 projection;
                            
                            void main()
                            {
                                gl_Position = projection * view * model * vec4(aPos, 1.0);
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            void main()
                            {
                                FragColor = vec4(0.0, 1.0, 0.0, 1.0);
                            }
                            """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        
        shader = new Program(shader_vs, shader_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        
        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
        //@formatter:off
        float[] cubeVertices = {
                // positions
                -0.5f, -0.5f, -0.5f,
                +0.5f, -0.5f, -0.5f,
                +0.5f, +0.5f, -0.5f,
                +0.5f, +0.5f, -0.5f,
                -0.5f, +0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
            
                -0.5f, -0.5f, +0.5f,
                +0.5f, -0.5f, +0.5f,
                +0.5f, +0.5f, +0.5f,
                +0.5f, +0.5f, +0.5f,
                -0.5f, +0.5f, +0.5f,
                -0.5f, -0.5f, +0.5f,
            
                -0.5f, +0.5f, +0.5f,
                -0.5f, +0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, +0.5f,
                -0.5f, +0.5f, +0.5f,
            
                +0.5f, +0.5f, +0.5f,
                +0.5f, +0.5f, -0.5f,
                +0.5f, -0.5f, -0.5f,
                +0.5f, -0.5f, -0.5f,
                +0.5f, -0.5f, +0.5f,
                +0.5f, +0.5f, +0.5f,
            
                -0.5f, -0.5f, -0.5f,
                +0.5f, -0.5f, -0.5f,
                +0.5f, -0.5f, +0.5f,
                +0.5f, -0.5f, +0.5f,
                -0.5f, -0.5f, +0.5f,
                -0.5f, -0.5f, -0.5f,
            
                -0.5f, +0.5f, -0.5f,
                +0.5f, +0.5f, -0.5f,
                +0.5f, +0.5f, +0.5f,
                +0.5f, +0.5f, +0.5f,
                -0.5f, +0.5f, +0.5f,
                -0.5f, +0.5f, -0.5f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute float3 = new VertexAttribute(GLType.FLOAT, 3, false);
            
            FloatBuffer cubeBuffer = stack.floats(cubeVertices);
            
            cubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, float3).build();
        }
        
        GL.DEFAULT_STATE.clearColor = new double[] {0.1, 0.1, 0.1, 1.0};
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        glEnable(GL_MULTISAMPLE); // TODO - Add this to GL.State
    
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("model", false, new Matrix4d());
        
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
    
        cubeVAO.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_4111_AntiAliasingMSAA();
        
        start(instance);
    }
}
