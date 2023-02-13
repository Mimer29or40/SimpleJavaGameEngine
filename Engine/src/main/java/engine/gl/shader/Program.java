package engine.gl.shader;

import engine.color.Colorc;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class Program
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    private static Program boundProgram;
    
    public static void bind(@NotNull Program program)
    {
        Program.LOGGER.trace("Binding:", Program.boundProgram = program);
        
        GL44.glUseProgram(program.id);
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
    
    public Program(@NotNull Shader @NotNull ... shaders)
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
    
    // -------------------- Attribute -------------------- //
    
    public void attributeShort(@NotNull String name, short value)
    {
        Program.LOGGER.trace("attributeShort(%s, %s)", name, value);
        
        GL44.glVertexAttrib1s(Program.boundProgram.getUniform(name), value);
    }
    
    public void attributeInt(@NotNull String name, int value)
    {
        Program.LOGGER.trace("attributeInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1i(Program.boundProgram.getUniform(name), value);
    }
    
    public void attributeUInt(@NotNull String name, long value)
    {
        Program.LOGGER.trace("attributeUInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1ui(Program.boundProgram.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public void attributeFloat(@NotNull String name, double value)
    {
        Program.LOGGER.trace("attributeFloat(%s, %s)", name, value);
        
        GL44.glVertexAttrib1f(Program.boundProgram.getUniform(name), (float) value);
    }
    
    public void attributeShort2(@NotNull String name, short x, short y)
    {
        Program.LOGGER.trace("attributeShort2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2s(Program.boundProgram.getUniform(name), x, y);
    }
    
    public void attributeInt2(@NotNull String name, int x, int y)
    {
        Program.LOGGER.trace("attributeInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2i(Program.boundProgram.getUniform(name), x, y);
    }
    
    public void attributeInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeInt2(name, vec.x(), vec.y());
    }
    
    public void attributeUInt2(@NotNull String name, long x, long y)
    {
        Program.LOGGER.trace("attributeUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public void attributeUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeUInt2(name, vec.x(), vec.y());
    }
    
    public void attributeFloat2(@NotNull String name, double x, double y)
    {
        Program.LOGGER.trace("attributeFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2f(Program.boundProgram.getUniform(name), (float) x, (float) y);
    }
    
    public void attributeFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        attributeFloat2(name, vec.x(), vec.y());
    }
    
    public void attributeShort3(@NotNull String name, short x, short y, short z)
    {
        Program.LOGGER.trace("attributeShort3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3s(Program.boundProgram.getUniform(name), x, y, z);
    }
    
    public void attributeInt3(@NotNull String name, int x, int y, int z)
    {
        Program.LOGGER.trace("attributeInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3i(Program.boundProgram.getUniform(name), x, y, z);
    }
    
    public void attributeInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public void attributeUInt3(@NotNull String name, long x, long y, long z)
    {
        Program.LOGGER.trace("attributeUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public void attributeUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public void attributeFloat3(@NotNull String name, double x, double y, double z)
    {
        Program.LOGGER.trace("attributeFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3f(Program.boundProgram.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public void attributeFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        attributeFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public void attributeShort4(@NotNull String name, short x, short y, short z, short w)
    {
        Program.LOGGER.trace("attributeShort4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4s(Program.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public void attributeInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Program.LOGGER.trace("attributeInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4i(Program.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public void attributeInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public void attributeUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Program.LOGGER.trace("attributeUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public void attributeUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public void attributeFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Program.LOGGER.trace("attributeFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4f(Program.boundProgram.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public void attributeFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        attributeFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public void attributeNormalizedUByte4(@NotNull String name, int x, int y, int z, int w)
    {
        Program.LOGGER.trace("attributeNormalizedUByte4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4Nub(Program.boundProgram.getUniform(name), (byte) (x & 0xFF), (byte) (y & 0xFF), (byte) (z & 0xFF), (byte) (w & 0xFF));
    }
    
    // -------------------- Uniform -------------------- //
    
    public static void uniformBool(@NotNull String name, boolean value)
    {
        Program.LOGGER.trace("uniformBool(%s, %s)", name, value);
        
        GL44.glUniform1i(Program.boundProgram.getUniform(name), value ? 1 : 0);
    }
    
    public static void uniformInt(@NotNull String name, int value)
    {
        Program.LOGGER.trace("uniformInt(%s, %s)", name, value);
        
        GL44.glUniform1i(Program.boundProgram.getUniform(name), value);
    }
    
    public static void uniformUInt(@NotNull String name, long value)
    {
        Program.LOGGER.trace("uniformUInt(%s, %s)", name, value);
        
        GL44.glUniform1ui(Program.boundProgram.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public static void uniformFloat(@NotNull String name, double value)
    {
        Program.LOGGER.trace("uniformFloat(%s, %s)", name, value);
        
        GL44.glUniform1f(Program.boundProgram.getUniform(name), (float) value);
    }
    
    public static void uniformBool2(@NotNull String name, boolean x, boolean y)
    {
        Program.LOGGER.trace("uniformBool2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Program.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0);
    }
    
    public static void uniformInt2(@NotNull String name, int x, int y)
    {
        Program.LOGGER.trace("uniformInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Program.boundProgram.getUniform(name), x, y);
    }
    
    public static void uniformInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformUInt2(@NotNull String name, long x, long y)
    {
        Program.LOGGER.trace("uniformUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformUInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformFloat2(@NotNull String name, double x, double y)
    {
        Program.LOGGER.trace("uniformFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2f(Program.boundProgram.getUniform(name), (float) x, (float) y);
    }
    
    public static void uniformFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        uniformFloat2(name, vec.x(), vec.y());
    }
    
    public static void uniformBool3(@NotNull String name, boolean x, boolean y, boolean z)
    {
        Program.LOGGER.trace("uniformBool3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Program.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
    }
    
    public static void uniformInt3(@NotNull String name, int x, int y, int z)
    {
        Program.LOGGER.trace("uniformInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Program.boundProgram.getUniform(name), x, y, z);
    }
    
    public static void uniformInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformUInt3(@NotNull String name, long x, long y, long z)
    {
        Program.LOGGER.trace("uniformUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformFloat3(@NotNull String name, double x, double y, double z)
    {
        Program.LOGGER.trace("uniformFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3f(Program.boundProgram.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public static void uniformFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        uniformFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformBool4(@NotNull String name, boolean x, boolean y, boolean z, boolean w)
    {
        Program.LOGGER.trace("uniformBool3(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Program.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
    }
    
    public static void uniformInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Program.LOGGER.trace("uniformInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Program.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public static void uniformInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Program.LOGGER.trace("uniformUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4ui(Program.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Program.LOGGER.trace("uniformFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4f(Program.boundProgram.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public static void uniformFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        uniformFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformMatrix2(@NotNull String name, boolean transpose, @NotNull Matrix2dc mat)
    {
        Program.LOGGER.trace("uniformMatrix2(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats((float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11());
            GL44.glUniformMatrix2fv(Program.boundProgram.getUniform(name), transpose, buffer);
        }
    }
    
    public static void uniformMatrix3(@NotNull String name, boolean transpose, @NotNull Matrix3dc mat)
    {
        Program.LOGGER.trace("uniformMatrix3(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix3fv(Program.boundProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
        }
    }
    
    public static void uniformMatrix4(@NotNull String name, boolean transpose, @NotNull Matrix4dc mat)
    {
        Program.LOGGER.trace("uniformMatrix4(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix4fv(Program.boundProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
        }
    }
    
    public static void uniformColor(@NotNull String name, @NotNull Colorc color)
    {
        Program.LOGGER.trace("uniformColor(%s, %s", name, color);
        
        GL44.glUniform4f(Program.boundProgram.getUniform(name), color.rf(), color.gf(), color.bf(), color.af());
    }
    
    // -------------------- Sub-Classes -------------------- //
    
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
