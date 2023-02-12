package engine;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImage.stbi_load;

public class ImageReader
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public static @NotNull Image read(@NotNull Path filePath) throws IOException
    {
        String extension = IOUtil.getExtension(filePath.toString());
        
        return switch (extension)
                {
                    case ".png" -> readPNG(filePath);
                    case ".bmp" -> readBMP(filePath);
                    case ".tga" -> readTGA(filePath);
                    case ".jpg", ".jpeg" -> readJPEG(filePath);
                    case ".gif" -> readGIF(filePath);
                    case ".pic" -> readPIC(filePath);
                    case ".psd" -> readPSD(filePath);
                    case ".pnm" -> readPNM(filePath);
                    case ".pgm" -> readPGM(filePath);
                    case ".hdr" -> readHDR(filePath);
                    //case ".raw" -> new readRAW(filePath);
                    default -> throw new IOException("Unable to load Image: " + filePath);
                };
    }
    
    private static @NotNull Image readSTB(@NotNull Path filePath) throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width    = stack.mallocInt(1);
            IntBuffer height   = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
        
            ByteBuffer container = stbi_load(filePath.toString(), width, height, channels, 0);
        
            if (container == null) throw new IOException("Unable to load Image: " + filePath);
        
            ColorFormat format = ColorFormat.get(channels.get(0));
            ColorBuffer buffer = ColorBuffer.wrap(format, container);
        
            return new Image(buffer, width.get(0), height.get(0));
        }
    }
    
    public static @NotNull Image readPNG(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readPNG(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readBMP(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readBMP(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readTGA(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readTGA(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readJPEG(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readJPEG(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readGIF(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readGIF(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readPIC(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readPIC(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readPSD(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readPSD(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readPNM(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readPNM(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readPGM(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readPGM(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    public static @NotNull Image readHDR(@NotNull Path filePath) throws IOException
    {
        ImageReader.LOGGER.trace("readHDR(%s)", filePath);
        
        return readSTB(filePath);
    }
    
    private ImageReader() {}
}
