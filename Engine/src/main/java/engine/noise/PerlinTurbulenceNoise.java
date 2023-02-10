package engine.noise;

import static org.lwjgl.stb.STBPerlin.stb_perlin_turbulence_noise3;

public class PerlinTurbulenceNoise implements Noise3D
{
    public double lacunarity = 2.0;
    public double gain       = 0.5;
    public int    octaves    = 6;
    
    @Override
    public double get(double x, double y, double z)
    {
        return stb_perlin_turbulence_noise3((float) x, (float) y, (float) z, (float) this.lacunarity, (float) this.gain, this.octaves);
    }
}
