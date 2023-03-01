package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import LearnOpenGL.Model;
import engine.Engine;
import engine.Key;
import engine.gl.DepthMode;
import engine.gl.GL;
import engine.gl.ScreenBuffer;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import static engine.IO.*;

public class LOGL_492_GeometryShaderExploding extends Engine
{
    Program shader;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Model model;
    
    protected LOGL_492_GeometryShaderExploding()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.9.2 - Geometry Shader Exploding");
        
        mouseCapture();
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 2) in vec2 aTexCoords;
                          
                          out VS_OUT {
                              vec2 texCoords;
                          } vs_out;
                          
                          uniform mat4 projection;
                          uniform mat4 view;
                          uniform mat4 model;
                          
                          void main()
                          {
                              vs_out.texCoords = aTexCoords;
                              gl_Position = projection * view * model * vec4(aPos, 1.0);
                          }
                          """;
        String geomCode = """
                          #version 330 core
                          layout (triangles) in;
                          layout (triangle_strip, max_vertices = 3) out;
                          
                          in VS_OUT {
                              vec2 texCoords;
                          } gs_in[];
                          
                          out vec2 TexCoords;
                          
                          uniform float time;
                          
                          vec4 explode(vec4 position, vec3 normal)
                          {
                              float magnitude = 2.0;
                              vec3 direction = normal * ((sin(time) + 1.0) / 2.0) * magnitude;
                              return position + vec4(direction, 0.0);
                          }
                          
                          vec3 GetNormal()
                          {
                              vec3 a = vec3(gl_in[0].gl_Position) - vec3(gl_in[1].gl_Position);
                              vec3 b = vec3(gl_in[2].gl_Position) - vec3(gl_in[1].gl_Position);
                              return normalize(cross(a, b));
                          }
                          
                          void main()
                          {
                              vec3 normal = GetNormal();
                              
                              gl_Position = explode(gl_in[0].gl_Position, normal);
                              TexCoords = gs_in[0].texCoords;
                              EmitVertex();
                              gl_Position = explode(gl_in[1].gl_Position, normal);
                              TexCoords = gs_in[1].texCoords;
                              EmitVertex();
                              gl_Position = explode(gl_in[2].gl_Position, normal);
                              TexCoords = gs_in[2].texCoords;
                              EmitVertex();
                              EndPrimitive();
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec2 TexCoords;
                          
                          uniform sampler2D texture_diffuse1;
                          
                          void main()
                          {
                              FragColor = texture(texture_diffuse1, TexCoords);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader geomShader = new Shader(ShaderType.GEOMETRY, geomCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        shader = new Program(vertShader, geomShader, fragShader);
        
        vertShader.delete();
        geomShader.delete();
        fragShader.delete();
        
        //model = new Model(IOUtil.getPath("LearnOpenGL/objects/backpack/backpack.obj"));
        model = new Model(IOUtil.getPath("LearnOpenGL/objects/nanosuit/nanosuit.obj"));
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
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
        
        Program.uniformFloat("time", time);
        
        Matrix4d model = new Matrix4d();
        Program.uniformMatrix4("model", false, model);
        this.model.Draw();
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_492_GeometryShaderExploding();
        
        start(instance);
    }
}
