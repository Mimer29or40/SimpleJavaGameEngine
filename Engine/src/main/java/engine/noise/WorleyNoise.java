package engine.noise;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.stb.STBPerlin.stb_perlin_fbm_noise3;

public class WorleyNoise implements Noise3D
{
    public double lacunarity = 2.0;
    public double gain       = 0.5;
    public int    octaves    = 1;
    
    private final Map<Integer, double[]> cache = new HashMap<>();
    
    private double @NotNull [] getPoint(int x, int y, int z)
    {
        int hash = Objects.hash(x, y, z);
        
        return this.cache.computeIfAbsent(hash, h -> new double[] {
                (stb_perlin_fbm_noise3(x * 11F, y * 13F, z * 17F, (float) Math.PI, 0.333F, 2) * 2.0 + 1.0) + x,
                (stb_perlin_fbm_noise3(x * 17F, y * 11F, z * 13F, (float) Math.PI, 0.333F, 2) * 2.0 + 1.0) + y,
                (stb_perlin_fbm_noise3(x * 13F, y * 17F, z * 11F, (float) Math.PI, 0.333F, 2) * 2.0 + 1.0) + z
        });
    }
    
    @Override
    public double get(double x, double y)
    {
        double value     = 0.0;
        double amplitude = 1.0;
        for (int i = 0; i < this.octaves; i++)
        {
            value += amplitude * getImpl(x, y);
            
            x *= this.lacunarity;
            y *= this.lacunarity;
            amplitude *= this.gain;
        }
        return value < -1.0 ? -1.0 : Math.min(value, 1.0);
    }
    
    @Override
    public double get(double x, double y, double z)
    {
        double value     = 0.0;
        double amplitude = 1.0;
        for (int i = 0; i < this.octaves; i++)
        {
            value += amplitude * getImpl(x, y, z);
            
            x *= this.lacunarity;
            y *= this.lacunarity;
            amplitude *= this.gain;
        }
        return value < -1.0 ? -1.0 : Math.min(value, 1.0);
    }
    
    public double getImpl(double x, double y)
    {
        int intX = (int) Math.floor(x);
        int intY = (int) Math.floor(y);
        
        double[] point;
        double   distX, distY;
        
        double dist, minDistance = Double.MAX_VALUE;
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                point = getPoint(intX + i, intY + j, 1);
                
                distX = point[0] - x;
                distY = point[1] - y;
                
                dist = Math.sqrt(distX * distX + distY * distY);
                
                minDistance = Math.min(minDistance, dist);
            }
        }
        return minDistance;
    }
    
    public double getImpl(double x, double y, double z)
    {
        int intX = (int) Math.floor(x);
        int intY = (int) Math.floor(y);
        int intZ = (int) Math.floor(z);
        
        double[] point;
        double   distX, distY, distZ;
        
        double dist, minDistance = Double.MAX_VALUE;
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                for (int k = -1; k <= 1; k++)
                {
                    point = getPoint(intX + i, intY + j, intZ + k);
                    
                    distX = point[0] - x;
                    distY = point[1] - y;
                    distZ = point[2] - z;
                    
                    dist = Math.sqrt(distX * distX + distY * distY + distZ * distZ);
                    
                    minDistance = Math.min(minDistance, dist);
                }
            }
        }
        return minDistance;
    }
}
