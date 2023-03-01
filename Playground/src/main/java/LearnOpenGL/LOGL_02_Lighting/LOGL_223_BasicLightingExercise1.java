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

public class LOGL_223_BasicLightingExercise1 extends Engine
{
    Program lightingShader;
    Program lightCubeShader;
    
    VertexArray cubeVAO;
    VertexArray lightCubeVAO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting
    Vector3d lightPos = new Vector3d(1.2, 1.0, 2.0);
    
    protected LOGL_223_BasicLightingExercise1()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 2.2.3 - Basic Lighting Exercise 1");
        
        mouseCapture();
        
        String _shader_vs = """
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
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec3 Normal;
                            in vec3 FragPos;
                            
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            uniform vec3 lightColor;
                            uniform vec3 objectColor;
                            
                            void main()
                            {
                                // ambient
                                float ambientStrength = 0.1;
                                vec3 ambient = ambientStrength * lightColor;
                              	
                                // diffuse
                                vec3 norm = normalize(Normal);
                                vec3 lightDir = normalize(lightPos - FragPos);
                                float diff = max(dot(norm, lightDir), 0.0);
                                vec3 diffuse = diff * lightColor;
                              
                                // specular
                                float specularStrength = 0.5;
                                vec3 viewDir = normalize(viewPos - FragPos);
                                vec3 reflectDir = reflect(-lightDir, norm);
                                float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
                                vec3 specular = specularStrength * spec * lightColor;
                                
                                vec3 result = (ambient + diffuse + specular) * objectColor;
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
        
        Shader colors_vs     = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader colors_fg     = new Shader(ShaderType.FRAGMENT, _shader_fg);
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
    
        lightPos.x = 1.0f + Math.sin(time) * 2.0;
        lightPos.y = Math.sin(time / 2.0);
        
        Program.bind(lightingShader);
        Program.uniformFloat3("objectColor", 1.0, 0.5, 0.31);
        Program.uniformFloat3("lightColor", 1.0, 1.0, 1.0);
        Program.uniformFloat3("lightPos", lightPos);
        Program.uniformFloat3("viewPos", camera.Position);
        
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
        Engine instance = new LOGL_223_BasicLightingExercise1();
        
        start(instance);
    }
}