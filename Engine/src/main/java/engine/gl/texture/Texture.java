package engine.gl.texture;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL44;

import java.util.Objects;

public abstract class Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static void bind(@NotNull Texture texture, int index)
    {
        Texture.LOGGER.trace("Binding: %s to index=%s", texture, index);
        
        GL40.glActiveTexture(GL40.GL_TEXTURE0 + index);
        GL40.glBindTexture(texture.type, texture.id);
    }
    
    public static void bind(@NotNull Texture texture)
    {
        bind(texture, 0);
    }
    
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
    
    public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t, @NotNull TextureWrap r)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s t=%s r=%s", s, t, r);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, s.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_T, t.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_R, r.ref);
    }
    
    public void wrapS(@NotNull TextureWrap s)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s", s);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, s.ref);
    }
    
    public void wrapT(@NotNull TextureWrap t)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: t=%s", t);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, t.ref);
    }
    
    public void wrapR(@NotNull TextureWrap r)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: r=%s", r);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_WRAP_S, r.ref);
    }
    
    public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s mag=%s", min, mag);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MIN_FILTER, min.ref);
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
    
    public void filterMin(@NotNull TextureFilter min)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s", min);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MIN_FILTER, min.ref);
    }
    
    public void filterMag(@NotNull TextureFilter mag)
    {
        bind(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: mag=%s", mag);
        
        GL44.glTexParameteri(this.type, GL44.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
}
