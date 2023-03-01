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

public class LOGL_260_MultipleLights extends Engine
{
    Program lightingShader;
    Program lightCubeShader;
    
    VertexArray cubeVAO;
    VertexArray lightCubeVAO;
    
    Texture2D diffuseMap;
    Texture2D specularMap;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Vector3d[] cubePositions;
    Vector3d[] pointLightPositions;
    
    // lighting
    Vector3d lightPos = new Vector3d(1.2, 1.0, 2.0);
    
    protected LOGL_260_MultipleLights()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 2.6.0 - Multiple Lights");
        
        mouseCapture();
        
        String _multiple_lights_vs = """
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
        String _multiple_lights_fg = """
                                     #version 330 core
                                     out vec4 FragColor;
                                     
                                     struct Material {
                                         sampler2D diffuse;
                                         sampler2D specular;
                                         float shininess;
                                     };
                                     
                                     struct DirLight {
                                         vec3 direction;
                                     	
                                         vec3 ambient;
                                         vec3 diffuse;
                                         vec3 specular;
                                     };
                                     
                                     struct PointLight {
                                         vec3 position;
                                         
                                         float constant;
                                         float linear;
                                         float quadratic;
                                     	
                                         vec3 ambient;
                                         vec3 diffuse;
                                         vec3 specular;
                                     };
                                     
                                     struct SpotLight {
                                         vec3 position;
                                         vec3 direction;
                                         float cutOff;
                                         float outerCutOff;
                                         
                                         float constant;
                                         float linear;
                                         float quadratic;
                                         
                                         vec3 ambient;
                                         vec3 diffuse;
                                         vec3 specular;
                                     };
                                     
                                     #define NR_POINT_LIGHTS 4
                                     
                                     in vec3 FragPos;
                                     in vec3 Normal;
                                     in vec2 TexCoords;
                                     
                                     uniform vec3 viewPos;
                                     uniform DirLight dirLight;
                                     uniform PointLight pointLights[NR_POINT_LIGHTS];
                                     uniform SpotLight spotLight;
                                     uniform Material material;
                                     
                                     // function prototypes
                                     vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir);
                                     vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
                                     vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir);
                                     
                                     void main()
                                     {
                                         // properties
                                         vec3 norm = normalize(Normal);
                                         vec3 viewDir = normalize(viewPos - FragPos);
                                         
                                         // == =====================================================
                                         // Our lighting is set up in 3 phases: directional, point lights and an optional flashlight
                                         // For each phase, a calculate function is defined that calculates the corresponding color
                                         // per lamp. In the main() function we take all the calculated colors and sum them up for
                                         // this fragment's final color.
                                         // == =====================================================
                                         // phase 1: directional lighting
                                         vec3 result = CalcDirLight(dirLight, norm, viewDir);
                                         // phase 2: point lights
                                         for(int i = 0; i < NR_POINT_LIGHTS; i++)
                                             result += CalcPointLight(pointLights[i], norm, FragPos, viewDir);
                                         // phase 3: spot light
                                         result += CalcSpotLight(spotLight, norm, FragPos, viewDir);
                                         
                                         FragColor = vec4(result, 1.0);
                                     }
                                     
                                     // calculates the color when using a directional light.
                                     vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir)
                                     {
                                         vec3 lightDir = normalize(-light.direction);
                                         // diffuse shading
                                         float diff = max(dot(normal, lightDir), 0.0);
                                         // specular shading
                                         vec3 reflectDir = reflect(-lightDir, normal);
                                         float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
                                         // combine results
                                         vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
                                         vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
                                         vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
                                         return (ambient + diffuse + specular);
                                     }
                                     
                                     // calculates the color when using a point light.
                                     vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
                                     {
                                         vec3 lightDir = normalize(light.position - fragPos);
                                         // diffuse shading
                                         float diff = max(dot(normal, lightDir), 0.0);
                                         // specular shading
                                         vec3 reflectDir = reflect(-lightDir, normal);
                                         float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
                                         // attenuation
                                         float distance = length(light.position - fragPos);
                                         float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
                                         // combine results
                                         vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
                                         vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
                                         vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
                                         ambient *= attenuation;
                                         diffuse *= attenuation;
                                         specular *= attenuation;
                                         return (ambient + diffuse + specular);
                                     }
                                     
                                     // calculates the color when using a spot light.
                                     vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir)
                                     {
                                         vec3 lightDir = normalize(light.position - fragPos);
                                         // diffuse shading
                                         float diff = max(dot(normal, lightDir), 0.0);
                                         // specular shading
                                         vec3 reflectDir = reflect(-lightDir, normal);
                                         float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
                                         // attenuation
                                         float distance = length(light.position - fragPos);
                                         float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
                                         // spotlight intensity
                                         float theta = dot(lightDir, normalize(-light.direction));
                                         float epsilon = light.cutOff - light.outerCutOff;
                                         float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
                                         // combine results
                                         vec3 ambient = light.ambient * vec3(texture(material.diffuse, TexCoords));
                                         vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, TexCoords));
                                         vec3 specular = light.specular * spec * vec3(texture(material.specular, TexCoords));
                                         ambient *= attenuation * intensity;
                                         diffuse *= attenuation * intensity;
                                         specular *= attenuation * intensity;
                                         return (ambient + diffuse + specular);
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
        
        Shader colors_vs     = new Shader(ShaderType.VERTEX, _multiple_lights_vs);
        Shader colors_fg     = new Shader(ShaderType.FRAGMENT, _multiple_lights_fg);
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
        
        pointLightPositions = new Vector3d[] {
                new Vector3d(0.7, 0.2, 2.0), new Vector3d(2.3, -3.3, -4.0), new Vector3d(-4.0, 2.0, -12.0), new Vector3d(0.0, 0.0, -3.f)
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
        Program.uniformFloat3("viewPos", camera.Position);
        
        /*
           Here we set all the uniforms for the 5/6 types of lights we have. We have to set them manually and index
           the proper PointLight struct in the array to set each uniform variable. This can be done more code-friendly
           by defining light types as classes and set their values in there, or by using a more efficient uniform approach
           by using 'Uniform buffer objects', but that is something we'll discuss in the 'Advanced GLSL' tutorial.
        */
        // directional light
        Program.uniformFloat3("dirLight.direction", -0.2, -1.0, -0.3);
        Program.uniformFloat3("dirLight.ambient", 0.05, 0.05, 0.05);
        Program.uniformFloat3("dirLight.diffuse", 0.4, 0.4, 0.4);
        Program.uniformFloat3("dirLight.specular", 0.5, 0.5, 0.5);
        // point light 1
        Program.uniformFloat3("pointLights[0].position", pointLightPositions[0]);
        Program.uniformFloat3("pointLights[0].ambient", 0.05, 0.05, 0.05);
        Program.uniformFloat3("pointLights[0].diffuse", 0.8, 0.8, 0.8);
        Program.uniformFloat3("pointLights[0].specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("pointLights[0].constant", 1.0);
        Program.uniformFloat("pointLights[0].linear", 0.09);
        Program.uniformFloat("pointLights[0].quadratic", 0.032);
        // point light 2
        Program.uniformFloat3("pointLights[1].position", pointLightPositions[1]);
        Program.uniformFloat3("pointLights[1].ambient", 0.05, 0.05, 0.05);
        Program.uniformFloat3("pointLights[1].diffuse", 0.8, 0.8, 0.8);
        Program.uniformFloat3("pointLights[1].specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("pointLights[1].constant", 1.0);
        Program.uniformFloat("pointLights[1].linear", 0.09);
        Program.uniformFloat("pointLights[1].quadratic", 0.032);
        // point light 3
        Program.uniformFloat3("pointLights[2].position", pointLightPositions[2]);
        Program.uniformFloat3("pointLights[2].ambient", 0.05, 0.05, 0.05);
        Program.uniformFloat3("pointLights[2].diffuse", 0.8, 0.8, 0.8);
        Program.uniformFloat3("pointLights[2].specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("pointLights[2].constant", 1.0);
        Program.uniformFloat("pointLights[2].linear", 0.09);
        Program.uniformFloat("pointLights[2].quadratic", 0.032);
        // point light 4
        Program.uniformFloat3("pointLights[3].position", pointLightPositions[3]);
        Program.uniformFloat3("pointLights[3].ambient", 0.05, 0.05, 0.05);
        Program.uniformFloat3("pointLights[3].diffuse", 0.8, 0.8, 0.8);
        Program.uniformFloat3("pointLights[3].specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("pointLights[3].constant", 1.0);
        Program.uniformFloat("pointLights[3].linear", 0.09);
        Program.uniformFloat("pointLights[3].quadratic", 0.032);
        // spotLight
        Program.uniformFloat3("spotLight.position", camera.Position);
        Program.uniformFloat3("spotLight.direction", camera.Front);
        Program.uniformFloat3("spotLight.ambient", 0.0, 0.0, 0.0);
        Program.uniformFloat3("spotLight.diffuse", 1.0, 1.0, 1.0);
        Program.uniformFloat3("spotLight.specular", 1.0, 1.0, 1.0);
        Program.uniformFloat("spotLight.constant", 1.0);
        Program.uniformFloat("spotLight.linear", 0.09);
        Program.uniformFloat("spotLight.quadratic", 0.032);
        Program.uniformFloat("spotLight.cutOff", Math.cos(Math.toRadians(12.5)));
        Program.uniformFloat("spotLight.outerCutOff", Math.cos(Math.toRadians(15.0)));
        
        // material properties
        Program.uniformInt("material.diffuse", 0);
        Program.uniformInt("material.specular", 1);
        Program.uniformFloat("material.shininess", 64.0);
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        
        Matrix4d model = new Matrix4d();
        
        // render containers
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
        Program.bind(lightCubeShader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        for (int i = 0; i < 4; i++)
        {
            model.identity();
            model.translate(pointLightPositions[i]);
            model.scale(new Vector3d(0.2)); // Make it a smaller cube
            Program.uniformMatrix4("model", false, model);
            
            lightCubeVAO.draw(DrawMode.TRIANGLES, 36);
        }
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
        Engine instance = new LOGL_260_MultipleLights();
        
        start(instance);
    }
}
