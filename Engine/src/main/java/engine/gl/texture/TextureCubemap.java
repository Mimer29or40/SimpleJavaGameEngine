package engine.gl.texture;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Arrays;

import static org.lwjgl.opengl.GL44.*;

public class TextureCubemap extends Texture
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected int width, height;
    
    private TextureCubemap()
    {
        super(0, GL_TEXTURE_CUBE_MAP, ColorFormat.DEFAULT);
    }
    
    protected TextureCubemap(@NotNull ColorFormat format)
    {
        super(0, GL_TEXTURE_CUBE_MAP, format);
    }
    
    public TextureCubemap(@NotNull ColorBuffer @NotNull [] data, int width, int height)
    {
        super(GL_TEXTURE_CUBE_MAP, data[0].format);
        
        this.width  = width;
        this.height = height;
        
        assert data.length == 6;
        
        long[] addresses = new long[6];
        for (int i = 0; i < data.length; i++) addresses[i] = MemoryUtil.memAddress(data[i]);
        
        load(addresses);
    }
    
    public TextureCubemap(@NotNull ColorFormat format, int width, int height)
    {
        super(GL_TEXTURE_CUBE_MAP, format);
        
        this.width  = width;
        this.height = height;
        
        long[] addresses = new long[6];
        Arrays.fill(addresses, MemoryUtil.NULL);
        
        load(addresses);
    }
    
    public TextureCubemap(@NotNull Image @NotNull [] images)
    {
        super(GL_TEXTURE_CUBE_MAP, images[0].format());
        
        this.width  = images[0].width();
        this.height = images[0].height();
        
        assert images.length == 6;
        
        long[] addresses = new long[6];
        for (int i = 0; i < images.length; i++) addresses[i] = MemoryUtil.memAddressSafe(images[i].data());
        
        load(addresses);
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
    
    protected void load(long[] data)
    {
        bind(this);
        
        TextureCubemap.LOGGER.trace("Loading texture data");
        
        for (int i = 0; i < 6; i++)
        {
            int tex = GL_TEXTURE_CUBE_MAP_POSITIVE_X + i;
            glTexImage2D(tex, 0, this.format.internalFormat, this.width, this.height, 0, this.format.format, GL_UNSIGNED_BYTE, data[i]);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                switch (format)
                {
                    case RED -> glTexParameteriv(tex, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_RED, GL_RED, GL_ONE));
                    case RED_ALPHA -> glTexParameteriv(tex, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_RED, GL_RED, GL_GREEN));
                    case RGB -> glTexParameteriv(tex, GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL_RED, GL_GREEN, GL_BLUE, GL_ONE));
                }
            }
        }
        
        // Default Texture Parameters
        wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(TextureFilter.DEFAULT, TextureFilter.DEFAULT);
        
        TextureCubemap.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        bind(this);
        
        TextureCubemap.LOGGER.trace("Getting Pixel Data");
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        glGetTexImage(this.type, 0, this.format.format, GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        bind(this);
        
        if (this.format != data.format)
        {
            TextureCubemap.LOGGER.warning("Data format (%s) does not match texture (%s)", data.format, this);
            return;
        }
        
        TextureCubemap.LOGGER.trace("Updating Pixel Data");
        
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
    
    public static final TextureCubemap NULL = new TextureCubemap()
    {
        @Override
        public @NotNull String toString()
        {
            return "TextureCubemap.NULL";
        }
        
        @Override
        public void delete()
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.get", this);
        }
        
        @Override
        public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t, @NotNull TextureWrap r)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.wrap", this);
        }
        
        @Override
        public void wrapS(@NotNull TextureWrap s)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.wrapS", this);
        }
        
        @Override
        public void wrapT(@NotNull TextureWrap t)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.wrapT", this);
        }
        
        @Override
        public void wrapR(@NotNull TextureWrap r)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.wrapR", this);
        }
        
        @Override
        public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.filter", this);
        }
        
        @Override
        public void filterMin(@NotNull TextureFilter min)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.filterMin", this);
        }
        
        @Override
        public void filterMag(@NotNull TextureFilter mag)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.filterMag", this);
        }
        
        @Override
        public @NotNull ColorBuffer getPixelData()
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.getPixelData", this);
            return ColorBuffer.malloc(this.format, 0);
        }
        
        @Override
        public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.update", this);
            super.update(data, x, y, width, height);
        }
        
        @Override
        public void update(@NotNull ColorBuffer data)
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.update", this);
            super.update(data);
        }
        
        @Override
        public @NotNull Image toImage()
        {
            TextureCubemap.LOGGER.warning("Cannot call %s.toImage", this);
            return Image.NULL;
        }
    };
}
