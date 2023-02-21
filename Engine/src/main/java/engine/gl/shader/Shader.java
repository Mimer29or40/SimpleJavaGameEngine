package engine.gl.shader;

import engine.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.opengl.GL44.*;

public class Shader
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected    int        id;
    public final ShaderType type;
    
    public Shader(@NotNull ShaderType type, @NotNull Path filePath)
    {
        this.id   = glCreateShader(type.ref);
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
    
    public Shader(@NotNull ShaderType type, @NotNull String code)
    {
        this.id   = glCreateShader(type.ref);
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
    
    public int id()
    {
        return id;
    }
    
    // -------------------- Functions -------------------- //
    
    private void compile(@NotNull String code)
    {
        Shader.LOGGER.trace("Compiling %s:%n%s", this, code);
        
        glShaderSource(this.id, code);
        glCompileShader(this.id);
        
        if (glGetShaderi(this.id, GL_COMPILE_STATUS) == GL_TRUE)
        {
            Shader.LOGGER.debug("Created", this);
        }
        else
        {
            throw new IllegalStateException("Failed to Compile: " + this + '\n' + glGetShaderInfoLog(this.id));
        }
    }
    
    public void delete()
    {
        Shader.LOGGER.debug("Deleting", this);
        
        glDeleteShader(this.id);
        
        this.id = 0;
    }
}
