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

public class LOGL_421_StencilTesting extends Engine
{
    Program shader;
    Program shaderSingleColor;
    
    VertexArray cubeVAO;
    VertexArray planeVAO;
    
    Texture2D cubeTexture;
    Texture2D planeTexture;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    StencilMode stencilMode1 = new StencilMode(StencilFunc.ALWAYS, 0, 0xFF, StencilOp.KEEP, StencilOp.KEEP, StencilOp.REPLACE);
    StencilMode stencilMode2 = new StencilMode(StencilFunc.ALWAYS, 1, 0xFF, StencilOp.KEEP, StencilOp.KEEP, StencilOp.REPLACE);
    StencilMode stencilMode3 = new StencilMode(StencilFunc.NOT_EQUAL, 1, 0xFF, StencilOp.KEEP, StencilOp.KEEP, StencilOp.REPLACE);
    
    protected LOGL_421_StencilTesting()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.2.1 - Stencil Testing");
        
        mouseCapture();
        
        String _stencil_testing_vs = """
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
        String _stencil_testing_fg = """
                                     #version 330 core
                                     out vec4 FragColor;
                                     
                                     in vec2 TexCoords;
                                     
                                     uniform sampler2D texture1;
                                     
                                     void main()
                                     {
                                         FragColor = texture(texture1, TexCoords);
                                     }
                                     """;
        
        String _stencil_single_color_fg = """
                                          #version 330 core
                                          out vec4 FragColor;
                                          
                                          void main()
                                          {
                                              FragColor = vec4(0.04, 0.28, 0.26, 1.0);
                                          }
                                          """;
        
        Shader stencil_testing_vs      = new Shader(ShaderType.VERTEX, _stencil_testing_vs);
        Shader stencil_testing_fg      = new Shader(ShaderType.FRAGMENT, _stencil_testing_fg);
        Shader stencil_single_color_fg = new Shader(ShaderType.FRAGMENT, _stencil_single_color_fg);
        
        shader            = new Program(stencil_testing_vs, stencil_testing_fg);
        shaderSingleColor = new Program(stencil_testing_vs, stencil_single_color_fg);
        
        stencil_testing_vs.delete();
        stencil_testing_fg.delete();
        stencil_single_color_fg.delete();
        
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
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH, ScreenBuffer.STENCIL);
        
        GL.depthMode(DepthMode.LESS);
    
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        
        Program.bind(shader);
        Program.uniformInt("texture1", 0);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        
        Matrix4d model = new Matrix4d();
        
        // draw floor as normal, but don't write the floor to the stencil buffer, we only care about the containers. We set its mask to 0x00 to not write to the stencil buffer.
        GL.stencilMode(stencilMode1);
        GL.stencilMask(0x00);
        // floor
        Texture.bind(planeTexture, 0);
        model.identity();
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 6);
    
        // 1st. render pass, draw objects as normal, writing to the stencil buffer
        // --------------------------------------------------------------------
        GL.stencilMode(stencilMode2);
        GL.stencilMask(0xFF);
        // cubes
        Texture.bind(cubeTexture, 0);
        model.identity();
        model.translate(new Vector3d(-1.0, 0.0, -1.0));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    
        model.identity();
        model.translate(new Vector3d(2.0, 0.0, 0.0));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    
        // 2nd. render pass: now draw slightly scaled versions of the objects, this time disabling stencil writing.
        // Because the stencil buffer is now filled with several 1s. The parts of the buffer that are 1 are not drawn, thus only drawing
        // the objects' size differences, making it look like borders.
        // -----------------------------------------------------------------------------------------------------------------------------
        GL.stencilMode(stencilMode3);
        GL.stencilMask(0x00);
        GL.depthMode(DepthMode.NONE);
        Program.bind(shaderSingleColor);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);

        float scale = 1.1f;
        // cubes
        Texture.bind(cubeTexture, 0);
        model.identity();
        model.translate(new Vector3d(-1.0, 0.0, -1.0));
        model.scale(new Vector3d(scale, scale, scale));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);

        model.identity();
        model.translate(new Vector3d(2.0, 0.0, 0.0));
        model.scale(new Vector3d(scale, scale, scale));
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        cubeTexture.delete();
        planeTexture.delete();
        
        cubeVAO.delete();
        planeVAO.delete();
        
        shader.delete();
        shaderSingleColor.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_421_StencilTesting();
        
        start(instance);
    }
}
