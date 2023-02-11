package engine.gl;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;
import engine.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

public class Shader
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    //static void setup()
    //{
    //    Shader.LOGGER.debug("Setup");
    //
    //    // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-1/
    //    // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-2/
    //    String vertCode = """
    //                      #version 330
    //                      in vec3 POSITION;
    //                      in vec3 TEXCOORD;
    //                      in vec4 COLOR;
    //                      out vec3 fragTexCoord;
    //                      out vec4 fragColor;
    //                      uniform mat4 MATRIX_MVP;
    //                      void main()
    //                      {
    //                          gl_Position = MATRIX_MVP * vec4(POSITION, 1.0);
    //                          fragTexCoord = TEXCOORD;
    //                          fragColor = COLOR;
    //                      }
    //                      """;
    //    String fragCode = """
    //                      #version 330
    //                      in vec3 fragTexCoord;
    //                      in vec4 fragColor;
    //                      out vec4 finalColor;
    //                      uniform sampler2D texture0;
    //                      void main()
    //                      {
    //                          vec4 texelColor = textureProj(texture0, fragTexCoord);
    //                          finalColor = texelColor * fragColor;
    //                      }
    //                      """;
    //
    //    GLState.defaultVertShader = create(Type.VERTEX, vertCode);
    //    GLState.defaultFragShader = create(Type.FRAGMENT, fragCode);
    //}
    //
    //static void destroy()
    //{
    //    Shader.LOGGER.debug("Destroy");
    //
    //    GLState.defaultVertShader.delete();
    //    GLState.defaultVertShader = null;
    //    GLState.defaultFragShader.delete();
    //    GLState.defaultFragShader = null;
    //}
    
    // -------------------- Creation -------------------- //
    
    public static @NotNull Shader create(@NotNull Type type, @NotNull String shader)
    {
        Shader.LOGGER.trace("Creating Shader: type=%s, shader=%s", type, shader);
        
        return new Shader(type, shader);
    }
    
    public static @NotNull Shader create(@NotNull Path file)
    {
        Shader.LOGGER.trace("Creating Shader: file=%s", file);
        
        Type type = Type.getFromFileName(file.toString());
        try
        {
            String shader = Files.readString(file);
            return new Shader(type, shader);
        }
        catch (IOException e)
        {
            Shader.LOGGER.severe("Unable to read file:", file);
            throw new RuntimeException(e);
        }
    }
    
    // -------------------- Instance -------------------- //
    
    protected    int  id;
    public final Type type;
    
    private Shader(@NotNull Type type, @NotNull String code)
    {
        this.id   = GL40.glCreateShader(type.ref);
        this.type = type;
        
        GL40.glShaderSource(this.id, code);
        GL40.glCompileShader(this.id);
        
        if (GL40.glGetShaderi(this.id, GL40.GL_COMPILE_STATUS) == GL40.GL_TRUE)
        {
            Shader.LOGGER.debug("Created", this);
        }
        else
        {
            throw new IllegalStateException("Failed to Compile: " + this + '\n' + GL40.glGetShaderInfoLog(this.id));
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shader shader = (Shader) o;
        return this.id == shader.id && this.type == shader.type;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.type);
    }
    
    @Override
    public String toString()
    {
        return "Shader{" + "id=" + this.id + ", type=" + this.type + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * @return The shader handle
     */
    public int id()
    {
        return id;
    }
    
    // -------------------- Functions -------------------- //
    
    /**
     * Unload this shader program from VRAM (GPU)
     * <p>
     * NOTE: When the application is shutdown, shader programs are
     * automatically unloaded.
     */
    public void delete()
    {
        Shader.LOGGER.debug("Deleting", this);
        
        GL40.glDeleteShader(this.id);
        
        this.id = 0;
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public enum Type
    {
        VERTEX(GL40.GL_VERTEX_SHADER, Pattern.compile(".*\\.(?:vert|vs)")),
        GEOMETRY(GL40.GL_GEOMETRY_SHADER, Pattern.compile(".*\\.(?:geom|gs)")),
        FRAGMENT(GL40.GL_FRAGMENT_SHADER, Pattern.compile(".*\\.(?:frag|fs)")),
        //COMPUTE(GL43.GL_COMPUTE_SHADER, Pattern.compile(".*\\.(?:comp|cs)")),  // TODO - OpenGL 4.3
        TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER, Pattern.compile(".*\\.(?:tesc|tc)")),
        TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER, Pattern.compile(".*\\.(?:tese|te)")),
        ;
        
        public final  int     ref;
        private final Pattern pattern;
        
        Type(int ref, Pattern pattern)
        {
            this.ref     = ref;
            this.pattern = pattern;
        }
        
        public static @NotNull Type getFromFileName(@NotNull String fileName)
        {
            for (Type type : Type.values())
            {
                if (type.pattern.matcher(fileName).matches())
                {
                    return type;
                }
            }
            throw new RuntimeException("Could not identify shader type from file's name: " + fileName);
        }
    }
}
