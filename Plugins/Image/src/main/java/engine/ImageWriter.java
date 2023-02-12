package engine;

import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImageWrite.*;

public class ImageWriter
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public static boolean write(@NotNull Image image, @NotNull Path filePath) throws IOException
    {
        String extension = IOUtil.getExtension(filePath.toString());
        
        return switch (extension)
                {
                    case ".png" -> writePNG(image, filePath);
                    case ".bmp" -> writeBMP(image, filePath);
                    case ".tga" -> writeTGA(image, filePath);
                    case ".jpg", ".jpeg" -> writeJPEG(image, filePath);
                    case ".raw" -> writeRAW(image, filePath);
                    default -> throw new IOException("Unable to save Image: " + filePath);
                };
    }
    
    public static boolean writePNG(@NotNull Image image, @NotNull Path filePath)
    {
        ImageWriter.LOGGER.trace("writePNG(%s, %s)", image, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(image.data);
        
        int channels = image.format().sizeof;
        int stride   = image.width * channels;
        
        return stbi_write_png(filePath.toString(), image.width, image.height, channels, buffer, stride);
    }
    
    public static boolean writeBMP(@NotNull Image image, @NotNull Path filePath)
    {
        ImageWriter.LOGGER.trace("writeBMP(%s, %s)", image, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(image.data);
        
        int channels = image.format().sizeof;
        
        return stbi_write_bmp(filePath.toString(), image.width, image.height, channels, buffer);
    }
    
    public static boolean writeTGA(@NotNull Image image, @NotNull Path filePath)
    {
        ImageWriter.LOGGER.trace("writeTGA(%s, %s)", image, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(image.data);
        
        int channels = image.format().sizeof;
        
        return stbi_write_tga(filePath.toString(), image.width, image.height, channels, buffer);
    }
    
    public static boolean writeJPEG(@NotNull Image image, @NotNull Path filePath)
    {
        ImageWriter.LOGGER.trace("writeJPEG(%s, %s)", image, filePath);
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(image.data);
        
        int channels = image.format().sizeof;
        
        return stbi_write_jpg(filePath.toString(), image.width, image.height, channels, buffer, 90);
    }
    
    public static boolean writeRAW(@NotNull Image image, @NotNull Path filePath)
    {
        ImageWriter.LOGGER.trace("writeRAW(%s, %s)", image, filePath);
        
        int sizeof = image.width * image.height * image.format().sizeof;
        
        ByteBuffer buffer = MemoryUtil.memByteBuffer(image.data);
        buffer.limit(buffer.position() + sizeof);
        
        int[] bytesWritten = new int[1];
        
        boolean success = IOUtil.writeToFile(filePath, buffer, bytesWritten);
        
        return success && bytesWritten[0] == sizeof;
    }
    
    private ImageWriter() {}
}
