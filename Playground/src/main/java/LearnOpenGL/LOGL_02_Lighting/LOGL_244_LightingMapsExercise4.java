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

public class LOGL_244_LightingMapsExercise4 extends Engine
{
    Program lightingShader;
    Program lightCubeShader;
    
    VertexArray cubeVAO;
    VertexArray lightCubeVAO;
    
    Texture2D diffuseMap;
    Texture2D specularMap;
    Texture2D emissionMap;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting
    Vector3d lightPos = new Vector3d(1.2, 1.0, 2.0);
    
    protected LOGL_244_LightingMapsExercise4()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 2.4.4 - Lighting Maps Exercise 4");
        
        mouseCapture();
        
        String _lighting_maps_vs = """
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
        String _lighting_maps_fg = """
                                   #version 330 core
                                   out vec4 FragColor;
                                   
                                   struct Material {
                                       sampler2D diffuse;
                                       sampler2D specular;
                                       sampler2D emission;
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
                                       
                                       // emission
                                       vec3 emission = texture(material.emission, TexCoords).rgb;
                                       
                                       vec3 result = ambient + diffuse + specular + emission;
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
        
        Shader colors_vs     = new Shader(ShaderType.VERTEX, _lighting_maps_vs);
        Shader colors_fg     = new Shader(ShaderType.FRAGMENT, _lighting_maps_fg);
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
    
        Image _diffuseMap = new Image(IOUtil.getPath("LearnOpenGL/textures/container2.png"));
        Image _specularMap  = new Image(IOUtil.getPath("LearnOpenGL/textures/container2_specular.png"));
        Image _emissionMap  = new Image(IOUtil.getPath("LearnOpenGL/textures/matrix.jpg"));
        
        diffuseMap = new Texture2D(_diffuseMap);
        diffuseMap.genMipmaps();
        diffuseMap.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        diffuseMap.filter(TextureFilter.LINEAR_MIPMAP_LINEAR, TextureFilter.LINEAR);
        
        specularMap = new Texture2D(_specularMap);
        specularMap.genMipmaps();
        specularMap.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        specularMap.filter(TextureFilter.LINEAR_MIPMAP_LINEAR, TextureFilter.LINEAR);
        
        emissionMap = new Texture2D(_emissionMap);
        emissionMap.genMipmaps();
        emissionMap.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        emissionMap.filter(TextureFilter.LINEAR_MIPMAP_LINEAR, TextureFilter.LINEAR);
        
        _diffuseMap.delete();
        _specularMap.delete();
        _emissionMap.delete();
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
        Texture.bind(emissionMap, 2);
        
        Program.bind(lightingShader);
        Program.uniformFloat3("light.position", lightPos);
        Program.uniformFloat3("viewPos", camera.Position);
        
        // light properties
        Program.uniformFloat3("light.ambient", 0.2, 0.2, 0.2);
        Program.uniformFloat3("light.diffuse", 0.5, 0.5, 0.5);
        Program.uniformFloat3("light.specular", 1.0, 1.0, 1.0);
        
        // material properties
        Program.uniformInt("material.diffuse", 0);
        Program.uniformInt("material.specular", 1);
        Program.uniformInt("material.emission", 2);
        Program.uniformFloat("material.shininess", 64.0);
        
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
        diffuseMap.delete();
        specularMap.delete();
        emissionMap.delete();
        
        cubeVAO.delete();
        lightCubeVAO.delete();
        
        lightingShader.delete();
        lightCubeShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_244_LightingMapsExercise4();
        
        start(instance);
    }
}
