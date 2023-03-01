package LearnOpenGL.LOGL_02_Lighting;

import LearnOpenGL.Camera;
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
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_231_Materials extends Engine
{
    Program lightingShader;
    Program lightCubeShader;
    
    VertexArray cubeVAO;
    VertexArray lightCubeVAO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting
    Vector3d lightPos = new Vector3d(1.2, 1.0, 2.0);
    
    protected LOGL_231_Materials()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 2.3.1 - Materials");
        
        mouseCapture();
        
        String _materials_vs = """
                               #version 330 core
                               layout (location = 0) in vec3 aPos;
                               layout (location = 1) in vec3 aNormal;
                               
                               out vec3 FragPos;
                               out vec3 Normal;
                               
                               uniform mat4 model;
                               uniform mat4 view;
                               uniform mat4 projection;
                               
                               void main()
                               {
                                   FragPos = vec3(model * vec4(aPos, 1.0));
                                   Normal = mat3(transpose(inverse(model))) * aNormal;
                                   
                                   gl_Position = projection * view * vec4(FragPos, 1.0);
                               }
                               """;
        String _materials_fg = """
                               #version 330 core
                               out vec4 FragColor;
                               
                               struct Material {
                                   vec3 ambient;
                                   vec3 diffuse;
                                   vec3 specular;
                                   float shininess;
                               };
                               
                               struct Light {
                                   vec3 position;
                               
                                   vec3 ambient;
                                   vec3 diffuse;
                                   vec3 specular;
                               };
                               
                               in vec3 FragPos;
                               in vec3 Normal;
                               
                               uniform vec3 viewPos;
                               uniform Material material;
                               uniform Light light;
                               
                               void main()
                               {
                                   // ambient
                                   vec3 ambient = light.ambient * material.ambient;
                                 	
                                   // diffuse
                                   vec3 norm = normalize(Normal);
                                   vec3 lightDir = normalize(light.position - FragPos);
                                   float diff = max(dot(norm, lightDir), 0.0);
                                   vec3 diffuse = light.diffuse * (diff * material.diffuse);
                                   
                                   // specular
                                   vec3 viewDir = normalize(viewPos - FragPos);
                                   vec3 reflectDir = reflect(-lightDir, norm);
                                   float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
                                   vec3 specular = light.specular * (spec * material.specular);
                                   
                                   vec3 result = ambient + diffuse + specular;
                                   FragColor = vec4(result, 1.0);
                               }
                               """;
        
        String _light_cube_vs = """
                                #version 330 core
                                layout (location = 0) in vec3 aPos;
                                
                                uniform mat4 model;
                                uniform mat4 view;
                                uniform mat4 projection;
                                
                                void main()
                                {
                                	gl_Position = projection * view * model * vec4(aPos, 1.0);
                                }
                                """;
        String _light_cube_fg = """
                                #version 330 core
                                out vec4 FragColor;
                                
                                void main()
                                {
                                    FragColor = vec4(1.0); // set all 4 vector values to 1.0
                                }
                                """;
        
        Shader colors_vs     = new Shader(ShaderType.VERTEX, _materials_vs);
        Shader colors_fg     = new Shader(ShaderType.FRAGMENT, _materials_fg);
        Shader light_cube_vs = new Shader(ShaderType.VERTEX, _light_cube_vs);
        Shader light_cube_fg = new Shader(ShaderType.FRAGMENT, _light_cube_fg);
        
        lightingShader  = new Program(colors_vs, colors_fg);
        lightCubeShader = new Program(light_cube_vs, light_cube_fg);
        
        colors_vs.delete();
        colors_fg.delete();
        light_cube_vs.delete();
        light_cube_fg.delete();
        
        //@formatter:off
        float[] vertices = {
                -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                -0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
        
                -0.5f, -0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
                -0.5f, +0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
                -0.5f, -0.5f, +0.5f, +0.0f, +0.0f,  1.0f,
        
                -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, +0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
        
                +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, +0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
        
                -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                -0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
        
                -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                -0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer _vertices = stack.floats(vertices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute normal   = new VertexAttribute(GLType.FLOAT, 3, false);
            
            cubeVAO      = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, normal).build();
            lightCubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, normal).build();
        }
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
        GL.clearColor(0.2, 0.3, 0.3, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        Program.bind(lightingShader);
        Program.uniformFloat3("light.position", lightPos);
        Program.uniformFloat3("viewPos", camera.Position);
    
        // light properties
        Vector3d lightColor = new Vector3d();
        lightColor.x = Math.sin(time * 2.0);
        lightColor.y = Math.sin(time * 0.7);
        lightColor.z = Math.sin(time * 1.3);
        Vector3d diffuseColor = lightColor.mul(0.5, new Vector3d()); // decrease the influence
        Vector3d ambientColor = diffuseColor.mul(0.2, new Vector3d()); // low influence
        Program.uniformFloat3("light.ambient", ambientColor);
        Program.uniformFloat3("light.diffuse", diffuseColor);
        Program.uniformFloat3("light.specular", 1.0, 1.0, 1.0);
    
        // material properties
        Program.uniformFloat3("material.ambient", 1.0, 0.5, 0.31);
        Program.uniformFloat3("material.diffuse", 1.0, 0.5, 0.31);
        Program.uniformFloat3("material.specular", 0.5, 0.5, 0.5); // specular lighting doesn't have full effect on this object's material
        Program.uniformFloat("material.shininess", 32.0);
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        
        // world transformation
        Matrix4d model = new Matrix4d();
        Program.uniformMatrix4("model", false, model);
        
        // render the cube
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
        
        // also draw the lamp object
        Program.bind(lightCubeShader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        model.identity();
        model.translate(lightPos);
        model.scale(0.2); // a smaller cube
        Program.uniformMatrix4("model", false, model);
        
        lightCubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        cubeVAO.delete();
        lightCubeVAO.delete();
        
        lightingShader.delete();
        lightCubeShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_231_Materials();
        
        start(instance);
    }
}
