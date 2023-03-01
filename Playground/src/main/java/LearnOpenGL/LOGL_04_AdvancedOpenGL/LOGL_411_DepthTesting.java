package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import LearnOpenGL.Model;
import engine.Engine;
import engine.Image;
import engine.Key;
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
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_411_DepthTesting extends Engine
{
    Program shader;
    
    VertexArray cubeVAO;
    VertexArray planeVAO;
    
    Texture2D cubeTexture;
    Texture2D planeTexture;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    protected LOGL_411_DepthTesting()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.1.1 - Depth Testing");
        
        mouseCapture();
        
        String _depth_testing_vs = """
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
        String _depth_testing_fg = """
                                   #version 330 core
                                   out vec4 FragColor;
                                   
                                   in vec2 TexCoords;
                                   
                                   uniform sampler2D texture1;
                                   
                                   void main()
                                   {
                                       FragColor = texture(texture1, TexCoords);
                                   }
                                   """;
        
        Shader model_loading_vs = new Shader(ShaderType.VERTEX, _depth_testing_vs);
        Shader model_loading_fg = new Shader(ShaderType.FRAGMENT, _depth_testing_fg);
        
        shader = new Program(model_loading_vs, model_loading_fg);
        
        model_loading_vs.delete();
        model_loading_fg.delete();
        
        
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
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texture  = new VertexAttribute(GLType.FLOAT, 2, false);
            
            FloatBuffer cubeBuffer  = stack.floats(cubeVertices);
            FloatBuffer planeBuffer = stack.floats(planeVertices);
            
            cubeVAO  = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, position, texture).build();
            planeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, planeBuffer, position, texture).build();
        }
        
        Image cubeImage  = new Image(IOUtil.getPath("LearnOpenGL/textures/marble.jpg"));
        Image planeImage = new Image(IOUtil.getPath("LearnOpenGL/textures/metal.png"));
        
        cubeTexture = new Texture2D(cubeImage);
        cubeTexture.genMipmaps();
        
        planeTexture = new Texture2D(planeImage);
        planeTexture.genMipmaps();
        
        cubeImage.delete();
        planeImage.delete();
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
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.ALWAYS);
        
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
    }
    
    @Override
    protected void destroy()
    {
        cubeTexture.delete();
        planeTexture.delete();
        
        cubeVAO.delete();
        planeVAO.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_411_DepthTesting();
        
        start(instance);
    }
}
