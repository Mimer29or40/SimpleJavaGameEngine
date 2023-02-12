package engine;

import engine.color.Color;
import engine.color.ColorFormat;
import engine.color.Colorc;
import engine.util.IOUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

class MutableImageTest
{
    static Path outputDir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        MutableImageTest.outputDir = IOUtil.getPath("out/MutableImage").toAbsolutePath();
        try
        {
            Files.createDirectories(MutableImageTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    final int width  = 1000;
    final int height = 1000;
    
    MutableImage image;
    
    @BeforeEach
    void setUp()
    {
        this.image = new MutableImage(ColorFormat.DEFAULT, this.width, this.height);
        
        final Colorc TL = Color.RED;
        final Colorc TR = Color.GREEN;
        final Colorc BL = Color.BLUE;
        final Colorc BR = Color.BLANK;
        
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
                
                this.image.data.put(j * this.width + i, color);
            }
        }
        
        this.image.save(MutableImageTest.outputDir.resolve("Image.png"));
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
        
        image = new MutableImage(ColorFormat.RED_ALPHA, 1000, 1000);
        Assertions.assertNotNull(image.data());
        Assertions.assertEquals(1000, image.width());
        Assertions.assertEquals(1000, image.height());
        Assertions.assertEquals(ColorFormat.RED_ALPHA, image.format());
        image.delete();
        
        image = new MutableImage(IOUtil.getPath("image/TestImage.png"));
        Assertions.assertNotNull(image.data());
        Assertions.assertEquals(1000, image.width());
        Assertions.assertEquals(1000, image.height());
        Assertions.assertEquals(ColorFormat.RGBA, image.format());
        
        image = new MutableImage(IOUtil.getPath("invalid/path/to/image.png"));
        Assertions.assertNull(image.data());
        Assertions.assertEquals(0, image.width());
        Assertions.assertEquals(0, image.height());
        Assertions.assertEquals(ColorFormat.UNKNOWN, image.format());
    }
    
    @Test
    void reformat()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.image.reformat(ColorFormat.UNKNOWN));
        
        int width  = this.image.width();
        int height = this.image.height();
        
        Image image = this.image.reformat(ColorFormat.RED_ALPHA);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(ColorFormat.RED_ALPHA, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("Image-Reformat.png"));
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void resize(boolean smaller)
    {
        ColorFormat format = this.image.format();
        
        int width  = smaller ? this.width / 4 : this.width * 4;
        int height = smaller ? this.height / 4 : this.height * 4;
        
        Image image = this.image.resize(width, height);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(format, this.image.format());
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        
        String mod = smaller ? "Smaller" : "Larger";
        this.image.save(MutableImageTest.outputDir.resolve("Image-Resize-" + mod + ".png"));
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void resizeNN(boolean smaller)
    {
        ColorFormat format = this.image.format();
        
        int width  = smaller ? this.width / 4 : this.width * 4;
        int height = smaller ? this.height / 4 : this.height * 4;
        
        Image image = this.image.resizeNN(width, height);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(format, this.image.format());
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        
        String mod = smaller ? "Smaller" : "Larger";
        this.image.save(MutableImageTest.outputDir.resolve("Image-ResizeNN-" + mod + ".png"));
    }
    
    @Test
    void toPOT()
    {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.image.reformat(ColorFormat.UNKNOWN));
        
        Image image = this.image.toPOT();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(1024, this.image.width());
        Assertions.assertEquals(1024, this.image.height());
        Assertions.assertEquals(ColorFormat.RGBA, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("Image-ToPOT.png"));
    }
    
    @Test
    void crop()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.image.crop(-1, 0, 50, 50));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.image.crop(0, -1, 50, 50));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.image.crop(0, 0, this.width + 1, 50));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.image.crop(0, 0, 50, this.height + 1));
        
        ColorFormat format = this.image.format();
        
        Image image = this.image.crop(50, 0, 50, 50);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(50, this.image.width());
        Assertions.assertEquals(50, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("Image-Crop.png"));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0x5650, 0x6666, 0x5551, 0x4444, 0x3320, 0x2420, 0x1610})
    void quantize(int value)
    {
        int r = (value >> 12) & 0xF;
        int g = (value >> 8) & 0xF;
        int b = (value >> 4) & 0xF;
        int a = value & 0xF;
        
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.quantize(r, g, b, a);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(format, this.image.format());
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("quantize-%04X.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20, 30, 40, 50})
    void neuQuantize(int value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.neuQuantize(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(format, this.image.format());
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("neuQuantize-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0x5650, 0x6666, 0x5551, 0x4444, 0x3320, 0x2420, 0x1610})
    void dither(int value)
    {
        int r = (value >> 12) & 0xF;
        int g = (value >> 8) & 0xF;
        int b = (value >> 4) & 0xF;
        int a = value & 0xF;
        
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.dither(r, g, b, a);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(format, this.image.format());
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("dither-%04X.png", value)));
    }
    
    @Test
    void flipV()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.flipV();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("flipV.png"));
    }
    
    @Test
    void flipH()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.flipH();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("flipH.png"));
    }
    
    @Test
    void rotateCW()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.rotateCW();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("rotateCW.png"));
    }
    
    @Test
    void rotateCCW()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.rotateCCW();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("rotateCCW.png"));
    }
    
    @Test
    void colorTint()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorTint(Color.DARKER_CYAN);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("colorTint.png"));
    }
    
    @Test
    void colorGrayscale()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorGrayscale();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("colorGrayscale.png"));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {+0.5, -0.5})
    void colorBrightness(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorBrightness(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorBrightness-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {+0.5, -0.5})
    void colorContrast(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorContrast(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorContrast-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {+1.5, -1.5})
    void colorGamma(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorGamma(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorGamma-%s.png", value)));
    }
    
    @Test
    void colorInvert()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorInvert();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("colorInvert.png"));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {+0.5})
    void colorBrighter(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorBrighter(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorBrighter-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {+0.5})
    void colorDarker(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorDarker(value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorDarker-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {0.25, 0.5, 1.0})
    void colorReplace(double value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.colorReplace(Color.GREEN, Color.CYAN, value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("colorReplace-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {63, 127, 196, 225})
    void alphaClear(int value)
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.alphaClear(Color.YELLOW, value);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("alphaClear-%s.png", value)));
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void alphaMask(boolean invert)
    {
        ImageGenerator generator = ImageGenerator.colorGradientRadial(Color.WHITE, Color.BLANK);
    
        MutableImage mask = new MutableImage(ColorFormat.RGBA, this.width, this.height);
        
        generator.generate(mask);
        
        if (invert) mask.colorInvert();
        
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.alphaMask(mask);
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve(String.format("alphaMask%s.png", invert ? "-Inverted" : "")));
    }
    
    @Test
    void alphaPreMultiply()
    {
        int         width  = this.image.width();
        int         height = this.image.height();
        ColorFormat format = this.image.format();
        
        Image image = this.image.alphaPreMultiply();
        Assertions.assertSame(this.image, image);
        Assertions.assertEquals(width, this.image.width());
        Assertions.assertEquals(height, this.image.height());
        Assertions.assertEquals(format, this.image.format());
        
        this.image.save(MutableImageTest.outputDir.resolve("alphaPreMultiply.png"));
    }
}