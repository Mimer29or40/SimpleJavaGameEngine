package LearnOpenGL.LOGL_05_AdvancedLighting;

import LearnOpenGL.Camera;
import engine.Engine;
import engine.Image;
import engine.Key;
import engine.color.ColorFormat;
import engine.gl.*;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureFilter;
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

public class LOGL_561_HDR extends Engine
{
    Program hdrShader;
    Program shader;
    
    Texture2D woodTexture;
    
    Framebuffer hdrFBO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting info
    // -------------
    // positions
    Vector3d[] lightPositions = { //@formatter:off
            new Vector3d(+0.0, +0.0, +49.5), // back light
            new Vector3d(-1.4, -1.9, +9.0),
            new Vector3d(+0.0, -1.8, +4.0),
            new Vector3d(+0.8, -1.7, +6.0)
    };//@formatter:on
    // colors
    Vector3d[] lightColors = { //@formatter:off
            new Vector3d(200.0, 200.0, 200.0),
            new Vector3d(200.0, 0.0, 0.0),
            new Vector3d(0.0, 0.0, 200.0),
            new Vector3d(0.0, 200.0, 0.0)
    };//@formatter:on
    
    boolean hdr      = true;
    double  exposure = 1.0;
    
    protected LOGL_561_HDR()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.6.1 - HDR");
        
        mouseCapture();
        
        String _shader_vs = """
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
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec2 TexCoords;
                            
                            uniform sampler2D hdrBuffer;
                            uniform bool hdr;
                            uniform float exposure;
                            
                            void main()
                            {
                                const float gamma = 2.2;
                                vec3 hdrColor = texture(hdrBuffer, TexCoords).rgb;
                                if(hdr)
                                {
                                    // reinhard
                                    // vec3 result = hdrColor / (hdrColor + vec3(1.0));
                                    // exposure
                                    vec3 result = vec3(1.0) - exp(-hdrColor * exposure);
                                    // also gamma correct while we're at it
                                    result = pow(result, vec3(1.0 / gamma));
                                    FragColor = vec4(result, 1.0);
                                }
                                else
                                {
                                    vec3 result = pow(hdrColor, vec3(1.0 / gamma));
                                    FragColor = vec4(result, 1.0);
                                }
                            }
                            """;
        
        String _lighting_vs = """
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
                              uniform mat4 model;
                              
                              uniform bool inverse_normals;
                              
                              void main()
                              {
                                  vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
                                  vs_out.TexCoords = aTexCoords;
                                  
                                  vec3 n = inverse_normals ? -aNormal : aNormal;
                                  
                                  mat3 normalMatrix = transpose(inverse(mat3(model)));
                                  vs_out.Normal = normalize(normalMatrix * n);
                                  
                                  gl_Position = projection * view * model * vec4(aPos, 1.0);
                              }
                              """;
        String _lighting_fg = """
                              #version 330 core
                              out vec4 FragColor;
                              
                              in VS_OUT {
                                  vec3 FragPos;
                                  vec3 Normal;
                                  vec2 TexCoords;
                              } fs_in;
                              
                              struct Light {
                                  vec3 Position;
                                  vec3 Color;
                              };
                              
                              uniform Light lights[16];
                              uniform sampler2D diffuseTexture;
                              uniform vec3 viewPos;
                              
                              void main()
                              {
                                  vec3 color = texture(diffuseTexture, fs_in.TexCoords).rgb;
                                  vec3 normal = normalize(fs_in.Normal);
                                  // ambient
                                  vec3 ambient = 0.0 * color;
                                  // lighting
                                  vec3 lighting = vec3(0.0);
                                  for(int i = 0; i < 16; i++)
                                  {
                                      // diffuse
                                      vec3 lightDir = normalize(lights[i].Position - fs_in.FragPos);
                                      float diff = max(dot(lightDir, normal), 0.0);
                                      vec3 diffuse = lights[i].Color * diff * color;
                                      vec3 result = diffuse;
                                      // attenuation (use quadratic as we have gamma correction)
                                      float distance = length(fs_in.FragPos - lights[i].Position);
                                      result *= 1.0 / (distance * distance);
                                      lighting += result;
                                  }
                                  FragColor = vec4(ambient + lighting, 1.0);
                              }
                              """;
        
        Shader shader_vs   = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg   = new Shader(ShaderType.FRAGMENT, _shader_fg);
        Shader lighting_vs = new Shader(ShaderType.VERTEX, _lighting_vs);
        Shader lighting_fg = new Shader(ShaderType.FRAGMENT, _lighting_fg);
        
        hdrShader = new Program(shader_vs, shader_fg);
        shader    = new Program(lighting_vs, lighting_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        lighting_vs.delete();
        lighting_fg.delete();
        
        Image image = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"), false);
        
        woodTexture = new Texture2D(image, true);
        woodTexture.genMipmaps();
        
        image.delete();
        
        int x = windowFramebufferSize().x();
        int y = windowFramebufferSize().y();
        hdrFBO = Framebuffer.builder(x, y).color(new Texture2D(ColorFormat.RGBA_16F, x, y)).depth().build();
        hdrFBO.color(0).filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        
        // shader configuration
        // --------------------
        Program.bind(shader);
        Program.uniformInt("diffuseTexture", 0);
        Program.bind(hdrShader);
        Program.uniformInt("hdrBuffer", 0);
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.H)) hdr = !hdr;
        
        if (keyboardKeyDown(Key.Q) || keyboardKeyRepeated(Key.Q))
        {
            if (exposure > 0.0f) {exposure -= 0.001f;}
            else {exposure = 0.0f;}
        }
        if (keyboardKeyDown(Key.E) || keyboardKeyRepeated(Key.E)) exposure += 0.001f;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        // render
        // ------
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        //GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Matrix4d  model      = new Matrix4d();
        
        // 1. render scene into floating point framebuffer
        // -----------------------------------------------
        Framebuffer.bind(hdrFBO);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Texture.bind(woodTexture, 0);
        // set lighting uniforms
        for (int i = 0; i < lightPositions.length; i++)
        {
            Program.uniformFloat3("lights[" + i + "].Position", lightPositions[i]);
            Program.uniformFloat3("lights[" + i + "].Color", lightColors[i]);
        }
        Program.uniformFloat3("viewPos", camera.Position);
        
        // render tunnel
        model.identity();
        model.translate(0.0, 0.0, 25.0);
        model.scale(2.5, 2.5, 27.5);
        Program.uniformMatrix4("model", false, model);
        Program.uniformBool("inverse_normals", true);
        renderCube();
        
        // 2. now render floating point color buffer to 2D quad and tonemap HDR colors to default framebuffer's (clamped) color range
        // --------------------------------------------------------------------------------------------------------------------------
        Framebuffer.bind(Framebuffer.NULL);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        Program.bind(hdrShader);
        Texture.bind(hdrFBO.color(0), 0);
        Program.uniformBool("hdr", hdr);
        Program.uniformFloat("exposure", exposure);
        renderQuad();
    }
    
    // renderCube() renders a 1x1 3D cube in NDC.
    // -------------------------------------------------
    VertexArray cubeVAO = null;
    
    void renderCube()
    {
        // initialize (if necessary)
        if (cubeVAO == null)
        {
            float[] vertices = { //@formatter:off
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
                    +1.0f, +1.0f , 1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f, // bottom-right
                    +1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 1.0f, // top-right
                    +1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f, // bottom-right
                    -1.0f, +1.0f, -1.0f, +0.0f, +1.0f, +0.0f, 0.0f, 1.0f, // top-left
                    -1.0f, +1.0f, +1.0f, +0.0f, +1.0f, +0.0f, 0.0f, 0.0f  // bottom-left
            }; //@formatter:on
            
            // setup cube VAO
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer buffer = stack.floats(vertices);
                
                VertexAttribute pos  = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute norm = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute tex  = new VertexAttribute(GLType.FLOAT, 2, false);
                
                cubeVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, pos, norm, tex).build();
            }
        }
        
        // render Cube
        cubeVAO.draw(DrawMode.TRIANGLES, 36);
    }
    
    // renders a 1x1 quad in NDC with manually calculated tangent vectors
    // ------------------------------------------------------------------
    VertexArray quadVAO = null;
    
    void renderQuad()
    {
        if (quadVAO == null)
        {
            float[] vertices = { //@formatter:off
                    // positions         // texture Coords
                    -1.0f, +1.0f, +0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, +0.0f, 0.0f, 0.0f,
                    +1.0f, +1.0f, +0.0f, 1.0f, 1.0f,
                    +1.0f, -1.0f, +0.0f, 1.0f, 0.0f
            }; //@formatter:on
            
            // setup plane VAO
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer buffer = stack.floats(vertices);
                
                VertexAttribute pos = new VertexAttribute(GLType.FLOAT, 3, false);
                VertexAttribute tex = new VertexAttribute(GLType.FLOAT, 2, false);
                
                quadVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, buffer, pos, tex).build();
            }
        }
        quadVAO.draw(DrawMode.TRIANGLE_STRIP, 4);
    }
    
    @Override
    protected void destroy()
    {
        hdrFBO.delete();
        
        if (quadVAO != null) quadVAO.delete();
        if (cubeVAO != null) cubeVAO.delete();
        
        woodTexture.delete();
        
        hdrShader.delete();
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_561_HDR();
        
        start(instance);
    }
}
