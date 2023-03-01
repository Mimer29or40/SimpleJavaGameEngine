package LearnOpenGL.LOGL_01_GettingStarted;

import engine.Engine;
import engine.Image;
import engine.Key;
import engine.gl.GL;
import engine.gl.GLType;
import engine.gl.ScreenBuffer;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureFilter;
import engine.gl.texture.TextureWrap;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static engine.IO.keyboardKeyDown;
import static engine.IO.windowTitle;

public class LOGL_145_TextureExercise3 extends Engine
{
    Program program;
    
    VertexArray VAO;
    
    Texture2D[] texture = new Texture2D[2];
    
    protected LOGL_145_TextureExercise3()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.4.5 - Texture Exercise 3");
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 1) in vec3 aColor;
                          layout (location = 2) in vec2 aTexCoord;
                          
                          out vec3 ourColor;
                          out vec2 TexCoord;
                          
                          void main()
                          {
                              gl_Position = vec4(aPos, 1.0);
                              ourColor = aColor;
                              TexCoord = vec2(aTexCoord.x, aTexCoord.y);
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec3 ourColor;
                          in vec2 TexCoord;
                          
                          // texture samplers
                          uniform sampler2D texture1;
                          uniform sampler2D texture2;
                          
                          void main()
                          {
                              // linearly interpolate between both textures (80% container, 20% awesomeface)
                              FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        program = new Program(vertShader, fragShader);
        
        vertShader.delete();
        fragShader.delete();
        
        float[] vertices = {
                // positions        // colors         // texture coords
                +0.5f, +0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.55f, 0.55f, // top right
                +0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.55f, 0.45f, // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.45f, 0.45f, // bottom left
                -0.5f, +0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.45f, 0.55f  // top left
        };
        int[] indices = {
                0, 1, 3, // first triangle
                1, 2, 3  // second triangle
        };
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer _vertices = stack.floats(vertices);
            IntBuffer   _indices  = stack.ints(indices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute color    = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, color, texCoord).indexBuffer(BufferUsage.STATIC_DRAW, _indices).build();
        }
        
        Image image0 = new Image(IOUtil.getPath("LearnOpenGL/textures/container.jpg"));
        Image image1 = new Image(IOUtil.getPath("LearnOpenGL/textures/awesomeface.png"));
        
        texture[0] = new Texture2D(image0);
        texture[0].wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        texture[0].filter(TextureFilter.NEAREST, TextureFilter.NEAREST);
        texture[0].genMipmaps();
        
        texture[1] = new Texture2D(image1);
        texture[1].wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        texture[1].filter(TextureFilter.NEAREST, TextureFilter.NEAREST);
        texture[1].genMipmaps();
        
        image0.delete();
        image1.delete();
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
        
        // bind Texture
        Texture.bind(texture[0], 0);
        Texture.bind(texture[1], 1);
        
        // render container
        Program.bind(program);
        Program.uniformInt("texture1", 0);
        Program.uniformInt("texture2", 1);
        
        VAO.drawElements(DrawMode.TRIANGLES, 6);
    }
    
    @Override
    protected void destroy()
    {
        texture[0].delete();
        texture[1].delete();
        
        VAO.delete();
        
        program.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_145_TextureExercise3();
        
        start(instance);
    }
}
