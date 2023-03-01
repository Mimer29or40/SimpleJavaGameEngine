package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Key;
import engine.gl.*;
import engine.gl.buffer.BufferUniform;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_481_UBOs extends Engine
{
    Program shaderRed;
    Program shaderGreen;
    Program shaderBlue;
    Program shaderYellow;
    
    VertexArray cubeVAO;
    
    BufferUniform uboMatrices;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    protected LOGL_481_UBOs()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.8.1 - Uniform Buffer Objects");
        
        mouseCapture();
        
        String _ubo_vs = """
                         #version 330 core
                         layout (location = 0) in vec3 aPos;
                         
                         layout (std140) uniform Matrices
                         {
                             mat4 projection;
                             mat4 view;
                         };
                         uniform mat4 model;
                         
                         void main()
                         {
                             gl_Position = projection * view * model * vec4(aPos, 1.0);
                         }
                         """;
        String _red_fg = """
                         #version 330 core
                         out vec4 FragColor;
                         
                         void main()
                         {
                             FragColor = vec4(1.0, 0.0, 0.0, 1.0);
                         }
                         """;
        String _green_fg = """
                           #version 330 core
                           out vec4 FragColor;
                           
                           void main()
                           {
                               FragColor = vec4(0.0, 1.0, 0.0, 1.0);
                           }
                           """;
        String _blue_fg = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          void main()
                          {
                              FragColor = vec4(0.0, 0.0, 1.0, 1.0);
                          }
                          """;
        String _yellow_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            void main()
                            {
                                FragColor = vec4(1.0, 1.0, 0.0, 1.0);
                            }
                            """;
        
        Shader ubo_vs    = new Shader(ShaderType.VERTEX, _ubo_vs);
        Shader red_fg    = new Shader(ShaderType.FRAGMENT, _red_fg);
        Shader green_fg  = new Shader(ShaderType.FRAGMENT, _green_fg);
        Shader blue_fg   = new Shader(ShaderType.FRAGMENT, _blue_fg);
        Shader yellow_fg = new Shader(ShaderType.FRAGMENT, _yellow_fg);
        
        shaderRed    = new Program(ubo_vs, red_fg);
        shaderGreen  = new Program(ubo_vs, green_fg);
        shaderBlue   = new Program(ubo_vs, blue_fg);
        shaderYellow = new Program(ubo_vs, yellow_fg);
        
        ubo_vs.delete();
        red_fg.delete();
        green_fg.delete();
        blue_fg.delete();
        yellow_fg.delete();
        
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
                -0.5f, +0.5f, -0.5f,
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            
            FloatBuffer cubeBuffer = stack.floats(cubeVertices);
            
            cubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, position).build();
        }
        
        // configure a uniform buffer object
        // ---------------------------------
        // first. We get the relevant block indices
        // then we link each shader's uniform block to this uniform binding point
        Program.bind(shaderRed);
        Program.uniformBlock("Matrices", 0);
        Program.bind(shaderGreen);
        Program.uniformBlock("Matrices", 0);
        Program.bind(shaderBlue);
        Program.uniformBlock("Matrices", 0);
        Program.bind(shaderYellow);
        Program.uniformBlock("Matrices", 0);
        
        // Now actually create the buffer
        uboMatrices = new BufferUniform(BufferUsage.STATIC_DRAW, Float.BYTES * 16 * 2);
        // define the range of the buffer that links to a uniform binding point
        uboMatrices.range(0);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) GL.polygonMode(PolygonMode.FILL);
        if (keyboardKeyDown(Key.F2)) GL.polygonMode(PolygonMode.LINE);
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        // render
        // ------
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer floats = stack.mallocFloat(32);
            camera.GetProjectionMatrix().get(floats.position(0));
            camera.GetViewMatrix().get(floats.position(16));
            
            // set the view and projection matrix in the uniform block - we only have to do this once per loop iteration.
            uboMatrices.set(floats.clear());
        }
        
        Matrix4d model = new Matrix4d();
        
        // draw 4 cubes
        // RED
        Program.bind(shaderRed);
        model.identity();
        model.translate(new Vector3d(-0.75, 0.75, 0.0)); // move top-left
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        // GREEN
        Program.bind(shaderGreen);
        model.identity();
        model.translate(new Vector3d(0.75, 0.75, 0.0)); // move top-right
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        // YELLOW
        Program.bind(shaderYellow);
        model.identity();
        model.translate(new Vector3d(-0.75, -0.75, 0.0)); // move bottom-left
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        // BLUE
        Program.bind(shaderBlue);
        model.identity();
        model.translate(new Vector3d(0.75, -0.75, 0.0)); // move bottom-right
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        uboMatrices.delete();
        
        cubeVAO.delete();
        
        shaderRed.delete();
        shaderGreen.delete();
        shaderBlue.delete();
        shaderYellow.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_481_UBOs();
        
        start(instance);
    }
}
