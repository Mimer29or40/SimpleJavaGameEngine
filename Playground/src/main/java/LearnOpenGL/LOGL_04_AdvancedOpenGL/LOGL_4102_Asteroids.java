package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import LearnOpenGL.Model;
import engine.Engine;
import engine.Key;
import engine.color.Color;
import engine.gl.DepthMode;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.ScreenBuffer;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import static engine.IO.*;
import static engine.Renderer.*;

public class LOGL_4102_Asteroids extends Engine
{
    Program shader;
    
    Model rock;
    Model planet;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    int        amount = 50000;
    Matrix4d[] modelMatrices;
    
    protected LOGL_4102_Asteroids()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.10.2 - Asteroids");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 2) in vec2 aTexCoords;
                            
                            out vec2 TexCoords;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            uniform mat4 model;
                            
                            void main()
                            {
                                TexCoords = aTexCoords;
                                gl_Position = projection * view * model * vec4(aPos, 1.0f);\s
                            }
                            """;
        String _shader_fg = """
                            #version 330 core
                            out vec4 FragColor;
                            
                            in vec2 TexCoords;
                            
                            uniform sampler2D texture_diffuse1;
                            
                            void main()
                            {
                                FragColor = texture(texture_diffuse1, TexCoords);
                            }
                            """;
        
        Shader shader_vs = new Shader(ShaderType.VERTEX, _shader_vs);
        Shader shader_fg = new Shader(ShaderType.FRAGMENT, _shader_fg);
        
        shader = new Program(shader_vs, shader_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        
        rock   = new Model(IOUtil.getPath("LearnOpenGL/objects/rock/rock.obj"));
        planet = new Model(IOUtil.getPath("LearnOpenGL/objects/planet/planet.obj"));
        
        // generate a large list of semi-random model transformation matrices
        // ------------------------------------------------------------------
        modelMatrices = new Matrix4d[amount];
        
        double radius = 150.0;
        double offset = 25.0;
        for (int i = 0; i < amount; i++)
        {
            Matrix4d model = new Matrix4d();
            
            // 1. translation: displace along circle with 'radius' in range [-offset, offset]
            double angle        = (double) i / amount * 360.0;
            double displacement = (Math.random() * 2 * offset) - offset;
            double x            = Math.sin(angle) * radius + displacement;
            displacement = (Math.random() * (int) (2 * offset * 100)) / 100.0f - offset;
            double y = displacement * 0.5f; // keep height of asteroid field smaller compared to width of x and z
            displacement = (Math.random() * (int) (2 * offset * 100)) / 100.0f - offset;
            double z = Math.cos(angle) * radius + displacement;
            
            model.translate(new Vector3d(x, y, z));
            
            // 2. scale: Scale between 0.05 and 0.25f
            double scale = (Math.random() * 20) / 100.0 + 0.05;
            model.scale(scale);
            
            // 3. rotation: add random rotation around a (semi)randomly picked rotation axis vector
            double rotAngle = Math.random() * 360;
            model.rotate(rotAngle, new Vector3d(0.4, 0.6, 0.8));
            
            // 4. now add to list of matrices
            modelMatrices[i] = model;
        }
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
        GL.clearColor(0.1, 0.1, 0.1, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
        
        Program.bind(shader);
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        Matrix4d model = new Matrix4d();
        
        // draw planet
        model.translate(new Vector3d(0.0, -3.0, 0.0));
        model.scale(new Vector3d(4.0, 4.0, 4.0));
        Program.uniformMatrix4("model", false, model);
        planet.Draw();
        
        // draw meteorites
        for (Matrix4d modelMatrix : modelMatrices)
        {
            Program.uniformMatrix4("model", false, modelMatrix);
            rock.Draw();
        }
    
        // TODO
        //GL.depthMode(DepthMode.NONE);
        //Framebuffer fb = Framebuffer.get();
        //rendererView(new Matrix4d().setOrtho(0, fb.width(), fb.height(), 0, -1, 1));
        //textColor(Color.WHITE);
        //textSize(36);
        //textDraw(String.format("Update: %.3f\nDraw: %.3f", updateTimeActual(), drawTimeActual()), 10, 10);
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_4102_Asteroids();
        
        start(instance);
    }
}
