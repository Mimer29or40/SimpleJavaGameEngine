package LearnOpenGL.LOGL_02_Lighting;

import LearnOpenGL.Camera;
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
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_254_LightCastersSpotSoft extends Engine
{
    Program lightingShader;
    Program lightCubeShader;
    
    VertexArray cubeVAO;
    VertexArray lightCubeVAO;
    
    Texture2D diffuseMap;
    Texture2D specularMap;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Vector3d[] cubePositions;
    
    protected LOGL_254_LightCastersSpotSoft()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 2.5.4 - Light Casters Spot Soft");
        
        mouseCapture();
        
        String _light_casters_vs = """
                                   #version 330 core
                                   layout (location = 0) in vec3 aPos;
                                   layout (location = 1) in vec3 aNormal;
                                   layout (location = 2) in vec2 aTexCoords;
                                   
                                   out vec3 FragPos;
                                   out vec3 Normal;
                                   out vec2 TexCoords;
                                   
                                   uniform mat4 model;
                                   uniform mat4 view;
                                   uniform mat4 projection;
                                   
                                   void main()
                                   {
                                       FragPos = vec3(model * vec4(aPos, 1.0));
                                       Normal = mat3(transpose(inverse(model))) * aNormal;
                                       TexCoords = aTexCoords;
                                       
                                       gl_Position = projection * view * vec4(FragPos, 1.0);
                                   }
                                   """;
        String _light_casters_fg = """
                                   #version 330 core
                                   out vec4 FragColor;
                                   
                                   struct Material {
                                       sampler2D diffuse;
                                       sampler2D specular;
                                       float shininess;
                                   };
                                   
                                   struct Light {
                                       vec3 position;
                                       vec3 direction;
                                       float cutOff;
                                       float outerCutOff;
                                       
                                       vec3 ambient;
                                       vec3 diffuse;
                                       vec3 specular;
                                   	
                                       float constant;
                                       float linear;
                                       float quadratic;
                                   };
                                   
                                   in vec3 FragPos;
                                   in vec3 Normal;
                                   in vec2 TexCoords;
                                   
                                   uniform vec3 viewPos;
                                   uniform Material material;
                                   uniform Light light;
                                   
                                   void main()
                                   {
                                       // ambient
                                       vec3 ambient = light.ambient * texture(material.diffuse, TexCoords).rgb;
                                       
                                       // diffuse
                                       vec3 norm = normalize(Normal);
                                       vec3 lightDir = normalize(light.position - FragPos);
                                       float diff = max(dot(norm, lightDir), 0.0);
                                       vec3 diffuse = light.diffuse * diff * texture(material.diffuse, TexCoords).rgb;
                                       
                                       // specular
                                       vec3 viewDir = normalize(viewPos - FragPos);
                                       vec3 reflectDir = reflect(-lightDir, norm);
                                       float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
                                       vec3 specular = light.specular * spec * texture(material.specular, TexCoords).rgb;
                                       
                                       // spotlight (soft edges)
                                       float theta = dot(lightDir, normalize(-light.direction));
                                       float epsilon = (light.cutOff - light.outerCutOff);
                                       float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
                                       diffuse  *= intensity;
                                       specular *= intensity;
                                       
                                       // attenuation
                                       float distance    = length(light.position - FragPos);
                                       float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
                                       ambient  *= attenuation;
                                       diffuse   *= attenuation;
                                       specular *= attenuation;
                                       
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
        
        Shader colors_vs     = new Shader(ShaderType.VERTEX, _light_casters_vs);
        Shader colors_fg     = new Shader(ShaderType.FRAGMENT, _light_casters_fg);
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
                // positions          // normals            // texture coords
                -0.5f, -0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  0.0f, 0.0f,
                +0.5f, -0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  1.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  1.0f, 1.0f,
                -0.5f, +0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, +0.0f, -1.0f,  0.0f, 0.0f,
        
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  0.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  1.0f, 1.0f,
                -0.5f, +0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  +0.0f, +0.0f, +1.0f,  0.0f, 0.0f,
        
                -0.5f, +0.5f, +0.5f,  -1.0f, +0.0f, +0.0f,  1.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  -1.0f, +0.0f, +0.0f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  -1.0f, +0.0f, +0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  -1.0f, +0.0f, +0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  -1.0f, +0.0f, +0.0f,  0.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  -1.0f, +0.0f, +0.0f,  1.0f, 0.0f,
        
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f, +0.0f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  +1.0f, +0.0f, +0.0f,  1.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  +1.0f, +0.0f, +0.0f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  +1.0f, +0.0f, +0.0f,  0.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  +1.0f, +0.0f, +0.0f,  0.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  +1.0f, +0.0f, +0.0f,  1.0f, 0.0f,
        
                -0.5f, -0.5f, -0.5f,  +0.0f, -1.0f, +0.0f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  +0.0f, -1.0f, +0.0f,  1.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  +0.0f, -1.0f, +0.0f,  1.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  +0.0f, -1.0f, +0.0f,  1.0f, 0.0f,
                -0.5f, -0.5f, +0.5f,  +0.0f, -1.0f, +0.0f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  +0.0f, -1.0f, +0.0f,  0.0f, 1.0f,
        
                -0.5f, +0.5f, -0.5f,  +0.0f, +1.0f, +0.0f,  0.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  +0.0f, +1.0f, +0.0f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  +0.0f, +1.0f, +0.0f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  +0.0f, +1.0f, +0.0f,  1.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  +0.0f, +1.0f, +0.0f,  0.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  +0.0f, +1.0f, +0.0f,  0.0f, 1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer _vertices = stack.floats(vertices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute normal   = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
            
            cubeVAO      = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, normal, texCoord).build();
            lightCubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, normal, texCoord).build();
        }
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
        
        Image _diffuseMap  = new Image(IOUtil.getPath("LearnOpenGL/textures/container2.png"));
        Image _specularMap = new Image(IOUtil.getPath("LearnOpenGL/textures/container2_specular.png"));
        
        diffuseMap = new Texture2D(_diffuseMap);
        diffuseMap.genMipmaps();
        diffuseMap.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        diffuseMap.filter(TextureFilter.NEAREST_MIPMAP_LINEAR, TextureFilter.NEAREST);
        
        specularMap = new Texture2D(_specularMap);
        specularMap.genMipmaps();
        specularMap.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        specularMap.filter(TextureFilter.NEAREST_MIPMAP_LINEAR, TextureFilter.NEAREST);
        
        _diffuseMap.delete();
        _specularMap.delete();
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
        
        // bind diffuse map
        Texture.bind(diffuseMap, 0);
        Texture.bind(specularMap, 1);
        
        Program.bind(lightingShader);
        Program.uniformFloat3("light.position", camera.Position);
        Program.uniformFloat3("light.direction", camera.Front);
        Program.uniformFloat("light.cutOff", Math.cos(Math.toRadians(12.5)));
        Program.uniformFloat("light.outerCutOff", Math.cos(Math.toRadians(17.5)));
        Program.uniformFloat3("viewPos", camera.Position);
        
        // light properties
        Program.uniformFloat3("light.ambient", 0.1, 0.1, 0.1);
        // we configure the diffuse intensity slightly higher; the right lighting conditions differ with each lighting method and environment.
        // each environment and lighting type requires some tweaking to get the best out of your environment.
        Program.uniformFloat3("light.diffuse", 0.8, 0.8, 0.8);
        Program.uniformFloat3("light.specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("light.constant", 1.0);
        Program.uniformFloat("light.linear", 0.09);
        Program.uniformFloat("light.quadratic", 0.032);
        
        // material properties
        Program.uniformInt("material.diffuse", 0);
        Program.uniformInt("material.specular", 1);
        Program.uniformFloat("material.shininess", 64.0);
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
    
        Matrix4d model = new Matrix4d();
        
        // render the cube
        for (int i = 0; i < 10; i++)
        {
            model.identity();
            // calculate the model matrix for each object and pass it to shader before drawing
            model.translate(cubePositions[i]);
            float angle = 20.0f * i;
            model.rotate(Math.toRadians(angle), new Vector3d(1.0, 0.3, 0.5).normalize());
            Program.uniformMatrix4("model", false, model);
    
            cubeVAO.draw(DrawMode.TRIANGLES, 36);
        }

        // also draw the lamp object
        //Program.bind(lightCubeShader);
        //Program.uniformMatrix4("projection", false, projection);
        //Program.uniformMatrix4("view", false, view);
        //model.identity();
        //model.translate(lightPos);
        //model.scale(0.2); // a smaller cube
        //Program.uniformMatrix4("model", false, model);
        
        lightCubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    @Override
    protected void destroy()
    {
        diffuseMap.delete();
        specularMap.delete();
        
        cubeVAO.delete();
        lightCubeVAO.delete();
        
        lightingShader.delete();
        lightCubeShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_254_LightCastersSpotSoft();
        
        start(instance);
    }
}
