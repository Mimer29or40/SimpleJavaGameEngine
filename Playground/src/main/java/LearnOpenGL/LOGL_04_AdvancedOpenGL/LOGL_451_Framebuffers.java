package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Image;
import engine.Key;
import engine.color.ColorFormat;
import engine.gl.*;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureWrap;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_451_Framebuffers extends Engine
{
    Program shader;
    Program screenShader;
    
    VertexArray cubeVAO;
    VertexArray planeVAO;
    VertexArray quadVAO;
    
    Texture2D cubeTexture;
    Texture2D planeTexture;
    
    Framebuffer framebuffer;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Key wireframe = Key.F1;
    Key mode      = Key.F1;
    
    protected LOGL_451_Framebuffers()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.5.1 - Framebuffers");
        
        mouseCapture();
        
        String _framebuffers_vs = """
                                  #version 330 core
                                  layout (location = 0) in vec3 aPos;
                                  layout (location = 1) in vec2 aTexCoords;
                                  
                                  out vec2 TexCoords;
                                  
                                  uniform mat4 model;
                                  uniform mat4 view;
                                  uniform mat4 projection;
                                  
                                  void main()
                                  {
                                      TexCoords = aTexCoords;
                                      gl_Position = projection * view * model * vec4(aPos, 1.0);
                                  }
                                  """;
        String _framebuffers_fg = """
                                  #version 330 core
                                  out vec4 FragColor;
                                  
                                  in vec2 TexCoords;
                                  
                                  uniform sampler2D texture1;
                                  
                                  void main()
                                  {
                                      FragColor = texture(texture1, TexCoords);
                                  }
                                  """;
        
        String _screen_vs = """
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
        String _screen_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec2 TexCoords;
                            
                            uniform sampler2D screenTexture;
                            
                            uniform int mode;
                            
                            const float offset = 1.0 / 400.0;
                            const vec2 offsets[9] = vec2[](
                                vec2(-offset,  offset), // top-left
                                vec2( 0.0f,    offset), // top-center
                                vec2( offset,  offset), // top-right
                                vec2(-offset,  0.0f),   // center-left
                                vec2( 0.0f,    0.0f),   // center-center
                                vec2( offset,  0.0f),   // center-right
                                vec2(-offset, -offset), // bottom-left
                                vec2( 0.0f,   -offset), // bottom-center
                                vec2( offset, -offset)  // bottom-right
                            );
                            
                            vec3 runKernel(float kernel[9])
                            {
                                vec3 samples[9];
                                for(int i = 0; i < 9; i++)
                                {
                                    samples[i] = vec3(texture(screenTexture, TexCoords.st + offsets[i]));
                                }
                                vec3 col = vec3(0.0);
                                for(int i = 0; i < 9; i++)
                                {
                                    col += samples[i] * kernel[i];
                                }
                                return col;
                            }
                            
                            void main()
                            {
                                vec3 col = texture(screenTexture, TexCoords).rgb;
                                if (mode == 1)
                                {
                                    col = 1.0 - col;
                                }
                                else if (mode == 2)
                                {
                                    col = vec3(0.2126 * col.r + 0.7152 * col.g + 0.0722 * col.b);
                                }
                                else if (mode == 3)
                                {
                                    float kernel[9] = float[](
                                        -1, -1, -1,
                                        -1,  9, -1,
                                        -1, -1, -1
                                    );
                                    col = runKernel(kernel);
                                }
                                else if (mode == 4)
                                {
                                    float kernel[9] = float[](
                                        1.0 / 16, 2.0 / 16, 1.0 / 16,
                                        2.0 / 16, 4.0 / 16, 2.0 / 16,
                                        1.0 / 16, 2.0 / 16, 1.0 / 16
                                    );
                                    col = runKernel(kernel);
                                }
                                else if (mode == 5)
                                {
                                    float kernel[9] = float[](
                                        1,  1, 1,
                                        1, -8, 1,
                                        1,  1, 1
                                    );
                                    col = runKernel(kernel);
                                }
                                FragColor = vec4(col, 1.0);
                            }
                            """;
        
        Shader framebuffers_vs = new Shader(ShaderType.VERTEX, _framebuffers_vs);
        Shader framebuffers_fg = new Shader(ShaderType.FRAGMENT, _framebuffers_fg);
        Shader screen_vs       = new Shader(ShaderType.VERTEX, _screen_vs);
        Shader screen_fg       = new Shader(ShaderType.FRAGMENT, _screen_fg);
        
        shader       = new Program(framebuffers_vs, framebuffers_fg);
        screenShader = new Program(screen_vs, screen_fg);
        
        framebuffers_vs.delete();
        framebuffers_fg.delete();
        screen_vs.delete();
        screen_fg.delete();
        
        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
        //@formatter:off
        float[] cubeVertices = {
                // positions          // texture Coords
                -0.5f, -0.5f, -0.5f,  +0.0f, +0.0f,
                +0.5f, -0.5f, -0.5f,  +1.0f, +0.0f,
                +0.5f, +0.5f, -0.5f,  +1.0f, +1.0f,
                +0.5f, +0.5f, -0.5f,  +1.0f, +1.0f,
                -0.5f, +0.5f, -0.5f,  +0.0f, +1.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, +0.0f,
            
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f,
                +0.5f, -0.5f, +0.5f,  +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +1.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +1.0f,
                -0.5f, +0.5f, +0.5f,  +0.0f, +1.0f,
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f,
            
                -0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
                -0.5f, +0.5f, -0.5f,  +1.0f, +1.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f,
                -0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
            
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
                +0.5f, +0.5f, -0.5f,  +1.0f, +1.0f,
                +0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
                +0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
                +0.5f, -0.5f, +0.5f,  +0.0f, +0.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
            
                -0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
                +0.5f, -0.5f, -0.5f,  +1.0f, +1.0f,
                +0.5f, -0.5f, +0.5f,  +1.0f, +0.0f,
                +0.5f, -0.5f, +0.5f,  +1.0f, +0.0f,
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, +1.0f,
            
                -0.5f, +0.5f, -0.5f,  +0.0f, +1.0f,
                +0.5f, +0.5f, -0.5f,  +1.0f, +1.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f,
                -0.5f, +0.5f, +0.5f,  +0.0f, +0.0f,
                -0.5f, +0.5f, -0.5f,  +0.0f, +1.0f
        };
        float[] planeVertices = {
                // positions          // texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture wrapping mode). this will cause the floor texture to repeat)
                +5.0f, -0.5f, +5.0f,  +2.0f, +0.0f,
                -5.0f, -0.5f, +5.0f,  +0.0f, +0.0f,
                -5.0f, -0.5f, -5.0f,  +0.0f, +2.0f,
            
                +5.0f, -0.5f, +5.0f,  +2.0f, +0.0f,
                -5.0f, -0.5f, -5.0f,  +0.0f, +2.0f,
                +5.0f, -0.5f, -5.0f,  +2.0f, +2.0f
        };
        float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
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
            VertexAttribute pos3 = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute pos2 = new VertexAttribute(GLType.FLOAT, 2, false);
            VertexAttribute tex  = new VertexAttribute(GLType.FLOAT, 2, false);
            
            FloatBuffer cubeBuffer  = stack.floats(cubeVertices);
            FloatBuffer planeBuffer = stack.floats(planeVertices);
            FloatBuffer quadBuffer  = stack.floats(quadVertices);
            
            cubeVAO  = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, pos3, tex).build();
            planeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, planeBuffer, pos3, tex).build();
            quadVAO  = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, quadBuffer, pos2, tex).build();
        }
        
        Image cubeImage  = new Image(IOUtil.getPath("LearnOpenGL/textures/container.jpg"));
        Image planeImage = new Image(IOUtil.getPath("LearnOpenGL/textures/metal.png"));
        
        cubeTexture = new Texture2D(cubeImage);
        cubeTexture.genMipmaps();
        
        planeTexture = new Texture2D(planeImage);
        planeTexture.genMipmaps();
        
        cubeImage.delete();
        planeImage.delete();
        
        int x = windowFramebufferSize().x();
        int y = windowFramebufferSize().y();
        framebuffer = Framebuffer.builder(x, y).color(ColorFormat.RGB).depthStencil().build();
        framebuffer.color(0).wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) wireframe = Key.F1;
        if (keyboardKeyDown(Key.F2)) wireframe = Key.F2;
        
        if (keyboardKeyDown(Key.K1)) mode = Key.K1;
        if (keyboardKeyDown(Key.K2)) mode = Key.K2;
        if (keyboardKeyDown(Key.K3)) mode = Key.K3;
        if (keyboardKeyDown(Key.K4)) mode = Key.K4;
        if (keyboardKeyDown(Key.K5)) mode = Key.K5;
        if (keyboardKeyDown(Key.K6)) mode = Key.K6;
        if (keyboardKeyDown(Key.K7)) mode = Key.K7;
        if (keyboardKeyDown(Key.K8)) mode = Key.K8;
        if (keyboardKeyDown(Key.K9)) mode = Key.K9;
        if (keyboardKeyDown(Key.K0)) mode = Key.K0;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        Framebuffer.bind(framebuffer);
        
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        if (wireframe == Key.F1) GL.polygonMode(PolygonMode.FILL);
        if (wireframe == Key.F2) GL.polygonMode(PolygonMode.LINE);
        
        Program.bind(shader);
        
        Program.uniformInt("texture1", 0);
        
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        Matrix4d model = new Matrix4d();
        
        // cubes
        Texture.bind(cubeTexture, 0);
        model.translate(new Vector3d(-1.0, 0.0, -1.0));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        model.identity();
        model.translate(new Vector3d(2.0, 0.0, 0.0));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        // floor
        Texture.bind(planeTexture, 0);
        model.identity();
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 6);
        
        // now bind back to default framebuffer and draw a quad plane with the attached framebuffer color texture
        Framebuffer.bind(Framebuffer.NULL);
        // clear all relevant buffers
        GL.clearColor(1.0, 1.0, 1.0, 1.0); // set clear color to white (not really necessary actually, since we won't be able to see behind the quad anyways)
        GL.clearBuffers(ScreenBuffer.COLOR);
    
        GL.polygonMode(PolygonMode.FILL);
        
        GL.depthMode(DepthMode.NONE); // disable depth test so screen-space quad isn't discarded due to depth test.
        
        Program.bind(screenShader);
        
        int mode = switch (this.mode)
                {
                    case K2 -> 1;
                    case K3 -> 2;
                    case K4 -> 3;
                    case K5 -> 4;
                    case K6 -> 5;
                    case K7 -> 6;
                    case K8 -> 7;
                    default -> 0;
                };
        Program.uniformInt("mode", mode);
        Texture.bind(framebuffer.color(0), 0); // use the color attachment texture as the texture of the quad plane
        quadVAO.draw(DrawMode.TRIANGLES, 0, 6);
    }
    
    @Override
    protected void destroy()
    {
        framebuffer.delete();
        
        cubeTexture.delete();
        planeTexture.delete();
        
        cubeVAO.delete();
        planeVAO.delete();
        quadVAO.delete();
        
        shader.delete();
        screenShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_451_Framebuffers();
        
        start(instance);
    }
}
