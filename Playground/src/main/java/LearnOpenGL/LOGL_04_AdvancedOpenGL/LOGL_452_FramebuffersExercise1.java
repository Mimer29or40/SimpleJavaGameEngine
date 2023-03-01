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
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_452_FramebuffersExercise1 extends Engine
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
    
    Key mode = Key.F1;
    
    protected LOGL_452_FramebuffersExercise1()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.5.2 - Framebuffers Exercise 1");
        
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
                            
                            void main()
                            {
                                vec3 col = texture(screenTexture, TexCoords).rgb;
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
        float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates. NOTE that this plane is now much smaller and at the top of the screen
                // positions   // texCoords
                -0.3f, +1.0f,  +0.0f, +1.0f,
                -0.3f, +0.7f,  +0.0f, +0.0f,
                +0.3f, +0.7f,  +1.0f, +0.0f,

                -0.3f, +1.0f,  +0.0f, +1.0f,
                +0.3f, +0.7f,  +1.0f, +0.0f,
                +0.3f, +1.0f,  +1.0f, +1.0f
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
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) mode = Key.F1;
        if (keyboardKeyDown(Key.F2)) mode = Key.F2;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        // first render pass: mirror texture.
        // bind to framebuffer and draw to color texture as we normally
        // would, but with the view camera reversed.
        // bind to framebuffer and draw scene as we normally would to color texture
        // ------------------------------------------------------------------------
        Framebuffer.bind(framebuffer);
        GL.depthMode(DepthMode.LESS); // enable depth testing (is disabled for rendering screen-space quad)
        
        // make sure we clear the framebuffer's content
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Program.bind(shader);
        Matrix4d model = new Matrix4d();
        camera.Yaw += 180.0f; // rotate the camera's yaw 180 degrees around
        camera.updateCameraVectors(); // call this to make sure it updates its camera vectors, note that we disable pitch constrains for this specific case (otherwise we can't reverse camera's pitch values)
        Matrix4dc view = camera.GetViewMatrix();
        camera.Yaw -= 180.0f; // reset it back to its original orientation
        camera.updateCameraVectors();
        Matrix4dc projection = camera.GetProjectionMatrix();
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("projection", false, projection);
        
        // cubes
        Texture.bind(cubeTexture, 0);
        model.identity();
        model.translate(new Vector3d(-1.0f, 0.0f, -1.0f));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 0, 36);
        
        model.identity();
        model.translate(new Vector3d(2.0f, 0.0f, 0.0f));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 0, 36);
        
        // floor
        Texture.bind(planeTexture, 0);
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 0, 6);
        
        // second render pass: draw as normal
        // ----------------------------------
        Framebuffer.bind(Framebuffer.NULL);
        
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        // cubes
        Texture.bind(cubeTexture, 0);
        model.identity();
        model.translate(new Vector3d(-1.0f, 0.0f, -1.0f));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 0, 36);
        
        model.identity();
        model.translate(new Vector3d(2.0f, 0.0f, 0.0f));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 0, 36);
        
        // floor
        Texture.bind(planeTexture, 0);
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 0, 6);
        
        
        // now draw the mirror quad with screen texture
        // --------------------------------------------
        GL.depthMode(DepthMode.NONE); // disable depth test so screen-space quad isn't discarded due to depth test.
        
        Program.bind(screenShader);
        Texture.bind(framebuffer.color(0), 0);    // use the color attachment texture as the texture of the quad plane
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
        Engine instance = new LOGL_452_FramebuffersExercise1();
        
        start(instance);
    }
}
