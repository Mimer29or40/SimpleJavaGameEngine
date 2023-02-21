package engine.gl.texture;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL44.*;

public class TextureDepthStencil extends Texture2D
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    private TextureDepthStencil()
    {
        super(ColorFormat.RGBA);
    }
    
    public TextureDepthStencil(int width, int height)
    {
        super(ColorFormat.RGBA, width, height);
    }
    
    // -------------------- Functions -------------------- //
    
    @Override
    protected void load(long data)
    {
        bind(this);
        
        TextureDepthStencil.LOGGER.trace("Loading texture data");
        
        glTexImage2D(this.type, 0, GL_DEPTH24_STENCIL8, width, height, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, data);
        
        // Default Texture Parameters
        wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(TextureFilter.DEFAULT, TextureFilter.DEFAULT);
        
        TextureDepthStencil.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        bind(this);
        
        TextureDepthStencil.LOGGER.trace("Getting Pixel Data");
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        glGetTexImage(this.type, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.address());
        
        return pixels;
    }
    
    @Override
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        TextureDepthStencil.LOGGER.warning("Not supported for TextureDepthStencil");
    }
    
    @Override
    public void genMipmaps()
    {
        TextureDepthStencil.LOGGER.warning("Not supported for TextureDepthStencil");
    }
    
    public static final TextureDepthStencil NULL = new TextureDepthStencil()
    {
        @Override
        public @NotNull String toString()
        {
            return "Texture2D.NULL";
        }
        
        @Override
        public void delete()
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.get", this);
        }
        
        @Override
        public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t, @NotNull TextureWrap r)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.wrap", this);
        }
        
        @Override
        public void wrapS(@NotNull TextureWrap s)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.wrapS", this);
        }
        
        @Override
        public void wrapT(@NotNull TextureWrap t)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.wrapT", this);
        }
        
        @Override
        public void wrapR(@NotNull TextureWrap r)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.wrapR", this);
        }
        
        @Override
        public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.filter", this);
        }
        
        @Override
        public void filterMin(@NotNull TextureFilter min)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.filterMin", this);
        }
        
        @Override
        public void filterMag(@NotNull TextureFilter mag)
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.filterMag", this);
        }
        
        @Override
        public @NotNull ColorBuffer getPixelData()
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.getPixelData", this);
            return ColorBuffer.malloc(this.format, 0);
        }
        
        @Override
        public @NotNull Image toImage()
        {
            TextureDepthStencil.LOGGER.warning("Cannot call %s.toImage", this);
            return Image.NULL;
        }
    };
}
