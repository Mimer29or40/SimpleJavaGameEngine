package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import engine.Engine;
import engine.gl.*;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.windowTitle;

public class LOGL_491_GeometryShaderHouses extends Engine
{
    Program shader;
    
    VertexArray VAO;
    
    protected LOGL_491_GeometryShaderHouses()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.9.1 - Geometry Shader Houses");
        
        GL.depthMode(DepthMode.LESS);
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec2 aPos;
                          layout (location = 1) in vec3 aColor;
                          
                          out VS_OUT {
                              vec3 color;
                          } vs_out;
                          
                          void main()
                          {
                              vs_out.color = aColor;
                              gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
                          }
                          """;
        String geomCode = """
                          #version 330 core
                          layout (points) in;
                          layout (triangle_strip, max_vertices = 5) out;
                          
                          in VS_OUT {
                              vec3 color;
                          } gs_in[];
                          
                          out vec3 fColor;
                          
                          void build_house(vec4 position)
                          {
                              fColor = gs_in[0].color; // gs_in[0] since there's only one input vertex
                              gl_Position = position + vec4(-0.2, -0.2, 0.0, 0.0); // 1:bottom-left
                              EmitVertex();
                              gl_Position = position + vec4( 0.2, -0.2, 0.0, 0.0); // 2:bottom-right
                              EmitVertex();
                              gl_Position = position + vec4(-0.2,  0.2, 0.0, 0.0); // 3:top-left
                              EmitVertex();
                              gl_Position = position + vec4( 0.2,  0.2, 0.0, 0.0); // 4:top-right
                              EmitVertex();
                              gl_Position = position + vec4( 0.0,  0.4, 0.0, 0.0); // 5:top
                              fColor = vec3(1.0, 1.0, 1.0);
                              EmitVertex();
                              EndPrimitive();
                          }
                          
                          void main() {
                              build_house(gl_in[0].gl_Position);
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec3 fColor;
                          
                          void main()
                          {
                              FragColor = vec4(fColor, 1.0);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader geomShader = new Shader(ShaderType.GEOMETRY, geomCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        shader = new Program(vertShader, geomShader, fragShader);
        
        vertShader.delete();
        geomShader.delete();
        fragShader.delete();
        
        float[] vertices = {
                -0.5f, +0.5f, 1.0f, 0.0f, 0.0f, // top-left
                +0.5f, +0.5f, 0.0f, 1.0f, 0.0f, // top-right
                +0.5f, -0.5f, 0.0f, 0.0f, 1.0f, // bottom-right
                -0.5f, -0.5f, 1.0f, 1.0f, 0.0f  // bottom-left
        };
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats(vertices);
            
            VertexAttribute pos   = new VertexAttribute(GLType.FLOAT, 2, false);
            VertexAttribute color = new VertexAttribute(GLType.FLOAT, 3, false);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, pos, color).build();
        }
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        Framebuffer.bind(Framebuffer.NULL);
        
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Program.bind(shader);
        VertexArray.bind(VAO); // seeing as we only have a single VAO there's no need to bind it every time, but we'll do so to keep things a bit more organized
        VAO.draw(DrawMode.POINTS, 4);
    }
    
    @Override
    protected void destroy()
    {
        VAO.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_491_GeometryShaderHouses();
        
        start(instance);
    }
}
