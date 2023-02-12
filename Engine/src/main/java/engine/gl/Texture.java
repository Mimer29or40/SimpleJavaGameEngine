package engine.gl;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.gl.texture.Texture2D;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL46;

import java.util.Objects;

import static engine.Renderer.stateTexture;

public abstract class Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    static void setup()
    {
        Texture.LOGGER.debug("Setup");
        
        //GLState.bind(GLTexture1D.NULL, 0);  // TOD0
        stateTexture(Texture2D.NULL, 0);
        //GLState.bind(GLTexture3D.NULL, 0);  //  TODO
        //GLState.bind(GLTexture1DArray.NULL, 0);  //  TODO
        //GLState.bind(GLTexture2DArray.NULL, 0);  //  TODO
        //GLState.bind(GLTextureRectangle.NULL, 0);  //  TODO
        //GLState.bind(GLTextureCubeMap.NULL, 0);  //  TODO
        //GLState.bind(GLTextureCubeMapArray.NULL, 0);  // TODO - OpenGL 4.0
        //GLState.bind(GLTextureBuffer.NULL, 0);  //  TODO
        //GLState.bind(GLTexture2DMultisample.NULL, 0);  //  TODO
        //GLState.bind(GLTexture2DMultisampleArray.NULL, 0);  //  TODO
    }
    
    static void destroy()
    {
        Texture.LOGGER.debug("Destroy");
        
        //stateTexture(GLTexture1D.NULL, 0);  // TOD0
        stateTexture(Texture2D.NULL, 0);
        //stateTexture(GLTexture3D.NULL, 0);  //  TODO
        //stateTexture(GLTexture1DArray.NULL, 0);  //  TODO
        //stateTexture(GLTexture2DArray.NULL, 0);  //  TODO
        //stateTexture(GLTextureRectangle.NULL, 0);  //  TODO
        //stateTexture(GLTextureCubeMap.NULL, 0);  //  TODO
        //stateTexture(GLTextureCubeMapArray.NULL, 0);  // TODO - OpenGL 4.0
        //stateTexture(GLTextureBuffer.NULL, 0);  //  TODO
        //stateTexture(GLTexture2DMultisample.NULL, 0);  //  TODO
        //stateTexture(GLTexture2DMultisampleArray.NULL, 0);  //  TODO
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
        this(GL40.glGenTextures(), type, format);
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
                    case GL40.GL_TEXTURE_1D -> "1D";
                    case GL40.GL_TEXTURE_2D -> "2D";
                    case GL40.GL_TEXTURE_3D -> "3D";
                    case GL40.GL_TEXTURE_1D_ARRAY -> "1D_ARRAY";
                    case GL40.GL_TEXTURE_2D_ARRAY -> "2D_ARRAY";
                    case GL40.GL_TEXTURE_RECTANGLE -> "RECTANGLE";
                    case GL40.GL_TEXTURE_CUBE_MAP -> "CUBE_MAP";
                    case GL40.GL_TEXTURE_CUBE_MAP_ARRAY -> "CUBE_MAP_ARRAY";
                    case GL40.GL_TEXTURE_BUFFER -> "BUFFER";
                    case GL40.GL_TEXTURE_2D_MULTISAMPLE -> "2D_MULTISAMPLE";
                    case GL40.GL_TEXTURE_2D_MULTISAMPLE_ARRAY -> "2D_MULTISAMPLE_ARRAY";
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
        
        GL40.glDeleteTextures(this.id);
        
        this.id = 0;
    }
    
    public void wrap(@NotNull Texture.Wrap s, @NotNull Texture.Wrap t, @NotNull Texture.Wrap r)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s t=%s r=%s", s, t, r);
        
        GL46.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_S, s.ref);
        GL46.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_T, t.ref);
        GL46.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_R, r.ref);
    }
    
    public void wrapS(@NotNull Texture.Wrap s)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: s=%s", s);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_S, s.ref);
    }
    
    public void wrapT(@NotNull Texture.Wrap t)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: t=%s", t);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_S, t.ref);
    }
    
    public void wrapR(@NotNull Texture.Wrap r)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Wrap: r=%s", r);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_WRAP_S, r.ref);
    }
    
    public void filter(@NotNull Filter min, @NotNull Filter mag)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s mag=%s", min, mag);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_MIN_FILTER, min.ref);
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
    
    public void filterMin(@NotNull Filter min)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: min=%s", min);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_MIN_FILTER, min.ref);
    }
    
    public void filterMag(@NotNull Filter mag)
    {
        stateTexture(this);
        
        Texture.LOGGER.trace("Setting Texture Filter: mag=%s", mag);
        
        GL40.glTexParameteri(this.type, GL40.GL_TEXTURE_MAG_FILTER, mag.ref);
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public enum Filter
    {
        NEAREST(GL40.GL_NEAREST),
        LINEAR(GL40.GL_LINEAR),
        NEAREST_MIPMAP_NEAREST(GL40.GL_NEAREST_MIPMAP_NEAREST),
        LINEAR_MIPMAP_NEAREST(GL40.GL_LINEAR_MIPMAP_NEAREST),
        NEAREST_MIPMAP_LINEAR(GL40.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP_LINEAR(GL40.GL_LINEAR_MIPMAP_LINEAR),
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
        CLAMP_TO_EDGE(GL40.GL_CLAMP_TO_EDGE),
        CLAMP_TO_BORDER(GL40.GL_CLAMP_TO_BORDER),
        MIRRORED_REPEAT(GL40.GL_MIRRORED_REPEAT),
        REPEAT(GL40.GL_REPEAT),
        // MIRROR_CLAMP_TO_EDGE(GL46.GL_MIRROR_CLAMP_TO_EDGE),
        ;
        
        public static final Wrap DEFAULT = REPEAT;
        
        public final int ref;
        
        Wrap(int ref)
        {
            this.ref = ref;
        }
    }
}
