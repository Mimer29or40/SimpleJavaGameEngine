package engine.gl;

import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Shader
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected    int  id;
    public final Type type;
    
    public Shader(@NotNull Type type, @NotNull Path filePath)
    {
        this.id   = GL44.glCreateShader(type.ref);
        this.type = type;
        try
        {
            String code = Files.readString(filePath);
            compile(code);
        }
        catch (IOException e)
        {
            Shader.LOGGER.severe("Unable to read file:", filePath);
            throw new RuntimeException(e);
        }
    }
    
    public Shader(@NotNull Type type, @NotNull String code)
    {
        this.id   = GL44.glCreateShader(type.ref);
        this.type = type;
        
        compile(code);
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
    
    private void compile(@NotNull String code)
    {
        Shader.LOGGER.trace("Compiling %s:%n%s", this, code);
        
        GL44.glShaderSource(this.id, code);
        GL44.glCompileShader(this.id);
        
        if (GL44.glGetShaderi(this.id, GL44.GL_COMPILE_STATUS) == GL44.GL_TRUE)
        {
            Shader.LOGGER.debug("Created", this);
        }
        else
        {
            throw new IllegalStateException("Failed to Compile: " + this + '\n' + GL44.glGetShaderInfoLog(this.id));
        }
    }
    
    /**
     * Unload this shader program from VRAM (GPU)
     * <p>
     * NOTE: When the application is shutdown, shader programs are
     * automatically unloaded.
     */
    public void delete()
    {
        Shader.LOGGER.debug("Deleting", this);
        
        GL44.glDeleteShader(this.id);
        
        this.id = 0;
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public enum Type
    {
        VERTEX(GL44.GL_VERTEX_SHADER),
        GEOMETRY(GL44.GL_GEOMETRY_SHADER),
        FRAGMENT(GL44.GL_FRAGMENT_SHADER),
        COMPUTE(GL44.GL_COMPUTE_SHADER),
        TESS_CONTROL(GL44.GL_TESS_CONTROL_SHADER),
        TESS_EVALUATION(GL44.GL_TESS_EVALUATION_SHADER),
        ;
        
        public final int ref;
        
        Type(int ref)
        {
            this.ref = ref;
        }
    }
}
