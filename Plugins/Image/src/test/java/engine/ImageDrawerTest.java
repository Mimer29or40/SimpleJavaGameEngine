package engine;

import engine.color.Color;
import engine.color.ColorBlend;
import engine.color.ColorFormat;
import engine.util.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class ImageDrawerTest
{
    static Path outputDir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        ImageDrawerTest.outputDir = IOUtil.getPath("out/ImageDrawer").toAbsolutePath();
        try
        {
            Files.createDirectories(ImageDrawerTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    final ImageGenerator generator = ImageGenerator.colorGradient(Color.RED, Color.GREEN, Color.BLUE, Color.BLANK);
    
    final int width  = 1000;
    final int height = 1000;
    
    Image       image;
    ImageDrawer drawer;
    
    String fileName = null;
    
    @BeforeEach
    void setUp()
    {
        this.image = this.generator.generate(ColorFormat.RGBA, this.width, this.height);
        this.image.save(ImageDrawerTest.outputDir.resolve("Image.png"));
        
        this.drawer = new ImageDrawer(this.image);
    }
    
    @AfterEach
    void tearDown()
    {
        if (this.image == null || this.fileName == null) return;
        
        this.image.save(ImageDrawerTest.outputDir.resolve(this.fileName));
        this.image.delete();
    }
    
    @Test
    void clear()
    {
        this.fileName = "Clear.png";
        this.drawer.clear(Color.GRAY);
        
        Color color = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.image.data.get(i, color);
            
            Assertions.assertEquals(Color.GRAY, color);
        }
    }
    
    @Test
    void drawPixel()
    {
        this.fileName = "DrawPixel.png";
        this.drawer.drawPixel(50, 50, Color.WHITE);
        
        int   index = 50 * this.width + 50;
        Color color = this.image.data.get(index);
        
        Assertions.assertEquals(Color.WHITE, color);
    }
    
    @Test
    void drawLine()
    {
        this.fileName = "DrawLine.png";
        //noinspection DataFlowIssue
        int size = Math.min(this.width, this.height);
        for (int i = 0, lines = 25; i < lines; i++)
        {
            int a = 0;
            int b = i * (size / lines);
            int c = size - a;
            int d = size - b;
            this.drawer.drawLine(a, b, b, c, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
            this.drawer.drawLine(b, c, c, d, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
            this.drawer.drawLine(c, d, d, a, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
            this.drawer.drawLine(d, a, a, b, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
        }
    }
    
    @Test
    void drawRectangle()
    {
        this.fileName = "DrawRectangle.png";
        this.drawer.drawRectangle(this.width / 4, this.height / 4, this.width / 2, this.height / 2, 10, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    @Test
    void fillRectangle()
    {
        this.fileName = "FillRectangle.png";
        this.drawer.fillRectangle(this.width / 4, this.height / 4, this.width / 2, this.height / 2, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    @Test
    void drawCircle()
    {
        this.fileName = "DrawCircle.png";
        this.drawer.drawCircle(this.width / 2, this.height / 2, this.width / 4, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    @Test
    void fillCircle()
    {
        this.fileName = "FillCircle.png";
        this.drawer.fillCircle(this.width / 2, this.height / 2, this.width / 4, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    @Test
    void drawTriangle()
    {
        this.fileName = "DrawTriangle.png";
        
        int x1 = this.width / 4;
        int y1 = 3 * this.height / 4;
        int x2 = 3 * this.width / 4;
        int y2 = 3 * this.height / 4;
        int x3 = this.width / 2;
        int y3 = this.height / 4;
        
        this.drawer.drawTriangle(x1, y1, x2, y2, x3, y3, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    @Test
    void fillTriangle()
    {
        this.fileName = "FillTriangle.png";
        
        int x1 = this.width / 4;
        int y1 = 3 * this.height / 4;
        int x2 = 3 * this.width / 4;
        int y2 = 3 * this.height / 4;
        int x3 = this.width / 2;
        int y3 = this.height / 4;
        
        this.drawer.fillTriangle(x1, y1, x2, y2, x3, y3, Color.DARK_MAGENTA, ColorBlend.ADDITIVE);
    }
    
    static @NotNull Stream<Arguments> blendModeStream()
    {
        return Stream.of(Arguments.of(true, ColorBlend.NONE, "NONE"),
                         Arguments.of(false, ColorBlend.NONE, "NONE"),
                         Arguments.of(true, ColorBlend.ALPHA, "ALPHA"),
                         Arguments.of(false, ColorBlend.ALPHA, "ALPHA"),
                         Arguments.of(true, ColorBlend.ADDITIVE, "ADDITIVE"),
                         Arguments.of(false, ColorBlend.ADDITIVE, "ADDITIVE"),
                         Arguments.of(true, ColorBlend.MULTIPLICATIVE, "MULTIPLICATIVE"),
                         Arguments.of(false, ColorBlend.MULTIPLICATIVE, "MULTIPLICATIVE"),
                         Arguments.of(true, ColorBlend.STENCIL, "STENCIL"),
                         Arguments.of(false, ColorBlend.STENCIL, "STENCIL"),
                         Arguments.of(true, ColorBlend.ADD_COLORS, "ADD_COLORS"),
                         Arguments.of(false, ColorBlend.ADD_COLORS, "ADD_COLORS"),
                         Arguments.of(true, ColorBlend.SUB_COLORS, "SUB_COLORS"),
                         Arguments.of(false, ColorBlend.SUB_COLORS, "SUB_COLORS"),
                         Arguments.of(true, ColorBlend.ILLUMINATE, "ILLUMINATE"),
                         Arguments.of(false, ColorBlend.ILLUMINATE, "ILLUMINATE"));
    }
    
    @ParameterizedTest
    @MethodSource("blendModeStream")
    void drawImageDefault(boolean invert, ColorBlend blendMode, String name)
    {
        this.fileName = String.format("DrawImage-%s%s.png", name, invert ? "-invert" : "");
        
        ImageGenerator generator = ImageGenerator.colorGradientRadial(Color.WHITE, Color.BLANK);
        
        MutableImage image = new MutableImage(generator.generate(ColorFormat.RGBA, this.width, this.height));
        if (invert) image.colorInvert();
        
        int dstX = this.width / 4;
        int dstY = this.height / 4;
        int dstW = this.width / 2;
        int dstH = this.height / 2;
        
        this.drawer.drawImage(image, 0, 0, this.width, this.height, dstX, dstY, dstW, dstH, blendMode);
        image.delete();
    }
}