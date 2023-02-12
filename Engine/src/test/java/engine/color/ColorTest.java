package engine.color;

import org.joml.Vector3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorTest
{
    @Test
    void constructor()
    {
        Color color;
        
        color = new Color();
        Assertions.assertEquals(0, color.r);
        Assertions.assertEquals(0, color.g);
        Assertions.assertEquals(0, color.b);
        Assertions.assertEquals(255, color.a);
        
        color = new Color(Color.DARK_GREEN);
        Assertions.assertEquals(0, color.r);
        Assertions.assertEquals(191, color.g);
        Assertions.assertEquals(0, color.b);
        Assertions.assertEquals(255, color.a);
        
        color = new Color(50, 200, 100, 250);
        Assertions.assertEquals(50, color.r);
        Assertions.assertEquals(200, color.g);
        Assertions.assertEquals(100, color.b);
        Assertions.assertEquals(250, color.a);
        
        color = new Color(50, 200, 100);
        Assertions.assertEquals(50, color.r);
        Assertions.assertEquals(200, color.g);
        Assertions.assertEquals(100, color.b);
        Assertions.assertEquals(255, color.a);
        
        color = new Color(150, 100);
        Assertions.assertEquals(150, color.r);
        Assertions.assertEquals(150, color.g);
        Assertions.assertEquals(150, color.b);
        Assertions.assertEquals(100, color.a);
        
        color = new Color(150);
        Assertions.assertEquals(150, color.r);
        Assertions.assertEquals(150, color.g);
        Assertions.assertEquals(150, color.b);
        Assertions.assertEquals(255, color.a);
        
        color = new Color(0.2, 0.8, 0.4, 0.95);
        Assertions.assertEquals(51, color.r);
        Assertions.assertEquals(204, color.g);
        Assertions.assertEquals(102, color.b);
        Assertions.assertEquals(243, color.a);
        
        color = new Color(0.2, 0.8, 0.4);
        Assertions.assertEquals(51, color.r);
        Assertions.assertEquals(204, color.g);
        Assertions.assertEquals(102, color.b);
        Assertions.assertEquals(255, color.a);
        
        color = new Color(0.6, 0.4);
        Assertions.assertEquals(153, color.r);
        Assertions.assertEquals(153, color.g);
        Assertions.assertEquals(153, color.b);
        Assertions.assertEquals(102, color.a);
        
        color = new Color(0.6);
        Assertions.assertEquals(153, color.r);
        Assertions.assertEquals(153, color.g);
        Assertions.assertEquals(153, color.b);
        Assertions.assertEquals(255, color.a);
    }
    
    @Test
    void equals()
    {
        Colorc color;
        
        color = new Color(50, 200, 100, 250);
        Assertions.assertTrue(color.equals(50, 200, 100, 250));
        Assertions.assertTrue(color.equals(50, 200, 100));
        Assertions.assertTrue(color.equals(Color.toGray(50, 200, 100), 250));
        Assertions.assertTrue(color.equals(Color.toGray(50, 200, 100)));
        
        color = new Color(0.2, 0.8, 0.4, 0.95);
        Assertions.assertTrue(color.equals(0.2, 0.8, 0.4, 0.95));
        Assertions.assertTrue(color.equals(0.2, 0.8, 0.4));
        Assertions.assertTrue(color.equals(Color.toGray(0.2, 0.8, 0.4), 0.95));
        Assertions.assertTrue(color.equals(Color.toGray(0.2, 0.8, 0.4)));
        
        // GRAYSCALE
        color = new Color(255, 255, 255, 255);
        Assertions.assertEquals(color, Color.WHITE);
        
        color = new Color(191, 191, 191, 255);
        Assertions.assertEquals(color, Color.LIGHT_GRAY);
        
        color = new Color(127, 127, 127, 255);
        Assertions.assertEquals(color, Color.GRAY);
        
        color = new Color(63, 63, 63, 255);
        Assertions.assertEquals(color, Color.DARK_GRAY);
        
        color = new Color(0, 0, 0, 255);
        Assertions.assertEquals(color, Color.BLACK);
        
        Assertions.assertEquals(Color.LIGHT_GREY, Color.LIGHT_GRAY);
        Assertions.assertEquals(Color.GREY, Color.GRAY);
        Assertions.assertEquals(Color.DARK_GREY, Color.DARK_GRAY);
        
        // RED
        Assertions.assertTrue(Color.LIGHTEST_RED.equals(255, 191, 191, 255));
        Assertions.assertTrue(Color.LIGHTER_RED.equals(255, 127, 127, 255));
        Assertions.assertTrue(Color.LIGHT_RED.equals(255, 63, 63, 255));
        Assertions.assertTrue(Color.RED.equals(255, 0, 0, 255));
        Assertions.assertTrue(Color.DARK_RED.equals(191, 0, 0, 255));
        Assertions.assertTrue(Color.DARKER_RED.equals(127, 0, 0, 255));
        Assertions.assertTrue(Color.DARKEST_RED.equals(63, 0, 0, 255));
        
        // YELLOW
        Assertions.assertTrue(Color.LIGHTEST_YELLOW.equals(255, 255, 191, 255));
        Assertions.assertTrue(Color.LIGHTER_YELLOW.equals(255, 255, 127, 255));
        Assertions.assertTrue(Color.LIGHT_YELLOW.equals(255, 255, 63, 255));
        Assertions.assertTrue(Color.YELLOW.equals(255, 255, 0, 255));
        Assertions.assertTrue(Color.DARK_YELLOW.equals(191, 191, 0, 255));
        Assertions.assertTrue(Color.DARKER_YELLOW.equals(127, 127, 0, 255));
        Assertions.assertTrue(Color.DARKEST_YELLOW.equals(63, 63, 0, 255));
        
        // GREEN
        Assertions.assertTrue(Color.LIGHTEST_GREEN.equals(191, 255, 191, 255));
        Assertions.assertTrue(Color.LIGHTER_GREEN.equals(127, 255, 127, 255));
        Assertions.assertTrue(Color.LIGHT_GREEN.equals(63, 255, 63, 255));
        Assertions.assertTrue(Color.GREEN.equals(0, 255, 0, 255));
        Assertions.assertTrue(Color.DARK_GREEN.equals(0, 191, 0, 255));
        Assertions.assertTrue(Color.DARKER_GREEN.equals(0, 127, 0, 255));
        Assertions.assertTrue(Color.DARKEST_GREEN.equals(0, 63, 0, 255));
        
        // CYAN
        Assertions.assertTrue(Color.LIGHTEST_CYAN.equals(191, 255, 255, 255));
        Assertions.assertTrue(Color.LIGHTER_CYAN.equals(127, 255, 255, 255));
        Assertions.assertTrue(Color.LIGHT_CYAN.equals(63, 255, 255, 255));
        Assertions.assertTrue(Color.CYAN.equals(0, 255, 255, 255));
        Assertions.assertTrue(Color.DARK_CYAN.equals(0, 191, 191, 255));
        Assertions.assertTrue(Color.DARKER_CYAN.equals(0, 127, 127, 255));
        Assertions.assertTrue(Color.DARKEST_CYAN.equals(0, 63, 63, 255));
        
        // BLUE
        Assertions.assertTrue(Color.LIGHTEST_BLUE.equals(191, 191, 255, 255));
        Assertions.assertTrue(Color.LIGHTER_BLUE.equals(127, 127, 255, 255));
        Assertions.assertTrue(Color.LIGHT_BLUE.equals(63, 63, 255, 255));
        Assertions.assertTrue(Color.BLUE.equals(0, 0, 255, 255));
        Assertions.assertTrue(Color.DARK_BLUE.equals(0, 0, 191, 255));
        Assertions.assertTrue(Color.DARKER_BLUE.equals(0, 0, 127, 255));
        Assertions.assertTrue(Color.DARKEST_BLUE.equals(0, 0, 63, 255));
        
        // MAGENTA
        Assertions.assertTrue(Color.LIGHTEST_MAGENTA.equals(255, 191, 255, 255));
        Assertions.assertTrue(Color.LIGHTER_MAGENTA.equals(255, 127, 255, 255));
        Assertions.assertTrue(Color.LIGHT_MAGENTA.equals(255, 63, 255, 255));
        Assertions.assertTrue(Color.MAGENTA.equals(255, 0, 255, 255));
        Assertions.assertTrue(Color.DARK_MAGENTA.equals(191, 0, 191, 255));
        Assertions.assertTrue(Color.DARKER_MAGENTA.equals(127, 0, 127, 255));
        Assertions.assertTrue(Color.DARKEST_MAGENTA.equals(63, 0, 63, 255));
        
        // BLANK
        Assertions.assertTrue(Color.BLANK.equals(0, 0, 0, 0));
    }
    
    @Test
    void r()
    {
        Color color;
        
        color = new Color(164, 0, 0, 255);
        Assertions.assertEquals(164, color.r());
        Assertions.assertEquals(0.6431372761726379, color.rf());
        
        color.r = 333;
        Assertions.assertEquals(333, color.r());
        Assertions.assertEquals(1.0, color.rf());
    }
    
    @Test
    void g()
    {
        Color color;
        
        color = new Color(0, 164, 0, 255);
        Assertions.assertEquals(164, color.g());
        Assertions.assertEquals(0.6431372761726379, color.gf());
        
        color.g = 333;
        Assertions.assertEquals(333, color.g());
        Assertions.assertEquals(1.0, color.gf());
    }
    
    @Test
    void b()
    {
        Color color;
        
        color = new Color(0, 0, 164, 255);
        Assertions.assertEquals(164, color.b());
        Assertions.assertEquals(0.6431372761726379, color.bf());
        
        color.b = 333;
        Assertions.assertEquals(333, color.b());
        Assertions.assertEquals(1.0, color.bf());
    }
    
    @Test
    void a()
    {
        Color color;
        
        color = new Color(0, 0, 0, 164);
        Assertions.assertEquals(164, color.a());
        Assertions.assertEquals(0.6431372761726379, color.af());
        
        color.a = 333;
        Assertions.assertEquals(333, color.a());
        Assertions.assertEquals(1.0, color.af());
    }
    
    @Test
    void set()
    {
        Color color = new Color();
        Color result;
        
        result = color.set(Color.DARK_GREEN);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(Color.DARK_GREEN, result);
        
        result = color.set(50, 200, 100, 255);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(50, result.r());
        Assertions.assertEquals(200, result.g());
        Assertions.assertEquals(100, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = color.set(-50, 200, 100, 255);
        Assertions.assertEquals(0, result.r());
        
        result = color.set(300, 200, 100, 255);
        Assertions.assertEquals(255, result.r());
        
        result = color.set(50, -200, 100, 255);
        Assertions.assertEquals(0, result.g());
        
        result = color.set(50, 300, 100, 255);
        Assertions.assertEquals(255, result.g());
        
        result = color.set(50, 200, -100, 255);
        Assertions.assertEquals(0, result.b());
        
        result = color.set(50, 200, 300, 255);
        Assertions.assertEquals(255, result.b());
        
        result = color.set(50, 200, 100, -255);
        Assertions.assertEquals(0, result.a());
        
        result = color.set(50, 200, 100, 300);
        Assertions.assertEquals(255, result.a());
        
        color.a = 123;
        result  = color.set(50, 200, 100);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(50, result.r());
        Assertions.assertEquals(200, result.g());
        Assertions.assertEquals(100, result.b());
        Assertions.assertEquals(123, result.a());
        
        result = color.set(-50, 200, 100);
        Assertions.assertEquals(0, result.r());
        
        result = color.set(300, 200, 100);
        Assertions.assertEquals(255, result.r());
        
        result = color.set(50, -200, 100);
        Assertions.assertEquals(0, result.g());
        
        result = color.set(50, 300, 100);
        Assertions.assertEquals(255, result.g());
        
        result = color.set(50, 200, -100);
        Assertions.assertEquals(0, result.b());
        
        result = color.set(50, 200, 300);
        Assertions.assertEquals(255, result.b());
        
        result = color.set(150, 250);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(150, result.r());
        Assertions.assertEquals(150, result.g());
        Assertions.assertEquals(150, result.b());
        Assertions.assertEquals(250, result.a());
        
        result = color.set(-150, 250);
        Assertions.assertEquals(0, result.r());
        
        result = color.set(300, 250);
        Assertions.assertEquals(255, result.r());
        
        result = color.set(150, -250);
        Assertions.assertEquals(0, result.a());
        
        result = color.set(150, 300);
        Assertions.assertEquals(255, result.a());
        
        color.a = 123;
        result  = color.set(150);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(150, result.r());
        Assertions.assertEquals(150, result.g());
        Assertions.assertEquals(150, result.b());
        Assertions.assertEquals(123, result.a());
        
        result = color.set(-150);
        Assertions.assertEquals(0, result.r());
        
        result = color.set(300);
        Assertions.assertEquals(255, result.r());
    }
    
    @Test
    void setFromInt()
    {
        Color color = new Color();
        Color result;
        
        result = color.setFromInt(0x7F042069);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(0x04, result.r());
        Assertions.assertEquals(0x20, result.g());
        Assertions.assertEquals(0x69, result.b());
        Assertions.assertEquals(0x7F, result.a());
    }
    
    @Test
    void setFromHSB()
    {
        Color color = new Color(0, 0, 0, 123);
        Color result;
        
        result = color.setFromHSB(23.0, 0.33, 0.28);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(71, result.r());
        Assertions.assertEquals(57, result.g());
        Assertions.assertEquals(48, result.b());
        Assertions.assertEquals(123, result.a());
        
        result = color.setFromHSB(196.0, 0.72, 0.49);
        Assertions.assertSame(color, result);
        Assertions.assertEquals(35, result.r());
        Assertions.assertEquals(101, result.g());
        Assertions.assertEquals(125, result.b());
        Assertions.assertEquals(123, result.a());
    }
    
    @Test
    void tint()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Colorc tint   = Color.DARK_GRAY;
        Color  output = new Color();
        Color  result;
        
        result = color.tint(tint, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(15, result.r());
        Assertions.assertEquals(63, result.g());
        Assertions.assertEquals(15, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).tint(tint);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(15, result.r());
        Assertions.assertEquals(63, result.g());
        Assertions.assertEquals(15, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void grayscale()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        int    grayscale;
        
        result    = color.grayscale(output);
        grayscale = Color.toGray(color.r(), color.g(), color.b());
        Assertions.assertSame(output, result);
        Assertions.assertEquals(grayscale, result.r());
        Assertions.assertEquals(grayscale, result.g());
        Assertions.assertEquals(grayscale, result.b());
        Assertions.assertEquals(255, result.a());
        
        result    = output.set(color).grayscale();
        grayscale = Color.toGray(color.r(), color.g(), color.b());
        Assertions.assertSame(output, result);
        Assertions.assertEquals(grayscale, result.r());
        Assertions.assertEquals(grayscale, result.g());
        Assertions.assertEquals(grayscale, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void brightness()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.brightness(0.125, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(95, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(95, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).brightness(-0.125);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(63, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(63, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void contrast()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.contrast(0.875, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(124, result.r());
        Assertions.assertEquals(134, result.g());
        Assertions.assertEquals(124, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).contrast(-0.875);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(127, result.r());
        Assertions.assertEquals(128, result.g());
        Assertions.assertEquals(127, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void gamma()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.gamma(0.875, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(51, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(51, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).gamma(-0.875);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(255, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(255, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void invert()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.invert(output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(192, result.r());
        Assertions.assertEquals(0, result.g());
        Assertions.assertEquals(192, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).invert();
        Assertions.assertSame(output, result);
        Assertions.assertEquals(192, result.r());
        Assertions.assertEquals(0, result.g());
        Assertions.assertEquals(192, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void brighter()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.brighter(0.1, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(69, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(69, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).brighter(-0.1);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(63, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(63, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void darker()
    {
        Colorc color  = Color.LIGHT_GREEN;
        Color  output = new Color();
        Color  result;
        
        result = color.darker(0.1, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(56, result.r());
        Assertions.assertEquals(229, result.g());
        Assertions.assertEquals(56, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = output.set(color).darker(-0.1);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(63, result.r());
        Assertions.assertEquals(255, result.g());
        Assertions.assertEquals(63, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void interpolate()
    {
        Colorc color       = Color.LIGHT_GREEN;
        Colorc interpolate = Color.DARK_RED;
        Color  output      = new Color();
        Color  result;
        
        result = color.interpolate(interpolate, 0.25, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(95, result.r());
        Assertions.assertEquals(191, result.g());
        Assertions.assertEquals(47, result.b());
        Assertions.assertEquals(255, result.a());
        
        result = color.interpolate(interpolate, -0.5, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(color, result);
        
        result = color.interpolate(interpolate, 1.5, output);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(interpolate, result);
        
        result = output.set(color).interpolate(interpolate, 0.25);
        Assertions.assertSame(output, result);
        Assertions.assertEquals(95, result.r());
        Assertions.assertEquals(191, result.g());
        Assertions.assertEquals(47, result.b());
        Assertions.assertEquals(255, result.a());
    }
    
    @Test
    void toInt()
    {
        int    colorInt = 0x7F042069;
        Colorc color    = new Color().setFromInt(0x7F042069);
        
        int result = color.toInt();
        Assertions.assertEquals(colorInt, result);
    }
    
    @Test
    void toHSV()
    {
        Colorc   color  = Color.DARK_GREEN;
        Vector3d vector = new Vector3d();
        Vector3d result;
        
        result = color.toHSV(vector);
        Assertions.assertSame(vector, result);
        Assertions.assertEquals(120.0, result.x());
        Assertions.assertEquals(1.0, result.y());
        Assertions.assertEquals(0.7490196228027344, result.z());
        
        result = color.toHSV();
        Assertions.assertEquals(120.0, result.x());
        Assertions.assertEquals(1.0, result.y());
        Assertions.assertEquals(0.7490196228027344, result.z());
    }
}
