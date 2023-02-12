package engine.gl;

import engine.util.Logger;
import org.jetbrains.annotations.Contract;
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
    
    public static final Program NULL = new Null();
    
    public static final String ATTRIBUTE_POSITION  = "POSITION";
    public static final String ATTRIBUTE_TEXCOORD  = "TEXCOORD";
    public static final String ATTRIBUTE_NORMAL    = "NORMAL";
    public static final String ATTRIBUTE_TANGENT   = "TANGENT";
    public static final String ATTRIBUTE_COLOR     = "COLOR";
    public static final String ATTRIBUTE_TEXCOORD2 = "TEXCOORD2";
    
    public static final List<String> DEFAULT_ATTRIBUTES = List.of(ATTRIBUTE_POSITION,
                                                                  ATTRIBUTE_TEXCOORD,
                                                                  ATTRIBUTE_NORMAL,
                                                                  ATTRIBUTE_TANGENT,
                                                                  ATTRIBUTE_COLOR,
                                                                  ATTRIBUTE_TEXCOORD2);
    
    public static final String UNIFORM_MATRIX_MVP        = "MATRIX_MVP";
    public static final String UNIFORM_MATRIX_PROJECTION = "MATRIX_PROJECTION";
    public static final String UNIFORM_MATRIX_VIEW       = "MATRIX_VIEW";
    public static final String UNIFORM_MATRIX_MODEL      = "MATRIX_MODEL";
    public static final String UNIFORM_MATRIX_NORMAL     = "MATRIX_NORMAL";
    public static final String UNIFORM_VECTOR_VIEW_X     = "VECTOR_VIEW_X";
    public static final String UNIFORM_VECTOR_VIEW_Y     = "VECTOR_VIEW_Y";
    public static final String UNIFORM_VECTOR_VIEW_Z     = "VECTOR_VIEW_Z";
    public static final String UNIFORM_COLOR_DIFFUSE     = "COLOR_DIFFUSE";
    public static final String UNIFORM_COLOR_SPECULAR    = "COLOR_SPECULAR";
    public static final String UNIFORM_COLOR_AMBIENT     = "COLOR_AMBIENT";
    
    public static final List<String> DEFAULT_UNIFORMS = List.of(UNIFORM_MATRIX_MVP,
                                                                UNIFORM_MATRIX_PROJECTION,
                                                                UNIFORM_MATRIX_VIEW,
                                                                UNIFORM_MATRIX_MODEL,
                                                                UNIFORM_MATRIX_NORMAL,
                                                                UNIFORM_VECTOR_VIEW_X,
                                                                UNIFORM_VECTOR_VIEW_Y,
                                                                UNIFORM_VECTOR_VIEW_Z,
                                                                UNIFORM_COLOR_DIFFUSE,
                                                                UNIFORM_COLOR_SPECULAR,
                                                                UNIFORM_COLOR_AMBIENT);
    
    public static final String MAP_ALBEDO     = "texture0";
    public static final String MAP_METALNESS  = "texture1";
    public static final String MAP_NORMAL     = "texture2";
    public static final String MAP_ROUGHNESS  = "texture3";
    public static final String MAP_OCCLUSION  = "texture4";
    public static final String MAP_EMISSION   = "texture5";
    public static final String MAP_HEIGHT     = "texture6";
    public static final String MAP_CUBEMAP    = "texture7";
    public static final String MAP_IRRADIANCE = "texture8";
    public static final String MAP_PREFILTER  = "texture9";
    public static final String MAP_BRDF       = "texture10";
    
    public static final String MAP_DIFFUSE  = MAP_ALBEDO;
    public static final String MAP_SPECULAR = MAP_METALNESS;
    
    public static final List<String> DEFAULT_MAPS = List.of(MAP_ALBEDO,
                                                            MAP_METALNESS,
                                                            MAP_NORMAL,
                                                            MAP_ROUGHNESS,
                                                            MAP_OCCLUSION,
                                                            MAP_EMISSION,
                                                            MAP_HEIGHT,
                                                            MAP_CUBEMAP,
                                                            MAP_IRRADIANCE,
                                                            MAP_PREFILTER,
                                                            MAP_BRDF);
    
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
        
        // NOTE: Default attribute program locations must be bound before linking
        for (int i = 0, n = Program.DEFAULT_ATTRIBUTES.size(); i < n; i++)
        {
            String name = Program.DEFAULT_ATTRIBUTES.get(i);
            
            Program.LOGGER.trace("Binding Default Attribute (%s) at Location (%s) for %s", name, i, this);
            
            GL44.glBindAttribLocation(this.id, i, name);
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
    
    private static final class Null extends Program
    {
        @Contract(pure = true)
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
    }
    
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
        
        public @NotNull Builder shader(@NotNull Shader.Type type, @NotNull Path filePath)
        {
            return shader(new Shader(type, filePath));
        }
        
        public @NotNull Builder shader(@NotNull Shader.Type type, @NotNull String code)
        {
            return shader(new Shader(type, code));
        }
        
        public @NotNull Program build()
        {
            return new Program(this.shaders);
        }
    }
}
