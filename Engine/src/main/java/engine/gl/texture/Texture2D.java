package engine.gl.texture;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.gl.Texture;
import engine.util.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static engine.Renderer.stateTexture;

public class Texture2D extends Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final Texture2D NULL = new Null();
    
    // -------------------- Instance -------------------- //
    
    protected int width, height;
    
    private Texture2D()
    {
        super(0, GL40.GL_TEXTURE_2D, ColorFormat.DEFAULT);
    }
    
    protected Texture2D(@NotNull ColorFormat format)
    {
        super(GL40.GL_TEXTURE_2D, format);
    }
    
    public Texture2D(@NotNull ColorBuffer data, int width, int height)
    {
        super(GL40.GL_TEXTURE_2D, data.format);
        
        this.width  = width;
        this.height = height;
        
        load(MemoryUtil.memAddress(data));
    }
    
    public Texture2D(@NotNull ColorFormat format, int width, int height)
    {
        super(GL40.GL_TEXTURE_2D, format);
        
        this.width  = width;
        this.height = height;
        
        load(MemoryUtil.NULL);
    }
    
    public Texture2D(@NotNull Image image)
    {
        super(GL40.GL_TEXTURE_2D, image.format());
        
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
        stateTexture(this);
        
        GL40.glTexImage2D(this.type, 0, this.format.internalFormat, this.width, this.height, 0, this.format.format, GL40.GL_UNSIGNED_BYTE, data);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            switch (format)
            {
                case RED -> GL40.glTexParameteriv(this.type, GL40.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL40.GL_RED, GL40.GL_RED, GL40.GL_RED, GL40.GL_ONE));
                case RED_ALPHA -> GL40.glTexParameteriv(this.type, GL40.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL40.GL_RED, GL40.GL_RED, GL40.GL_RED, GL40.GL_GREEN));
                case RGB -> GL40.glTexParameteriv(this.type, GL40.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL40.GL_RED, GL40.GL_GREEN, GL40.GL_BLUE, GL40.GL_ONE));
            }
        }
        
        // Default Texture Parameters
        wrap(Wrap.DEFAULT, Wrap.DEFAULT, Wrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(Filter.DEFAULT, Filter.DEFAULT);
        
        Texture2D.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        stateTexture(this);
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        GL40.glGetTexImage(this.type, 0, this.format.format, GL40.GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    /**
     * Update GPU texture rectangle with new data
     * <p>
     * NOTE: pixels data must match the texture format
     */
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        if (this.format != data.format)
        {
            Texture2D.LOGGER.warning("Data format (%s) does not match texture (%s)", data.format, this);
            return;
        }
        
        long pixels = MemoryUtil.memAddressSafe(data);
        
        GL40.glTexSubImage2D(this.type, 0, x, y, width, height, this.format.format, GL40.GL_UNSIGNED_BYTE, pixels);
    }
    
    /**
     * Update GPU texture with new data
     * <p>
     * NOTE: pixels data must match the texture format
     */
    public void update(@NotNull ColorBuffer data)
    {
        update(data, 0, 0, this.width, this.height);
    }
    
    /**
     * Get pixel data from GPU texture and return an Image
     * <p>
     * NOTE: Compressed texture formats not supported
     */
    public @NotNull Image toImage()
    {
        return new Image(getPixelData(), this.width, this.height);
    }
    
    /**
     * Generate mipmap data for selected texture
     */
    public void genMipmaps()
    {
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        stateTexture(this);
        
        // Check if texture is power-of-two (POT)
        if (this.width > 0 && (this.width & this.width - 1) == 0 && this.height > 0 && (this.height & this.height - 1) == 0)
        {
            // Hint for mipmaps generation algorithm: GL.FASTEST, GL.NICEST, GL.DONT_CARE
            GL40.glHint(GL40.GL_GENERATE_MIPMAP_HINT, GL40.GL_DONT_CARE);
            GL40.glGenerateMipmap(this.type); // Generate mipmaps automatically
            
            filter(Filter.LINEAR, Filter.LINEAR_MIPMAP_LINEAR); // Activate Tri-Linear filtering for mipmaps
            
            Texture2D.LOGGER.info("Mipmaps Generated: %s", this);
        }
        else
        {
            Texture2D.LOGGER.warning("Texture is not power of 2:", this);
        }
    }
    
    private static final class Null extends Texture2D
    {
        @Contract(pure = true)
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
        public void wrap(@NotNull Texture.Wrap s, @NotNull Texture.Wrap t, @NotNull Texture.Wrap r)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrap", this);
        }
        
        @Override
        public void wrapS(@NotNull Texture.Wrap s)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapS", this);
        }
        
        @Override
        public void wrapT(@NotNull Texture.Wrap t)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapT", this);
        }
        
        @Override
        public void wrapR(@NotNull Texture.Wrap r)
        {
            Texture2D.LOGGER.warning("Cannot call %s.wrapR", this);
        }
        
        @Override
        public void filter(@NotNull Filter min, @NotNull Filter mag)
        {
            Texture2D.LOGGER.warning("Cannot call %s.filter", this);
        }
        
        @Override
        public void filterMin(@NotNull Filter min)
        {
            Texture2D.LOGGER.warning("Cannot call %s.filterMin", this);
        }
        
        @Override
        public void filterMag(@NotNull Filter mag)
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
    }
}
