package LearnOpenGL.LOGL_04_AdvancedOpenGL;

import LearnOpenGL.Camera;
import LearnOpenGL.Mesh;
import LearnOpenGL.Model;
import engine.Engine;
import engine.Key;
import engine.color.Color;
import engine.gl.DepthMode;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.ScreenBuffer;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferArray;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.util.IOUtil;
import engine.util.MemUtil;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static engine.IO.*;
import static engine.Renderer.*;
import static engine.Renderer.textDraw;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class LOGL_4103_AsteroidsInstanced extends Engine
{
    Program asteroidShader;
    Program planetShader;
    
    Model rock;
    Model planet;
    
    Camera camera = new Camera(new Vector3d(0.0, 0.0, 3.0), null, null, null);
    
    int amount = 50000;
    
    protected LOGL_4103_AsteroidsInstanced()
    {
        super(800, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("LearnOpenGL - 4.10.3 - Asteroids Instanced");
        
        mouseCapture();
        
        String _shader_vs = """
                            #version 330 core
                            layout (location = 0) in vec3 aPos;
                            layout (location = 2) in vec2 aTexCoords;
                            layout (location = 7) in mat4 aInstanceMatrix;
                            
                            out vec2 TexCoords;
                            
                            uniform mat4 projection;
                            uniform mat4 view;
                            uniform mat4 model;
                            
                            void main()
                            {
                                TexCoords = aTexCoords;
                                gl_Position = projection * view * aInstanceMatrix * vec4(aPos, 1.0f);
                                //gl_Position = projection * view * model * vec4(aPos, 1.0f);
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
        
        String _planet_vs = """
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
        String _planet_fg = """
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
        Shader planet_vs = new Shader(ShaderType.VERTEX, _planet_vs);
        Shader planet_fg = new Shader(ShaderType.FRAGMENT, _planet_fg);
        
        asteroidShader = new Program(shader_vs, shader_fg);
        planetShader   = new Program(planet_vs, planet_fg);
        
        shader_vs.delete();
        shader_fg.delete();
        planet_vs.delete();
        planet_fg.delete();
        
        rock   = new Model(IOUtil.getPath("LearnOpenGL/objects/rock/rock.obj"));
        planet = new Model(IOUtil.getPath("LearnOpenGL/objects/planet/planet.obj"));
        
        // generate a large list of semi-random model transformation matrices
        // ------------------------------------------------------------------
        float[] modelMatrices = new float[amount * 16];
        //Matrix4d[] modelMatrices = new Matrix4d[amount];
        Matrix4d model = new Matrix4d();
        
        double radius = 150.0;
        double offset = 25.0;
        for (int i = 0, index = 0; i < amount; i++, index += 16)
        {
            model.identity();
            
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
            model.get(modelMatrices, index);
        }
        
        // configure instanced array
        // -------------------------
        FloatBuffer matrices = MemoryUtil.memAllocFloat(modelMatrices.length);
        MemUtil.memCopy(modelMatrices, matrices);
        
        BufferArray buffer = new BufferArray(BufferUsage.STATIC_DRAW, matrices.clear());
        
        MemoryUtil.memFree(matrices);
        
        for (Mesh mesh : rock.meshes)
        {
            VertexArray VAO = mesh.VAO;
            
            glBindVertexArray(VAO.id());
            Buffer.bind(buffer);
            
            // set attribute pointers for matrix
            glEnableVertexAttribArray(7);
            glVertexAttribPointer(7, 4, GL_FLOAT, false, 16 * Float.BYTES, 0);
            glVertexAttribDivisor(7, 1);
            glEnableVertexAttribArray(8);
            glVertexAttribPointer(8, 4, GL_FLOAT, false, 16 * Float.BYTES, 4 * Float.BYTES);
            glVertexAttribDivisor(8, 1);
            glEnableVertexAttribArray(9);
            glVertexAttribPointer(9, 4, GL_FLOAT, false, 16 * Float.BYTES, 8 * Float.BYTES);
            glVertexAttribDivisor(9, 1);
            glEnableVertexAttribArray(10);
            glVertexAttribPointer(10, 4, GL_FLOAT, false, 16 * Float.BYTES, 12 * Float.BYTES);
            glVertexAttribDivisor(10, 1);
            
            glBindVertexArray(0);
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
        
        Matrix4dc projection = camera.GetProjectionMatrix();
        Matrix4dc view       = camera.GetViewMatrix();
        Matrix4d  model      = new Matrix4d();
        
        // draw planet
        Program.bind(planetShader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        model.translate(new Vector3d(0.0, -3.0, 0.0));
        model.scale(new Vector3d(4.0, 4.0, 4.0));
        Program.uniformMatrix4("model", false, model);
        planet.Draw();
        
        // draw meteorites
        Program.bind(asteroidShader);
        Program.uniformMatrix4("projection", false, projection);
        Program.uniformMatrix4("view", false, view);
        Program.uniformInt("texture_diffuse1", 0);
        Program.uniformMatrix4("model", false, model);
        //rock.Draw();
        Texture.bind(rock.textures_loaded.get(0).texture);
        for (Mesh mesh : rock.meshes)
        {
            //mesh.VAO.drawElements(DrawMode.TRIANGLES, mesh.indices.size());
            mesh.VAO.drawElementsInstanced(DrawMode.TRIANGLES, mesh.indices.size(), amount);
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
        asteroidShader.delete();
        planetShader.delete();
    }
    
    public static void main(String[] args)
    {
        Engine instance = new LOGL_4103_AsteroidsInstanced();
        
        start(instance);
    }
}
