package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import LearnOpenGL.Model;
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
import engine.gl.texture.TextureCubemap;
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

public class LOGL_462_CubeMapsEnvironmentMapping extends Engine
{
    Program shader;
    Program skyboxShader;
    
    VertexArray cubeVAO;
    VertexArray skyboxVAO;
    
    TextureCubemap cubemapTexture;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Model model;
    
    Key wireframe = Key.F1;
    Key mode      = Key.F1;
    
    protected LOGL_462_CubeMapsEnvironmentMapping()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.6.2 - Cube Maps Environment Mapping");
        
        mouseCapture();
    
        String _cube_maps_fg = """
                               #version 330 core
                               out vec4 FragColor;
                               
                               in vec3 Normal;
                               in vec3 Position;
                               
                               uniform vec3 cameraPos;
                               uniform samplerCube skybox;
                               
                               void main()
                               {
                                   vec3 I = normalize(Position - cameraPos);
                                   float ratio = 1.00 / 1.52;
                                   
                                   vec3 norm = normalize(Normal);
                                   
                                   float _dot = dot(-I, norm);
                                   
                                   vec3 refl = texture(skybox, reflect(I, norm)).rgb;
                                   vec3 refr = texture(skybox, refract(I, norm, ratio)).rgb;
                                   FragColor = vec4(_dot * refl + (1.0 - _dot) * refr, 1.0);
                               }
                               """;
        String _cube_maps_vs = """
                               #version 330 core
                               layout (location = 0) in vec3 aPos;
                               layout (location = 1) in vec3 aNormal;
                               
                               out vec3 Normal;
                               out vec3 Position;
                               
                               uniform mat4 model;
                               uniform mat4 view;
                               uniform mat4 projection;
                               
                               void main()
                               {
                                   Normal = mat3(transpose(inverse(model))) * aNormal;
                                   Position = vec3(model * vec4(aPos, 1.0));
                                   gl_Position = projection * view * model * vec4(aPos, 1.0);
                               }
                               """;
        
        String _skybox_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            
                            out vec3 TexCoords;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            
                            void main()
                            {
                                TexCoords = aPos;
                                vec4 pos = projection * view * vec4(aPos, 1.0);
                                gl_Position = pos.xyww;
                            }
                            """;
        String _skybox_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec3 TexCoords;
                            
                            uniform samplerCube skybox;
                            
                            void main()
                            {
                                FragColor = texture(skybox, TexCoords);
                            }
                            """;
        
        Shader cube_maps_vs = new Shader(ShaderType.VERTEX, _cube_maps_vs);
        Shader cube_maps_fg = new Shader(ShaderType.FRAGMENT, _cube_maps_fg);
        Shader skybox_vs    = new Shader(ShaderType.VERTEX, _skybox_vs);
        Shader skybox_fg    = new Shader(ShaderType.FRAGMENT, _skybox_fg);
        
        shader       = new Program(cube_maps_vs, cube_maps_fg);
        skyboxShader = new Program(skybox_vs, skybox_fg);
        
        cube_maps_vs.delete();
        cube_maps_fg.delete();
        skybox_vs.delete();
        skybox_fg.delete();
        
        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
        //@formatter:off
        float[] cubeVertices = {
                // positions          // normals
                -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                -0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
                -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f,
            
                -0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
                -0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
                -0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f,
            
                -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, +0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, -0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
                -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f,
            
                +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, +0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f,
            
                -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                -0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f,
                -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f,
            
                -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                -0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f,
                -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f
        };
        float[] skyboxVertices = {
                // positions
                -1.0f, +1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                +1.0f, -1.0f, -1.0f,
                +1.0f, -1.0f, -1.0f,
                +1.0f, +1.0f, -1.0f,
                -1.0f, +1.0f, -1.0f,
            
                -1.0f, -1.0f, +1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, +1.0f, -1.0f,
                -1.0f, +1.0f, -1.0f,
                -1.0f, +1.0f, +1.0f,
                -1.0f, -1.0f, +1.0f,
            
                +1.0f, -1.0f, -1.0f,
                +1.0f, -1.0f, +1.0f,
                +1.0f, +1.0f, +1.0f,
                +1.0f, +1.0f, +1.0f,
                +1.0f, +1.0f, -1.0f,
                +1.0f, -1.0f, -1.0f,
            
                -1.0f, -1.0f, +1.0f,
                -1.0f, +1.0f, +1.0f,
                +1.0f, +1.0f, +1.0f,
                +1.0f, +1.0f, +1.0f,
                +1.0f, -1.0f, +1.0f,
                -1.0f, -1.0f, +1.0f,
            
                -1.0f, +1.0f, -1.0f,
                +1.0f, +1.0f, -1.0f,
                +1.0f, +1.0f, +1.0f,
                +1.0f, +1.0f, +1.0f,
                -1.0f, +1.0f, +1.0f,
                -1.0f, +1.0f, -1.0f,
            
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, +1.0f,
                +1.0f, -1.0f, -1.0f,
                +1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, +1.0f,
                +1.0f, -1.0f, +1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VertexAttribute pos  = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute norm = new VertexAttribute(GLType.FLOAT, 3, false);
            
            FloatBuffer cubeBuffer   = stack.floats(cubeVertices);
            FloatBuffer skyboxBuffer = stack.floats(skyboxVertices);
            
            cubeVAO   = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, cubeBuffer, pos, norm).build();
            skyboxVAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, skyboxBuffer, pos).build();
        }
        
        Image[] cubemapImages = {
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/right.jpg"), false),
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/left.jpg"), false),
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/top.jpg"), false),
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/bottom.jpg"), false),
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/front.jpg"), false),
                new Image(IOUtil.getPath("LearnOpenGL/textures/skybox/back.jpg"), false)
        };
        cubemapTexture = new TextureCubemap(cubemapImages);
        
        for (Image image : cubemapImages) image.delete();
    
        //model = new Model(IOUtil.getPath("LearnOpenGL/objects/backpack/backpack.obj"));
        model = new Model(IOUtil.getPath("LearnOpenGL/objects/nanosuit/nanosuit.obj"));
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        if (keyboardKeyDown(Key.F1)) wireframe = Key.F1;
        if (keyboardKeyDown(Key.F2)) wireframe = Key.F2;
        
        if (keyboardKeyDown(Key.K1)) mode = Key.K1;
        if (keyboardKeyDown(Key.K2)) mode = Key.K2;
        if (keyboardKeyDown(Key.K3)) mode = Key.K3;
        if (keyboardKeyDown(Key.K4)) mode = Key.K4;
        if (keyboardKeyDown(Key.K5)) mode = Key.K5;
        if (keyboardKeyDown(Key.K6)) mode = Key.K6;
        if (keyboardKeyDown(Key.K7)) mode = Key.K7;
        if (keyboardKeyDown(Key.K8)) mode = Key.K8;
        if (keyboardKeyDown(Key.K9)) mode = Key.K9;
        if (keyboardKeyDown(Key.K0)) mode = Key.K0;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS); // set depth function back to default
        
        // draw scene as normal
        Program.bind(shader);
        Matrix4d  model      = new Matrix4d();
        Matrix4dc view       = camera.GetViewMatrix();
        Matrix4dc projection = camera.GetProjectionMatrix();
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformFloat3("cameraPos", camera.Position);
        
        // cubes
        Texture.bind(cubemapTexture, 0);
        model.translate(6, 0, 0);
        Program.uniformMatrix4("model", false, model);
        cubeVAO.draw(DrawMode.TRIANGLES, 0, 36);
        this.model.Draw();
        
        // draw skybox as last
        GL.depthMode(DepthMode.L_EQUAL);  // change depth function so depth test passes when values are equal to depth buffer's content
        
        Program.bind(skyboxShader);
        view = new Matrix4d(view).setTranslation(0, 0, 0); // remove translation from the view matrix
        Program.uniformMatrix4("view", false, view);
        Program.uniformMatrix4("projection", false, projection);
        
        // skybox cube
        Texture.bind(cubemapTexture, 0);
        skyboxVAO.draw(DrawMode.TRIANGLES, 0, 36);
    }
    
    @Override
    protected void destroy()
    {
        cubemapTexture.delete();
        
        cubeVAO.delete();
        skyboxVAO.delete();
        
        shader.delete();
        skyboxShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_462_CubeMapsEnvironmentMapping();
        
        start(instance);
    }
}
