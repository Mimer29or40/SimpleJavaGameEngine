package engine.noise;

public interface Noise2D
{
    /**
     * Computes the two-dimensional noise at the given coordinate.
     *
     * @param x The x coordinate
     * @param y The x coordinate
     *
     * @return The noise value in the range [-1..1).
     */
    double get(double x, double y);
}
