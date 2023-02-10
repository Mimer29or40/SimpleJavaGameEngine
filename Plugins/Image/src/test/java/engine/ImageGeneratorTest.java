package engine;

import engine.color.Color;
import engine.color.ColorFormat;
import engine.noise.*;
import engine.util.IOUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

class ImageGeneratorTest
{
    static Path outputDir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        ImageGeneratorTest.outputDir = IOUtil.getPath("out/ImageGenerator").toAbsolutePath();
        try
        {
            Files.createDirectories(ImageGeneratorTest.outputDir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    final int width  = 1000;
    final int height = 1000;
    
    String fileName = null;
    Image  image;
    
    @AfterEach
    void tearDown()
    {
        if (this.image == null || this.fileName == null) return;
        
        this.image.save(ImageGeneratorTest.outputDir.resolve(this.fileName).toString());
        this.image.delete();
    }
    
    @Test
    void colorSolid()
    {
        ImageGenerator generator = ImageGenerator.colorSolid(Color.LIGHTER_BLUE);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorSolid.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradient()
    {
        ImageGenerator generator = ImageGenerator.colorGradient(Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradient.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradient(Color.RED, Color.GREEN, Color.BLUE, Color.WHITE, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientVertical()
    {
        ImageGenerator generator = ImageGenerator.colorGradientVertical(Color.BLUE, Color.GREEN);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientVertical.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientVerticalSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradientVertical(Color.BLUE, Color.GREEN, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientVerticalSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientHorizontal()
    {
        ImageGenerator generator = ImageGenerator.colorGradientHorizontal(Color.YELLOW, Color.RED);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientHorizontal.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientHorizontalSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradientHorizontal(Color.YELLOW, Color.RED, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientHorizontalSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientDiagonalTLBR()
    {
        ImageGenerator generator = ImageGenerator.colorGradientDiagonalTLBR(Color.BLUE, Color.WHITE);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientDiagonalTLBR.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientDiagonalTLBRSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradientDiagonalTLBR(Color.BLUE, Color.WHITE, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientDiagonalTLBRSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientDiagonalTRBL()
    {
        ImageGenerator generator = ImageGenerator.colorGradientDiagonalTRBL(Color.BLACK, Color.RED);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientDiagonalTRBL.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientDiagonalTRBLSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradientDiagonalTRBL(Color.BLACK, Color.RED, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientDiagonalTRBLSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientRadial()
    {
        ImageGenerator generator = ImageGenerator.colorGradientRadial(Color.BLACK, Color.RED);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientRadial.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorGradientRadialSmoothstep()
    {
        ImageGenerator generator = ImageGenerator.colorGradientRadial(Color.BLACK, Color.RED, ImageGeneratorTest::smoothstep);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorGradientRadialSmoothstep.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void colorCheckered()
    {
        ImageGenerator generator = ImageGenerator.colorCheckered(this.width / 12, this.height / 12, Color.DARK_GREEN, Color.DARK_RED);
        Assertions.assertNotNull(generator);
        
        this.fileName = "colorCheckered.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void noiseWhite()
    {
        WhiteNoise noise = new WhiteNoise();
        noise.random = new Random(0x69420);
        
        ImageGenerator generator = ImageGenerator.noise(noise, 0, 0, 1.0, 1.0);
        Assertions.assertNotNull(generator);
        
        this.fileName = "noiseWhite.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void noisePerlinRidge()
    {
        Noise2D noise = new PerlinRidgeNoise();
        
        ImageGenerator generator = ImageGenerator.noise(noise, 0, 0, 1.0, 1.0);
        Assertions.assertNotNull(generator);
        
        this.fileName = "noisePerlinRidge.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void noisePerlinFBM()
    {
        Noise2D noise = new PerlinFBMNoise();
        
        ImageGenerator generator = ImageGenerator.noise(noise, 0, 0, 1.0, 1.0);
        Assertions.assertNotNull(generator);
        
        this.fileName = "noisePerlinFBM.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void noisePerlinTurbulence()
    {
        Noise2D noise = new PerlinTurbulenceNoise();
        
        ImageGenerator generator = ImageGenerator.noise(noise, 0, 0, 1.0, 1.0);
        Assertions.assertNotNull(generator);
        
        this.fileName = "noisePerlinTurbulence.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    @Test
    void noiseWorley()
    {
        WorleyNoise noise = new WorleyNoise();
        
        ImageGenerator generator = ImageGenerator.noise(noise, 0, 0, 10.0, 10.0);
        Assertions.assertNotNull(generator);
        
        this.fileName = "noiseWorley.png";
        this.image    = generator.generate(ColorFormat.DEFAULT, this.width, this.height);
        Assertions.assertNotNull(this.image);
        Assertions.assertEquals(this.width, this.image.width());
        Assertions.assertEquals(this.height, this.image.height());
        Assertions.assertEquals(ColorFormat.DEFAULT, this.image.format());
    }
    
    static double smoothstep(double t)
    {
        return t * t * (3.0 - 2.0 * t);
    }
}