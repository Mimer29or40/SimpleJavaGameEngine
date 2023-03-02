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
import engine.gl.texture.*;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_532_2_PointShadowsSoft extends Engine // TODO - Is not working
{
    Program shader;
    Program depthShader;
    Program debugShader;
    
    Texture2D woodTexture;
    
    Framebuffer depthMapFBO;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    // lighting info
    // -------------
    Vector3d lightPos = new Vector3d(-2.0f, 4.0f, -1.0f);
    
    boolean shadows = true;
    
    protected LOGL_532_2_PointShadowsSoft()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 5.3.2.2 - Point Shadows Soft");
        
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
                            } vs_out;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            uniform mat4 model;
                            
                            uniform bool reverse_normals;
                            
                            void main()
                            {
                                vs_out.FragPos = vec3(model * vec4(aPos, 1.0));
                                if(reverse_normals) // a slight hack to make sure the outer large cube displays lighting from the 'inside' instead of the default 'outside'.
                                    vs_out.Normal = transpose(inverse(mat3(model))) * (-1.0 * aNormal);
                                else
                                    vs_out.Normal = transpose(inverse(mat3(model))) * aNormal;
                                vs_out.TexCoords = aTexCoords;
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
                            } fs_in;
                            
                            uniform sampler2D diffuseTexture;
                            uniform samplerCube depthMap;
                            
                            uniform vec3 lightPos;
                            uniform vec3 viewPos;
                            
                            uniform float far_plane;
                            uniform bool shadows;
                            
                            float ShadowCalculation(vec3 fragPos)
                            {
                                // get vector between fragment position and light position
                                vec3 fragToLight = fragPos - lightPos;
                                // ise the fragment to light vector to sample from the depth map
                                float closestDepth = texture(depthMap, fragToLight).r;
                                // it is currently in linear range between [0,1], let's re-transform it back to original depth value
                                closestDepth *= far_plane;
                                // now get current linear depth as the length between the fragment and light position
                                float currentDepth = length(fragToLight);
                                // test for shadows
                                float bias = 0.05; // we use a much larger bias since depth is now in [near_plane, far_plane] range
                                float shadow = currentDepth -  bias > closestDepth ? 1.0 : 0.0;
                                // display closestDepth as debug (to visualize depth cubemap)
                                // FragColor = vec4(vec3(closestDepth / far_plane), 1.0);
                                
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
                                float shadow = shadows ? ShadowCalculation(fs_in.FragPos) : 0.0;
                                vec3 lighting = (ambient + (1.0 - shadow) * (diffuse + specular)) * color;
                                
                                FragColor = vec4(lighting, 1.0);
                            }
                            """;
        
        String _depth_vs = """
                           #version 330 core
                           layout (location = 0) in vec3 aPos;
                           
                           uniform mat4 model;
                           
                           void main()
                           {
                               gl_Position = model * vec4(aPos, 1.0);
                           }
                           """;
        String _depth_gs = """
                           #version 330 core
                           layout (triangles) in;
                           layout (triangle_strip, max_vertices=18) out;
                           
                           uniform mat4 shadowMatrices[6];
                           
                           out vec4 FragPos; // FragPos from GS (output per emitvertex)
                           
                           void main()
                           {
                               for(int face = 0; face < 6; ++face)
                               {
                                   gl_Layer = face; // built-in variable that specifies to which face we render.
                                   for(int i = 0; i < 3; ++i) // for each triangle's vertices
                                   {
                                       FragPos = gl_in[i].gl_Position;
                                       gl_Position = shadowMatrices[face] * FragPos;
                                       EmitVertex();
                                   }
                                   EndPrimitive();
                               }
                           }
                           """;
        String _depth_fg = """
                           #version 330 core
                           in vec4 FragPos;
                           
                           uniform vec3 lightPos;
                           uniform float far_plane;
                           
                           void main()
                           {
                               float lightDistance = length(FragPos.xyz - lightPos);
                               
                               // map to [0;1] range by dividing by far_plane
                               lightDistance = lightDistance / far_plane;
                               
                               // write this as modified depth
                               gl_FragDepth = lightDistance;
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
                           
                           uniform samplerCube depthMap;
                           uniform float near_plane;
                           uniform float far_plane;
                           
                           // required when using a perspective projection matrix
                           float LinearizeDepth(float depth)
                           {
                               float z = depth * 2.0 - 1.0; // Back to NDC
                               return (2.0 * near_plane * far_plane) / (far_plane + near_plane - z * (far_plane - near_plane));
                           }
                           
                           vec4 cubeMap()
                           {
                               //In this example i use a debthmap with only 1 channel, but the projection should work with a colored cubemap to, just replace this with a vec3 or vec4
                               float debth=0;
                               vec2 localST=TexCoords;
                               
                               //Scale Tex coordinates such that each quad has local coordinates from 0,0 to 1,1
                               localST.t = mod(localST.t*3,1);
                               localST.s = mod(localST.s*4,1);
                               
                               //Due to the way my debth-cubemap is rendered, objects to the -x,y,z side is projected to the positive x,y,z side
                               
                               //Inside where tob/bottom is to be drawn?
                               if (TexCoords.s*4>1 && TexCoords.s*4<2)
                               {
                                   //Bottom (-y) quad
                                   if (TexCoords.t*3.f < 1)
                                   {
                                       vec3 dir=vec3(localST.s*2-1,1,localST.t*2-1);//Get lower y texture, which is projected to the +y part of my cubemap
                                       
                                       debth = texture( depthMap, dir ).r;
                                   }
                                   //top (+y) quad
                                   else if (TexCoords.t*3.f > 2)
                                   {
                                       vec3 dir=vec3(localST.s*2-1,-1,-localST.t*2+1);//Due to the (arbitrary) way I choose as up in my debth-viewmatrix, i her emultiply the latter coordinate with -1
                                       
                                       debth = texture( depthMap, dir ).r;
                                   }
                                   else//Front (-z) quad
                                   {
                                       vec3 dir=vec3(localST.s*2-1,-localST.t*2+1,1);
                                       debth = texture( depthMap, dir ).r;
                                   }
                                   
                               }
                               //If not, only these ranges should be drawn
                               else if (TexCoords.t*3.f > 1 && TexCoords.t*3 < 2)
                               {
                                   if (TexCoords.x*4.f < 1)//left (-x) quad
                                   {
                                       vec3 dir=vec3(-1,-localST.t*2+1,localST.s*2-1);
                                       
                                       debth = texture( depthMap, dir ).r;
                                       
                                   }
                                   else if (TexCoords.x*4.f < 3)//right (+x) quad (front was done above)
                                   {
                                       vec3 dir=vec3(1,-localST.t*2+1,-localST.s*2+1);
                                       
                                       debth = texture( depthMap, dir ).r;
                                   }
                                   else //back (+z) quad
                                   {
                                       vec3 dir=vec3(-localST.s*2+1,-localST.t*2+1,-1);
                                       
                                       debth = texture( depthMap, dir ).r;
                                   }
                                   
                               }
                               else//Tob/bottom, but outside where we need to put something
                               {
                                   discard;//No need to add fancy semi transparant borders for quads, this is just for debugging purpose after all
                               }
                               
                               return vec4(vec3(LinearizeDepth(debth)),1);
                           }
                           
                           void main()
                           {
                               //float depthValue = texture(depthMap, TexCoords).r;
                               // FragColor = vec4(vec3(LinearizeDepth(depthValue) / far_plane), 1.0); // perspective
                               //FragColor = vec4(vec3(depthValue), 1.0); // orthographic
                               
                               //float phi=TexCoords.s*3.1415*2;
                               //float theta=(-TexCoords.t+0.5)*3.1415;
                               //
                               //vec3 dir = vec3(cos(phi)*cos(theta),sin(theta),sin(phi)*cos(theta));
                               //
                               ////In this example i use a debthmap with only 1 channel, but the projection should work with a colored cubemap to
                               //vec3 depth = texture(depthMap, dir).rgb;
                               //FragColor = vec4(depth,1);
                               FragColor = cubeMap();
                           }
                           """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        Shader depth_vs  = new Shader(ShaderType.VERTEX, _depth_vs);
        Shader depth_gs  = new Shader(ShaderType.GEOMETRY, _depth_gs);
        Shader depth_fg  = new Shader(ShaderType.FRAGMENT, _depth_fg);
        Shader debug_vs  = new Shader(ShaderType.VERTEX, _debug_vs);
        Shader debug_fg  = new Shader(ShaderType.FRAGMENT, _debug_fg);
        
        shader      = new Program(shader_vs, shader_fg);
        depthShader = new Program(depth_vs, depth_gs, depth_fg);
        debugShader = new Program(debug_vs, debug_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        depth_vs.delete();
        depth_gs.delete();
        depth_fg.delete();
        debug_vs.delete();
        debug_fg.delete();
        
        Image image = new Image(IOUtil.getPath("LearnOpenGL/textures/wood.png"));
        
        woodTexture = new Texture2D(image);
        woodTexture.genMipmaps();
        
        image.delete();
        
        final int x = 1024, y = 1024;
        depthMapFBO = Framebuffer.builder(x, y).color().depth(new TextureCubemap(ColorFormat.DEPTH, x, y)).build();
        Texture.bind(depthMapFBO.depth());
        depthMapFBO.depth().filter(TextureFilter.NEAREST, TextureFilter.NEAREST);
        depthMapFBO.depth().wrap(TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE, TextureWrap.CLAMP_TO_EDGE);
        
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
        GL.DEFAULT_STATE.cullFace  = CullFace.BACK;
        
        // shader configuration
        // --------------------
        Program.bind(shader);
        Program.uniformInt("diffuseTexture", 0);
        Program.uniformInt("depthMap", 1);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.SPACE)) shadows = !shadows;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        // render
        // ------
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        //GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        // 0. create depth cubemap transformation matrices
        // -----------------------------------------------
        double   near_plane = 1.0;
        double   far_plane  = 25.0;
        double   aspect     = (double) windowFramebufferSize().x() / windowFramebufferSize().y();
        Matrix4d shadowProj = new Matrix4d().setPerspective(Math.toRadians(90.0f), aspect, near_plane, far_plane);
        Matrix4d[] shadowTransforms = {
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(+1.0, +0.0, +0.0, new Vector3d()), new Vector3d(+0.0, -1.0, +0.0)), new Matrix4d()),
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(-1.0, +0.0, +0.0, new Vector3d()), new Vector3d(+0.0, -1.0, +0.0)), new Matrix4d()),
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(+0.0, +1.0, +0.0, new Vector3d()), new Vector3d(+0.0, +0.0, +1.0)), new Matrix4d()),
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(+0.0, -1.0, +0.0, new Vector3d()), new Vector3d(+0.0, +0.0, -1.0)), new Matrix4d()),
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(+0.0, +0.0, +1.0, new Vector3d()), new Vector3d(+0.0, -1.0, +0.0)), new Matrix4d()),
                shadowProj.mul(new Matrix4d().setLookAt(lightPos, lightPos.add(+0.0, +0.0, -1.0, new Vector3d()), new Vector3d(+0.0, -1.0, +0.0)), new Matrix4d())
        };
        
        // 1. render scene to depth cubemap
        // --------------------------------
        Framebuffer.bind(depthMapFBO);
        GL.clearBuffers(ScreenBuffer.DEPTH);
        Program.bind(depthShader);
        for (int i = 0; i < 6; ++i)
        {Program.uniformMatrix4("shadowMatrices[" + i + "]", false, shadowTransforms[i]);}
        Program.uniformFloat("far_plane", far_plane);
        Program.uniformFloat3("lightPos", lightPos);
        renderScene();
        
        // 2. render scene as normal
        // -------------------------
        Framebuffer.bind(Framebuffer.NULL);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        // set lighting uniforms
        Program.uniformFloat3("lightPos", lightPos);
        Program.uniformFloat3("viewPos", camera.Position);
        Program.uniformBool("shadows", shadows); // enable/disable shadows by pressing 'SPACE'
        Program.uniformFloat("far_plane", far_plane);
        Texture.bind(woodTexture, 0);
        Texture.bind(depthMapFBO.depth(), 1);
        renderScene();
        
        
        // render Depth map to quad for visual debugging
        // ---------------------------------------------
        Program.bind(debugShader);
        Program.uniformFloat("near_plane", near_plane);
        Program.uniformFloat("far_plane", far_plane);
        Program.uniformInt("depthMap", 0);
        Texture.bind(depthMapFBO.depth(), 0);
        renderQuad();
    }
    
    // renders the 3D scene
    // --------------------
    void renderScene()
    {
        // room cube
        Matrix4d model = new Matrix4d();
        model.scale(5.0);
        Program.uniformMatrix4("model", false, model);
        
        GL.cullFace(CullFace.NONE); // note that we disable culling here since we render 'inside' the cube instead of the usual 'outside' which throws off the normal culling methods.
        Program.uniformBool("reverse_normals", true); // A small little hack to invert normals when drawing cube from the inside so lighting still works.
        renderCube();
        Program.uniformBool("reverse_normals", false); // and of course disable it
        GL.cullFace(CullFace.BACK);
        
        // cubes
        model.identity();
        model.translate(new Vector3d(4.0, -3.5, 0.0));
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(2.0, 3.0, 1.0));
        model.scale(0.75);
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(-3.0, -1.0, 0.0));
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(-1.5, 1.0, 1.5));
        model.scale(0.5);
        Program.uniformMatrix4("model", false, model);
        renderCube();
        
        model.identity();
        model.translate(new Vector3d(-1.5, 2.0, -3.0));
        model.rotate(Math.toRadians(60.0), new Vector3d(1.0, 0.0, 1.0).normalize());
        model.scale(0.75);
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
                    +0.0f, +1.0f, +0.0f, 0.0f, 1.0f, // Vertex 0
                    +0.0f, +0.0f, +0.0f, 0.0f, 0.0f, // Vertex 1
                    +1.0f, +1.0f, +0.0f, 1.0f, 1.0f, // Vertex 2
                    +1.0f, +0.0f, +0.0f, 1.0f, 0.0f, // Vertex 3
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
        
        shader.delete();
        depthShader.delete();
        debugShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_532_2_PointShadowsSoft();
        
        start(instance);
    }
}
