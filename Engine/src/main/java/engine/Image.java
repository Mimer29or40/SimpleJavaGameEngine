package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

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
    
    public Image(@NotNull ColorFormat format, int width, int height)
    {
        this.data = ColorBuffer.malloc(format, width * height);
        
        this.width  = width;
        this.height = height;
    }
    
    public Image(@NotNull Path filePath)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width    = stack.mallocInt(1);
            IntBuffer height   = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            ByteBuffer container = stbi_load(filePath.toString(), width, height, channels, 0);
            
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
    
    public boolean save(@NotNull Path filePath)
    {
        Image.LOGGER.trace("Saving %s to \"%s\"", this, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(this.data);
        
        int channels = this.data.format.sizeof;
        int stride   = this.width * channels;
        
        return stbi_write_png(filePath.toString(), this.width, this.height, channels, buffer, stride);
    }
    
    @NotNull
    public ColorBuffer palette(int maxPaletteSize)
    {
        int count = 0;
        
        ColorBuffer palette = ColorBuffer.malloc(ColorFormat.RGBA, maxPaletteSize);
        
        Color color0 = new Color();
        Color color1 = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, color0);
            
            if (color0.a() != 0)
            {
                // Check if the color is already on palette
                boolean present = false;
                for (int j = 0; j < count; j++)
                {
                    palette.get(j, color1);
                    if (color0.equals(color1))
                    {
                        present = true;
                        break;
                    }
                }
                
                // Store color if not on the palette
                if (!present)
                {
                    // Add pixels[i] to palette
                    palette.put(count, color0);
                    count++;
                    
                    // We reached the limit of colors supported by palette
                    if (count >= maxPaletteSize)
                    {
                        Image.LOGGER.warning("Palette is greater than %s colors", maxPaletteSize);
                        break;
                    }
                }
            }
        }
        return palette.limit(count);
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
        public boolean save(@NotNull Path filePath)
        {
            Image.LOGGER.warning("Cannot call %s.save(String)", this);
            return false;
        }
    }
    
    // -------------------- Utility Functions -------------------- //
    
    protected void validateRect(int x, int y, int width, int height)
    {
        if (x < 0) throw new IllegalArgumentException("subregion x exceeds image bounds");
        if (y < 0) throw new IllegalArgumentException("subregion y exceeds image bounds");
        if (x + width - 1 >= this.width) throw new IllegalArgumentException("subregion width exceeds image bounds");
        if (y + height - 1 >= this.height) throw new IllegalArgumentException("subregion height exceeds image bounds");
    }
}
