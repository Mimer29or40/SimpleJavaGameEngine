package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Key;
import engine.color.ColorFormat;
import engine.gl.*;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.EnumSet;

import static engine.IO.*;

public class LOGL_4112_AntiAliasingOffscreen extends Engine
{
    Program shader;
    Program screenShader;
    
    VertexArray cubeVAO;
    VertexArray quadVAO;
    
    Framebuffer framebuffer;
    Framebuffer intermediateFBO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    protected LOGL_4112_AntiAliasingOffscreen()
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
        
        String _post_vs = """
                          #version 330 core
                          layout (location = 0) in vec2 aPos;
                          layout (location = 1) in vec2 aTexCoords;
                          
                          out vec2 TexCoords;
                          
                          void main()
                          {
                              TexCoords = aTexCoords;
                              gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                          }
                          """;
        String _post_fg = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec2 TexCoords;
                          
                          uniform sampler2D screenTexture;
                          
                          void main()
                          {
                              vec3 col = texture(screenTexture, TexCoords).rgb;
                              float grayscale = 0.2126 * col.r + 0.7152 * col.g + 0.0722 * col.b;
                              FragColor = vec4(vec3(grayscale), 1.0);
                          }
                          """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        Shader post_vs   = new Shader(ShaderType.VERTEX, _post_vs);
        Shader post_fg   = new Shader(ShaderType.FRAGMENT, _post_fg);
        
        shader       = new Program(shader_vs, shader_fg);
        screenShader = new Program(post_vs, post_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        post_vs.delete();
        post_fg.delete();
        
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
        float[] quadVertices = {   // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                // positions   // texCoords
                -1.0f, +1.0f,  +0.0f, +1.0f,
                -1.0f, -1.0f,  +0.0f, +0.0f,
                +1.0f, -1.0f,  +1.0f, +0.0f,
    
                -1.0f, +1.0f,  +0.0f, +1.0f,
                +1.0f, -1.0f,  +1.0f, +0.0f,
                +1.0f, +1.0f,  +1.0f, +1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute float3 = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute float2 = new VertexAttribute(GLType.FLOAT, 2, false);
            
            FloatBuffer cubeBuffer = stack.floats(cubeVertices);
            FloatBuffer quadBuffer = stack.floats(quadVertices);
            
            cubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, float3).build();
            quadVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, quadBuffer, float2, float2).build();
        }
        
        int x = windowFramebufferSize().x();
        int y = windowFramebufferSize().y();
        framebuffer = Framebuffer.builder(x, y, 4).color(ColorFormat.RGB).depthStencil().build();
        
        intermediateFBO = Framebuffer.builder(x, y).color(ColorFormat.RGB).build();
        
        GL.DEFAULT_STATE.clearColor = new double[] {0.1, 0.1, 0.1, 1.0};
        GL.DEFAULT_STATE.depthMode  = DepthMode.LESS;
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
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        
        // 1. draw scene as normal in multisampled buffers
        Framebuffer.bind(framebuffer);
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        // set transformation matrices
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("model", false, new Matrix4d());
        
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        //framebuffer.color(0).toImage().save(Path.of("framebuffer.png"));
        
        // 2. now blit multisampled buffer(s) to normal colorbuffer of intermediate FBO. Image is stored in screenTexture
        Framebuffer.blit(framebuffer, intermediateFBO, EnumSet.of(ScreenBuffer.COLOR), true);
        
        // 3. now render quad with scene's visuals as its texture image
        Framebuffer.bind(Framebuffer.NULL);
        GL.clearColor(1.0, 1.0, 1.0, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR);
        GL.depthMode(DepthMode.NONE);
        
        // draw Screen quad
        Program.bind(screenShader);
        Texture.bind(intermediateFBO.color(0)); // use the now resolved color attachment as the quad's texture
        //Texture.bind(framebuffer.color(0));
        quadVAO.draw(DrawMode.TRIANGLES, 6);
        //Program.bind(shader);
        //cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        framebuffer.delete();
        intermediateFBO.delete();
        
        cubeVAO.delete();
        quadVAO.delete();
        
        shader.delete();
        screenShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_4112_AntiAliasingOffscreen();
        
        start(instance);
    }
}
