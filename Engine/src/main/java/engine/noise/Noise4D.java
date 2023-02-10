package engine.noise;

public interface Noise4D extends Noise3D
{
    /**
     * Computes the four-dimensional noise at the given coordinate.
     *
     * @param x The x coordinate
     * @param y The x coordinate
     * @param z The z coordinate
     * @param w The w coordinate
     *
     * @return The noise value in the range [-1..1).
     */
    double get(double x, double y, double z, double w);
    
    @Override
    default double get(double x, double y, double z)
    {
        return get(x, y, z, 1.0);
    }
}
