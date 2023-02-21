package engine.gl.texture;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL44.*;

public class Texture2D extends Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected int width, height;
    
    private Texture2D()
    {
        super(0, GL_TEXTURE_2D, ColorFormat.DEFAULT);
    }
    
    protected Texture2D(@NotNull ColorFormat format)
    {
        super(0, GL_TEXTURE_2D, format);
    }
    
    public Texture2D(@NotNull ColorBuffer data, int width, int height)
    {
        super(GL_TEXTURE_2D, data.format);
        
        this.width  = width;
        this.height = height;
        
        load(MemoryUtil.memAddress(data));
    }
    
    public Texture2D(@NotNull ColorFormat format, int width, int height)
    {
        super(GL_TEXTURE_2D, format);
        
        this.width  = width;
        this.height = height;
        
        load(MemoryUtil.NULL);
    }
    
    public Texture2D(@NotNull Image image)
    {
        super(GL_TEXTURE_2D, image.format());
        
        this.width  = image.width();
        this.height = image.height();
        
        load(MemoryUtil.memAddressSafe(image.data()));
    }
    
    // -------------------- Properties -------------------- //
    
    @Override
    public int width()
    {
        return this.width;
    }
    
    @Override
    public int height()
    {
        return this.height;
    }
    
    @Override
    public int depth()
    {
        return 1;
    }
    
    // -------------------- Functions -------------------- //
    
    protected void load(long data)
    {
        bind(this);
        
        Texture2D.LOGGER.trace("Loading texture data");
        
        glTexImage2D(this.type, 0, this.format.internalFormat, this.width, this.height, 0, this.format.format, GL_UNSIGNED_BYTE, data);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            switch (format)
            {
                case RED -> glTexParameteriv(this.type, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_RED, GL_RED, GL_ONE));
                case RED_ALPHA -> glTexParameteriv(this.type, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_RED, GL_RED, GL_GREEN));
                case RGB -> glTexParameteriv(this.type, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_GREEN, GL_BLUE, GL_ONE));
            }
        }
        
        // Default Texture Parameters
        wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(TextureFilter.DEFAULT, TextureFilter.DEFAULT);
        
        Texture2D.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        bind(this);
        
        Texture2D.LOGGER.trace("Getting Pixel Data");
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        glGetTexImage(this.type, 0, this.format.format, GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        bind(this);
        
        if (this.format != data.format)
        {
            Texture2D.LOGGER.warning("Data format (%s) does not match texture (%s)", data.format, this);
            return;
        }
        
        Texture2D.LOGGER.trace("Updating Pixel Data");
        
        long pixels = MemoryUtil.memAddressSafe(data);
        
        glTexSubImage2D(this.type, 0, x, y, width, height, this.format.format, GL_UNSIGNED_BYTE, pixels);
    }
    
    public void update(@NotNull ColorBuffer data)
    {
        update(data, 0, 0, this.width, this.height);
    }
    
    public @NotNull Image toImage()
    {
        return new Image(getPixelData(), this.width, this.height);
    }
    
    public void genMipmaps()
    {
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        bind(this);
        
        // Check if texture is power-of-two (POT)
        if (this.width > 0 && (this.width & this.width - 1) == 0 && this.height > 0 && (this.height & this.height - 1) == 0)
        {
            // Hint for mipmaps generation algorithm: GL.FASTEST, GL.NICEST, GL.DONT_CARE
            glHint(GL_GENERATE_MIPMAP_HINT, GL_DONT_CARE);
            glGenerateMipmap(this.type); // Generate mipmaps automatically
            
            filter(TextureFilter.LINEAR, TextureFilter.LINEAR_MIPMAP_LINEAR); // Activate Tri-Linear filtering for mipmaps
            
            Texture2D.LOGGER.info("Mipmaps Generated: %s", this);
        }
        else
        {
            Texture2D.LOGGER.warning("Texture is not power of 2:", this);
        }
    }
    
    public static final Texture2D NULL = new Texture2D()
    {
        @Override
        public @NotNull String toString()
        {
            return "Texture2D.NULL";
        }
        
        @Override
        public void delete()
        {
            Texture2D.LOGGER.warning("Cannot call %s.get", this);
        }
        
        @Override
        public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t, @NotNull TextureWrap r)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrap", this);
        }
        
        @Override
        public void wrapS(@NotNull TextureWrap s)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapS", this);
        }
        
        @Override
        public void wrapT(@NotNull TextureWrap t)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapT", this);
        }
        
        @Override
        public void wrapR(@NotNull TextureWrap r)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapR", this);
        }
        
        @Override
        public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
        {
            Texture2D.LOGGER.warning("Cannot call %s.filter", this);
        }
        
        @Override
        public void filterMin(@NotNull TextureFilter min)
        {
            Texture2D.LOGGER.warning("Cannot call %s.filterMin", this);
        }
        
        @Override
        public void filterMag(@NotNull TextureFilter mag)
        {
            Texture2D.LOGGER.warning("Cannot call %s.filterMag", this);
        }
        
        @Override
        public @NotNull ColorBuffer getPixelData()
        {
            Texture2D.LOGGER.warning("Cannot call %s.getPixelData", this);
            return ColorBuffer.malloc(this.format, 0);
        }
        
        @Override
        public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
        {
            Texture2D.LOGGER.warning("Cannot call %s.update", this);
            super.update(data, x, y, width, height);
        }
        
        @Override
        public void update(@NotNull ColorBuffer data)
        {
            Texture2D.LOGGER.warning("Cannot call %s.update", this);
            super.update(data);
        }
        
        @Override
        public @NotNull Image toImage()
        {
            Texture2D.LOGGER.warning("Cannot call %s.toImage", this);
            return Image.NULL;
        }
        
        @Override
        public void genMipmaps()
        {
            Texture2D.LOGGER.warning("Cannot call %s.genMipmaps", this);
        }
    };
}
