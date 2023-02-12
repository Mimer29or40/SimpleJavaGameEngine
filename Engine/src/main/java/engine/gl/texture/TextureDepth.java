package engine.gl.texture;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;

import static engine.Renderer.bind;

public class TextureDepth extends Texture2D
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    public TextureDepth(int width, int height)
    {
        super(ColorFormat.RGB, width, height);
    }
    
    // -------------------- Functions -------------------- //
    
    @Override
    protected void load(long data)
    {
        bind(this);
        
        TextureDepth.LOGGER.trace("Loading data@%08X for %s", data, this);
        
        // TODO - Verify
        GL40.glTexImage2D(this.type, 0, GL40.GL_DEPTH_COMPONENT24, this.width, this.height, 0, GL40.GL_DEPTH_COMPONENT, GL40.GL_UNSIGNED_BYTE, data);
        
        // Default Texture Parameters
        wrap(Wrap.DEFAULT, Wrap.DEFAULT, Wrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(Filter.DEFAULT, Filter.DEFAULT);
        
        TextureDepth.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        bind(this);
        
        TextureDepth.LOGGER.trace("Getting Pixel Data for %s", this);
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        GL40.glGetTexImage(this.type, 0, GL40.GL_DEPTH_COMPONENT, GL40.GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    @Override
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        TextureDepth.LOGGER.warning("Not supported for TextureDepthStencil");
    }
    
    @Override
    public void genMipmaps()
    {
        TextureDepth.LOGGER.warning("Not supported for TextureDepthStencil");
    }
}
