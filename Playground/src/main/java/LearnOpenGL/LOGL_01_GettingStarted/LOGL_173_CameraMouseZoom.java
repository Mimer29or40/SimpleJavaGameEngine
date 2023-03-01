package LearnOpenGL.LOGL_01_GettingStarted;

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
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static engine.IO.*;

public class LOGL_173_CameraMouseZoom extends Engine
{
    Program program;
    
    VertexArray VAO;
    
    Texture2D[] texture = new Texture2D[2];
    
    Vector3d[] cubePositions;
    
    Vector3d cameraPos   = new Vector3d(0.0, 0.0, +3.0);
    Vector3d cameraFront = new Vector3d(0.0, 0.0, -1.0);
    Vector3d cameraUp    = new Vector3d(0.0, 1.0, +0.0);
    
    double yaw   = -90.0; // yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right so we initially rotate a bit to the left.
    double pitch = 0.0;
    double fov   = 45.0;
    
    protected LOGL_173_CameraMouseZoom()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 1.7.3 - Camera Mouse Zoom");
        
        mouseCapture();
        
        String vertCode = """
                          #version 330 core
                          layout (location = 0) in vec3 aPos;
                          layout (location = 1) in vec2 aTexCoord;
                          
                          out vec2 TexCoord;
                          
                          uniform mat4 model;
                          uniform mat4 view;
                          uniform mat4 projection;
                          
                          void main()
                          {
                              gl_Position = projection * view * model * vec4(aPos, 1.0f);
                              TexCoord = vec2(aTexCoord.x, aTexCoord.y);
                          }
                          """;
        String fragCode = """
                          #version 330 core
                          out vec4 FragColor;
                          
                          in vec2 TexCoord;
                          
                          // texture samplers
                          uniform sampler2D texture1;
                          uniform sampler2D texture2;
                          
                          void main()
                          {
                              // linearly interpolate between both textures (80% container, 20% awesomeface)
                              FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        program = new Program(vertShader, fragShader);
        
        vertShader.delete();
        fragShader.delete();
        
        //@formatter:off
        float[] vertices = {
                // positions          // texture coords
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
                +0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 1.0f,
                -0.5f, +0.5f, +0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,

                -0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  1.0f, 0.0f,

                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, -0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, -0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f,
                +0.5f, +0.5f, -0.5f,  1.0f, 1.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                +0.5f, +0.5f, +0.5f,  1.0f, 0.0f,
                -0.5f, +0.5f, +0.5f,  0.0f, 0.0f,
                -0.5f, +0.5f, -0.5f,  0.0f, 1.0f
        };
        //@formatter:on
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer _vertices = stack.floats(vertices);
            
            VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
            VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
            
            VAO = VertexArray.builder().buffer(BufferUsage.STATIC_DRAW, _vertices, position, texCoord).build();
        }
        
        Image image0 = new Image(IOUtil.getPath("LearnOpenGL/textures/container.jpg"));
        Image image1 = new Image(IOUtil.getPath("LearnOpenGL/textures/awesomeface.png"));
        
        texture[0] = new Texture2D(image0);
        texture[0].wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        texture[0].filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        texture[0].genMipmaps();
        
        texture[1] = new Texture2D(image1);
        texture[1].wrap(TextureWrap.REPEAT, TextureWrap.REPEAT, TextureWrap.REPEAT);
        texture[1].filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
        texture[1].genMipmaps();
        
        image0.delete();
        image1.delete();
        
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
        
        Program.bind(program);
        Program.uniformInt("texture1", 0);
        Program.uniformInt("texture2", 1);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        
        Vector3d temp = new Vector3d();
    
        float sensitivity = 0.1f; // change this value to your liking
        if (mouseOnPosChange().fired())
        {
            double xoffset = mousePosDelta().x();
            double yoffset = -mousePosDelta().y();
    
            xoffset *= sensitivity;
            yoffset *= sensitivity;
    
            yaw += xoffset;
            pitch += yoffset;
            
            // make sure that when pitch is out of bounds, screen doesn't get flipped
            if (pitch > 89.0f) pitch = 89.0f;
            if (pitch < -89.0f) pitch = -89.0f;
    
            cameraFront.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            cameraFront.y = Math.sin(Math.toRadians(pitch));
            cameraFront.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            cameraFront.normalize();
        }
        
        if (mouseOnScrollChange().fired())
        {
            fov -= mouseScroll().y();
            
            if (fov < 1.0f) fov = 1.0f;
            if (fov > 45.0f) fov = 45.0f;
        }
        
        double cameraSpeed = 2.5 * deltaTime;
        if (keyboardKeyHeld(Key.W)) cameraPos.add(temp.set(cameraFront).mul(cameraSpeed));
        if (keyboardKeyHeld(Key.S)) cameraPos.sub(temp.set(cameraFront).mul(cameraSpeed));
        if (keyboardKeyHeld(Key.A)) cameraPos.sub(temp.set(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        if (keyboardKeyHeld(Key.D)) cameraPos.add(temp.set(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.2, 0.3, 0.3, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        // bind Texture
        Texture.bind(texture[0], 0);
        Texture.bind(texture[1], 1);
        
        Program.bind(program);
    
        Matrix4d projection = new Matrix4d();
        projection.perspective(Math.toRadians(fov), (double) windowFramebufferSize().x() / windowFramebufferSize().y(), 0.1, 100.0);
        Program.uniformMatrix4("projection", false, projection);
        
        // create transformations
        Matrix4d view = new Matrix4d(); // make sure to initialize matrix to identity matrix first
        view.lookAt(cameraPos, cameraPos.add(cameraFront, new Vector3d()), cameraUp);
        Program.uniformMatrix4("view", false, view);
        
        for (int i = 0; i < 10; i++)
        {
            Matrix4d model = new Matrix4d();
            model.translate(cubePositions[i]);
            double angle = 20 * i;
            model.rotate(Math.toRadians(angle), new Vector3d(1.0, 0.3, 0.5));
            Program.uniformMatrix4("model", false, model);
            
            VAO.draw(DrawMode.TRIANGLES, 36);
        }
    }
    
    @Override
    protected void destroy()
    {
        texture[0].delete();
        texture[1].delete();
        
        VAO.delete();
        
        program.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_173_CameraMouseZoom();
        
        start(instance);
    }
}
