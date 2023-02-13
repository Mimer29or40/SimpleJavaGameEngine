package engine.gl.shader;

import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.*;

public class Program
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static void bind(@NotNull Program program)
    {
        Program.LOGGER.trace("Binding:", program);
    
        GL44.glUseProgram(program.id);
    }
    
    // -------------------- Creation -------------------- //
    
    private static final Builder BUILDER = new Builder();
    
    public static @NotNull Builder builder()
    {
        return Program.BUILDER.reset();
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    protected final List<Shader> shaders = new ArrayList<>();
    
    protected final Map<String, Integer> attributes = new HashMap<>();
    protected final Map<String, Integer> uniforms   = new HashMap<>();
    
    private Program()
    {
        this.id = 0;
    }
    
    private Program(@NotNull List<Shader> shaders)
    {
        this.id = GL44.glCreateProgram();
        
        for (Shader shader : shaders)
        {
            Program.LOGGER.trace("Attaching %s to %s", shader, this);
            
            GL44.glAttachShader(this.id, shader.id);
            this.shaders.add(shader);
        }
        
        GL44.glLinkProgram(this.id);
        if (GL44.glGetProgrami(this.id, GL44.GL_LINK_STATUS) != GL44.GL_TRUE) throw new IllegalStateException("Link failure: " + this + '\n' + GL44.glGetProgramInfoLog(this.id));
        
        GL44.glValidateProgram(this.id);
        if (GL44.glGetProgrami(this.id, GL44.GL_VALIDATE_STATUS) != GL44.GL_TRUE)
        {throw new IllegalStateException("Validation failure: " + this + '\n' + GL44.glGetProgramInfoLog(this.id));}
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            String    name;
            IntBuffer size = stack.callocInt(1);
            IntBuffer type = stack.callocInt(1);
            
            for (int i = 0, attr, n = GL44.glGetProgrami(this.id, GL44.GL_ACTIVE_ATTRIBUTES); i < n; i++)
            {
                name = GL44.glGetActiveAttrib(this.id, i, size, type);
                
                attr = getAttribute(name);
                assert attr == i;
            }
            
            for (int i = 0, uniform, n = GL44.glGetProgrami(this.id, GL44.GL_ACTIVE_UNIFORMS); i < n; i++)
            {
                name = GL44.glGetActiveUniform(this.id, i, size, type);
                
                uniform = getUniform(name);
                assert uniform == i;
            }
        }
        
        Program.LOGGER.debug("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return this.id == program.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return "Program{" + "id=" + this.id + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    public int id()
    {
        return this.id;
    }
    
    // -------------------- Functions -------------------- //
    
    // TODO - Compute Shader Functions
    
    public void delete()
    {
        Program.LOGGER.debug("Deleting", this);
        
        for (Shader shader : this.shaders) GL44.glDetachShader(this.id, shader.id());
        
        GL44.glDeleteProgram(this.id);
        
        this.id = 0;
        
        this.shaders.clear();
        
        this.uniforms.clear();
        this.attributes.clear();
    }
    
    private int _getAttribute(String attribute)
    {
        int location = GL44.glGetAttribLocation(this.id, attribute);
        if (location == -1)
        {
            Program.LOGGER.warning("Failed to find Attribute (%s) for %s", attribute, this);
        }
        else
        {
            Program.LOGGER.trace("Attribute (%s) Set at Location (%s) for %s", attribute, location, this);
        }
        return location;
    }
    
    public int getAttribute(@NotNull String attribute)
    {
        return this.attributes.computeIfAbsent(attribute, this::_getAttribute);
    }
    
    private int _getUniform(String uniform)
    {
        int location = GL44.glGetUniformLocation(this.id, uniform);
        if (location == -1)
        {
            Program.LOGGER.warning("Failed to find Uniform (%s) for %s", uniform, this);
        }
        else
        {
            Program.LOGGER.trace("Uniform (%s) Set at Location (%s) for %s", uniform, location, this);
        }
        return location;
    }
    
    public int getUniform(@NotNull String uniform)
    {
        return this.uniforms.computeIfAbsent(uniform, this::_getUniform);
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public static final class Builder
    {
        private final List<Shader> shaders = new ArrayList<>();
        
        private Builder reset()
        {
            this.shaders.clear();
            return this;
        }
        
        public @NotNull Builder shader(@NotNull Shader shader)
        {
            this.shaders.add(shader);
            return this;
        }
        
        public @NotNull Builder shader(@NotNull ShaderType type, @NotNull Path filePath)
        {
            return shader(new Shader(type, filePath));
        }
        
        public @NotNull Builder shader(@NotNull ShaderType type, @NotNull String code)
        {
            return shader(new Shader(type, code));
        }
        
        public @NotNull Program build()
        {
            return new Program(this.shaders);
        }
    }
    
    public static final Program NULL = new Program()
    {
        @Override
        public @NotNull String toString()
        {
            return "Program.NULL";
        }
        
        @Override
        public void delete()
        {
            Program.LOGGER.warning("Cannot call %s.delete", this);
        }
        
        @Override
        public int getAttribute(@NotNull String attribute)
        {
            return -1;
        }
        
        @Override
        public int getUniform(@NotNull String uniform)
        {
            return -1;
        }
    };
}
