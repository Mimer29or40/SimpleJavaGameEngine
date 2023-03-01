package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
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
import engine.gl.texture.TextureWrap;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.List;

import static engine.IO.*;

public class LOGL_431_BlendingDiscard extends Engine
{
    Program shader;
    
    VertexArray cubeVAO;
    VertexArray planeVAO;
    VertexArray transparentVAO;
    
    Texture2D cubeTexture;
    Texture2D planeTexture;
    Texture2D transparentTexture;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    List<Vector3d> vegetation;
    
    protected LOGL_431_BlendingDiscard()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.3.1 - Blending Discard");
        
        mouseCapture();
        
        String _blending_vs = """
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
        String _blending_fg = """
                              #version 330 core
                              out vec4 FragColor;
                              
                              in vec2 TexCoords;
                              
                              uniform sampler2D texture1;
                              
                              void main()
                              {
                                  vec4 texColor = texture(texture1, TexCoords);
                                  if(texColor.a < 0.1)
                                      discard;
                                  FragColor = texColor;
                              }
                              """;
        
        
        Shader blending_vs = new Shader(ShaderType.VERTEX, _blending_vs);
        Shader blending_fg = new Shader(ShaderType.FRAGMENT, _blending_fg);
        
        shader = new Program(blending_vs, blending_fg);
        
        blending_vs.delete();
        blending_fg.delete();
        
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
        float[] transparentVertices = {
                // positions          // texture Coords (swapped y coordinates because texture is flipped upside down)
                +0.0f, +0.5f, +0.0f,  +0.0f, +1.0f,
                +0.0f, -0.5f, +0.0f,  +0.0f, +0.0f,
                +1.0f, -0.5f, +0.0f,  +1.0f, +0.0f,
                
                +0.0f, +0.5f, +0.0f,  +0.0f, +1.0f,
                +1.0f, -0.5f, +0.0f,  +1.0f, +0.0f,
                +1.0f, +0.5f,+ 0.0f,  +1.0f, +1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texture  = new VertexAttribute(GLType.FLOAT, 2, false);
            
            FloatBuffer cubeBuffer        = stack.floats(cubeVertices);
            FloatBuffer planeBuffer       = stack.floats(planeVertices);
            FloatBuffer transparentBuffer = stack.floats(transparentVertices);
            
            cubeVAO        = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, position, texture).build();
            planeVAO       = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, planeBuffer, position, texture).build();
            transparentVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, transparentBuffer, position, texture).build();
        }
        
        Image cubeImage        = new Image(IOUtil.getPath("LearnOpenGL/textures/marble.jpg"));
        Image planeImage       = new Image(IOUtil.getPath("LearnOpenGL/textures/metal.png"));
        Image transparentImage = new Image(IOUtil.getPath("LearnOpenGL/textures/grass.png"));
        
        cubeTexture = new Texture2D(cubeImage);
        cubeTexture.genMipmaps();
        
        planeTexture = new Texture2D(planeImage);
        planeTexture.genMipmaps();
        
        transparentTexture = new Texture2D(transparentImage);
        transparentTexture.genMipmaps();
        transparentTexture.wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        
        cubeImage.delete();
        planeImage.delete();
        transparentImage.delete();
        
        vegetation = List.of(new Vector3d(-1.5f, +0.0f, -0.48f),
                             new Vector3d(+1.5f, +0.0f, +0.51f),
                             new Vector3d(+0.0f, +0.0f, +0.70f),
                             new Vector3d(-0.3f, +0.0f, -2.30f),
                             new Vector3d(+0.5f, +0.0f, -0.60f));
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
        
        GL.depthMode(DepthMode.LESS);
        
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        Matrix4d model = new Matrix4d();
        
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
        model.identity();
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 0, 6);
        
        // vegetation
        Texture.bind(transparentTexture, 0);
        for (Vector3d veg : vegetation)
        {
            model.identity();
            model.translate(veg);
            Program.uniformMatrix4("model", false, model);
            transparentVAO.draw(DrawMode.TRIANGLES, 0, 6);
        }
    }
    
    @Override
    protected void destroy()
    {
        cubeTexture.delete();
        planeTexture.delete();
        transparentTexture.delete();
        
        cubeVAO.delete();
        planeVAO.delete();
        transparentVAO.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_431_BlendingDiscard();
        
        start(instance);
    }
}
