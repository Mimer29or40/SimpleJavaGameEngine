package engine.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class MathUtil
{
    public static final double E = java.lang.Math.E;
    
    public static final double PI   = java.lang.Math.PI;
    public static final double PI2  = 2.0 * PI;
    public static final double PI_2 = PI / 2;
    public static final double PI_3 = PI / 3;
    public static final double PI_4 = PI / 4;
    public static final double PI_6 = PI / 6;
    
    public static final float PIf   = (float) PI;
    public static final float PI2f  = (float) PI2;
    public static final float PI_2f = (float) PI_2;
    public static final float PI_3f = (float) PI_3;
    public static final float PI_4f = (float) PI_4;
    public static final float PI_6f = (float) PI_6;
    
    /**
     * @see java.lang.Math#sin(double)
     */
    public static float sin(float a)
    {
        return (float) java.lang.Math.sin(a);
    }
    
    /**
     * @see java.lang.Math#sin(double)
     */
    public static double sin(double a)
    {
        return java.lang.Math.sin(a);
    }
    
    /**
     * @see java.lang.Math#cos(double)
     */
    public static float cos(float a)
    {
        return (float) java.lang.Math.cos(a);
    }
    
    /**
     * @see java.lang.Math#cos(double)
     */
    public static double cos(double a)
    {
        return java.lang.Math.cos(a);
    }
    
    /**
     * @see java.lang.Math#tan(double)
     */
    public static float tan(float a)
    {
        return (float) java.lang.Math.tan(a);
    }
    
    /**
     * @see java.lang.Math#tan(double)
     */
    public static double tan(double a)
    {
        return java.lang.Math.tan(a);
    }
    
    /**
     * @see java.lang.Math#asin(double)
     */
    public static float asin(float a)
    {
        return (float) java.lang.Math.asin(a);
    }
    
    /**
     * @see java.lang.Math#asin(double)
     */
    public static double asin(double a)
    {
        return java.lang.Math.asin(a);
    }
    
    /**
     * @see java.lang.Math#acos(double)
     */
    public static float acos(float a)
    {
        return (float) java.lang.Math.acos(a);
    }
    
    /**
     * @see java.lang.Math#acos(double)
     */
    public static double acos(double a)
    {
        return java.lang.Math.acos(a);
    }
    
    /**
     * @see java.lang.Math#atan(double)
     */
    public static float atan(float a)
    {
        return (float) java.lang.Math.atan(a);
    }
    
    /**
     * @see java.lang.Math#atan(double)
     */
    public static double atan(double a)
    {
        return java.lang.Math.atan(a);
    }
    
    /**
     * @see java.lang.Math#sinh(double)
     */
    public static float sinh(float x)
    {
        return (float) java.lang.Math.sinh(x);
    }
    
    /**
     * @see java.lang.Math#sinh(double)
     */
    public static double sinh(double x)
    {
        return java.lang.Math.sinh(x);
    }
    
    /**
     * @see java.lang.Math#cosh(double)
     */
    public static float cosh(float x)
    {
        return (float) java.lang.Math.cosh(x);
    }
    
    /**
     * @see java.lang.Math#cosh(double)
     */
    public static double cosh(double x)
    {
        return java.lang.Math.cosh(x);
    }
    
    /**
     * @see java.lang.Math#tanh(double)
     */
    public static float tanh(float x)
    {
        return (float) java.lang.Math.tanh(x);
    }
    
    /**
     * @see java.lang.Math#tanh(double)
     */
    public static double tanh(double x)
    {
        return java.lang.Math.tanh(x);
    }
    
    /**
     * @see java.lang.Math#hypot(double, double)
     */
    public static float hypot(float x, float y)
    {
        return (float) java.lang.Math.hypot(x, y);
    }
    
    /**
     * @see java.lang.Math#hypot(double, double)
     */
    public static double hypot(double x, double y)
    {
        return java.lang.Math.hypot(x, y);
    }
    
    /**
     * @see java.lang.Math#toRadians(double)
     */
    public static float toRadians(float ang_deg)
    {
        return (float) java.lang.Math.toRadians(ang_deg);
    }
    
    /**
     * @see java.lang.Math#toRadians(double)
     */
    public static double toRadians(double ang_deg)
    {
        return java.lang.Math.toRadians(ang_deg);
    }
    
    /**
     * @see java.lang.Math#toDegrees(double)
     */
    public static float toDegrees(float ang_rad)
    {
        return (float) java.lang.Math.toDegrees(ang_rad);
    }
    
    /**
     * @see java.lang.Math#toDegrees(double)
     */
    public static double toDegrees(double ang_rad)
    {
        return java.lang.Math.toDegrees(ang_rad);
    }
    
    /**
     * @see java.lang.Math#exp(double)
     */
    public static float exp(float a)
    {
        return (float) java.lang.Math.exp(a);
    }
    
    /**
     * @see java.lang.Math#exp(double)
     */
    public static double exp(double a)
    {
        return java.lang.Math.exp(a);
    }
    
    /**
     * @see java.lang.Math#expm1(double)
     */
    public static float expm1(float x)
    {
        return (float) java.lang.Math.expm1(x);
    }
    
    /**
     * @see java.lang.Math#expm1(double)
     */
    public static double expm1(double x)
    {
        return java.lang.Math.expm1(x);
    }
    
    /**
     * @see java.lang.Math#log(double)
     */
    public static float log(float a)
    {
        return (float) java.lang.Math.log(a);
    }
    
    /**
     * @see java.lang.Math#log(double)
     */
    public static double log(double a)
    {
        return java.lang.Math.log(a);
    }
    
    /**
     * @see java.lang.Math#log10(double)
     */
    public static float log10(float a)
    {
        return (float) java.lang.Math.log10(a);
    }
    
    /**
     * @see java.lang.Math#log10(double)
     */
    public static double log10(double a)
    {
        return java.lang.Math.log10(a);
    }
    
    /**
     * @see java.lang.Math#log1p(double)
     */
    public static float log1p(float x)
    {
        return (float) java.lang.Math.log1p(x);
    }
    
    /**
     * @see java.lang.Math#log1p(double)
     */
    public static double log1p(double x)
    {
        return java.lang.Math.log1p(x);
    }
    
    /**
     * @see java.lang.Math#sqrt(double)
     */
    public static float sqrt(float a)
    {
        return (float) java.lang.Math.sqrt(a);
    }
    
    /**
     * @see java.lang.Math#sqrt(double)
     */
    public static double sqrt(double a)
    {
        return java.lang.Math.sqrt(a);
    }
    
    /**
     * @see java.lang.Math#cbrt(double)
     */
    public static float cbrt(float a)
    {
        return (float) java.lang.Math.cbrt(a);
    }
    
    /**
     * @see java.lang.Math#cbrt(double)
     */
    public static double cbrt(double a)
    {
        return java.lang.Math.cbrt(a);
    }
    
    /**
     * @see java.lang.Math#ceil(double)
     */
    public static float ceil(float a)
    {
        return (float) java.lang.Math.ceil(a);
    }
    
    /**
     * @see java.lang.Math#ceil(double)
     */
    public static double ceil(double a)
    {
        return java.lang.Math.ceil(a);
    }
    
    /**
     * Calculated the ceiling of a double. Much faster than {@code Math.ceil}
     *
     * @param x The value
     *
     * @return The ceiling value.
     */
    public static int fastCeil(float x)
    {
        int xi = (int) x;
        return x > xi ? xi + 1 : xi;
    }
    
    /**
     * Calculated the ceiling of a double. Much faster than {@code Math.ceil}
     *
     * @param x The value
     *
     * @return The ceiling value.
     */
    public static int fastCeil(double x)
    {
        int xi = (int) x;
        return x > xi ? xi + 1 : xi;
    }
    
    /**
     * @see java.lang.Math#floor(double)
     */
    public static float floor(float a)
    {
        return (float) java.lang.Math.floor(a);
    }
    
    /**
     * @see java.lang.Math#floor(double)
     */
    public static double floor(double a)
    {
        return java.lang.Math.floor(a);
    }
    
    /**
     * Calculated the floor of a double. Much faster than {@code Math.floor}
     *
     * @param x The value
     *
     * @return The floored value.
     */
    public static int fastFloor(float x)
    {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
    
    /**
     * Calculated the floor of a double. Much faster than {@code Math.floor}
     *
     * @param x The value
     *
     * @return The floored value.
     */
    public static int fastFloor(double x)
    {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
    
    /**
     * @see java.lang.Math#rint(double)
     */
    public static float rint(float a)
    {
        return (float) java.lang.Math.rint(a);
    }
    
    /**
     * @see java.lang.Math#rint(double)
     */
    public static double rint(double a)
    {
        return java.lang.Math.rint(a);
    }
    
    /**
     * @see java.lang.Math#atan2(double, double)
     */
    public static float atan2(float y, float x)
    {
        return (float) java.lang.Math.atan2(y, x);
    }
    
    /**
     * @see java.lang.Math#atan2(double, double)
     */
    public static double atan2(double y, double x)
    {
        return java.lang.Math.atan2(y, x);
    }
    
    /**
     * @see java.lang.Math#pow(double, double)
     */
    public static float pow(float a, float b)
    {
        return (float) java.lang.Math.pow(a, b);
    }
    
    /**
     * @see java.lang.Math#pow(double, double)
     */
    public static double pow(double a, double b)
    {
        return java.lang.Math.pow(a, b);
    }
    
    /**
     * @see java.lang.Math#round(float)
     */
    public static int round(float a)
    {
        return java.lang.Math.round(a);
    }
    
    /**
     * Rounds a number to a specified number of decimal places.
     *
     * @param value  The number.
     * @param places The number of places.
     *
     * @return The rounded number.
     */
    public static float round(float value, int places)
    {
        if (places <= 0) return java.lang.Math.round(value);
        float pow = (float) java.lang.Math.pow(10, places);
        return java.lang.Math.round(value * pow) / pow;
    }
    
    /**
     * @see java.lang.Math#round(double)
     */
    public static long round(double a)
    {
        return java.lang.Math.round(a);
    }
    
    /**
     * Rounds a number to a specified number of decimal places.
     *
     * @param value  The number.
     * @param places The number of places.
     *
     * @return The rounded number.
     */
    public static double round(double value, int places)
    {
        if (places <= 0) return java.lang.Math.round(value);
        double pow = java.lang.Math.pow(10, places);
        return java.lang.Math.round(value * pow) / pow;
    }
    
    /**
     * @see java.lang.Math#random()
     */
    public static double random()
    {
        return java.lang.Math.random();
    }
    
    /**
     * Adds {@code x} to {@code y}
     */
    public static int add(int x, int y)
    {
        return x + y;
    }
    
    /**
     * Adds {@code x} to {@code y}
     */
    public static long add(long x, long y)
    {
        return x + y;
    }
    
    /**
     * Adds {@code x} to {@code y}
     */
    public static float add(float x, float y)
    {
        return x + y;
    }
    
    /**
     * Adds {@code x} to {@code y}
     */
    public static double add(double x, double y)
    {
        return x + y;
    }
    
    /**
     * Subtracts {@code x} from {@code y}
     */
    public static int sub(int x, int y)
    {
        return x - y;
    }
    
    /**
     * Subtracts {@code x} from {@code y}
     */
    public static long sub(long x, long y)
    {
        return x - y;
    }
    
    /**
     * Subtracts {@code x} from {@code y}
     */
    public static float sub(float x, float y)
    {
        return x - y;
    }
    
    /**
     * Subtracts {@code x} from {@code y}
     */
    public static double sub(double x, double y)
    {
        return x - y;
    }
    
    /**
     * Subtracts {@code y} from {@code x}
     */
    public static int revSub(int x, int y)
    {
        return y - x;
    }
    
    /**
     * Subtracts {@code y} from {@code x}
     */
    public static long revSub(long x, long y)
    {
        return y - x;
    }
    
    /**
     * Subtracts {@code y} from {@code x}
     */
    public static float revSub(float x, float y)
    {
        return y - x;
    }
    
    /**
     * Subtracts {@code y} from {@code x}
     */
    public static double revSub(double x, double y)
    {
        return y - x;
    }
    
    /**
     * Multiplies {@code x} to {@code y}
     */
    public static int mul(int x, int y)
    {
        return x * y;
    }
    
    /**
     * Multiplies {@code x} to {@code y}
     */
    public static long mul(long x, long y)
    {
        return x * y;
    }
    
    /**
     * Multiplies {@code x} to {@code y}
     */
    public static float mul(float x, float y)
    {
        return x * y;
    }
    
    /**
     * Multiplies {@code x} to {@code y}
     */
    public static double mul(double x, double y)
    {
        return x * y;
    }
    
    /**
     * Divides {@code x} from {@code y}
     */
    public static int div(int x, int y)
    {
        return x / y;
    }
    
    /**
     * Divides {@code x} from {@code y}
     */
    public static long div(long x, long y)
    {
        return x / y;
    }
    
    /**
     * Divides {@code x} from {@code y}
     */
    public static float div(float x, float y)
    {
        return x / y;
    }
    
    /**
     * Divides {@code x} from {@code y}
     */
    public static double div(double x, double y)
    {
        return x / y;
    }
    
    /**
     * Computes the summation of the given data set.
     *
     * @param array The data set.
     *
     * @return The sum of the data set.
     */
    public static int sum(int @NotNull ... array)
    {
        int sum = 0;
        for (int x : array) sum += x;
        return sum;
    }
    
    /**
     * Computes the summation of the given data set.
     *
     * @param array The data set.
     *
     * @return The sum of the data set.
     */
    @Contract(pure = true)
    public static long sum(long @NotNull ... array)
    {
        long sum = 0;
        for (long x : array) sum += x;
        return sum;
    }
    
    /**
     * Computes the summation of the given data set.
     *
     * @param array The data set.
     *
     * @return The sum of the data set.
     */
    public static float sum(float @NotNull ... array)
    {
        float sum = 0;
        for (float x : array) sum += x;
        return sum;
    }
    
    /**
     * Computes the summation of the given data set.
     *
     * @param array The data set.
     *
     * @return The sum of the data set.
     */
    public static double sum(double @NotNull ... array)
    {
        double sum = 0;
        for (double x : array) sum += x;
        return sum;
    }
    
    /**
     * Computes the product of the given data set.
     *
     * @param array The data set.
     *
     * @return The product of the data set.
     */
    public static int product(int @NotNull ... array)
    {
        int product = 1;
        for (int x : array) product *= x;
        return product;
    }
    
    /**
     * Computes the product of the given data set.
     *
     * @param array The data set.
     *
     * @return The product of the data set.
     */
    public static long product(long @NotNull ... array)
    {
        long product = 1;
        for (long x : array) product *= x;
        return product;
    }
    
    /**
     * Computes the product of the given data set.
     *
     * @param array The data set.
     *
     * @return The product of the data set.
     */
    public static float product(float @NotNull ... array)
    {
        float product = 1;
        for (float x : array) product *= x;
        return product;
    }
    
    /**
     * Computes the product of the given data set.
     *
     * @param array The data set.
     *
     * @return The product of the data set.
     */
    public static double product(double @NotNull ... array)
    {
        double product = 1;
        for (double x : array) product *= x;
        return product;
    }
    
    /**
     * @see java.lang.Math#abs(int)
     */
    public static int abs(int a)
    {
        return java.lang.Math.abs(a);
    }
    
    /**
     * @see java.lang.Math#abs(long)
     */
    public static long abs(long a)
    {
        return java.lang.Math.abs(a);
    }
    
    /**
     * @see java.lang.Math#abs(float)
     */
    public static float abs(float a)
    {
        return java.lang.Math.abs(a);
    }
    
    /**
     * @see java.lang.Math#abs(double)
     */
    public static double abs(double a)
    {
        return java.lang.Math.abs(a);
    }
    
    /**
     * @see java.lang.Math#min(int, int)
     */
    public static int min(int a, int b)
    {
        return java.lang.Math.min(a, b);
    }
    
    /**
     * Computes the minimum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The minimum value of the data set.
     */
    public static int min(int @NotNull ... array)
    {
        int min = Integer.MAX_VALUE;
        for (int x : array) min = java.lang.Math.min(min, x);
        return min;
    }
    
    /**
     * @see java.lang.Math#min(long, long)
     */
    public static long min(long a, long b)
    {
        return java.lang.Math.min(a, b);
    }
    
    /**
     * Computes the minimum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The minimum value of the data set.
     */
    public static long min(long @NotNull ... array)
    {
        long min = Long.MAX_VALUE;
        for (long x : array) min = java.lang.Math.min(min, x);
        return min;
    }
    
    /**
     * @see java.lang.Math#min(float, float)
     */
    public static float min(float a, float b)
    {
        return java.lang.Math.min(a, b);
    }
    
    /**
     * Computes the minimum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The minimum value of the data set.
     */
    public static float min(float @NotNull ... array)
    {
        float min = Float.MAX_VALUE;
        for (float x : array) min = java.lang.Math.min(min, x);
        return min;
    }
    
    /**
     * @see java.lang.Math#min(double, double)
     */
    public static double min(double a, double b)
    {
        return java.lang.Math.min(a, b);
    }
    
    /**
     * Computes the minimum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The minimum value of the data set.
     */
    public static double min(double @NotNull ... array)
    {
        double min = Double.MAX_VALUE;
        for (double x : array) min = java.lang.Math.min(min, x);
        return min;
    }
    
    /**
     * @see java.lang.Math#max(int, int)
     */
    public static int max(int a, int b)
    {
        return java.lang.Math.max(a, b);
    }
    
    /**
     * Computes the maximum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The maximum value of the data set.
     */
    public static int max(int @NotNull ... array)
    {
        int max = Integer.MIN_VALUE;
        for (int x : array) max = java.lang.Math.max(max, x);
        return max;
    }
    
    /**
     * @see java.lang.Math#max(long, long)
     */
    public static long max(long a, long b)
    {
        return java.lang.Math.max(a, b);
    }
    
    /**
     * Computes the maximum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The maximum value of the data set.
     */
    public static long max(long @NotNull ... array)
    {
        long max = Long.MIN_VALUE;
        for (long x : array) max = java.lang.Math.max(max, x);
        return max;
    }
    
    /**
     * @see java.lang.Math#max(float, float)
     */
    public static float max(float a, float b)
    {
        return java.lang.Math.max(a, b);
    }
    
    /**
     * Computes the maximum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The maximum value of the data set.
     */
    public static float max(float @NotNull ... array)
    {
        float max = Float.MIN_VALUE;
        for (float x : array) max = java.lang.Math.max(max, x);
        return max;
    }
    
    /**
     * @see java.lang.Math#max(double, double)
     */
    public static double max(double a, double b)
    {
        return java.lang.Math.max(a, b);
    }
    
    /**
     * Computes the maximum value of the given data set.
     *
     * @param array The data set.
     *
     * @return The maximum value of the data set.
     */
    public static double max(double @NotNull ... array)
    {
        double max = Double.MIN_VALUE;
        for (double x : array) max = java.lang.Math.max(max, x);
        return max;
    }
    
    /**
     * Clamps a value between two bounds.
     *
     * @param x   The value to clamp.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static int clamp(int x, int min, int max)
    {
        return x <= min ? min : java.lang.Math.min(x, max);
    }
    
    /**
     * Clamps a value between zero and an upper bound.
     *
     * @param x   The value to clamp.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static int clamp(int x, int max)
    {
        return clamp(x, 0, max);
    }
    
    /**
     * Clamps a value between two bounds.
     *
     * @param x   The value to clamp.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static long clamp(long x, long min, long max)
    {
        return x <= min ? min : java.lang.Math.min(x, max);
    }
    
    /**
     * Clamps a value between zero and an upper bound.
     *
     * @param x   The value to clamp.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static long clamp(long x, long max)
    {
        return clamp(x, 0, max);
    }
    
    /**
     * Clamps a value between two bounds.
     *
     * @param x   The value to clamp.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static float clamp(float x, float min, float max)
    {
        return x <= min ? min : java.lang.Math.min(x, max);
    }
    
    /**
     * Clamps a value between zero and an upper bound.
     *
     * @param x   The value to clamp.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static float clamp(float x, float max)
    {
        return clamp(x, 0, max);
    }
    
    /**
     * Clamps a value between two bounds.
     *
     * @param x   The value to clamp.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static double clamp(double x, double min, double max)
    {
        return x <= min ? min : java.lang.Math.min(x, max);
    }
    
    /**
     * Clamps a value between zero and an upper bound.
     *
     * @param x   The value to clamp.
     * @param max The upper bound.
     *
     * @return The clamped value.
     */
    public static double clamp(double x, double max)
    {
        return clamp(x, 0, max);
    }
    
    /**
     * Cycles a value between two bounds. If {@code x} is less than {@code min}
     * or greater than {@code max} then the value is wrapped around until the
     * values if between the bounds.
     *
     * @param x   The value to cycle.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static int cycle(int x, int min, int max)
    {
        if (min == max) return min;
        return x < min ? cycle(max - (x - min + 1), min, max) : x > max ? cycle(min + (x - max - 1), min, max) : x;
    }
    
    /**
     * Cycles a value. If {@code x} is less than {@code min} or greater than
     * {@code max} then the value is wrapped around until the values if between
     * the bounds.
     *
     * @param x   The value to cycle.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static int cycle(int x, int max)
    {
        return cycle(x, 0, max);
    }
    
    /**
     * Cycles a value between two bounds. If {@code x} is less than {@code min}
     * or greater than {@code max} then the value is wrapped around until the
     * values if between the bounds.
     *
     * @param x   The value to cycle.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static long cycle(long x, long min, long max)
    {
        if (min == max) return min;
        return x < min ? cycle(max - (x - min + 1), min, max) : x > max ? cycle(min + (x - max - 1), min, max) : x;
    }
    
    /**
     * Cycles a value. If {@code x} is less than {@code min} or greater than
     * {@code max} then the value is wrapped around until the values if between
     * the bounds.
     *
     * @param x   The value to cycle.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static long cycle(long x, long max)
    {
        return cycle(x, 0, max);
    }
    
    /**
     * Cycles a value between two bounds. If {@code x} is less than {@code min}
     * or greater than {@code max} then the value is wrapped around until the
     * values if between the bounds.
     *
     * @param x   The value to cycle.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static float cycle(float x, float min, float max)
    {
        if (Float.compare(min, max) == 0) return min;
        return x < min ? cycle(max - (x - min + 1), min, max) : x > max ? cycle(min + (x - max - 1), min, max) : x;
    }
    
    /**
     * Cycles a value. If {@code x} is less than {@code min} or greater than
     * {@code max} then the value is wrapped around until the values if between
     * the bounds.
     *
     * @param x   The value to cycle.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static float cycle(float x, float max)
    {
        return cycle(x, 0, max);
    }
    
    /**
     * Cycles a value between two bounds. If {@code x} is less than {@code min}
     * or greater than {@code max} then the value is wrapped around until the
     * values if between the bounds.
     *
     * @param x   The value to cycle.
     * @param min The lower bound.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static double cycle(double x, double min, double max)
    {
        if (Double.compare(min, max) == 0) return min;
        return x < min ? cycle(max - (x - min + 1), min, max) : x > max ? cycle(min + (x - max - 1), min, max) : x;
    }
    
    /**
     * Cycles a value. If {@code x} is less than {@code min} or greater than
     * {@code max} then the value is wrapped around until the values if between
     * the bounds.
     *
     * @param x   The value to cycle.
     * @param max The upper bound.
     *
     * @return The cycles value.
     */
    public static double cycle(double x, double max)
    {
        return cycle(x, 0, max);
    }
    
    /**
     * Returns the index of a list, wrapping if needed. Negative numbers are
     * indexed from the back of the list.
     *
     * @param i    The index.
     * @param size The size of the list.
     *
     * @return The true index.
     */
    public static int index(int i, int size)
    {
        return (i + size) % size;
    }
    
    /**
     * Returns the fused multiply add of the three arguments; that is, returns
     * the exact product of the first two arguments summed with the third
     * argument and then rounded once to the nearest {@code int}.
     *
     * @param a a value
     * @param b a value
     * @param c a value
     *
     * @return {@code (a * b + c)} computed, as if with unlimited range and
     * precision, and rounded to the nearest {@code int} value
     */
    public static int fma(int a, int b, int c)
    {
        return (int) (((long) a * (long) b) + (long) c);
    }
    
    /**
     * Returns the fused multiply add of the three arguments; that is, returns
     * the exact product of the first two arguments summed with the third
     * argument and then rounded once to the nearest {@code long}.
     *
     * @param a a value
     * @param b a value
     * @param c a value
     *
     * @return {@code (a * b + c)} computed, as if with unlimited range and
     * precision, and rounded to the nearest {@code long} value
     */
    public static long fma(long a, long b, long c)
    {
        BigInteger product = BigInteger.valueOf(a).multiply(BigInteger.valueOf(b));
        if (c == 0L)
        {
            if (a == 0L || b == 0L) return 0L;
            return product.longValue();
        }
        return product.add(BigInteger.valueOf(c)).longValue();
    }
    
    /**
     * @see java.lang.Math#fma(float, float, float)
     */
    public static float fma(float a, float b, float c)
    {
        return java.lang.Math.fma(a, b, c);
    }
    
    /**
     * @see java.lang.Math#fma(double, double, double)
     */
    public static double fma(double a, double b, double c)
    {
        return java.lang.Math.fma(a, b, c);
    }
    
    /**
     * @see java.lang.Integer#signum(int)
     */
    public static int signum(int i)
    {
        return Integer.signum(i);
    }
    
    /**
     * @see java.lang.Long#signum(long)
     */
    public static long signum(long i)
    {
        return Long.signum(i);
    }
    
    /**
     * @see java.lang.Math#signum(float)
     */
    public static float signum(float f)
    {
        return java.lang.Math.signum(f);
    }
    
    /**
     * @see java.lang.Math#signum(double)
     */
    public static double signum(double d)
    {
        return java.lang.Math.signum(d);
    }
    
    /**
     * Returns the first integer argument with the sign of the second integer
     * argument.
     *
     * @param magnitude the parameter providing the magnitude of the result
     * @param sign      the parameter providing the sign of the result
     *
     * @return a value with the magnitude of {@code magnitude} and the sign of
     * {@code sign}.
     */
    public static int copySign(int magnitude, int sign)
    {
        return (sign & Integer.MIN_VALUE) != (magnitude & Integer.MIN_VALUE) ? ~magnitude + 1 : magnitude;
    }
    
    /**
     * Returns the first argument with the sign of the second argument.
     *
     * @param magnitude the parameter providing the magnitude of the result
     * @param sign      the parameter providing the sign of the result
     *
     * @return a value with the magnitude of {@code magnitude} and the sign of
     * {@code sign}.
     */
    public static long copySign(long magnitude, long sign)
    {
        return (sign & Long.MIN_VALUE) != (magnitude & Long.MIN_VALUE) ? ~magnitude + 1 : magnitude;
    }
    
    /**
     * @see java.lang.Math#copySign(float, float)
     */
    public static float copySign(float magnitude, float sign)
    {
        return java.lang.Math.copySign(magnitude, sign);
    }
    
    /**
     * @see java.lang.Math#copySign(double, double)
     */
    public static double copySign(double magnitude, double sign)
    {
        return java.lang.Math.copySign(magnitude, sign);
    }
    
    /**
     * @see java.lang.Math#getExponent(float)
     */
    public static int getExponent(float f)
    {
        return java.lang.Math.getExponent(f);
    }
    
    /**
     * @see java.lang.Math#getExponent(double)
     */
    public static int getExponent(double d)
    {
        return java.lang.Math.getExponent(d);
    }
    
    public static double map(double x, double x0, double x1, double y0, double y1)
    {
        return (x - x0) * (y1 - y0) / (x1 - x0) + y0;
    }
    
    /**
     * Gets the decimal part of a number.
     *
     * @param value The number.
     *
     * @return The decimal part.
     */
    public static float getDecimal(float value)
    {
        return value > 0 ? value - floor(value) : (value - ceil(value)) * -1;
    }
    
    /**
     * Gets the decimal part of a number.
     *
     * @param value The number.
     *
     * @return The decimal part.
     */
    public static double getDecimal(double value)
    {
        return value > 0 ? value - floor(value) : (value - ceil(value)) * -1;
    }
    
    /**
     * @return {@code true} if {@code value} is even
     */
    public static boolean isEven(long value)
    {
        return (value & 1) == 0;
    }
    
    /**
     * @return {@code true} if {@code value} is odd
     */
    public static boolean isOdd(long value)
    {
        return (value & 1) == 1;
    }
    
    /**
     * @return {@code true} if {@code value} is even
     */
    public static boolean isEven(double value)
    {
        return isEven((long) java.lang.Math.floor(value));
    }
    
    /**
     * @return {@code true} if {@code value} is odd
     */
    public static boolean isOdd(double value)
    {
        return isOdd((long) java.lang.Math.floor(value));
    }
    
    /**
     * Computes the mean value of the given data set.
     *
     * @param array The data set.
     *
     * @return The mean value of the data set.
     */
    public static float mean(int @NotNull ... array)
    {
        float mean = 0;
        for (int x : array) mean += x;
        return mean / array.length;
    }
    
    /**
     * Computes the mean value of the given data set.
     *
     * @param array The data set.
     *
     * @return The mean value of the data set.
     */
    public static double mean(long @NotNull ... array)
    {
        double mean = 0;
        for (long x : array) mean += x;
        return mean / array.length;
    }
    
    /**
     * Computes the mean value of the given data set.
     *
     * @param array The data set.
     *
     * @return The mean value of the data set.
     */
    public static float mean(float @NotNull ... array)
    {
        float mean = 0;
        for (float x : array) mean += x;
        return mean / array.length;
    }
    
    /**
     * Computes the mean value of the given data set.
     *
     * @param array The data set.
     *
     * @return The mean value of the data set.
     */
    public static double mean(double @NotNull ... array)
    {
        double mean = 0;
        for (double x : array) mean += x;
        return mean / array.length;
    }
    
    /**
     * Computes the standard deviation of the given data set.
     *
     * @param array The data set.
     *
     * @return The standard deviation of the data set.
     */
    public static float stdDev(int @NotNull ... array)
    {
        float mean   = mean(array);
        float stdDev = 0;
        for (int x : array) stdDev += (x - mean) * (x - mean);
        return (float) java.lang.Math.sqrt(stdDev / array.length);
    }
    
    /**
     * Computes the standard deviation of the given data set.
     *
     * @param array The data set.
     *
     * @return The standard deviation of the data set.
     */
    public static double stdDev(long @NotNull ... array)
    {
        double mean   = mean(array);
        double stdDev = 0;
        for (long x : array) stdDev += (x - mean) * (x - mean);
        return java.lang.Math.sqrt(stdDev / array.length);
    }
    
    /**
     * Computes the standard deviation of the given data set.
     *
     * @param array The data set.
     *
     * @return The standard deviation of the data set.
     */
    public static float stdDev(float @NotNull ... array)
    {
        float mean   = mean(array);
        float stdDev = 0;
        for (float x : array) stdDev += (x - mean) * (x - mean);
        return (float) java.lang.Math.sqrt(stdDev / array.length);
    }
    
    /**
     * Computes the standard deviation of the given data set.
     *
     * @param array The data set.
     *
     * @return The standard deviation of the data set.
     */
    public static double stdDev(double @NotNull ... array)
    {
        double mean   = mean(array);
        double stdDev = 0;
        for (double x : array) stdDev += (x - mean) * (x - mean);
        return java.lang.Math.sqrt(stdDev / array.length);
    }
    
    /**
     * Interpolates a value between two endpoints. Results are clamped between endpoints.
     *
     * @param a The first value.
     * @param b The second value.
     * @param x The amount to interpolate. [0-1]
     *
     * @return The interpolated value.
     */
    public static int lerp(int a, int b, float x)
    {
        return x <= 0 ? a : x >= 1 ? b : (int) fma((b - a), x, a);
    }
    
    /**
     * Interpolates a value between two endpoints. Results are clamped between endpoints.
     *
     * @param a The first value.
     * @param b The second value.
     * @param x The amount to interpolate. [0-1]
     *
     * @return The interpolated value.
     */
    public static long lerp(long a, long b, double x)
    {
        return x <= 0 ? a : x >= 1 ? b : (long) fma((b - a), x, a);
    }
    
    /**
     * Interpolates a value between two endpoints. Results are clamped between endpoints.
     *
     * @param a The first value.
     * @param b The second value.
     * @param x The amount to interpolate. [0-1]
     *
     * @return The interpolated value.
     */
    public static float lerp(float a, float b, float x)
    {
        return x <= 0 ? a : x >= 1 ? b : fma((b - a), x, a);
    }
    
    /**
     * Interpolates a value between two endpoints. Results are clamped between endpoints.
     *
     * @param a The first value.
     * @param b The second value.
     * @param x The amount to interpolate. [0-1]
     *
     * @return The interpolated value.
     */
    public static double lerp(double a, double b, double x)
    {
        return x <= 0 ? a : x >= 1 ? b : fma((b - a), x, a);
    }
    
    public static double smoothstep(double x)
    {
        return x * x * (3 - 2 * x);
    }
    
    public static double smootherstep(double x)
    {
        return x * x * x * (x * (x * 6 - 15) + 10);
    }
    
    /**
     * Determines if two value are equal up two a {@code delta}
     */
    public static boolean equals(float a, float b, double delta)
    {
        return Float.floatToIntBits(a) == Float.floatToIntBits(b) || Math.abs(a - b) < delta;
    }
    
    /**
     * Determines if two value are equal up two a {@code delta}
     */
    public static boolean equals(double a, double b, double delta)
    {
        return Double.doubleToLongBits(a) == Double.doubleToLongBits(b) || Math.abs(a - b) < delta;
    }
    
    private MathUtil() {}
}
