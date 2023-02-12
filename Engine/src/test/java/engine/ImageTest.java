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
        this.image = new Image(ColorFormat.DEFAULT, this.width, this.height);
        
        final Colorc TL = Color.RED;
        final Colorc TR = Color.GREEN;
        final Colorc BL = Color.BLUE;
        final Colorc BR = Color.BLANK;
        
        final Color color  = new Color();
        final Color colorL = new Color();
        final Color colorR = new Color();
        
        int rs = 0x0F;
        int gs = 0x0F;
        int bs = 0x0F;
        int as = 0x0F;
        
        double t;
        int    r, g, b, a;
        for (int j = 0; j < this.height; j++)
        {
            t = (double) j / (this.height - 1);
            
            TL.interpolate(BL, t, colorL);
            TR.interpolate(BR, t, colorR);
            
            for (int i = 0; i < this.width; i++)
            {
                t = (double) i / (this.width - 1);
                
                colorL.interpolate(colorR, t, color);
                
                r = ((((color.r() + (127 / rs)) * rs) / 255) * 255) / rs;
                g = ((((color.g() + (127 / gs)) * gs) / 255) * 255) / gs;
                b = ((((color.b() + (127 / bs)) * bs) / 255) * 255) / bs;
                a = ((((color.a() + (127 / as)) * as) / 255) * 255) / as;
                
                this.image.data.put(j * this.width + i, r, g, b, a);
            }
        }
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
        
        image = new Image(ColorFormat.RED_ALPHA, 1000, 1000);
        Assertions.assertNotNull(image.data());
        Assertions.assertEquals(1000, image.width());
        Assertions.assertEquals(1000, image.height());
        Assertions.assertEquals(ColorFormat.RED_ALPHA, image.format());
        image.delete();
        
        image = new Image(IOUtil.getPath("image/TestImage.png"));
        Assertions.assertNotNull(image.data());
        Assertions.assertEquals(1000, image.width());
        Assertions.assertEquals(1000, image.height());
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        image.delete();
        
        image = new Image(IOUtil.getPath("invalid/path/to/image.png"));
        Assertions.assertNull(image.data());
        Assertions.assertEquals(0, image.width());
        Assertions.assertEquals(0, image.height());
        Assertions.assertEquals(ColorFormat.UNKNOWN, image.format());
        image.delete();
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
        boolean result   = this.image.save(filePath);
        Assertions.assertTrue(result);
        Assertions.assertTrue(filePath.toFile().exists());
    }
    
    @Test
    void palette()
    {
        ColorBuffer palette;
        
        Colorc[] colors = {
                Color.DARK_RED, Color.RED, Color.LIGHT_RED, Color.DARK_GREEN, Color.GREEN, Color.LIGHT_GREEN, Color.DARK_BLUE, Color.BLUE, Color.LIGHT_BLUE
        };
        
        int colorIndex = 0;
        for (int j = 0; j < this.height; j++)
        {
            for (int i = 0; i < this.width; i++)
            {
                this.image.data.put(j * this.width + i, colors[colorIndex++]);
                if (colorIndex >= colors.length) colorIndex = 0;
            }
        }
        
        palette = this.image.palette(256);
        Assertions.assertEquals(256, palette.capacity());
        Assertions.assertEquals(9, palette.limit());
        palette.free();
        
        palette = this.image.palette(8);
        Assertions.assertEquals(8, palette.capacity());
        Assertions.assertEquals(8, palette.limit());
        palette.free();
    }
}