package engine.noise;

import org.jetbrains.annotations.NotNull;

import java.util.random.RandomGenerator;

public class WhiteNoise implements Noise4D
{
    public @NotNull RandomGenerator random = RandomGenerator.getDefault();
    
    @Override
    public double get(double x, double y, double z, double w)
    {
        return this.random.nextDouble(-1.0, 1.0);
    }
}
