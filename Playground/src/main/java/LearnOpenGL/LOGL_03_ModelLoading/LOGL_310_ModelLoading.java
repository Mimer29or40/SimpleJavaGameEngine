package LearnOpenGL.LOGL_03_ModelLoading;

import LearnOpenGL.Camera;
import LearnOpenGL.Model;
import engine.Engine;
import engine.Key;
import engine.gl.DepthMode;
import engine.gl.GL;
import engine.gl.PolygonMode;
import engine.gl.ScreenBuffer;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.util.IOUtil;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import static engine.IO.*;

public class LOGL_310_ModelLoading extends Engine
{
    Program shader;
    
    Model model;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    Key mode = Key.F1;
    
    protected LOGL_310_ModelLoading()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 3.1.0 - Model Loading");
        
        mouseCapture();
        
        String _model_loading_vs = """
                                   #version 330 core
                                   layout (location = 0) in vec3 aPos;
                                   layout (location = 1) in vec3 aNormal;
                                   layout (location = 2) in vec2 aTexCoords;
                                   
                                   out vec2 TexCoords;
                                   
                                   uniform mat4 model;
                                   uniform mat4 view;
                                   uniform mat4 projection;
                                   
                                   void main()
                                   {
                                       TexCoords = aTexCoords;
                                       gl_Position = projection * view * model * vec4(aPos, 1.0);
                                   }
                                   """;
        String _model_loading_fg = """
                                   #version 330 core
                                   out vec4 FragColor;
                                   
                                   in vec2 TexCoords;
                                   
                                   uniform sampler2D texture_diffuse1;
                                   
                                   void main()
                                   {
                                       FragColor = texture(texture_diffuse1, TexCoords);
                                   }
                                   """;
        
        Shader model_loading_vs = new Shader(ShaderType.VERTEX, _model_loading_vs);
        Shader model_loading_fg = new Shader(ShaderType.FRAGMENT, _model_loading_fg);
        
        shader = new Program(model_loading_vs, model_loading_fg);
        
        model_loading_vs.delete();
        model_loading_fg.delete();
        
        model = new Model(IOUtil.getPath("LearnOpenGL/objects/backpack/backpack.obj"));
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    
        if (keyboardKeyDown(Key.F1)) mode = Key.F1;
        if (keyboardKeyDown(Key.F2)) mode = Key.F2;
        
        camera.update(time, deltaTime, true);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearColor(0.2, 0.3, 0.3, 1.0);
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        GL.depthMode(DepthMode.LESS);
    
        if (mode == Key.F1) GL.polygonMode(PolygonMode.FILL);
        if (mode == Key.F2) GL.polygonMode(PolygonMode.LINE);
        
        Program.bind(shader);
        
        Program.uniformMatrix4("projection", false, camera.GetProjectionMatrix());
        Program.uniformMatrix4("view", false, camera.GetViewMatrix());
        
        // world transformation
        Matrix4d model = new Matrix4d();
        model.translate(new Vector3d(0, 0, 0));
        model.scale(1.0);
        Program.uniformMatrix4("model", false, model);
        
        this.model.Draw();
    }
    
    @Override
    protected void destroy()
    {
        shader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_310_ModelLoading();
        
        start(instance);
    }
}
