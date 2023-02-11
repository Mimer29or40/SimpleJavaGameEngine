package engine.gl;

import engine.Renderer;
import engine.util.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    protected final Map<Shader.Type, Shader> shaders = new HashMap<>();
    
    protected final Map<String, Integer> attributes = new HashMap<>();
    protected final Map<String, Integer> uniforms   = new HashMap<>();
    
    private Program()
    {
        this.id = 0;
    }
    
    protected Program(@Nullable Shader vert, @Nullable Shader geom, @Nullable Shader frag)
    {
        this.id = GL40.glCreateProgram();
        
        if (vert != null)
        {
            GL40.glAttachShader(this.id, vert.id());
            this.shaders.put(vert.type, vert);
        }
        if (geom != null)
        {
            GL40.glAttachShader(this.id, geom.id());
            this.shaders.put(geom.type, geom);
        }
        if (frag != null)
        {
            GL40.glAttachShader(this.id, frag.id());
            this.shaders.put(frag.type, frag);
        }
        
        // NOTE: Default attribute program locations must be bound before linking
        for (int i = 0, n = Program.DEFAULT_ATTRIBUTES.size(); i < n; i++)
        {
            String name = Program.DEFAULT_ATTRIBUTES.get(i);
            
            Program.LOGGER.trace("Binding Default Attribute (%s) at Location (%s) for %s", name, i, this);
            
            GL40.glBindAttribLocation(this.id, i, name);
        }
        
        GL40.glLinkProgram(this.id);
        if (GL40.glGetProgrami(this.id, GL40.GL_LINK_STATUS) != GL40.GL_TRUE) throw new IllegalStateException("Link failure: " + this + '\n' + GL40.glGetProgramInfoLog(this.id));
        
        GL40.glValidateProgram(this.id);
        if (GL40.glGetProgrami(this.id, GL40.GL_VALIDATE_STATUS) != GL40.GL_TRUE)
        {throw new IllegalStateException("Validation failure: " + this + '\n' + GL40.glGetProgramInfoLog(this.id));}
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            String    name;
            IntBuffer size = stack.callocInt(1);
            IntBuffer type = stack.callocInt(1);
            
            for (int i = 0, attr, n = GL40.glGetProgrami(this.id, GL40.GL_ACTIVE_ATTRIBUTES); i < n; i++)
            {
                name = GL40.glGetActiveAttrib(this.id, i, size, type);
                
                attr = getAttribute(name);
                assert attr == i;
            }
            
            for (int i = 0, uniform, n = GL40.glGetProgrami(this.id, GL40.GL_ACTIVE_UNIFORMS); i < n; i++)
            {
                name = GL40.glGetActiveUniform(this.id, i, size, type);
                
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
    
    /**
     * @return The shader program handle
     */
    public int id()
    {
        return this.id;
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
        Program.LOGGER.debug("Deleting", this);
        
        for (Shader shader : this.shaders.values()) GL40.glDetachShader(this.id, shader.id());
        
        GL40.glDeleteProgram(this.id);
        
        this.id = 0;
        
        this.shaders.clear();
        
        this.uniforms.clear();
        this.attributes.clear();
    }
    
    private int _getAttribute(String attribute)
    {
        int location = GL40.glGetAttribLocation(this.id, attribute);
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
    
    /**
     * Gets the location of the specified attribute, or -1 if the attribute is
     * not present.
     *
     * @param attribute The specified attribute name
     *
     * @return The attribute location, or -1
     */
    public int getAttribute(@NotNull String attribute)
    {
        return this.attributes.computeIfAbsent(attribute, this::_getAttribute);
    }
    
    private int _getUniform(String uniform)
    {
        int location = GL40.glGetUniformLocation(this.id, uniform);
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
    
    /**
     * Gets the location of the specified uniform, or -1 if the uniform is not
     * present.
     *
     * @param uniform The specified uniform name
     *
     * @return The uniform location, or -1
     */
    public int getUniform(@NotNull String uniform)
    {
        return this.uniforms.computeIfAbsent(uniform, this::_getUniform);
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    //public static final class Attribute
    //{
    //    /**
    //     * Sets a short attribute in the shader program.
    //     *
    //     * @param name  The attribute name.
    //     * @param value The value.
    //     */
    //    public static void short1(@NotNull String name, short value)
    //    {
    //        Program.LOGGER.trace("%s: Setting short Attribute: %s=%s", GL.program, name, value);
    //
    //        GL40.glVertexAttrib1s(GL.program.getAttribute(name), value);
    //    }
    //
    //    /**
    //     * Sets an int attribute in the shader program.
    //     *
    //     * @param name  The attribute name.
    //     * @param value The value.
    //     */
    //    public static void int1(@NotNull String name, int value)
    //    {
    //        Program.LOGGER.trace("%s: Setting int Attribute: %s=%s", GL.program, name, value);
    //
    //        GL40.glVertexAttribI1i(GL.program.getAttribute(name), value);
    //    }
    //
    //    /**
    //     * Sets an uint attribute in the shader program.
    //     *
    //     * @param name  The attribute name.
    //     * @param value The value.
    //     */
    //    public static void uint1(@NotNull String name, long value)
    //    {
    //        Program.LOGGER.trace("%s: Setting uint Attribute: %s=%s", GL.program, name, value);
    //
    //        GL40.glVertexAttribI1ui(GL.program.getAttribute(name), (int) (value & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a float attribute in the shader program.
    //     *
    //     * @param name  The attribute name.
    //     * @param value The value.
    //     */
    //    public static void float1(@NotNull String name, double value)
    //    {
    //        Program.LOGGER.trace("%s: Setting float Attribute: %s=%s", GL.program, name, value);
    //
    //        GL40.glVertexAttrib1f(GL.program.getAttribute(name), (float) value);
    //    }
    //
    //    /**
    //     * Sets a svec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void svec2(@NotNull String name, short x, short y)
    //    {
    //        Program.LOGGER.trace("%s: Setting svec2 Attribute: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glVertexAttrib2s(GL.program.getAttribute(name), x, y);
    //    }
    //
    //    /**
    //     * Sets an ivec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void ivec2(@NotNull String name, int x, int y)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec2 Attribute: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glVertexAttribI2i(GL.program.getAttribute(name), x, y);
    //    }
    //
    //    /**
    //     * Sets an uvec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void uvec2(@NotNull String name, long x, long y)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec2 Attribute: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glVertexAttribI2ui(GL.program.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void vec2(@NotNull String name, double x, double y)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec2 Attribute: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glVertexAttrib2f(GL.program.getAttribute(name), (float) x, (float) y);
    //    }
    //
    //    /**
    //     * Sets an ivec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec2(@NotNull String name, @NotNull Vector2ic vec)
    //    {
    //        ivec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets an uvec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec2(@NotNull String name, @NotNull Vector2ic vec)
    //    {
    //        uvec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a vec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec2(@NotNull String name, @NotNull Vector2fc vec)
    //    {
    //        vec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a vec2 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec2(@NotNull String name, @NotNull Vector2dc vec)
    //    {
    //        vec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a svec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void svec3(@NotNull String name, short x, short y, short z)
    //    {
    //        Program.LOGGER.trace("%s: Setting svec3 Attribute: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glVertexAttrib3s(GL.program.getAttribute(name), x, y, z);
    //    }
    //
    //    /**
    //     * Sets an ivec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void ivec3(@NotNull String name, int x, int y, int z)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec3 Attribute: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glVertexAttribI3i(GL.program.getAttribute(name), x, y, z);
    //    }
    //
    //    /**
    //     * Sets an uvec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void uvec3(@NotNull String name, long x, long y, long z)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec3 Attribute: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glVertexAttribI3ui(GL.program.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void vec3(@NotNull String name, double x, double y, double z)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec3 Attribute: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glVertexAttrib3f(GL.program.getAttribute(name), (float) x, (float) y, (float) z);
    //    }
    //
    //    /**
    //     * Sets an ivec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec3(@NotNull String name, @NotNull Vector3ic vec)
    //    {
    //        ivec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets an uvec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec3(@NotNull String name, @NotNull Vector3ic vec)
    //    {
    //        uvec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets a vec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec3(@NotNull String name, @NotNull Vector3fc vec)
    //    {
    //        vec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets a vec3 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec3(@NotNull String name, @NotNull Vector3dc vec)
    //    {
    //        vec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets an ubvec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void ubvec4(@NotNull String name, int x, int y, int z, int w)
    //    {
    //        Program.LOGGER.trace("%s: Setting ubvec4 Attribute: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glVertexAttrib4Nub(GL.program.getAttribute(name), (byte) (x & 0xFF), (byte) (y & 0xFF), (byte) (z & 0xFF), (byte) (w & 0xFF));
    //    }
    //
    //    /**
    //     * Sets a svec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void svec4(@NotNull String name, short x, short y, short z, short w)
    //    {
    //        Program.LOGGER.trace("%s: Setting svec4 Attribute: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glVertexAttrib4s(GL.program.getAttribute(name), x, y, z, w);
    //    }
    //
    //    /**
    //     * Sets an ivec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void ivec4(@NotNull String name, int x, int y, int z, int w)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec4 Attribute: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glVertexAttribI4i(GL.program.getAttribute(name), x, y, z, w);
    //    }
    //
    //    /**
    //     * Sets an uvec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void uvec4(@NotNull String name, long x, long y, long z, long w)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec4 Attribute: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glVertexAttribI4ui(GL.program.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void vec4(@NotNull String name, double x, double y, double z, double w)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec4 Attribute: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glVertexAttrib4f(GL.program.getAttribute(name), (float) x, (float) y, (float) z, (float) w);
    //    }
    //
    //    /**
    //     * Sets an ivec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec4(@NotNull String name, @NotNull Vector4ic vec)
    //    {
    //        ivec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets an uvec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec4(@NotNull String name, @NotNull Vector4ic vec)
    //    {
    //        uvec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets a vec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec4(@NotNull String name, @NotNull Vector4fc vec)
    //    {
    //        vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets a vec4 attribute in the shader program.
    //     *
    //     * @param name The attribute name.
    //     * @param vec  The value.
    //     */
    //    public static void vec4(@NotNull String name, @NotNull Vector4dc vec)
    //    {
    //        vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //}
    //
    //public static final class Uniform
    //{
    //
    //    // -------------------- Uniform -------------------- //
    //
    //    public static void uniformBool(@NotNull String name, boolean value)
    //    {
    //        Program program = Renderer.program[Renderer.stackIndex];
    //        Renderer.LOGGER.trace("%s: Setting bool Uniform: %s=%s", program, name, value);
    //
    //        GL40.glUniform1i(program.getUniform(name), value ? 1 : 0);
    //    }
    //    /**
    //     * Sets an uint uniform in the shader program.
    //     *
    //     * @param name  The uniform name.
    //     * @param value The value.
    //     */
    //    public static void uint1(@NotNull String name, long value)
    //    {
    //        Program.LOGGER.trace("%s: Setting uint Uniform: %s=%s", GL.program, name, value);
    //
    //        GL40.glUniform1ui(GL.program.getUniform(name), (int) (value & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets an int uniform in the shader program.
    //     *
    //     * @param name  The uniform name.
    //     * @param value The value.
    //     */
    //    public static void int1(@NotNull String name, int value)
    //    {
    //        Program.LOGGER.trace("%s: Setting int Uniform: %s=%s", GL.program, name, value);
    //
    //        GL40.glUniform1i(GL.program.getUniform(name), value);
    //    }
    //
    //    /**
    //     * Sets a float uniform in the shader program.
    //     *
    //     * @param name  The uniform name.
    //     * @param value The value.
    //     */
    //    public static void float1(@NotNull String name, double value)
    //    {
    //        Program.LOGGER.trace("%s: Setting float Uniform: %s=%s", GL.program, name, value);
    //
    //        GL40.glUniform1f(GL.program.getUniform(name), (float) value);
    //    }
    //
    //    /**
    //     * Sets a bvec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void bvec2(@NotNull String name, boolean x, boolean y)
    //    {
    //        Program.LOGGER.trace("%s: Setting bvec2 Uniform: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glUniform2i(GL.program.getUniform(name), x ? 1 : 0, y ? 1 : 0);
    //    }
    //
    //    /**
    //     * Sets an ivec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void ivec2(@NotNull String name, int x, int y)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec2 Uniform: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glUniform2i(GL.program.getUniform(name), x, y);
    //    }
    //
    //    /**
    //     * Sets an uvec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void uvec2(@NotNull String name, long x, long y)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec2 Uniform: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glUniform2ui(GL.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     */
    //    public static void vec2(@NotNull String name, double x, double y)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec2 Uniform: %s=(%s, %s)", GL.program, name, x, y);
    //
    //        GL40.glUniform2f(GL.program.getUniform(name), (float) x, (float) y);
    //    }
    //
    //    /**
    //     * Sets an ivec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec2(@NotNull String name, @NotNull Vector2ic vec)
    //    {
    //        ivec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets an uvec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec2(@NotNull String name, @NotNull Vector2ic vec)
    //    {
    //        uvec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a vec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec2(@NotNull String name, @NotNull Vector2fc vec)
    //    {
    //        vec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a vec2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec2(@NotNull String name, @NotNull Vector2dc vec)
    //    {
    //        vec2(name, vec.x(), vec.y());
    //    }
    //
    //    /**
    //     * Sets a bvec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void bvec3(@NotNull String name, boolean x, boolean y, boolean z)
    //    {
    //        Program.LOGGER.trace("%s: Setting bvec3 Uniform: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glUniform3i(GL.program.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
    //    }
    //
    //    /**
    //     * Sets an ivec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void ivec3(@NotNull String name, int x, int y, int z)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec3 Uniform: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glUniform3i(GL.program.getUniform(name), x, y, z);
    //    }
    //
    //    /**
    //     * Sets an uvec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void uvec3(@NotNull String name, long x, long y, long z)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec3 Uniform: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glUniform3ui(GL.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     */
    //    public static void vec3(@NotNull String name, double x, double y, double z)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec3 Uniform: %s=(%s, %s, %s)", GL.program, name, x, y, z);
    //
    //        GL40.glUniform3f(GL.program.getUniform(name), (float) x, (float) y, (float) z);
    //    }
    //
    //    /**
    //     * Sets an ivec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec3(@NotNull String name, @NotNull Vector3ic vec)
    //    {
    //        ivec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets an uvec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec3(@NotNull String name, @NotNull Vector3ic vec)
    //    {
    //        uvec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets a vec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec3(@NotNull String name, @NotNull Vector3fc vec)
    //    {
    //        vec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets a vec3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec3(@NotNull String name, @NotNull Vector3dc vec)
    //    {
    //        vec3(name, vec.x(), vec.y(), vec.z());
    //    }
    //
    //    /**
    //     * Sets a bvec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void bvec4(@NotNull String name, boolean x, boolean y, boolean z, boolean w)
    //    {
    //        Program.LOGGER.trace("%s: Setting bvec4 Uniform: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glUniform4i(GL.program.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
    //    }
    //
    //    /**
    //     * Sets an ivec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void ivec4(@NotNull String name, int x, int y, int z, int w)
    //    {
    //        Program.LOGGER.trace("%s: Setting ivec4 Uniform: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glUniform4i(GL.program.getUniform(name), x, y, z, w);
    //    }
    //
    //    /**
    //     * Sets an uvec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void uvec4(@NotNull String name, long x, long y, long z, long w)
    //    {
    //        Program.LOGGER.trace("%s: Setting uvec4 Uniform: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glUniform4ui(GL.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    //    }
    //
    //    /**
    //     * Sets a vec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param x    The x value.
    //     * @param y    The y value.
    //     * @param z    The z value.
    //     * @param w    The w value.
    //     */
    //    public static void vec4(@NotNull String name, double x, double y, double z, double w)
    //    {
    //        Program.LOGGER.trace("%s: Setting vec3 Uniform: %s=(%s, %s, %s, %s)", GL.program, name, x, y, z, w);
    //
    //        GL40.glUniform4f(GL.program.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    //    }
    //
    //    /**
    //     * Sets an ivec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void ivec4(@NotNull String name, @NotNull Vector4ic vec)
    //    {
    //        ivec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets an uvec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void uvec4(@NotNull String name, @NotNull Vector4ic vec)
    //    {
    //        uvec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets a vec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec4(@NotNull String name, @NotNull Vector4fc vec)
    //    {
    //        vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets a vec4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param vec  The value.
    //     */
    //    public static void vec4(@NotNull String name, @NotNull Vector4dc vec)
    //    {
    //        vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
    //    }
    //
    //    /**
    //     * Sets a mat2 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat2(@NotNull String name, boolean transpose, @NotNull Matrix2fc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat2 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix2fv(GL.program.getUniform(name), transpose, mat.get(stack.mallocFloat(4)));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat2(@NotNull String name, @NotNull Matrix2fc mat)
    //    {
    //        mat2(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a mat2 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat2(@NotNull String name, boolean transpose, @NotNull Matrix2dc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat2 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix2fv(GL.program.getUniform(name), transpose, stack.floats((float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11()));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat2 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat2(@NotNull String name, @NotNull Matrix2dc mat)
    //    {
    //        mat2(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a mat3 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat3(@NotNull String name, boolean transpose, @NotNull Matrix3fc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat3 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix3fv(GL.program.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat3(@NotNull String name, @NotNull Matrix3fc mat)
    //    {
    //        mat3(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a mat3 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat3(@NotNull String name, boolean transpose, @NotNull Matrix3dc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat3 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix3fv(GL.program.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat3 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat3(@NotNull String name, @NotNull Matrix3dc mat)
    //    {
    //        mat3(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a mat4 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat4(@NotNull String name, boolean transpose, @NotNull Matrix4fc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat4 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix4fv(GL.program.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat4(@NotNull String name, @NotNull Matrix4fc mat)
    //    {
    //        mat4(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a mat4 uniform in the shader program.
    //     *
    //     * @param name      The uniform name.
    //     * @param transpose If the matrix is transposed
    //     * @param mat       The matrix value.
    //     */
    //    public static void mat4(@NotNull String name, boolean transpose, @NotNull Matrix4dc mat)
    //    {
    //        Program.LOGGER.trace("%s: Setting mat4 Uniform: %s=%n%s", GL.program, name, mat);
    //
    //        try (MemoryStack stack = MemoryStack.stackPush())
    //        {
    //            GL40.glUniformMatrix4fv(GL.program.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
    //        }
    //    }
    //
    //    /**
    //     * Sets a mat4 uniform in the shader program.
    //     *
    //     * @param name The uniform name.
    //     * @param mat  The matrix value.
    //     */
    //    public static void mat4(@NotNull String name, @NotNull Matrix4dc mat)
    //    {
    //        mat4(name, false, mat);
    //    }
    //
    //    /**
    //     * Sets a vec4 uniform that represents a color in the shader program.
    //     *
    //     * @param name  The uniform name.
    //     * @param color The color value.
    //     */
    //    public static void color(@NotNull String name, @NotNull Colorc color)
    //    {
    //        Program.LOGGER.trace("%s: Setting Color (vec4) Uniform: %s=%s", GL.program, name, color);
    //
    //        int r = Color.toInt(color.r());
    //        int g = Color.toInt(color.g());
    //        int b = Color.toInt(color.b());
    //        int a = Color.toInt(color.a());
    //        GL40.glUniform4f(GL.program.getUniform(name), r, g, b, a);
    //    }
    //}
    
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
}
