package LearnOpenGL.LOGL_01_GettingStarted;

import engine.Engine;
import engine.Image;
import engine.Key;
import engine.gl.DepthMode;
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
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_164_CoordinateSystemsExercise1 extends Engine
{
    Program program;
    
    VertexArray VAO;
    
    Texture2D[] texture = new Texture2D[2];
    
    Vector3d[] cubePositions;
    
    protected LOGL_164_CoordinateSystemsExercise1()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.6.4 - Coordinate Systems Exercise 1");
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 1) in vec2 aTexCoord;
                          
                          out vec2 TexCoord;
                          
                          uniform mat4 model;
                          uniform mat4 view;
                          uniform mat4 projection;
                          
                          void main()
                          {
                              gl_Position = projection * view * model * vec4(aPos, 1.0);
                              TexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec2 TexCoord;
                          
                          uniform sampler2D texture1;
                          uniform sampler2D texture2;
                          
                          void main()
                          {
                              FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        program = new Program(vertShader, fragShader);
        
        vertShader.delete();
        fragShader.delete();
        
        //@formatter:off
        float[] vertices = {
                // positions          // texture coords
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                +0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 1.0f,
                -0.5f, +0.5f, +0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,

                -0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  1.0f, 0.0f,

                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer _vertices = stack.floats(vertices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, texCoord).build();
        }
        
        Image image0 = new Image(IOUtil.getPath("LearnOpenGL/textures/container.jpg"));
        Image image1 = new Image(IOUtil.getPath("LearnOpenGL/textures/awesomeface.png"));
        
        texture[0] = new Texture2D(image0);
        texture[0].wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        texture[0].filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        texture[0].genMipmaps();
        
        texture[1] = new Texture2D(image1);
        texture[1].wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        texture[1].filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        texture[1].genMipmaps();
        
        image0.delete();
        image1.delete();
        
        cubePositions = new Vector3d[] {
                new Vector3d(0.0, 0.0, 0.0),
                new Vector3d(2.0, 5.0, -15.0),
                new Vector3d(-1.5, -2.2, -2.5),
                new Vector3d(-3.8, -2.0, -12.3),
                new Vector3d(2.4, -0.4, -3.5),
                new Vector3d(-1.7, 3.0, -7.5),
                new Vector3d(1.3, -2.0, -2.5),
                new Vector3d(1.5, 2.0, -2.5),
                new Vector3d(1.5, 0.2, -1.5),
                new Vector3d(-1.3, 1.0, -1.f)
        };
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
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        // bind Texture
        Texture.bind(texture[0], 0);
        Texture.bind(texture[1], 1);
        
        Program.bind(program);
        Program.uniformInt("texture1", 0);
        Program.uniformInt("texture2", 1);
        
        // create transformations
        Matrix4d view       = new Matrix4d(); // make sure to initialize matrix to identity matrix first
        Matrix4d projection = new Matrix4d();
        
        view.translate(new Vector3d(0.0, 0.0, -3.0));
        projection.perspective(Math.toRadians(45.0), (double) windowFramebufferSize().x() / windowFramebufferSize().y(), 0.1, 100.0);
        
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("projection", false, projection);
        
        for (int i = 0; i < 10; i++)
        {
            Matrix4d model = new Matrix4d();
            model.translate(cubePositions[i]);
            double angle = 20 * i;
            if (i % 3 == 0) angle += time * 25;
            model.rotate(Math.toRadians(angle), new Vector3d(1.0, 0.3, 0.5));
            Program.uniformMatrix4("model", false, model);
            
            VAO.draw(DrawMode.TRIANGLES, 36);
        }
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
        Engine instance = new LOGL_164_CoordinateSystemsExercise1();
        
        start(instance);
    }
}
