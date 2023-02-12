package engine.gl.texture;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;

import static engine.Renderer.stateTexture;

public class TextureStencil extends Texture2D
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Instance -------------------- //
    
    protected TextureStencil(int width, int height)
    {
        super(ColorFormat.RED, width, height);
    }
    
    // -------------------- Functions -------------------- //
    
    
    @Override
    protected void load(long data)
    {
        stateTexture(this);
        
        // TODO - Verify
        GL40.glTexImage2D(this.type, 0, GL40.GL_STENCIL_INDEX8, this.width, this.height, 0, GL40.GL_STENCIL_INDEX, GL40.GL_UNSIGNED_BYTE, data);
        
        // Default Texture Parameters
        wrap(Wrap.DEFAULT, Wrap.DEFAULT, Wrap.DEFAULT);
        
        // Magnification and Minification filters
        filter(Filter.DEFAULT, Filter.DEFAULT);
        
        TextureStencil.LOGGER.debug("Created", this);
    }
    
    @Override
    public @NotNull ColorBuffer getPixelData()
    {
        stateTexture(this);
        
        ColorBuffer pixels = ColorBuffer.malloc(this.format, this.width * this.height);
        
        GL40.glGetTexImage(this.type, 0, GL40.GL_STENCIL_INDEX, GL40.GL_UNSIGNED_BYTE, pixels.address());
        
        return pixels;
    }
    
    @Override
    public void update(@NotNull ColorBuffer data, int x, int y, int width, int height)
    {
        TextureStencil.LOGGER.warning("Not supported for TextureDepthStencil");
    }
    
    @Override
    public void genMipmaps()
    {
        TextureStencil.LOGGER.warning("Not supported for TextureDepthStencil");
    }
}
