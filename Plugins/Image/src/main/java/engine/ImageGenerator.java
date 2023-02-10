package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.color.Colorc;
import engine.noise.Noise2D;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ImageGenerator
{
    void generate(@NotNull ColorBuffer data, int width, int height);
    
    default void generate(@NotNull Image image)
    {
        generate(image.data, image.width, image.height);
    }
    
    default @NotNull Image generate(@NotNull ColorFormat format, int width, int height)
    {
        Image image = new Image(format, width, height);
        generate(image);
        return image;
    }
    
    static @NotNull ImageGenerator colorSolid(@NotNull Colorc color)
    {
        return (data, width, height) -> {
            for (int i = 0, n = width * height; i < n; i++) data.put(i, color);
        };
    }
    
    static @NotNull ImageGenerator colorGradient(
            @NotNull Colorc topLeft, @NotNull Colorc topRight, @NotNull Colorc bottomRight, @NotNull Colorc bottomLeft, @NotNull Function<Double, Double> lerpFunc)
    {
        final Color color  = new Color();
        final Color colorL = new Color();
        final Color colorR = new Color();
        return (data, width, height) -> {
            double t;
            for (int j = 0; j < height; j++)
            {
                t = lerpFunc.apply((double) j / (height - 1));
                
                topLeft.interpolate(bottomLeft, t, colorL);
                topRight.interpolate(bottomRight, t, colorR);
                
                for (int i = 0; i < width; i++)
                {
                    t = lerpFunc.apply((double) i / (width - 1));
                    
                    colorL.interpolate(colorR, t, color);
                    
                    data.put(j * width + i, color);
                }
            }
        };
    }
    
    static @NotNull ImageGenerator colorGradient(@NotNull Colorc topLeft, @NotNull Colorc topRight, @NotNull Colorc bottomRight, @NotNull Colorc bottomLeft)
    {
        return colorGradient(topLeft, topRight, bottomRight, bottomLeft, i -> i);
    }
    
    static @NotNull ImageGenerator colorGradientVertical(@NotNull Colorc top, @NotNull Colorc bottom, @NotNull Function<Double, Double> lerpFunc)
    {
        final Color color = new Color();
        return (data, width, height) -> {
            double t;
            for (int j = 0; j < height; j++)
            {
                t = lerpFunc.apply((double) j / (height - 1));
                
                top.interpolate(bottom, t, color);
                
                for (int i = 0; i < width; i++) data.put(j * width + i, color);
            }
        };
    }
    
    static @NotNull ImageGenerator colorGradientVertical(@NotNull Colorc top, @NotNull Colorc bottom)
    {
        return colorGradientVertical(top, bottom, i -> i);
    }
    
    static @NotNull ImageGenerator colorGradientHorizontal(@NotNull Colorc left, @NotNull Colorc right, @NotNull Function<Double, Double> lerpFunc)
    {
        final Color color = new Color();
        return (data, width, height) -> {
            double t;
            for (int i = 0; i < width; i++)
            {
                t = lerpFunc.apply((double) i / (width - 1));
                
                left.interpolate(right, t, color);
                
                for (int j = 0; j < height; j++) data.put(j * width + i, color);
            }
        };
    }
    
    static @NotNull ImageGenerator colorGradientHorizontal(@NotNull Colorc left, @NotNull Colorc right)
    {
        return colorGradientHorizontal(left, right, i -> i);
    }
    
    static @NotNull ImageGenerator colorGradientDiagonalTLBR(@NotNull Colorc topLeft, @NotNull Colorc bottomRight, @NotNull Function<Double, Double> lerpFunc)
    {
        Color mid = bottomRight.interpolate(topLeft, 0.5, new Color());
        return colorGradient(topLeft, mid, bottomRight, mid, lerpFunc);
    }
    
    static @NotNull ImageGenerator colorGradientDiagonalTLBR(@NotNull Colorc topLeft, @NotNull Colorc bottomRight)
    {
        return colorGradientDiagonalTLBR(topLeft, bottomRight, i -> i);
    }
    
    static @NotNull ImageGenerator colorGradientDiagonalTRBL(@NotNull Colorc topRight, @NotNull Colorc bottomLeft, @NotNull Function<Double, Double> lerpFunc)
    {
        Color mid = topRight.interpolate(bottomLeft, 0.5, new Color());
        return colorGradient(mid, topRight, mid, bottomLeft, lerpFunc);
    }
    
    static @NotNull ImageGenerator colorGradientDiagonalTRBL(@NotNull Colorc topRight, @NotNull Colorc bottomLeft)
    {
        return colorGradientDiagonalTRBL(topRight, bottomLeft, i -> i);
    }
    
    static @NotNull ImageGenerator colorGradientRadial(@NotNull Colorc inner, @NotNull Colorc outer, @NotNull Function<Double, Double> lerpFunc)
    {
        final Color color = new Color();
        return (data, width, height) -> {
            double radius  = Math.min(width, height) * 0.5;  // TODO - Specify Radius multiplier to fill corners
            double centerX = width * 0.5;
            double centerY = height * 0.5;
            
            double dist, t;
            for (int j = 0; j < height; j++)
            {
                for (int i = 0; i < width; i++)
                {
                    dist = Math.hypot(i - centerX, j - centerY) / radius;
                    t    = lerpFunc.apply(dist < 0.0 ? 0.0 : Math.min(dist, 1.0));
                    
                    inner.interpolate(outer, t, color);
                    data.put(j * width + i, color);
                }
            }
        };
    }
    
    static @NotNull ImageGenerator colorGradientRadial(@NotNull Colorc inner, @NotNull Colorc outer)
    {
        return colorGradientRadial(inner, outer, i -> i);
    }
    
    static @NotNull ImageGenerator colorCheckered(int checksX, int checksY, @NotNull Colorc color1, @NotNull Colorc color2)
    {
        return (data, width, height) -> {
            for (int j = 0; j < height; j++)
            {
                for (int i = 0; i < width; i++)
                {
                    data.put(j * width + i, (i / checksX + j / checksY) % 2 == 0 ? color1 : color2);
                }
            }
        };
    }
    
    static @NotNull ImageGenerator noise(@NotNull Noise2D noiseFunc, int offsetX, int offsetY, double scaleX, double scaleY)
    {
        final Color color = new Color();
        return (data, width, height) -> {
            for (int j = 0; j < height; j++)
            {
                for (int i = 0; i < width; i++)
                {
                    double nx = (i + offsetX) * scaleX / width;
                    double ny = (j + offsetY) * scaleY / height;
                    
                    double noiseValue = 0.5 * noiseFunc.get(nx, ny) + 0.5;
                    
                    color.set(noiseValue);
                    
                    data.put(j * width + i, color);
                }
            }
        };
    }
}
