package engine.gl.texture;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;

import java.util.Objects;

import static engine.Renderer.bind;

public abstract class Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    public final int         type;
    public final ColorFormat format;
    
    protected Texture(int id, int type, @NotNull ColorFormat format)
    {
        this.id = id;
        
        this.type   = type;
        this.format = format;
        
        if (format.sizeof <= 0) throw new IllegalArgumentException("Invalid Format: " + format);
    }
    
    protected Texture(int type, @NotNull ColorFormat format)
    {
        this(GL44.glGenTextures(), type, format);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Texture other)) return false;
        return this.id == other.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.type, this.format);
    }
    
    @Override
    public @NotNull String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", width=" + width() + ", height=" + height() + ", format=" + this.format + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    public int id()
    {
        return this.id;
    }
    
    public abstract int width();
    
    public abstract int height();
    
    public abstract int depth();
    
    public @NotNull String type()
    {
        return switch (this.type)
                {
                    case GL44.GL_TEXTURE_1D -> "1D";
                    case GL44.GL_TEXTURE_2D -> "2D";
                    case GL44.GL_TEXTURE_3D -> "3D";
                    case GL44.GL_TEXTURE_1D_ARRAY -> "1D_ARRAY";
                    case GL44.GL_TEXTURE_2D_ARRAY -> "2D_ARRAY";
                    case GL44.GL_TEXTURE_RECTANGLE -> "RECTANGLE";
                    case GL44.GL_TEXTURE_CUBE_MAP -> "CUBE_MAP";
                    case GL44.GL_TEXTURE_CUBE_MAP_ARRAY -> "CUBE_MAP_ARRAY";
                    case GL44.GL_TEXTURE_BUFFER -> "BUFFER";
                    case GL44.GL_TEXTURE_2D_MULTISAMPLE -> "2D_MULTISAMPLE";
                    case GL44.GL_TEXTURE_2D_MULTISAMPLE_ARRAY -> "2D_MULTISAMPLE_ARRAY";
                    default -> "UNKNOWN";
                };
    }
    
    // -------------------- Functions -------------------- //
    
    public abstract @NotNull ColorBuffer getPixelData();
    
    /**
     * Unload texture from GPU memory
     */
    public void delete()
    {
        Texture.LOGGER.debug("Deleting", this);
        
        GL44.glDeleteTextures(this.id);
        
        this.id = 0;
    }
    
    public void wrap(@NotNull Texture.Wrap s, @NotNull Texture.Wrap t, @NotNull Texture.Wrap r)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s t=%s r=%s", s, t, r);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, s.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_T, t.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_R, r.ref);
    }
    
    public void wrapS(@NotNull Texture.Wrap s)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s", s);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, s.ref);
    }
    
    public void wrapT(@NotNull Texture.Wrap t)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: t=%s", t);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, t.ref);
    }
    
    public void wrapR(@NotNull Texture.Wrap r)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: r=%s", r);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, r.ref);
    }
    
    public void filter(@NotNull Filter min, @NotNull Filter mag)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s mag=%s", min, mag);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MIN_FILTER, min.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
    
    public void filterMin(@NotNull Filter min)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s", min);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MIN_FILTER, min.ref);
    }
    
    public void filterMag(@NotNull Filter mag)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: mag=%s", mag);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public enum Filter
    {
        NEAREST(GL44.GL_NEAREST),
        LINEAR(GL44.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GL44.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GL44.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GL44.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GL44.GL_LINEAR_MIPMAP_LINEAR),
        ;
        
        public static final Filter DEFAULT = NEAREST;
        
        public final int ref;
        
        Filter(int ref)
        {
            this.ref = ref;
        }
    }
    
    public enum Wrap
    {
        CLAMP_TO_EDGE(GL44.GL_CLAMP_TO_EDGE),
        CLAMP_TO_BORDER(GL44.GL_CLAMP_TO_BORDER),
        MIRRORED_REPEAT(GL44.GL_MIRRORED_REPEAT),
        REPEAT(GL44.GL_REPEAT),
        MIRROR_CLAMP_TO_EDGE(GL44.GL_MIRROR_CLAMP_TO_EDGE),
        ;
        
        public static final Wrap DEFAULT = REPEAT;
        
        public final int ref;
        
        Wrap(int ref)
        {
            this.ref = ref;
        }
    }
}
