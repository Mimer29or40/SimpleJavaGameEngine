package engine;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

public class Image
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final Image NULL = new Null();
    
    // -------------------- Instance -------------------- //
    
    protected ColorBuffer data = null;
    
    protected int width  = 0;
    protected int height = 0;
    
    private Image() {}
    
    public Image(@NotNull ColorBuffer data, int width, int height)
    {
        this.data = data;
        
        this.width  = width;
        this.height = height;
    }
    
    public Image(@NotNull String filePath)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width    = stack.mallocInt(1);
            IntBuffer height   = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            ByteBuffer container = stbi_load(filePath, width, height, channels, 0);
            
            if (container == null)
            {
                Image.LOGGER.warning("Unable to load image from file: \"%s\"", filePath);
                return;
            }
            ColorFormat format = ColorFormat.get(channels.get(0));
            this.data = ColorBuffer.wrap(format, container);
            
            this.width  = width.get(0);
            this.height = height.get(0);
        }
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Image{" + "width=" + this.width + ", height=" + this.height + ", format=" + this.data.format + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    public @Nullable ColorBuffer data()
    {
        return this.data;
    }
    
    public int width()
    {
        return this.width;
    }
    
    public int height()
    {
        return this.height;
    }
    
    public @NotNull ColorFormat format()
    {
        return this.data != null ? this.data.format : ColorFormat.UNKNOWN;
    }
    
    // -------------------- Functions -------------------- //
    
    public void delete()
    {
        MemoryUtil.memFree(this.data);
        this.data = null;
        
        this.width  = 0;
        this.height = 0;
    }
    
    public @NotNull Image copy()
    {
        return new Image(this.data.copy(), this.width, this.height);
    }
    
    public boolean save(@NotNull String filePath)
    {
        Image.LOGGER.trace("Saving %s to \"%s\"", this, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(this.data);
        
        int channels = this.data.format.sizeof;
        int stride   = this.width * channels;
        
        return stbi_write_png(filePath, this.width, this.height, channels, buffer, stride);
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    private static final class Null extends Image
    {
        @Override
        public @NotNull String toString()
        {
            return "Framebuffer.NULL";
        }
        
        @Override
        public void delete()
        {
            Image.LOGGER.warning("Cannot call %s.delete()", this);
        }
        
        @Override
        public boolean save(@NotNull String filePath)
        {
            Image.LOGGER.warning("Cannot call %s.save(String)", this);
            return false;
        }
    }
}
