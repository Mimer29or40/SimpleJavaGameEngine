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
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;

public class LOGL_571_Bloom extends Engine // TODO - Get working https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/5.advanced_lighting/7.bloom/bloom.cpp
{
    Program shader;
    Program shaderLight;
    Program shaderBlur;
    Program shaderBloomFinal;
    
    Texture2D woodTexture;
    Texture2D containerTexture;
    
    Framebuffer   hdrFBO;
    Framebuffer[] pingpongFBO = new Framebuffer[2];
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting info
    // -------------
    // positions
    Vector3d[] lightPositions = { //@formatter:off
            new Vector3d(+0.0, +0.5, +1.5),
            new Vector3d(-4.0, +0.5, -3.0),
            new Vector3d(+3.0, +0.5, +1.0),
            new Vector3d(-0.8, +2.4, -1.0)
    };//@formatter:on
    // colors
    Vector3d[] lightColors = { //@formatter:off
            new Vector3d(+5.0, +5.0, +5.0),
            new Vector3d(10.0, +0.0, +0.0),
            new Vector3d(+0.0, +0.0, 15.0),
            new Vector3d(+0.0, +5.0, +0.0)
    };//@formatter:on
    
    boolean bloom    = true;
    double  exposure = 1.0;
    
    protected LOGL_571_Bloom()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.6.1 - HDR");
        
        mouseCapture();
        
        String _bloom_vs = """
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
                           
                           void main()
                           {
                               vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
                               vs_out.TexCoords = aTexCoords;
                               
                               mat3 normalMatrix = transpose(inverse(mat3(model)));
                               vs_out.Normal = normalize(normalMatrix * aNormal);
                               
                               gl_Position = projection * view * model * vec4(aPos, 1.0);
                           }
                           """;
        String _bloom_fg = """
                           #version 330 core
                           layout (location = 0) out vec4 FragColor;
                           layout (location = 1) out vec4 BrightColor;
                           
                           in VS_OUT {
                               vec3 FragPos;
                               vec3 Normal;
                               vec2 TexCoords;
                           } fs_in;
                           
                           struct Light {
                               vec3 Position;
                               vec3 Color;
                           };
                           
                           uniform Light lights[4];
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
                               vec3 viewDir = normalize(viewPos - fs_in.FragPos);
                               for(int i = 0; i < 4; i++)
                               {
                                   // diffuse
                                   vec3 lightDir = normalize(lights[i].Position - fs_in.FragPos);
                                   float diff = max(dot(lightDir, normal), 0.0);
                                   vec3 result = lights[i].Color * diff * color;
                                   // attenuation (use quadratic as we have gamma correction)
                                   float distance = length(fs_in.FragPos - lights[i].Position);
                                   result *= 1.0 / (distance * distance);
                                   lighting += result;
                               }
                               vec3 result = ambient + lighting;
                               // check whether result is higher than some threshold, if so, output as bloom threshold color
                               float brightness = dot(result, vec3(0.2126, 0.7152, 0.0722));
                               if(brightness > 1.0)
                                   BrightColor = vec4(result, 1.0);
                               else
                                   BrightColor = vec4(0.0, 0.0, 0.0, 1.0);
                               FragColor = vec4(result, 1.0);
                           }
                           """;
        
        String _bloom_final_vs = """
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
        String _bloom_final_fg = """
                                 #version 330 core
                                 out vec4 FragColor;
                                 
                                 in vec2 TexCoords;
                                 
                                 uniform sampler2D scene;
                                 uniform sampler2D bloomBlur;
                                 uniform bool bloom;
                                 uniform float exposure;
                                 
                                 void main()
                                 {
                                     const float gamma = 2.2;
                                     vec3 hdrColor = texture(scene, TexCoords).rgb;
                                     vec3 bloomColor = texture(bloomBlur, TexCoords).rgb;
                                     if(bloom)
                                         hdrColor += bloomColor; // additive blending
                                     // tone mapping
                                     vec3 result = vec3(1.0) - exp(-hdrColor * exposure);
                                     // also gamma correct while we're at it
                                     result = pow(result, vec3(1.0 / gamma));
                                     FragColor = vec4(result, 1.0);
                                 }
                                 """;
        
        String _blur_vs = """
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
        String _blur_fg = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec2 TexCoords;
                          
                          uniform sampler2D image;
                          
                          uniform bool horizontal;
                          uniform float weight[5] = float[] (0.2270270270, 0.1945945946, 0.1216216216, 0.0540540541, 0.0162162162);
                          
                          void main()
                          {
                               vec2 tex_offset = 1.0 / textureSize(image, 0); // gets size of single texel
                               vec3 result = texture(image, TexCoords).rgb * weight[0];
                               if(horizontal)
                               {
                                   for(int i = 1; i < 5; ++i)
                                   {
                                      result += texture(image, TexCoords + vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
                                      result += texture(image, TexCoords - vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
                                   }
                               }
                               else
                               {
                                   for(int i = 1; i < 5; ++i)
                                   {
                                       result += texture(image, TexCoords + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
                                       result += texture(image, TexCoords - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
                                   }
                               }
                               FragColor = vec4(result, 1.0);
                          }
                          """;
        
        String _light_box_fg = """
                               #version 330 core
                               layout (location = 0) out vec4 FragColor;
                               layout (location = 1) out vec4 BrightColor;
                               
                               in VS_OUT {
                                   vec3 FragPos;
                                   vec3 Normal;
                                   vec2 TexCoords;
                               } fs_in;
                               
                               uniform vec3 lightColor;
                               
                               void main()
                               {
                                   FragColor = vec4(lightColor, 1.0);
                                   float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
                                   if(brightness > 1.0)
                                       BrightColor = vec4(FragColor.rgb, 1.0);
                               	else
                               		BrightColor = vec4(0.0, 0.0, 0.0, 1.0);
                               }
                               """;
        
        Shader bloom_vs       = new Shader(ShaderType.VERTEX, _bloom_vs);
        Shader bloom_fg       = new Shader(ShaderType.FRAGMENT, _bloom_fg);
        Shader bloom_final_vs = new Shader(ShaderType.VERTEX, _bloom_final_vs);
        Shader bloom_final_fg = new Shader(ShaderType.FRAGMENT, _bloom_final_fg);
        Shader blur_vs        = new Shader(ShaderType.VERTEX, _blur_vs);
        Shader blur_fg        = new Shader(ShaderType.FRAGMENT, _blur_fg);
        Shader light_box_fg   = new Shader(ShaderType.FRAGMENT, _light_box_fg);
        
        shader           = new Program(bloom_vs, bloom_fg);
        shaderLight      = new Program(bloom_vs, light_box_fg);
        shaderBlur       = new Program(blur_vs, blur_fg);
        shaderBloomFinal = new Program(bloom_final_vs, bloom_final_fg);
        
        bloom_vs.delete();
        bloom_fg.delete();
        bloom_final_vs.delete();
        bloom_final_fg.delete();
        blur_vs.delete();
        blur_fg.delete();
        light_box_fg.delete();
        
        Image woodImage      = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"), false);
        Image containerImage = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"), false);
        
        woodTexture = new Texture2D(woodImage, true);
        woodTexture.genMipmaps();
        containerTexture = new Texture2D(containerImage, true);
        containerTexture.genMipmaps();
        
        woodImage.delete();
        containerImage.delete();
        
        int x = windowFramebufferSize().x();
        int y = windowFramebufferSize().y();
        hdrFBO = Framebuffer.builder(x, y).color(new Texture2D(ColorFormat.RGBA_16F, x, y)).color(new Texture2D(ColorFormat.RGBA_16F, x, y)).depth().build();
        hdrFBO.color(0).filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        hdrFBO.color(0).wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        hdrFBO.color(1).filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        hdrFBO.color(1).wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glDrawBuffers(stack.ints(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1));
        }
        for (int i = 0; i < pingpongFBO.length; i++)
        {
            pingpongFBO[i] = Framebuffer.builder(x, y).color(new Texture2D(ColorFormat.RGBA_16F, x, y)).build();
            pingpongFBO[i].color(0).filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
            pingpongFBO[i].color(0).wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        }
        
        // shader configuration
        // --------------------
        Program.bind(shader);
        Program.uniformInt("diffuseTexture", 0);
        Program.bind(shaderBlur);
        Program.uniformInt("image", 0);
        Program.bind(shaderBloomFinal);
        Program.uniformInt("scene", 0);
        Program.uniformInt("bloomBlur", 1);
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.B)) bloom = !bloom;
        
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
        GL.clearColor(0.0, 0.0, 0.0, 1.0);
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
        
        // create one large cube that acts as the floor
        model.identity();
        model.translate(0.0, -1.0f, 0.0);
        model.scale(12.5, 0.5, 12.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        // then create multiple cubes as the scenery
        Texture.bind(containerTexture, 0);
        model.identity();
        model.translate(0.0f, 1.5, 0.0);
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        model.identity();
        model.translate(2.0, 0.0, 1.0);
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        model.identity();
        model.translate(-1.0, -1.0, 2.0);
        model.rotate(Math.toRadians(60.0), new Vector3d(1.0, 0.0, 1.0).normalize());
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        model.identity();
        model.translate(0.0, 2.7, 4.0);
        model.rotate(Math.toRadians(23.0), new Vector3d(1.0, 0.0, 1.0).normalize());
        model.scale(1.25);
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        model.identity();
        model.translate(-2.0, 1.0, -3.0);
        model.rotate(Math.toRadians(124.0), new Vector3d(1.0, 0.0, 1.0).normalize());
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        model.identity();
        model.translate(-3.0, 0.0, 0.0);
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
    
        // finally show all the light sources as bright cubes
        Program.bind(shaderLight);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
    
        for (int i = 0; i < lightPositions.length; i++)
        {
            model.identity();
            model.translate(lightPositions[i]);
            model.scale(0.25);
            Program.uniformMatrix4("model", false, model);
            Program.uniformFloat3("lightColor", lightColors[i]);
            renderCube();
        }
    
        // 2. blur bright fragments with two-pass Gaussian Blur
        // --------------------------------------------------
        boolean horizontal = true, first_iteration = true;
        int amount = 10;
        Program.bind(shaderBlur);
        for (int i = 0; i < amount; i++)
        {
            Framebuffer.bind(pingpongFBO[horizontal ? 1 : 0]);
            Program.uniformBool("horizontal", horizontal);
            Texture.bind(first_iteration ? hdrFBO.color(1) : pingpongFBO[horizontal ? 0 : 1].color(0)); // bind texture of other framebuffer (or scene if first iteration)
            renderQuad();
            horizontal = !horizontal;
            if (first_iteration) first_iteration = false;
        }
    
        // 3. now render floating point color buffer to 2D quad and tonemap HDR colors to default framebuffer's (clamped) color range
        // --------------------------------------------------------------------------------------------------------------------------
        Framebuffer.bind(Framebuffer.NULL);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        Program.bind(shaderBloomFinal);
        Texture.bind(hdrFBO.color(0), 0);
        Texture.bind(pingpongFBO[horizontal ? 0 : 1].color(0), 1);
        Program.uniformBool("bloom", bloom);
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
        for (Framebuffer framebuffer : pingpongFBO) framebuffer.delete();
        
        if (quadVAO != null) quadVAO.delete();
        if (cubeVAO != null) cubeVAO.delete();
        
        woodTexture.delete();
        containerTexture.delete();
        
        shader.delete();
        shaderLight.delete();
        shaderBlur.delete();
        shaderBloomFinal.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_571_Bloom();
        
        start(instance);
    }
}
