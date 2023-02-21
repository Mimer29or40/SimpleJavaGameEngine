package engine.gl.texture;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL44.*;

public class TextureStencil extends Texture2D
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    private TextureStencil()
    {
        super(ColorFormat.RED);
    }
    
    public TextureStencil(int width, int height)
    {
        super(ColorFormat.RED, width, height);
    }
    
    // -------------------- Functions -------------------- //
    
    @Override
    protected void load(long data)
    {
        bind(this);
        
        TextureStencil.LOGGER.trace("Loading texture data");
        
        // TODO - Verify
        glTexImage2D(this.type, 0, GL_STENCIL_INDEX8, this.width, this.height, 0, GL_STENCIL_INDEX, GL_UNSIGNED_BYTE, data);
        
        // Default Texture Parameters
        wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(TextureFilter.DEFAULT, TextureFilter.DEFAULT);
        
        TextureStencil.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        bind(this);
        
        TextureStencil.LOGGER.trace("Getting Pixel Data");
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        glGetTexImage(this.type, 0, GL_STENCIL_INDEX, GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    @Override
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        TextureStencil.LOGGER.warning("Not supported for TextureStencil");
    }
    
    @Override
    public void genMipmaps()
    {
        TextureStencil.LOGGER.warning("Not supported for TextureStencil");
    }
    
    public static final TextureStencil NULL = new TextureStencil()
    {
        @Override
        public @NotNull String toString()
        {
            return "Texture2D.NULL";
        }
        
        @Override
        public void delete()
        {
            TextureStencil.LOGGER.warning("Cannot call %s.get", this);
        }
        
        @Override
        public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t, @NotNull TextureWrap r)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.wrap", this);
        }
        
        @Override
        public void wrapS(@NotNull TextureWrap s)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.wrapS", this);
        }
        
        @Override
        public void wrapT(@NotNull TextureWrap t)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.wrapT", this);
        }
        
        @Override
        public void wrapR(@NotNull TextureWrap r)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.wrapR", this);
        }
        
        @Override
        public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.filter", this);
        }
        
        @Override
        public void filterMin(@NotNull TextureFilter min)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.filterMin", this);
        }
        
        @Override
        public void filterMag(@NotNull TextureFilter mag)
        {
            TextureStencil.LOGGER.warning("Cannot call %s.filterMag", this);
        }
        
        @Override
        public @NotNull ColorBuffer getPixelData()
        {
            TextureStencil.LOGGER.warning("Cannot call %s.getPixelData", this);
            return ColorBuffer.malloc(this.format, 0);
        }
        
        @Override
        public @NotNull Image toImage()
        {
            TextureStencil.LOGGER.warning("Cannot call %s.toImage", this);
            return Image.NULL;
        }
    };
}
