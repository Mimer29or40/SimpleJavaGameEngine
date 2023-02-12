package engine;

import engine.color.Color;
import engine.color.ColorFormat;
import engine.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

class ImageWriterTest
{
    static Path  outputDir;
    static Image image;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        ImageWriterTest.outputDir = IOUtil.getPath("out/ImageWriter").toAbsolutePath();
        try
        {
            Files.createDirectories(ImageWriterTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
        
        ImageGenerator generator = ImageGenerator.colorGradient(Color.RED, Color.GREEN, Color.BLUE, Color.BLANK);
        
        ImageWriterTest.image = generator.generate(ColorFormat.RGBA, 128, 128);
    }
    
    @Test
    void write() throws IOException
    {
        Path    filePath;
        boolean result;
        
        filePath = ImageWriterTest.outputDir.resolve("Image.png");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        filePath = ImageWriterTest.outputDir.resolve("Image.bmp");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        filePath = ImageWriterTest.outputDir.resolve("Image.tga");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        filePath = ImageWriterTest.outputDir.resolve("Image.jpg");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        filePath = ImageWriterTest.outputDir.resolve("Image.jpeg");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        filePath = ImageWriterTest.outputDir.resolve("Image.raw");
        result   = ImageWriter.write(ImageWriterTest.image, filePath);
        Assertions.assertTrue(result);
        
        Assertions.assertThrows(IOException.class, () -> ImageWriter.write(ImageWriterTest.image, ImageWriterTest.outputDir.resolve("Image")));
    }
}