package LearnOpenGL.LOGL_05_AdvancedLighting;

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
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_541_NormalMapping extends Engine
{
    Program shader;
    
    Texture2D diffuseMap;
    Texture2D normalMap;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting info
    // -------------
    Vector3d lightPos = new Vector3d(0.5f, 1.0f, 0.3f);
    
    protected LOGL_541_NormalMapping()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.4.1 - Normal Mapping");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 1) in vec3 aNormal;
                            layout (location = 2) in vec2 aTexCoords;
                            layout (location = 3) in vec3 aTangent;
                            layout (location = 4) in vec3 aBitangent;
                            
                            out VS_OUT {
                                vec3 FragPos;
                                vec2 TexCoords;
                                vec3 TangentLightPos;
                                vec3 TangentViewPos;
                                vec3 TangentFragPos;
                            } vs_out;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            uniform mat4 model;
                            
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            
                            void main()
                            {
                                vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
                                vs_out.TexCoords = aTexCoords;
                                
                                mat3 normalMatrix = transpose(inverse(mat3(model)));
                                vec3 T = normalize(normalMatrix * aTangent);
                                vec3 N = normalize(normalMatrix * aNormal);
                                T = normalize(T - dot(T, N) * N);
                                vec3 B = cross(N, T);
                                
                                mat3 TBN = transpose(mat3(T, B, N));
                                vs_out.TangentLightPos = TBN * lightPos;
                                vs_out.TangentViewPos  = TBN * viewPos;
                                vs_out.TangentFragPos  = TBN * vs_out.FragPos;
                                
                                gl_Position = projection * view * model * vec4(aPos, 1.0);
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in VS_OUT {
                                vec3 FragPos;
                                vec2 TexCoords;
                                vec3 TangentLightPos;
                                vec3 TangentViewPos;
                                vec3 TangentFragPos;
                            } fs_in;
                            
                            uniform sampler2D diffuseMap;
                            uniform sampler2D normalMap;
                            
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            
                            void main()
                            {
                                 // obtain normal from normal map in range [0,1]
                                vec3 normal = texture(normalMap, fs_in.TexCoords).rgb;
                                // transform normal vector to range [-1,1]
                                normal = normalize(normal * 2.0 - 1.0);  // this normal is in tangent space
                                
                                // get diffuse color
                                vec3 color = texture(diffuseMap, fs_in.TexCoords).rgb;
                                // ambient
                                vec3 ambient = 0.1 * color;
                                // diffuse
                                vec3 lightDir = normalize(fs_in.TangentLightPos - fs_in.TangentFragPos);
                                float diff = max(dot(lightDir, normal), 0.0);
                                vec3 diffuse = diff * color;
                                // specular
                                vec3 viewDir = normalize(fs_in.TangentViewPos - fs_in.TangentFragPos);
                                vec3 reflectDir = reflect(-lightDir, normal);
                                vec3 halfwayDir = normalize(lightDir + viewDir);
                                float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);
                                
                                vec3 specular = vec3(0.2) * spec;
                                FragColor = vec4(ambient + diffuse + specular, 1.0);
                            }
                            """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        
        shader = new Program(shader_vs, shader_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        
        Image diffuseImage = new Image(IOUtil.getPath("LearnOpenGL/textures/brickwall.jpg"), false);
        Image normalImage  = new Image(IOUtil.getPath("LearnOpenGL/textures/brickwall_normal.jpg"), false);
        
        diffuseMap = new Texture2D(diffuseImage);
        diffuseMap.genMipmaps();
        normalMap = new Texture2D(normalImage);
        normalMap.genMipmaps();
        
        diffuseImage.delete();
        normalImage.delete();
        
        // shader configuration
        // --------------------
        Program.bind(shader);
        Program.uniformInt("diffuseMap", 0);
        Program.uniformInt("normalMap", 1);
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
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
        // render
        // ------
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        // configure view/projection matrices
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        
        // render normal-mapped quad
        Matrix4d model = new Matrix4d();
        model.rotate(Math.toRadians(time * -10.0f), new Vector3d(1.0, 0.0, 1.0).normalize()); // rotate the quad to show normal mapping from multiple directions
        Program.uniformMatrix4("model", false, model);
        Program.uniformFloat3("viewPos", camera.Position);
        Program.uniformFloat3("lightPos", lightPos);
        Texture.bind(diffuseMap, 0);
        Texture.bind(normalMap, 1);
        renderQuad();
        
        // render light source (simply re-renders a smaller plane at the light's position for debugging/visualization)
        model.identity();
        model.translate(lightPos);
        model.scale(0.1);
        Program.uniformMatrix4("model", false, model);
        renderQuad();
    }
    
    // renders a 1x1 quad in NDC with manually calculated tangent vectors
    // ------------------------------------------------------------------
    VertexArray quadVAO = null;
    
    void renderQuad()
    {
        if (quadVAO == null)
        {
            // positions
            Vector3d[] pos = { //@formatter:off
                    new Vector3d(-1.0, +1.0, +0.0),
                    new Vector3d(-1.0, -1.0, +0.0),
                    new Vector3d(+1.0, -1.0, +0.0),
                    new Vector3d(+1.0, +1.0, +0.0)
            }; //@formatter:on
            
            // texture coordinates
            Vector2d[] uv = { //@formatter:off
                    new Vector2d(0.0, 1.0),
                    new Vector2d(0.0, 0.0),
                    new Vector2d(1.0, 0.0),
                    new Vector2d(1.0, 1.0)
            }; //@formatter:on
            
            // normal vector
            Vector3d nm = new Vector3d(0.0, 0.0, 1.0);
            
            // calculate tangent/bitangent vectors of both triangles
            Vector3d tangent1 = new Vector3d(), bitangent1 = new Vector3d();
            Vector3d tangent2 = new Vector3d(), bitangent2 = new Vector3d();
            
            // triangle 1
            // ----------
            Vector3d edge1    = pos[1].sub(pos[0], new Vector3d());
            Vector3d edge2    = pos[2].sub(pos[0], new Vector3d());
            Vector2d deltaUV1 = uv[1].sub(uv[0], new Vector2d());
            Vector2d deltaUV2 = uv[2].sub(uv[0], new Vector2d());
            
            double f = 1.0 / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
            
            tangent1.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
            tangent1.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
            tangent1.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
            tangent1.normalize();
            
            bitangent1.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
            bitangent1.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
            bitangent1.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
            bitangent1.normalize();
            
            // triangle 2
            // ----------
            edge1    = pos[2].sub(pos[0], new Vector3d());
            edge2    = pos[3].sub(pos[0], new Vector3d());
            deltaUV1 = uv[2].sub(uv[0], new Vector2d());
            deltaUV2 = uv[3].sub(uv[0], new Vector2d());
            
            f = 1.0 / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);
            
            tangent2.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
            tangent2.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
            tangent2.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
            tangent2.normalize();
            
            bitangent2.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
            bitangent2.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
            bitangent2.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
            bitangent2.normalize();
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer vertices = stack.mallocFloat((3 + 3 + 2 + 3 + 3) * 6);
                
                boolean tangentLatch = true;
                for (int i : new int[] {0, 1, 2, 0, 2, 3})
                {
                    vertices.put((float) pos[i].x).put((float) pos[i].y).put((float) pos[i].z);
                    vertices.put((float) nm.x).put((float) nm.y).put((float) nm.z);
                    vertices.put((float) uv[i].x).put((float) uv[i].y);
                    if (tangentLatch)
                    {
                        vertices.put((float) tangent1.x).put((float) tangent1.y).put((float) tangent1.z);
                        vertices.put((float) bitangent1.x).put((float) bitangent1.y).put((float) bitangent1.z);
                    }
                    else
                    {
                        vertices.put((float) tangent2.x).put((float) tangent2.y).put((float) tangent2.z);
                        vertices.put((float) bitangent2.x).put((float) bitangent2.y).put((float) bitangent2.z);
                    }
                    if (i == 2) tangentLatch = false;
                }
    
                // configure plane VAO
                VertexAttribute position  = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute normal    = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute texCoord  = new VertexAttribute(GLType.FLOAT, 2, false);
                VertexAttribute tangent   = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute bitangent = new VertexAttribute(GLType.FLOAT, 3, false);
    
                quadVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, vertices.clear(), position, normal, texCoord, tangent, bitangent).build();
            }
        }
        quadVAO.draw(DrawMode.TRIANGLES, 6);
    }
    
    @Override
    protected void destroy()
    {
        if (quadVAO != null) quadVAO.delete();
        
        diffuseMap.delete();
        normalMap.delete();
        
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_541_NormalMapping();
        
        start(instance);
    }
}
