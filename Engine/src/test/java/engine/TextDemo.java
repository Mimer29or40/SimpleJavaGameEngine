package engine;

import engine.gl.Framebuffer;
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
import engine.util.Logger;
import org.joml.Matrix4d;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class TextDemo extends Engine
{
    protected FloatBuffer posBuffer;
    protected FloatBuffer texBuffer;
    protected VertexArray vertexArray;
    
    protected Program program;
    
    protected final Matrix4d view = new Matrix4d();
    
    protected TextDemo()
    {
        super("Text Demo", 640, 400);
        
        this.updateFreq = 120;
        this.drawFreq   = 60;
    }
    
    @Override
    protected void setup()
    {
        int quadCount = 8192;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
        
        this.posBuffer = MemoryUtil.memAllocFloat(vertexCount * 3); // 3 floats per position
        this.texBuffer = MemoryUtil.memAllocFloat(vertexCount * 2); // 2 floats per texcoord
        
        this.posBuffer.put(-0.5F).put(-0.5F).put(0F);
        this.texBuffer.put(0F).put(0F);
        
        this.posBuffer.put(-0.5F).put(+0.5F).put(0F);
        this.texBuffer.put(0F).put(1F);
        
        this.posBuffer.put(+0.5F).put(+0.5F).put(0F);
        this.texBuffer.put(1F).put(1F);
        
        this.posBuffer.put(+0.5F).put(-0.5F).put(0F);
        this.texBuffer.put(1F).put(0F);
        
        IntBuffer indices = MemoryUtil.memCallocInt(quadCount * 6); // 6 indices per quad
        for (int i = 0; i < quadCount; ++i)
        {
            indices.put(4 * i);
            indices.put(4 * i + 1);
            indices.put(4 * i + 2);
            indices.put(4 * i);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
        }
        
        VertexAttribute pos = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute tex = new VertexAttribute(GLType.FLOAT, 2, false);
        
        this.vertexArray = VertexArray.builder()
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.posBuffer.clear(), pos)
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.texBuffer.clear(), tex)
                                      .indexBuffer(BufferUsage.STATIC_DRAW, indices.clear())
                                      .build();
        
        String vertCode = """
                          #version 440 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 1) in vec2 aTex;
                          out vec2 tex;
                          //uniform mat4 view;
                          void main()
                          {
                              gl_Position = vec4(aPos, 1.0);
                              tex = aTex;
                          }
                          """;
        String fragCode = """
                          #version 440 core
                          //in vec2 tex;
                          out vec4 FragColor;
                          //uniform sampler2D fontTexture;
                          void main()
                          {
                              //FragColor = texture(fontTexture, tex);
                              FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        this.program = new Program(vertShader, fragShader);
        
        vertShader.delete();
        fragShader.delete();
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        Framebuffer.bind(Framebuffer.NULL);
    
        GL.clearColor(0.2, 0.3, 0.3, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR);
        
        GL.defaultState();
        
        Framebuffer fb = Framebuffer.get();
        
        //Texture.bind(this.font.texture, 0);
        
        Program.bind(this.program);
        Program.uniformInt("fontTexture", 0);
        Program.uniformMatrix4("view", false, this.view.setOrtho(0, fb.width(), fb.height(), 0, -1, 1));
        
        VertexArray.bind(this.vertexArray);
        //this.vertexArray.buffer(0).set(0, this.posBuffer.flip());
        //this.vertexArray.buffer(1).set(0, this.texBuffer.flip());
        this.vertexArray.drawElements(DrawMode.TRIANGLES);
        
        this.posBuffer.clear();
        this.texBuffer.clear();
    }
    
    @Override
    protected void destroy()
    {
        MemoryUtil.memFree(this.posBuffer);
        MemoryUtil.memFree(this.texBuffer);
        this.vertexArray.delete();
        this.program.delete();
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        
        Engine instance = new TextDemo();
        start(instance);
    }
}
