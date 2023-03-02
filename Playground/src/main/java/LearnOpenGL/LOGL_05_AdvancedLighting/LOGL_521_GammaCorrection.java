package LearnOpenGL.LOGL_05_AdvancedLighting;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Image;
import engine.Key;
import engine.gl.*;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_521_GammaCorrection extends Engine
{
    Program shader;
    
    VertexArray planeVAO;
    
    Texture2D planeTexture;
    Texture2D planeTextureGammaCorrected;
    
    Camera  camera       = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    boolean gammaEnabled = false;
    
    // lighting info
    // -------------
    //@formatter:off
    Vector3d[] lightPositions = {
            new Vector3d(-3.0, 0.0, 0.0),
            new Vector3d(-1.0, 0.0, 0.0),
            new Vector3d(1.0, 0.0, 0.0),
            new Vector3d(3.0, 0.0, 0.0)
    };
    Vector3d[] lightColors    = {
            new Vector3d(0.25),
            new Vector3d(0.50),
            new Vector3d(0.75),
            new Vector3d(1.00)
    };
    //@formatter:on
    
    protected LOGL_521_GammaCorrection()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.2.1 - Gamma Correction");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 1) in vec3 aNormal;
                            layout (location = 2) in vec2 aTexCoords;
                            
                            out VS_OUT {
                                vec3 FragPos;
                                vec3 Normal;
                                vec2 TexCoords;
                            } vs_out;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            
                            void main()
                            {
                                vs_out.FragPos = aPos;
                                vs_out.Normal = aNormal;
                                vs_out.TexCoords = aTexCoords;
                                gl_Position = projection * view * vec4(aPos, 1.0);
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in VS_OUT {
                                vec3 FragPos;
                                vec3 Normal;
                                vec2 TexCoords;
                            } fs_in;
                            
                            uniform sampler2D floorTexture;
                            
                            uniform vec3 lightPositions[4];
                            uniform vec3 lightColors[4];
                            uniform vec3 viewPos;
                            uniform bool gamma;
                            
                            vec3 BlinnPhong(vec3 normal, vec3 fragPos, vec3 lightPos, vec3 lightColor)
                            {
                                // diffuse
                                vec3 lightDir = normalize(lightPos - fragPos);
                                float diff = max(dot(lightDir, normal), 0.0);
                                vec3 diffuse = diff * lightColor;
                                // specular
                                vec3 viewDir = normalize(viewPos - fragPos);
                                vec3 reflectDir = reflect(-lightDir, normal);
                                float spec = 0.0;
                                vec3 halfwayDir = normalize(lightDir + viewDir);
                                spec = pow(max(dot(normal, halfwayDir), 0.0), 64.0);
                                vec3 specular = spec * lightColor;
                                // simple attenuation
                                float max_distance = 1.5;
                                float distance = length(lightPos - fragPos);
                                float attenuation = 1.0 / (gamma ? distance * distance : distance);
                                
                                diffuse *= attenuation;
                                specular *= attenuation;
                                
                                return diffuse + specular;
                            }
                            
                            void main()
                            {
                                vec3 color = texture(floorTexture, fs_in.TexCoords).rgb;
                                vec3 lighting = vec3(0.0);
                                for(int i = 0; i < 4; ++i)
                                    lighting += BlinnPhong(normalize(fs_in.Normal), fs_in.FragPos, lightPositions[i], lightColors[i]);
                                color *= lighting;
                                if(gamma)
                                    color = pow(color, vec3(1.0/2.2));
                                FragColor = vec4(color, 1.0);
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
        float[] planeVertices = {
                // positions            // normals         // texcoords
                +10.0f, -0.5f, +10.0f,  +0.0f, +1.0f, +0.0f,  +10.0f,  +0.0f,
                -10.0f, -0.5f, +10.0f,  +0.0f, +1.0f, +0.0f,   +0.0f,  +0.0f,
                -10.0f, -0.5f, -10.0f,  +0.0f, +1.0f, +0.0f,   +0.0f, +10.0f,
        
                +10.0f, -0.5f, +10.0f,  +0.0f, +1.0f, +0.0f,  +10.0f,  +0.0f,
                -10.0f, -0.5f, -10.0f,  +0.0f, +1.0f, +0.0f,   +0.0f, +10.0f,
                +10.0f, -0.5f, -10.0f,  +0.0f, +1.0f, +0.0f,  +10.0f, +10.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute normal   = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texture  = new VertexAttribute(GLType.FLOAT, 2, false);
            
            FloatBuffer planeBuffer = stack.floats(planeVertices);
            
            planeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, planeBuffer, position, normal, texture).build();
        }
        
        Image planeImage = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"));
        
        planeTexture = new Texture2D(planeImage);
        planeTexture.genMipmaps();
        planeTextureGammaCorrected = new Texture2D(planeImage, true);
        planeTextureGammaCorrected.genMipmaps();
        
        planeImage.delete();
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
        GL.DEFAULT_STATE.blendMode = BlendMode.ALPHA;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) GL.polygonMode(PolygonMode.FILL);
        if (keyboardKeyDown(Key.F2)) GL.polygonMode(PolygonMode.LINE);
        if (keyboardKeyDown(Key.SPACE)) gammaEnabled = !gammaEnabled;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.ALWAYS);
        
        // draw objects
        Program.bind(shader);
        Program.uniformInt("floorTexture", 0);
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        // set light uniforms
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer positions = stack.mallocFloat(lightPositions.length * 3);
            for (int i = 0, index = 0; i < lightPositions.length; i++, index += 3) lightPositions[i].get(index, positions);
            Program.uniformFloat3v("lightPositions", positions);
            
            FloatBuffer colors = stack.mallocFloat(lightColors.length * 3);
            for (int i = 0, index = 0; i < lightColors.length; i++, index += 3) lightColors[i].get(index, colors);
            Program.uniformFloat3v("lightColors", colors);
        }
        //glUniform3fv(glGetUniformLocation(shader.ID, "lightPositions"), 4, &lightPositions[0][0]);
        //glUniform3fv(glGetUniformLocation(shader.ID, "lightColors"), 4, &lightColors[0][0]);
        Program.uniformFloat3("viewPos", camera.Position);
        Program.uniformBool("gamma", gammaEnabled);
        
        // floor
        Texture.bind(gammaEnabled ? planeTextureGammaCorrected : planeTexture, 0);
        planeVAO.draw(DrawMode.TRIANGLES, 6);
    }
    
    @Override
    protected void destroy()
    {
        planeTexture.delete();
        planeTextureGammaCorrected.delete();
        
        planeVAO.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_521_GammaCorrection();
        
        start(instance);
    }
}
