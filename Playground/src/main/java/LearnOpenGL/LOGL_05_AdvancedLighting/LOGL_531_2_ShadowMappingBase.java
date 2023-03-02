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

public class LOGL_531_2_ShadowMappingBase extends Engine
{
    Program shader;
    Program depthShader;
    Program debugDepthShader;
    
    VertexArray planeVAO;
    
    Texture2D woodTexture;
    
    Framebuffer depthMapFBO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting info
    // -------------
    Vector3d lightPos = new Vector3d(-2.0f, 4.0f, -1.0f);
    
    protected LOGL_531_2_ShadowMappingBase()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.3.1.2 - Shadow Mapping Base");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 1) in vec3 aNormal;
                            layout (location = 2) in vec2 aTexCoords;
                            
                            out vec2 TexCoords;
                            
                            out VS_OUT {
                                vec3 FragPos;
                                vec3 Normal;
                                vec2 TexCoords;
                                vec4 FragPosLightSpace;
                            } vs_out;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            uniform mat4 model;
                            uniform mat4 lightSpaceMatrix;
                            
                            void main()
                            {
                                vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
                                vs_out.Normal = transpose(inverse(mat3(model))) * aNormal;
                                vs_out.TexCoords = aTexCoords;
                                vs_out.FragPosLightSpace = lightSpaceMatrix * vec4(vs_out.FragPos, 1.0);
                                gl_Position = projection * view * model * vec4(aPos, 1.0);
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in VS_OUT {
                                vec3 FragPos;
                                vec3 Normal;
                                vec2 TexCoords;
                                vec4 FragPosLightSpace;
                            } fs_in;
                            
                            uniform sampler2D diffuseTexture;
                            uniform sampler2D shadowMap;
                            
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            
                            float ShadowCalculation(vec4 fragPosLightSpace)
                            {
                                // perform perspective divide
                                vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
                                // transform to [0,1] range
                                projCoords = projCoords * 0.5 + 0.5;
                                // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
                                float closestDepth = texture(shadowMap, projCoords.xy).r;
                                // get depth of current fragment from light's perspective
                                float currentDepth = projCoords.z;
                                // check whether current frag pos is in shadow
                                float shadow = currentDepth > closestDepth  ? 1.0 : 0.0;
                                
                                return shadow;
                            }
                            
                            void main()
                            {
                                vec3 color = texture(diffuseTexture, fs_in.TexCoords).rgb;
                                vec3 normal = normalize(fs_in.Normal);
                                vec3 lightColor = vec3(0.3);
                                // ambient
                                vec3 ambient = 0.3 * lightColor;
                                // diffuse
                                vec3 lightDir = normalize(lightPos - fs_in.FragPos);
                                float diff = max(dot(lightDir, normal), 0.0);
                                vec3 diffuse = diff * lightColor;
                                // specular
                                vec3 viewDir = normalize(viewPos - fs_in.FragPos);
                                vec3 reflectDir = reflect(-lightDir, normal);
                                float spec = 0.0;
                                vec3 halfwayDir = normalize(lightDir + viewDir);
                                spec = pow(max(dot(normal, halfwayDir), 0.0), 64.0);
                                vec3 specular = spec * lightColor;
                                // calculate shadow
                                float shadow = ShadowCalculation(fs_in.FragPosLightSpace);
                                vec3 lighting = (ambient + (1.0 - shadow) * (diffuse + specular)) * color;
                                
                                FragColor = vec4(lighting, 1.0);
                            }
                            """;
        
        String _depth_vs = """
                           #version 330 core
                           layout (location = 0) in vec3 aPos;
                           
                           uniform mat4 lightSpaceMatrix;
                           uniform mat4 model;
                           
                           void main()
                           {
                               gl_Position = lightSpaceMatrix * model * vec4(aPos, 1.0);
                           }
                           """;
        String _depth_fg = """
                           #version 330 core
                           
                           void main()
                           {
                               // gl_FragDepth = gl_FragCoord.z;
                           }
                           """;
        
        String _debug_vs = """
                           #version 330 core
                           layout (location = 0) in vec3 aPos;
                           layout (location = 1) in vec2 aTexCoords;
                           
                           out vec2 TexCoords;
                           
                           void main()
                           {
                               TexCoords = aTexCoords;
                               gl_Position = vec4(aPos, 1.0);
                           }
                           """;
        String _debug_fg = """
                           #version 330 core
                           out vec4 FragColor;
                           
                           in vec2 TexCoords;
                           
                           uniform sampler2D depthMap;
                           uniform float near_plane;
                           uniform float far_plane;
                           
                           // required when using a perspective projection matrix
                           float LinearizeDepth(float depth)
                           {
                               float z = depth * 2.0 - 1.0; // Back to NDC
                               return (2.0 * near_plane * far_plane) / (far_plane + near_plane - z * (far_plane - near_plane));
                           }
                           
                           void main()
                           {
                               float depthValue = texture(depthMap, TexCoords).r;
                               // FragColor = vec4(vec3(LinearizeDepth(depthValue) / far_plane), 1.0); // perspective
                               FragColor = vec4(vec3(depthValue), 1.0); // orthographic
                           }
                           """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        Shader depth_vs  = new Shader(ShaderType.VERTEX, _depth_vs);
        Shader depth_fg  = new Shader(ShaderType.FRAGMENT, _depth_fg);
        Shader debug_vs  = new Shader(ShaderType.VERTEX, _debug_vs);
        Shader debug_fg  = new Shader(ShaderType.FRAGMENT, _debug_fg);
        
        shader           = new Program(shader_vs, shader_fg);
        depthShader      = new Program(depth_vs, depth_fg);
        debugDepthShader = new Program(debug_vs, debug_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        depth_vs.delete();
        depth_fg.delete();
        debug_vs.delete();
        debug_fg.delete();
        
        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
        //@formatter:off
        float[] planeVertices = {
                // positions            // normals            // texcoords
                +25.0f, -0.5f, +25.0f,  +0.0f, +1.0f, +0.0f,  +25.0f,  +0.0f,
                -25.0f, -0.5f, +25.0f,  +0.0f, +1.0f, +0.0f,   +0.0f,  +0.0f,
                -25.0f, -0.5f, -25.0f,  +0.0f, +1.0f, +0.0f,   +0.0f, +25.0f,
        
                +25.0f, -0.5f, +25.0f,  +0.0f, +1.0f, +0.0f,  +25.0f,  +0.0f,
                -25.0f, -0.5f, -25.0f,  +0.0f, +1.0f, +0.0f,   +0.0f, +25.0f,
                +25.0f, -0.5f, -25.0f,  +0.0f, +1.0f, +0.0f,  +25.0f, +25.0f
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
        
        Image image = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"));
        
        woodTexture = new Texture2D(image);
        woodTexture.genMipmaps();
        
        image.delete();
        
        final int x = 1024, y = 1024;
        depthMapFBO = Framebuffer.builder(x, y).depth().build();
        Texture.bind(depthMapFBO.depth());
        depthMapFBO.depth().filter(TextureFilter.NEAREST, TextureFilter.NEAREST);
        depthMapFBO.depth().wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
    
        // shader configuration
        // --------------------
        Program.bind(shader);
        Program.uniformInt("diffuseTexture", 0);
        Program.uniformInt("shadowMap", 1);
        Program.bind(debugDepthShader);
        Program.uniformInt("depthMap", 0);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) GL.polygonMode(PolygonMode.FILL);
        if (keyboardKeyDown(Key.F2)) GL.polygonMode(PolygonMode.LINE);
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        // render
        // ------
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        //GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
    
        // 1. render depth of scene to texture (from light's perspective)
        // --------------------------------------------------------------
        Matrix4d lightProjection  = new Matrix4d(), lightView = new Matrix4d();
        Matrix4d lightSpaceMatrix = new Matrix4d();
    
        double near_plane = 1.0, far_plane = 7.5;
        lightProjection.setOrtho(-10.0, 10.0, -10.0, 10.0, near_plane, far_plane);
        lightView.setLookAt(lightPos, new Vector3d(0.0f), new Vector3d(0.0, 1.0, 0.0));
        lightProjection.mul(lightView, lightSpaceMatrix);
        
        // render scene from light's point of view
        Program.bind(depthShader);
        Program.uniformMatrix4("lightSpaceMatrix", false, lightSpaceMatrix);
    
        Framebuffer.bind(depthMapFBO);
        GL.clearBuffers(ScreenBuffer.DEPTH);
        Texture.bind(woodTexture, 0);
        renderScene();
    
        // reset viewport
        Framebuffer.bind(Framebuffer.NULL);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
    
        // 2. render scene as normal using the generated depth/shadow map
        // --------------------------------------------------------------
        Program.bind(shader);
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view = camera.GetViewMatrix();
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        
        // set light uniforms
        Program.uniformFloat3("viewPos", camera.Position);
        Program.uniformFloat3("lightPos", lightPos);
        Program.uniformMatrix4("lightSpaceMatrix", false, lightSpaceMatrix);
        
        Texture.bind(woodTexture, 0);
        Texture.bind(depthMapFBO.depth(), 1);
        renderScene();
    
        // render Depth map to quad for visual debugging
        // ---------------------------------------------
        Program.bind(debugDepthShader);
        Program.uniformFloat("near_plane", near_plane);
        Program.uniformFloat("far_plane", far_plane);
        Program.uniformInt("depthMap", 0);
        Texture.bind(depthMapFBO.depth());
        //renderQuad();
    }
    
    // renders the 3D scene
    // --------------------
    void renderScene()
    {
        // floor
        Matrix4d model = new Matrix4d();
        Program.uniformMatrix4("model", false, model);
        planeVAO.draw(DrawMode.TRIANGLES, 6);
        
        // cubes
        model.identity();
        model.translate(new Vector3d(0.0f, 1.5f, 0.0));
        model.scale(new Vector3d(0.5f));
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(2.0f, 0.0f, 1.0));
        model.scale(new Vector3d(0.5f));
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(-1.0f, 0.0f, 2.0));
        model.rotate(Math.toRadians(60.0f), new Vector3d(1.0, 0.0, 1.0).normalize());
        model.scale(new Vector3d(0.25));
        Program.uniformMatrix4("model", false, model);
        renderCube();
    }
    
    
    // renderCube() renders a 1x1 3D cube in NDC.
    // -------------------------------------------------
    VertexArray cubeVAO = null;
    
    void renderCube()
    {
        // initialize (if necessary)
        if (cubeVAO == null)
        {
            float[] vertices = {
                    // back face
                    -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 0.0f, 0.0f, // bottom-left
                    +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 1.0f, 1.0f, // top-right
                    +1.0f, -1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 1.0f, 0.0f, // bottom-right
                    +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 1.0f, 1.0f, // top-right
                    -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 0.0f, 0.0f, // bottom-left
                    -1.0f, +1.0f, -1.0f, +0.0f, +0.0f, -1.0f, 0.0f, 1.0f, // top-left
                    // front face
                    -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 0.0f, 0.0f, // bottom-left
                    +1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 1.0f, 0.0f, // bottom-right
                    +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 1.0f, 1.0f, // top-right
                    +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 1.0f, 1.0f, // top-right
                    -1.0f, +1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 0.0f, 1.0f, // top-left
                    -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, +1.0f, 0.0f, 0.0f, // bottom-left
                    // left face
                    -1.0f, +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, 1.0f, 0.0f, // top-right
                    -1.0f, +1.0f, -1.0f, -1.0f, +0.0f, +0.0f, 1.0f, 1.0f, // top-left
                    -1.0f, -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, 0.0f, 1.0f, // bottom-left
                    -1.0f, -1.0f, -1.0f, -1.0f, +0.0f, +0.0f, 0.0f, 1.0f, // bottom-left
                    -1.0f, -1.0f, +1.0f, -1.0f, +0.0f, +0.0f, 0.0f, 0.0f, // bottom-right
                    -1.0f, +1.0f, +1.0f, -1.0f, +0.0f, +0.0f, 1.0f, 0.0f, // top-right
                    // right face
                    +1.0f, +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, 1.0f, 0.0f, // top-left
                    +1.0f, -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, 0.0f, 1.0f, // bottom-right
                    +1.0f, +1.0f, -1.0f, +1.0f, +0.0f, +0.0f, 1.0f, 1.0f, // top-right
                    +1.0f, -1.0f, -1.0f, +1.0f, +0.0f, +0.0f, 0.0f, 1.0f, // bottom-right
                    +1.0f, +1.0f, +1.0f, +1.0f, +0.0f, +0.0f, 1.0f, 0.0f, // top-left
                    +1.0f, -1.0f, +1.0f, +1.0f, +0.0f, +0.0f, 0.0f, 0.0f, // bottom-left
                    // bottom face
                    -1.0f, -1.0f, -1.0f, +0.0f, -1.0f, +0.0f, 0.0f, 1.0f, // top-right
                    +1.0f, -1.0f, -1.0f, +0.0f, -1.0f, +0.0f, 1.0f, 1.0f, // top-left
                    +1.0f, -1.0f, +1.0f, +0.0f, -1.0f, +0.0f, 1.0f, 0.0f, // bottom-left
                    +1.0f, -1.0f, +1.0f, +0.0f, -1.0f, +0.0f, 1.0f, 0.0f, // bottom-left
                    -1.0f, -1.0f, +1.0f, +0.0f, -1.0f, +0.0f, 0.0f, 0.0f, // bottom-right
                    -1.0f, -1.0f, -1.0f, +0.0f, -1.0f, +0.0f, 0.0f, 1.0f, // top-right
                    // top face
                    -1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, 0.0f, 1.0f, // top-left
                    +1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f, // bottom-right
                    +1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 1.0f, // top-right
                    +1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f, // bottom-right
                    -1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, 0.0f, 1.0f, // top-left
                    -1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, 0.0f, 0.0f  // bottom-left
            };
            
            // setup cube VAO
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute normal   = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute texture  = new VertexAttribute(GLType.FLOAT, 2, false);
                
                FloatBuffer buffer = stack.floats(vertices);
                
                cubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, position, normal, texture).build();
            }
        }
        // render Cube
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    // renderQuad() renders a 1x1 XY quad in NDC
    // -----------------------------------------
    VertexArray quadVAO = null;
    
    void renderQuad()
    {
        if (quadVAO == null)
        {
            float[] vertices = {
                    // positions         // texture Coords
                    -1.0f, +1.0f, +0.0f, 0.0f, 1.0f, // Vertex 0
                    -1.0f, -1.0f, +0.0f, 0.0f, 0.0f, // Vertex 1
                    +1.0f, +1.0f, +0.0f, 1.0f, 1.0f, // Vertex 2
                    +1.0f, -1.0f, +0.0f, 1.0f, 0.0f, // Vertex 3
            };
            
            // setup plane VAO
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute texture  = new VertexAttribute(GLType.FLOAT, 2, false);
                
                FloatBuffer buffer = stack.floats(vertices);
                
                quadVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, position, texture).build();
            }
        }
        
        // render quad
        quadVAO.draw(DrawMode.TRIANGLE_STRIP, 4);
    }
    
    @Override
    protected void destroy()
    {
        woodTexture.delete();
        
        planeVAO.delete();
    
        shader.delete();
        depthShader.delete();
        debugDepthShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_531_2_ShadowMappingBase();
        
        start(instance);
    }
}
