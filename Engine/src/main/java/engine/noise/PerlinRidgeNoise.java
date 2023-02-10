package engine.noise;

import static org.lwjgl.stb.STBPerlin.stb_perlin_ridge_noise3;

public class PerlinRidgeNoise implements Noise3D
{
    public double lacunarity = 2.0;
    public double gain       = 0.5;
    public double offset     = 1.0;
    public int    octaves    = 6;
    
    @Override
    public double get(double x, double y, double z)
    {
        return stb_perlin_ridge_noise3((float) x, (float) y, (float) z, (float) this.lacunarity, (float) this.gain, (float) this.offset, this.octaves);
    }
}
