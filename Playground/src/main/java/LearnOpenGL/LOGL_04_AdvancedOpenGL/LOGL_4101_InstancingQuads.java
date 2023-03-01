package LearnOpenGL.LOGL_04_AdvancedOpenGL;

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
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.keyboardKeyDown;
import static engine.IO.windowTitle;

public class LOGL_4101_InstancingQuads extends Engine
{
    Program shader;
    
    VertexArray quadVAO;
    
    protected LOGL_4101_InstancingQuads()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.10.1 - Instancing Quads");
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec2 aPos;
                            layout (location = 1) in vec3 aColor;
                            layout (location = 2) in vec2 aOffset;
                            
                            out vec3 fColor;
                            
                            void main()
                            {
                                fColor = aColor;
                                gl_Position = vec4(aPos + aOffset, 0.0, 1.0);
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec3 fColor;
                            
                            void main()
                            {
                                FragColor = vec4(fColor, 1.0);
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
        float[] quadVertices = {
                // positions     // colors
                -0.05f, +0.05f,  1.0f, 0.0f, 0.0f,
                +0.05f, -0.05f,  0.0f, 1.0f, 0.0f,
                -0.05f, -0.05f,  0.0f, 0.0f, 1.0f,
            
                -0.05f, +0.05f,  1.0f, 0.0f, 0.0f,
                +0.05f, -0.05f,  0.0f, 1.0f, 0.0f,
                +0.05f, +0.05f,  0.0f, 1.0f, 1.0f
        };
        float[] translations = new float[100 * 2];
        int index = 0;
        float offset = 0.1f;
        for (int y = -10; y < 10; y += 2)
        {
            for (int x = -10; x < 10; x += 2)
            {
                translations[index++] = (float)x / 10.0f + offset;
                translations[index++] = (float)y / 10.0f + offset;
            }
        }
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute float3          = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute float2          = new VertexAttribute(GLType.FLOAT, 2, false);
            VertexAttribute float2Instanced = new VertexAttribute(GLType.FLOAT, 2, false, 1);
            
            FloatBuffer quadBuffer        = stack.floats(quadVertices);
            FloatBuffer translationBuffer = stack.floats(translations);
            
            quadVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, quadBuffer, float2, float3).buffer(BufferUsage.STATIC_DRAW, translationBuffer, float2Instanced).build();
        }
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        Program.bind(shader);
        
        quadVAO.drawInstanced(DrawMode.TRIANGLES, 6, 100);
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_4101_InstancingQuads();
        
        start(instance);
    }
}
