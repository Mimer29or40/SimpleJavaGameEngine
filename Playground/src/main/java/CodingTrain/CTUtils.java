package CodingTrain;

public class CTUtils
{
    public static float map(float value, float x0, float x1, float y0, float y1)
    {
        return (value - x0) * (y1 - y0) / (x1 - x0) + y0;
    }
    
    public static double map(double value, double x0, double x1, double y0, double y1)
    {
        return (value - x0) * (y1 - y0) / (x1 - x0) + y0;
    }
}
