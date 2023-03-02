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

public class LOGL_511_AdvancedLighting extends Engine
{
    Program shader;
    
    VertexArray planeVAO;
    
    Texture2D planeTexture;
    
    Camera  camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    boolean blinn  = false;
    
    // lighting info
    // -------------
    Vector3d lightPos = new Vector3d(0.0, 0.0, 0.0);
    
    protected LOGL_511_AdvancedLighting()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.1.1 - Depth Testing");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 1) in vec3 aNormal;
                            layout (location = 2) in vec2 aTexCoords;
                            
                            // declare an interface block; see 'Advanced GLSL' for what these are.
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
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            uniform bool blinn;
                            
                            void main()
                            {
                                vec3 color = texture(floorTexture, fs_in.TexCoords).rgb;
                                // ambient
                                vec3 ambient = 0.05 * color;
                                // diffuse
                                vec3 lightDir = normalize(lightPos - fs_in.FragPos);
                                vec3 normal = normalize(fs_in.Normal);
                                float diff = max(dot(lightDir, normal), 0.0);
                                vec3 diffuse = diff * color;
                                // specular
                                vec3 viewDir = normalize(viewPos - fs_in.FragPos);
                                vec3 reflectDir = reflect(-lightDir, normal);
                                float spec = 0.0;
                                if(blinn)
                                {
                                    vec3 halfwayDir = normalize(lightDir + viewDir);
                                    spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
                                }
                                else
                                {
                                    vec3 reflectDir = reflect(-lightDir, normal);
                                    spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
                                }
                                vec3 specular = vec3(0.3) * spec; // assuming bright white light color
                                FragColor = vec4(ambient + diffuse + specular, 1.0);
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
        
        planeImage.delete();
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
        GL.DEFAULT_STATE.blendMode = BlendMode.ALPHA;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        lightPos.y = 3.0 + Math.sin(time) * 2.0;
        
        if (keyboardKeyDown(Key.F1)) GL.polygonMode(PolygonMode.FILL);
        if (keyboardKeyDown(Key.F2)) GL.polygonMode(PolygonMode.LINE);
        if (keyboardKeyDown(Key.B)) blinn = !blinn;
        
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
        Program.uniformInt("texture1", 0);
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        // set light uniforms
        Program.uniformFloat3("viewPos", camera.Position);
        Program.uniformFloat3("lightPos", lightPos);
        Program.uniformBool("blinn", blinn);
        
        // floor
        Texture.bind(planeTexture, 0);
        planeVAO.draw(DrawMode.TRIANGLES, 6);
    }
    
    @Override
    protected void destroy()
    {
        planeTexture.delete();
        
        planeVAO.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_511_AdvancedLighting();
        
        start(instance);
    }
}
