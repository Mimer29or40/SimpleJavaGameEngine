package engine.noise;

public interface Noise3D extends Noise2D
{
    /**
     * Computes the three-dimensional noise at the given coordinate.
     *
     * @param x The x coordinate
     * @param y The x coordinate
     * @param z The z coordinate
     *
     * @return The noise value in the range [-1..1).
     */
    double get(double x, double y, double z);
    
    @Override
    default double get(double x, double y)
    {
        return get(x, y, 1.0);
    }
}
