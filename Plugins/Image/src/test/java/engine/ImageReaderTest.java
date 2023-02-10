package engine;

import engine.color.ColorFormat;
import engine.util.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

class ImageReaderTest
{
    static Path outputDir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        ImageReaderTest.outputDir = IOUtil.getPath("out/ImageReader").toAbsolutePath();
        try
        {
            Files.createDirectory(ImageReaderTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    @Test
    void read() throws IOException
    {
        Path  filePath;
        Image image;
        
        filePath = IOUtil.getPath("image/Test.png");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        filePath = IOUtil.getPath("image/Test.bmp");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        filePath = IOUtil.getPath("image/Test.tga");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        filePath = IOUtil.getPath("image/Test.jpg");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGB, image.format());
        
        filePath = IOUtil.getPath("image/Test.jpeg");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGB, image.format());
        
        filePath = IOUtil.getPath("image/Test.gif");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        filePath = IOUtil.getPath("image/Test.psd");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        filePath = IOUtil.getPath("image/Test.pnm");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGB, image.format());
        
        filePath = IOUtil.getPath("image/Test.hdr");
        image    = ImageReader.read(filePath.toString());
        Assertions.assertNotNull(image.data);
        Assertions.assertEquals(128, image.width);
        Assertions.assertEquals(128, image.height);
        Assertions.assertEquals(ColorFormat.RGB, image.format());
    }
}