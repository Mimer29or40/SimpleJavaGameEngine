package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.color.Colorc;
import engine.util.IOUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

class ImageTest
{
    static Path outputDir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        ImageTest.outputDir = IOUtil.getPath("out/Image").toAbsolutePath();
        try
        {
            Files.createDirectories(ImageTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    final int width  = 1000;
    final int height = 1000;
    
    Image image;
    
    @BeforeEach
    void setUp()
    {
        ColorBuffer data = ColorBuffer.malloc(this.width * this.height);
    
        final Colorc TL = Color.RED;
        final Colorc TR = Color.GREEN;
        final Colorc BL = Color.BLUE;
        final Colorc BR = Color.WHITE;
        
        final Color color  = new Color();
        final Color colorL = new Color();
        final Color colorR = new Color();
        
        double t;
        for (int j = 0; j < this.height; j++)
        {
            t = (double) j / (this.height - 1);
            
            TL.interpolate(BL, t, colorL);
            TR.interpolate(BR, t, colorR);
            
            for (int i = 0; i < this.width; i++)
            {
                t = (double) i / (this.width - 1);
                
                colorL.interpolate(colorR, t, color);
                
                data.put(j * this.width + i, color);
            }
        }
        
        this.image = new Image(data, this.width, this.height);
    }
    
    @AfterEach
    void tearDown()
    {
        this.image.delete();
        this.image = null;
    }
    
    @Test
    void load()
    {
        Image image;
        
        image = new Image(IOUtil.getPath("image/TestImage.png").toString());
        Assertions.assertNotNull(image.data());
        Assertions.assertEquals(1000, image.width());
        Assertions.assertEquals(1000, image.height());
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        image = new Image("invalid/path/to/image.png");
        Assertions.assertNull(image.data());
        Assertions.assertEquals(0, image.width());
        Assertions.assertEquals(0, image.height());
        Assertions.assertEquals(ColorFormat.UNKNOWN, image.format());
    }
    
    @Test
    void properties()
    {
        Assertions.assertNotNull(this.image.data());
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void delete()
    {
        Assertions.assertNotNull(this.image.data());
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        
        this.image.delete();
        Assertions.assertNull(this.image.data());
        Assertions.assertEquals(0, this.image.width());
        Assertions.assertEquals(0, this.image.height());
        Assertions.assertEquals(ColorFormat.UNKNOWN, this.image.format());
    }
    
    @Test
    void save()
    {
        Path    filePath = ImageTest.outputDir.resolve("Image.png");
        boolean result   = this.image.save(filePath.toString());
        Assertions.assertTrue(result);
        Assertions.assertTrue(filePath.toFile().exists());
    }
}