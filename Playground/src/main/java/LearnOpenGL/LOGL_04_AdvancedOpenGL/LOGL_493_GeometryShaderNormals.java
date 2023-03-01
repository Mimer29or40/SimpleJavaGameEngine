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
import org.joml.Matrix4dc;
import org.joml.Vector3d;

import static engine.IO.*;

public class LOGL_493_GeometryShaderNormals extends Engine
{
    Program shader;
    Program normalShader;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Model model;
    
    protected LOGL_493_GeometryShaderNormals()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.9.3 - Geometry Shader Normals");
        
        mouseCapture();
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 1) in vec3 aNormal;
                          
                          out VS_OUT {
                              vec3 normal;
                          } vs_out;
                          
                          uniform mat4 view;
                          uniform mat4 model;
                          
                          void main()
                          {
                              mat3 normalMatrix = mat3(transpose(inverse(view * model)));
                              vs_out.normal = vec3(vec4(normalMatrix * aNormal, 0.0));
                              gl_Position = view * model * vec4(aPos, 1.0);\s
                          }
                          """;
        String geomCode = """
                          #version 330 core
                          layout (triangles) in;
                          layout (line_strip, max_vertices = 6) out;
                          
                          in VS_OUT {
                              vec3 normal;
                          } gs_in[];
                          
                          const float MAGNITUDE = 0.2;
                          
                          uniform mat4 projection;
                          
                          void GenerateLine(int index)
                          {
                              gl_Position = projection * gl_in[index].gl_Position;
                              EmitVertex();
                              gl_Position = projection * (gl_in[index].gl_Position + vec4(gs_in[index].normal, 0.0) * MAGNITUDE);
                              EmitVertex();
                              EndPrimitive();
                          }
                          
                          void main()
                          {
                              GenerateLine(0); // first vertex normal
                              GenerateLine(1); // second vertex normal
                              GenerateLine(2); // third vertex normal
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          void main()
                          {
                              FragColor = vec4(1.0, 1.0, 0.0, 1.0);
                          }
                          """;
        
        String _default_vs = """
                             #version 330 core
                             layout (location = 0) in vec3 aPos;
                             layout (location = 2) in vec2 aTexCoords;
                             
                             out vec2 TexCoords;
                             
                             uniform mat4 projection;
                             uniform mat4 view;
                             uniform mat4 model;
                             
                             void main()
                             {
                                 TexCoords = aTexCoords;
                                 gl_Position = projection * view * model * vec4(aPos, 1.0);
                             }
                             """;
        String _default_fs = """
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
        Shader default_vs = new Shader(ShaderType.VERTEX, _default_vs);
        Shader default_fs = new Shader(ShaderType.FRAGMENT, _default_fs);
        
        shader       = new Program(vertShader, geomShader, fragShader);
        normalShader = new Program(default_vs, default_fs);
        
        vertShader.delete();
        geomShader.delete();
        fragShader.delete();
        default_vs.delete();
        default_fs.delete();
        
        model = new Model(IOUtil.getPath("LearnOpenGL/objects/backpack/backpack.obj"));
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
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Matrix4d  model      = new Matrix4d();
        
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("model", false, model);
        this.model.Draw();
        
        Program.bind(normalShader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("model", false, model);
        this.model.Draw();
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
        normalShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_493_GeometryShaderNormals();
        
        start(instance);
    }
}
