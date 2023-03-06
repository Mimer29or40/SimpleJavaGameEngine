package engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MathUtilTest
{
    @Test
    void constants()
    {
        Assertions.assertEquals(java.lang.Math.E, MathUtil.E);
        
        Assertions.assertEquals(java.lang.Math.PI, MathUtil.PI);
        Assertions.assertEquals(2.0 * java.lang.Math.PI, MathUtil.PI2);
        Assertions.assertEquals(java.lang.Math.PI / 2.0, MathUtil.PI_2);
        Assertions.assertEquals(java.lang.Math.PI / 3.0, MathUtil.PI_3);
        Assertions.assertEquals(java.lang.Math.PI / 4.0, MathUtil.PI_4);
        Assertions.assertEquals(java.lang.Math.PI / 6.0, MathUtil.PI_6);
        
        Assertions.assertEquals((float) (java.lang.Math.PI), MathUtil.PIf);
        Assertions.assertEquals((float) (2.0 * java.lang.Math.PI), MathUtil.PI2f);
        Assertions.assertEquals((float) (java.lang.Math.PI / 2.0), MathUtil.PI_2f);
        Assertions.assertEquals((float) (java.lang.Math.PI / 3.0), MathUtil.PI_3f);
        Assertions.assertEquals((float) (java.lang.Math.PI / 4.0), MathUtil.PI_4f);
        Assertions.assertEquals((float) (java.lang.Math.PI / 6.0), MathUtil.PI_6f);
    }
    
    int iterations = 360;
    
    @Test
    void sin()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.sin((float) angle), MathUtil.sin((float) angle));
            Assertions.assertEquals(java.lang.Math.sin(angle), MathUtil.sin(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.sin(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.sin(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.sin(Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.sin(Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.sin(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0, MathUtil.sin(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.sin(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.sin(Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.sin(Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.sin(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void cos()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.sin((float) angle), MathUtil.sin((float) angle));
            Assertions.assertEquals(java.lang.Math.sin(angle), MathUtil.sin(angle));
        }
        
        Assertions.assertEquals(1.0F, MathUtil.cos(+0.0F));
        Assertions.assertEquals(1.0F, MathUtil.cos(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.cos(Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.cos(Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.cos(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(1.0, MathUtil.cos(+0.0));
        Assertions.assertEquals(1.0, MathUtil.cos(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.cos(Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.cos(Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.cos(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void tan()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.tan((float) angle), MathUtil.tan((float) angle));
            Assertions.assertEquals(java.lang.Math.tan(angle), MathUtil.tan(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.tan(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.tan(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.tan(Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.tan(Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.tan(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0, MathUtil.tan(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.tan(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.tan(Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.tan(Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.tan(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void asin()
    {
        double start = -1.0;
        double stop  = 1.0;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.asin((float) angle), MathUtil.asin((float) angle));
            Assertions.assertEquals(java.lang.Math.asin(angle), MathUtil.asin(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.asin(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.asin(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.asin(Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.asin(Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.asin(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0, MathUtil.asin(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.asin(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.asin(Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.asin(Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.asin(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void acos()
    {
        double start = -1.0;
        double stop  = 1.0;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.acos((float) angle), MathUtil.acos((float) angle));
            Assertions.assertEquals(java.lang.Math.acos(angle), MathUtil.acos(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.acos(+1.0F));
        Assertions.assertEquals(MathUtil.PIf, MathUtil.acos(-1.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.acos(Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.acos(Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.acos(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0, MathUtil.acos(+1.0));
        Assertions.assertEquals(MathUtil.PI, MathUtil.acos(-1.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.acos(Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.acos(Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.acos(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void atan()
    {
        double start = -MathUtil.PI_2;
        double stop  = MathUtil.PI_2;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.atan((float) angle), MathUtil.atan((float) angle));
            Assertions.assertEquals(java.lang.Math.atan(angle), MathUtil.atan(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.atan(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.atan(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.atan(Float.NaN)));
        Assertions.assertEquals(+MathUtil.PI_2f, MathUtil.atan(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_2f, MathUtil.atan(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.atan(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.atan(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.atan(Double.NaN)));
        Assertions.assertEquals(+MathUtil.PI_2, MathUtil.atan(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_2, MathUtil.atan(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void sinh()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.sinh((float) angle), MathUtil.sinh((float) angle));
            Assertions.assertEquals(java.lang.Math.sinh(angle), MathUtil.sinh(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.sinh(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.sinh(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.sinh(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.sinh(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.sinh(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.sinh(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.sinh(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.sinh(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.sinh(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.sinh(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void cosh()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.cosh((float) angle), MathUtil.cosh((float) angle));
            Assertions.assertEquals(java.lang.Math.cosh(angle), MathUtil.cosh(angle));
        }
        
        Assertions.assertEquals(+1.0F, MathUtil.cosh(+0.0F));
        Assertions.assertEquals(+1.0F, MathUtil.cosh(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.sinh(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.cosh(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.cosh(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+1.0, MathUtil.cosh(+0.0));
        Assertions.assertEquals(+1.0, MathUtil.cosh(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.cosh(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.cosh(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.cosh(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void tanh()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.tanh((float) angle), MathUtil.tanh((float) angle));
            Assertions.assertEquals(java.lang.Math.tanh(angle), MathUtil.tanh(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.tanh(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.tanh(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.tanh(Float.NaN)));
        Assertions.assertEquals(+1.0F, MathUtil.tanh(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(-1.0F, MathUtil.tanh(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.tanh(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.tanh(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.tanh(Double.NaN)));
        Assertions.assertEquals(+1.0, MathUtil.tanh(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(-1.0, MathUtil.tanh(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void hypot()
    {
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(0.0F, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(0.0F, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.POSITIVE_INFINITY, 0.0F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.NEGATIVE_INFINITY, 0.0F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.NaN, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.NaN, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.POSITIVE_INFINITY, Float.NaN));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.hypot(Float.NEGATIVE_INFINITY, Float.NaN));
        Assertions.assertTrue(Float.isNaN(MathUtil.hypot(0.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.hypot(0.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.hypot(Float.NaN, 0.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.hypot(Float.NaN, 0.0F)));
        Assertions.assertEquals(+0.0F, MathUtil.hypot(-0.0F, -0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.hypot(-0.0F, +0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.hypot(+0.0F, -0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.hypot(+0.0F, +0.0F));
        
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(0.0, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(0.0, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.POSITIVE_INFINITY, 0.0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.NEGATIVE_INFINITY, 0.0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.NaN, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.NaN, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.POSITIVE_INFINITY, Double.NaN));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.hypot(Double.NEGATIVE_INFINITY, Double.NaN));
        Assertions.assertTrue(Double.isNaN(MathUtil.hypot(0.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.hypot(0.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.hypot(Double.NaN, 0.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.hypot(Double.NaN, 0.0)));
        Assertions.assertEquals(+0.0, MathUtil.hypot(-0.0, -0.0));
        Assertions.assertEquals(+0.0, MathUtil.hypot(-0.0, +0.0));
        Assertions.assertEquals(+0.0, MathUtil.hypot(+0.0, -0.0));
        Assertions.assertEquals(+0.0, MathUtil.hypot(+0.0, +0.0));
    }
    
    @Test
    void toRadians()
    {
        double start = 0.0;
        double stop  = 360.0;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.toRadians((float) angle), MathUtil.toRadians((float) angle));
            Assertions.assertEquals(java.lang.Math.toRadians(angle), MathUtil.toRadians(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.toRadians(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.toRadians(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.toRadians(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.toRadians(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.toRadians(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.toRadians(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.toRadians(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.toRadians(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.toRadians(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.toRadians(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void toDegrees()
    {
        double start = -MathUtil.PI;
        double stop  = MathUtil.PI;
        double angle;
        for (int i = 0; i < this.iterations; i++)
        {
            angle = start + (stop - start) * ((double) i / (this.iterations - 1));
            
            Assertions.assertEquals((float) java.lang.Math.toDegrees((float) angle), MathUtil.toDegrees((float) angle));
            Assertions.assertEquals(java.lang.Math.toDegrees(angle), MathUtil.toDegrees(angle));
        }
        
        Assertions.assertEquals(+0.0F, MathUtil.toDegrees(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.toDegrees(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.toDegrees(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.toDegrees(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.toDegrees(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.toDegrees(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.toDegrees(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.toDegrees(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.toDegrees(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.toDegrees(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void exp()
    {
        Assertions.assertEquals(+1.0F, MathUtil.exp(+0.0F));
        Assertions.assertEquals(+1.0F, MathUtil.exp(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.exp(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.exp(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(+0.0F, MathUtil.exp(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+1.0, MathUtil.exp(+0.0));
        Assertions.assertEquals(+1.0, MathUtil.exp(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.exp(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.exp(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(+0.0, MathUtil.exp(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void expm1()
    {
        Assertions.assertEquals(+0.0F, MathUtil.expm1(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.expm1(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.expm1(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.expm1(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(-1.0F, MathUtil.expm1(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.expm1(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.expm1(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.expm1(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.expm1(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(-1.0, MathUtil.expm1(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void log()
    {
        Assertions.assertTrue(Float.isNaN(MathUtil.log(-1.0F)));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.log(+0.0F));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.log(-0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.log(+1.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.log(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.log(Float.POSITIVE_INFINITY));
        Assertions.assertTrue(Float.isNaN(MathUtil.log(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.log(-1.0)));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.log(+0.0));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.log(-0.0));
        Assertions.assertEquals(+0.0, MathUtil.log(+1.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.log(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.log(Double.POSITIVE_INFINITY));
        Assertions.assertTrue(Double.isNaN(MathUtil.log(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void log10()
    {
        Assertions.assertTrue(Float.isNaN(MathUtil.log10(-1.0F)));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.log10(+0.0F));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.log10(-0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.log10(+1.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.log10(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.log10(Float.POSITIVE_INFINITY));
        Assertions.assertTrue(Float.isNaN(MathUtil.log10(Float.NEGATIVE_INFINITY)));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.log10(-1.0)));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.log10(+0.0));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.log10(-0.0));
        Assertions.assertEquals(+0.0, MathUtil.log10(+1.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.log10(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.log10(Double.POSITIVE_INFINITY));
        Assertions.assertTrue(Double.isNaN(MathUtil.log10(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    void log1p()
    {
        Assertions.assertEquals(+0.0F, MathUtil.log1p(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.log1p(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.log1p(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.log1p(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.log1p(-1.0F));
        
        Assertions.assertEquals(+0.0, MathUtil.log1p(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.log1p(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.log1p(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.log1p(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.log1p(-1.0));
    }
    
    @Test
    void sqrt()
    {
        Assertions.assertEquals(+0.0F, MathUtil.sqrt(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.sqrt(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.sqrt(-1.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.sqrt(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.sqrt(Float.POSITIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.sqrt(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.sqrt(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.sqrt(-1.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.sqrt(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.sqrt(Double.POSITIVE_INFINITY));
    }
    
    @Test
    void cbrt()
    {
        Assertions.assertEquals(+0.0F, MathUtil.cbrt(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.cbrt(-0.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.cbrt(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.cbrt(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.cbrt(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.cbrt(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.cbrt(-0.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.cbrt(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.cbrt(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.cbrt(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void ceil()
    {
        Assertions.assertEquals(+0.0F, MathUtil.ceil(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.ceil(-0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.ceil(-0.5F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.ceil(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.ceil(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.ceil(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.ceil(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.ceil(-0.0));
        Assertions.assertEquals(-0.0, MathUtil.ceil(-0.5));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.ceil(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.ceil(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.ceil(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void fastCeil()
    {
        Assertions.assertEquals(+0, MathUtil.fastCeil(+0.0F));
        Assertions.assertEquals(-0, MathUtil.fastCeil(-0.0F));
        Assertions.assertEquals(-0, MathUtil.fastCeil(-0.5F));
        
        Assertions.assertEquals(0, MathUtil.fastCeil(Float.NaN));
        
        Assertions.assertEquals(+0, MathUtil.fastCeil(+0.0));
        Assertions.assertEquals(-0, MathUtil.fastCeil(-0.0));
        Assertions.assertEquals(-0, MathUtil.fastCeil(-0.5));
        
        Assertions.assertEquals(0, MathUtil.fastCeil(Double.NaN));
    }
    
    @Test
    void floor()
    {
        Assertions.assertEquals(+0.0F, MathUtil.floor(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.floor(-0.0F));
        Assertions.assertEquals(-1.0F, MathUtil.floor(-0.5F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.floor(Float.NaN)));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.floor(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.floor(Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.floor(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.floor(-0.0));
        Assertions.assertEquals(-1.0, MathUtil.floor(-0.5));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.floor(Double.NaN)));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.floor(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.floor(Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void fastFloor()
    {
        Assertions.assertEquals(+0, MathUtil.fastFloor(+0.0F));
        Assertions.assertEquals(-0, MathUtil.fastFloor(-0.0F));
        Assertions.assertEquals(-1, MathUtil.fastFloor(-0.5F));
        
        Assertions.assertEquals(+0, MathUtil.fastFloor(Float.NaN));
        
        Assertions.assertEquals(+0, MathUtil.fastFloor(+0.0));
        Assertions.assertEquals(-0, MathUtil.fastFloor(-0.0));
        Assertions.assertEquals(-1, MathUtil.fastFloor(-0.5));
        
        Assertions.assertEquals(+0, MathUtil.fastFloor(Double.NaN));
    }
    
    @Test
    void rint()
    {
        Assertions.assertEquals(+0.0F, MathUtil.rint(+0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.rint(-0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.rint(-0.5F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.rint(Float.NaN)));
        
        Assertions.assertEquals(+0.0, MathUtil.rint(+0.0));
        Assertions.assertEquals(-0.0, MathUtil.rint(-0.0));
        Assertions.assertEquals(-0.0, MathUtil.rint(-0.5));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.rint(Double.NaN)));
    }
    
    @Test
    void atan2()
    {
        Assertions.assertTrue(Float.isNaN(MathUtil.atan2(Float.NaN, +0.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.atan2(+0.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.atan2(Float.NaN, Float.NaN)));
        
        Assertions.assertEquals(+0.0F, MathUtil.atan2(+0.0F, +1.0F));
        Assertions.assertEquals(+0.0F, MathUtil.atan2(+1.0F, Float.POSITIVE_INFINITY));
        
        Assertions.assertEquals(-0.0F, MathUtil.atan2(-0.0F, +1.0F));
        Assertions.assertEquals(-0.0F, MathUtil.atan2(-1.0F, Float.POSITIVE_INFINITY));
        
        Assertions.assertEquals(MathUtil.PIf, MathUtil.atan2(+0.0F, -1.0F));
        Assertions.assertEquals(MathUtil.PIf, MathUtil.atan2(+1.0F, Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(-MathUtil.PIf, MathUtil.atan2(-0.0F, -1.0F));
        Assertions.assertEquals(-MathUtil.PIf, MathUtil.atan2(-1.0F, Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(MathUtil.PI_2f, MathUtil.atan2(+1.0F, +0.0F));
        Assertions.assertEquals(MathUtil.PI_2f, MathUtil.atan2(+1.0F, -0.0F));
        Assertions.assertEquals(MathUtil.PI_2f, MathUtil.atan2(Float.POSITIVE_INFINITY, +1.0F));
        
        Assertions.assertEquals(-MathUtil.PI_2f, MathUtil.atan2(-1.0F, +0.0F));
        Assertions.assertEquals(-MathUtil.PI_2f, MathUtil.atan2(-1.0F, -0.0F));
        Assertions.assertEquals(-MathUtil.PI_2f, MathUtil.atan2(Float.NEGATIVE_INFINITY, +1.0F));
        
        Assertions.assertEquals(MathUtil.PI_4f, MathUtil.atan2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(MathUtil.PI_2f + MathUtil.PI_4f, MathUtil.atan2(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_4f, MathUtil.atan2(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_2f - MathUtil.PI_4f, MathUtil.atan2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.atan2(Double.NaN, +0.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.atan2(+0.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.atan2(Double.NaN, Double.NaN)));
        
        Assertions.assertEquals(+0.0, MathUtil.atan2(+0.0, +1.0));
        Assertions.assertEquals(+0.0, MathUtil.atan2(+1.0, Double.POSITIVE_INFINITY));
        
        Assertions.assertEquals(-0.0, MathUtil.atan2(-0.0, +1.0));
        Assertions.assertEquals(-0.0, MathUtil.atan2(-1.0, Double.POSITIVE_INFINITY));
        
        Assertions.assertEquals(MathUtil.PI, MathUtil.atan2(+0.0, -1.0));
        Assertions.assertEquals(MathUtil.PI, MathUtil.atan2(+1.0, Double.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(-MathUtil.PI, MathUtil.atan2(-0.0, -1.0));
        Assertions.assertEquals(-MathUtil.PI, MathUtil.atan2(-1.0, Double.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(MathUtil.PI_2, MathUtil.atan2(+1.0, +0.0));
        Assertions.assertEquals(MathUtil.PI_2, MathUtil.atan2(+1.0, -0.0));
        Assertions.assertEquals(MathUtil.PI_2, MathUtil.atan2(Double.POSITIVE_INFINITY, +1.0));
        
        Assertions.assertEquals(-MathUtil.PI_2, MathUtil.atan2(-1.0, +0.0));
        Assertions.assertEquals(-MathUtil.PI_2, MathUtil.atan2(-1.0, -0.0));
        Assertions.assertEquals(-MathUtil.PI_2, MathUtil.atan2(Double.NEGATIVE_INFINITY, +1.0));
        
        Assertions.assertEquals(MathUtil.PI_4, MathUtil.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(MathUtil.PI_2 + MathUtil.PI_4, MathUtil.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_4, MathUtil.atan2(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(-MathUtil.PI_2 - MathUtil.PI_4, MathUtil.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }
    
    @Test
    void pow()
    {
        Assertions.assertEquals(+1.0F, MathUtil.pow(+1.0F, -0.0F));
        Assertions.assertEquals(+1.0F, MathUtil.pow(+1.0F, +0.0F));
        
        Assertions.assertEquals(+2.0F, MathUtil.pow(+2.0F, +1.0F));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(+2.0F, Float.NaN)));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(Float.NaN, +1.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(Float.NaN, -1.0F)));
        Assertions.assertEquals(+1.0F, MathUtil.pow(Float.NaN, -0.0F));
        Assertions.assertEquals(+1.0F, MathUtil.pow(Float.NaN, +0.0F));
        
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(+1.1F, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(-1.1F, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(+0.9F, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(-0.9F, Float.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0F, MathUtil.pow(+1.1F, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(+0.0F, MathUtil.pow(-1.1F, Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(+0.0F, MathUtil.pow(+0.9F, Float.POSITIVE_INFINITY));
        Assertions.assertEquals(+0.0F, MathUtil.pow(-0.9F, Float.POSITIVE_INFINITY));
        
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(+1.0F, Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(+1.0F, Float.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0F, MathUtil.pow(+0.0F, +0.5F));
        Assertions.assertEquals(+0.0F, MathUtil.pow(Float.POSITIVE_INFINITY, -0.5F));
        
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(+0.0F, -0.5F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(Float.POSITIVE_INFINITY, +0.5F));
        
        Assertions.assertEquals(+0.0F, MathUtil.pow(-0.0F, +2.0F));
        Assertions.assertEquals(+0.0F, MathUtil.pow(Float.NEGATIVE_INFINITY, -2.0F));
        
        Assertions.assertEquals(-0.0F, MathUtil.pow(-0.0F, +3.0F));
        Assertions.assertEquals(-0.0F, MathUtil.pow(Float.NEGATIVE_INFINITY, -3.0F));
        
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(-0.0F, -2.0F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.pow(Float.NEGATIVE_INFINITY, +2.0F));
        
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.pow(-0.0F, -3.0F));
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, MathUtil.pow(Float.NEGATIVE_INFINITY, +3.0F));
        
        Assertions.assertEquals(MathUtil.pow(4.0F, +2.0F), MathUtil.pow(-4.0F, +2.0F));
        Assertions.assertEquals(-MathUtil.pow(4.0F, +3.0F), MathUtil.pow(-4.0F, +3.0F));
        Assertions.assertTrue(Float.isNaN(MathUtil.pow(-4.0F, -1.5F)));
        //
        Assertions.assertEquals(+1.0, MathUtil.pow(+1.0, -0.0));
        Assertions.assertEquals(+1.0, MathUtil.pow(+1.0, +0.0));
        
        Assertions.assertEquals(+2.0, MathUtil.pow(+2.0, +1.0));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(+2.0, Double.NaN)));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(Double.NaN, +1.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(Double.NaN, -1.0)));
        Assertions.assertEquals(+1.0, MathUtil.pow(Double.NaN, -0.0));
        Assertions.assertEquals(+1.0, MathUtil.pow(Double.NaN, +0.0));
        
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(+1.1, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(-1.1, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(+0.9, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(-0.9, Double.NEGATIVE_INFINITY));
        
        Assertions.assertEquals(+0.0, MathUtil.pow(+1.1, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(+0.0, MathUtil.pow(-1.1, Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(+0.0, MathUtil.pow(+0.9, Double.POSITIVE_INFINITY));
        Assertions.assertEquals(+0.0, MathUtil.pow(-0.9, Double.POSITIVE_INFINITY));
        
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(+1.0, Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(+1.0, Double.NEGATIVE_INFINITY)));
        
        Assertions.assertEquals(+0.0, MathUtil.pow(+0.0, +0.5));
        Assertions.assertEquals(+0.0, MathUtil.pow(Double.POSITIVE_INFINITY, -0.5));
        
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(+0.0, -0.5));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(Double.POSITIVE_INFINITY, +0.5));
        
        Assertions.assertEquals(+0.0, MathUtil.pow(-0.0, +2.0));
        Assertions.assertEquals(+0.0, MathUtil.pow(Double.NEGATIVE_INFINITY, -2.0));
        
        Assertions.assertEquals(-0.0, MathUtil.pow(-0.0, +3.0));
        Assertions.assertEquals(-0.0, MathUtil.pow(Double.NEGATIVE_INFINITY, -3.0));
        
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(-0.0, -2.0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.pow(Double.NEGATIVE_INFINITY, +2.0));
        
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.pow(-0.0, -3.0));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtil.pow(Double.NEGATIVE_INFINITY, +3.0));
        
        Assertions.assertEquals(MathUtil.pow(4.0, +2.0), MathUtil.pow(-4.0, +2.0));
        Assertions.assertEquals(-MathUtil.pow(4.0, +3.0), MathUtil.pow(-4.0, +3.0));
        Assertions.assertTrue(Double.isNaN(MathUtil.pow(-4.0, -1.5)));
    }
    
    @Test
    void round()
    {
        Assertions.assertEquals(+0, MathUtil.round(Float.NaN));
        Assertions.assertEquals(Integer.MIN_VALUE, MathUtil.round(Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(Integer.MIN_VALUE, MathUtil.round((float) Integer.MIN_VALUE));
        Assertions.assertEquals(Integer.MIN_VALUE, MathUtil.round((float) Integer.MIN_VALUE - 1.0F));
        
        Assertions.assertEquals(Integer.MAX_VALUE, MathUtil.round(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Integer.MAX_VALUE, MathUtil.round((float) Integer.MAX_VALUE));
        Assertions.assertEquals(Integer.MAX_VALUE, MathUtil.round((float) Integer.MAX_VALUE + 1.0F));
        
        Assertions.assertEquals(+0L, MathUtil.round(Double.NaN));
        Assertions.assertEquals(Long.MIN_VALUE, MathUtil.round(Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(Long.MIN_VALUE, MathUtil.round((double) Long.MIN_VALUE));
        Assertions.assertEquals(Long.MIN_VALUE, MathUtil.round((double) Long.MIN_VALUE - 1.0));
        
        Assertions.assertEquals(Long.MAX_VALUE, MathUtil.round(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Long.MAX_VALUE, MathUtil.round((double) Long.MAX_VALUE));
        Assertions.assertEquals(Long.MAX_VALUE, MathUtil.round((double) Long.MAX_VALUE + 1.0));
        
        Assertions.assertEquals(+1, MathUtil.round(+1.234F, -1));
        Assertions.assertEquals(+1, MathUtil.round(+1.234F, +0));
        Assertions.assertEquals(+1.2F, MathUtil.round(+1.234F, +1));
        Assertions.assertEquals(+1.23F, MathUtil.round(+1.234F, +2));
        
        Assertions.assertEquals(+1L, MathUtil.round(+1.234, -1));
        Assertions.assertEquals(+1L, MathUtil.round(+1.234, +0));
        Assertions.assertEquals(+1.2, MathUtil.round(+1.234, +1));
        Assertions.assertEquals(+1.23, MathUtil.round(+1.234, +2));
    }
    
    @Test
    void random()
    {
        double value;
        for (int i = 0; i < this.iterations; i++)
        {
            value = MathUtil.random();
            Assertions.assertTrue(0.0 <= value && value < 1.0);
        }
    }
    
    @Test
    void add()
    {
        Assertions.assertEquals(16, MathUtil.add(4, 12));
        Assertions.assertEquals(16L, MathUtil.add(4L, 12L));
        Assertions.assertEquals(16.0F, MathUtil.add(4.0F, 12.0F));
        Assertions.assertEquals(16.0, MathUtil.add(4.0, 12.0));
    }
    
    @Test
    void sub()
    {
        Assertions.assertEquals(-8, MathUtil.sub(4, 12));
        Assertions.assertEquals(-8L, MathUtil.sub(4L, 12L));
        Assertions.assertEquals(-8.0F, MathUtil.sub(4.0F, 12.0F));
        Assertions.assertEquals(-8.0, MathUtil.sub(4.0, 12.0));
    }
    
    @Test
    void revSub()
    {
        Assertions.assertEquals(8, MathUtil.revSub(4, 12));
        Assertions.assertEquals(8L, MathUtil.revSub(4L, 12L));
        Assertions.assertEquals(8.0F, MathUtil.revSub(4.0F, 12.0F));
        Assertions.assertEquals(8.0, MathUtil.revSub(4.0, 12.0));
    }
    
    @Test
    void mul()
    {
        Assertions.assertEquals(48, MathUtil.mul(4, 12));
        Assertions.assertEquals(48L, MathUtil.mul(4L, 12L));
        Assertions.assertEquals(48.0F, MathUtil.mul(4.0F, 12.0F));
        Assertions.assertEquals(48.0, MathUtil.mul(4.0, 12.0));
    }
    
    @Test
    void div()
    {
        Assertions.assertEquals(3, MathUtil.div(12, 4));
        Assertions.assertEquals(3L, MathUtil.div(12L, 4L));
        Assertions.assertEquals(3.0F, MathUtil.div(12.0F, 4.0F));
        Assertions.assertEquals(3.0, MathUtil.div(12.0, 4.0));
    }
    
    @Test
    void sum()
    {
        Assertions.assertEquals(10, MathUtil.sum(1, 2, 3, 4));
        Assertions.assertEquals(10L, MathUtil.sum(1L, 2L, 3L, 4L));
        Assertions.assertEquals(10.0F, MathUtil.sum(1.0F, 2.0F, 3.0F, 4.0F));
        Assertions.assertEquals(10.0, MathUtil.sum(1.0, 2.0, 3.0, 4.0));
    }
    
    @Test
    void product()
    {
        Assertions.assertEquals(24, MathUtil.product(1, 2, 3, 4));
        Assertions.assertEquals(24L, MathUtil.product(1L, 2L, 3L, 4L));
        Assertions.assertEquals(24.0F, MathUtil.product(1.0F, 2.0F, 3.0F, 4.0F));
        Assertions.assertEquals(24.0, MathUtil.product(1.0, 2.0, 3.0, 4.0));
    }
    
    @Test
    void abs()
    {
        Assertions.assertEquals(+1, MathUtil.abs(+1));
        Assertions.assertEquals(+1, MathUtil.abs(-1));
        Assertions.assertEquals(Integer.MIN_VALUE, MathUtil.abs(Integer.MIN_VALUE));
        
        Assertions.assertEquals(+1L, MathUtil.abs(+1F));
        Assertions.assertEquals(+1L, MathUtil.abs(-1F));
        Assertions.assertEquals(Long.MIN_VALUE, MathUtil.abs(Long.MIN_VALUE));
        
        Assertions.assertEquals(+1.0F, MathUtil.abs(+1.0F));
        Assertions.assertEquals(+1.0F, MathUtil.abs(-1.0F));
        Assertions.assertEquals(+0.0F, MathUtil.abs(+0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.abs(-0.0F));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.abs(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.POSITIVE_INFINITY, MathUtil.abs(Float.NEGATIVE_INFINITY));
        Assertions.assertTrue(Float.isNaN(MathUtil.abs(Float.NaN)));
        
        Assertions.assertEquals(+1.0, MathUtil.abs(+1.0));
        Assertions.assertEquals(+1.0, MathUtil.abs(-1.0));
        Assertions.assertEquals(+0.0, MathUtil.abs(+0.0));
        Assertions.assertEquals(+0.0, MathUtil.abs(-0.0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.abs(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtil.abs(Double.NEGATIVE_INFINITY));
        Assertions.assertTrue(Double.isNaN(MathUtil.abs(Double.NaN)));
    }
    
    @Test
    void min()
    {
        Assertions.assertEquals(1, MathUtil.min(1, 10));
        Assertions.assertEquals(1, MathUtil.min(1, 10, 5));
        
        Assertions.assertEquals(1L, MathUtil.min(1L, 10L));
        Assertions.assertEquals(1L, MathUtil.min(1L, 10L, 5L));
        
        Assertions.assertEquals(1.0F, MathUtil.min(1.0F, 10.0F));
        Assertions.assertEquals(1.0F, MathUtil.min(1.0F, 10.0F, 5.0F));
        Assertions.assertTrue(Float.isNaN(MathUtil.min(Float.NaN, +1.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.min(+1.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.min(+1.0F, Float.NaN, -1.0F)));
        Assertions.assertEquals(-0.0F, MathUtil.min(-0.0F, +0.0F));
        Assertions.assertEquals(-0.0F, MathUtil.min(+0.0F, -0.0F));
        
        Assertions.assertEquals(1.0, MathUtil.min(1.0, 10.0));
        Assertions.assertEquals(1.0, MathUtil.min(1.0, 10.0, 5.0));
        Assertions.assertTrue(Double.isNaN(MathUtil.min(Double.NaN, +1.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.min(+1.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.min(+1.0, Double.NaN, -1.0)));
        Assertions.assertEquals(-0.0, MathUtil.min(-0.0, +0.0));
        Assertions.assertEquals(-0.0, MathUtil.min(+0.0, -0.0));
    }
    
    @Test
    void max()
    {
        Assertions.assertEquals(10, MathUtil.max(1, 10));
        Assertions.assertEquals(10, MathUtil.max(1, 10, 5));
        
        Assertions.assertEquals(10L, MathUtil.max(1L, 10L));
        Assertions.assertEquals(10L, MathUtil.max(1L, 10L, 5L));
        
        Assertions.assertEquals(10.0F, MathUtil.max(1.0F, 10.0F));
        Assertions.assertEquals(10.0F, MathUtil.max(1.0F, 10.0F, 5.0F));
        Assertions.assertTrue(Float.isNaN(MathUtil.max(Float.NaN, +1.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.max(+1.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.max(+1.0F, Float.NaN, -1.0F)));
        Assertions.assertEquals(+0.0F, MathUtil.max(-0.0F, +0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.max(+0.0F, -0.0F));
        
        Assertions.assertEquals(10.0, MathUtil.max(1.0, 10.0));
        Assertions.assertEquals(10.0, MathUtil.max(1.0, 10.0, 5.0));
        Assertions.assertTrue(Double.isNaN(MathUtil.max(Double.NaN, +1.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.max(+1.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.max(+1.0, Double.NaN, -1.0)));
        Assertions.assertEquals(+0.0, MathUtil.max(-0.0, +0.0));
        Assertions.assertEquals(+0.0, MathUtil.max(+0.0, -0.0));
    }
    
    @Test
    void clamp()
    {
        Assertions.assertEquals(+0, MathUtil.clamp(-1, 10));
        Assertions.assertEquals(+5, MathUtil.clamp(+5, 10));
        Assertions.assertEquals(10, MathUtil.clamp(11, 10));
        Assertions.assertEquals(+0, MathUtil.clamp(-1, 0, 10));
        Assertions.assertEquals(+5, MathUtil.clamp(+5, 0, 10));
        Assertions.assertEquals(10, MathUtil.clamp(11, 0, 10));
        
        Assertions.assertEquals(+0L, MathUtil.clamp(-1L, 10L));
        Assertions.assertEquals(+5L, MathUtil.clamp(+5L, 10L));
        Assertions.assertEquals(10L, MathUtil.clamp(11L, 10L));
        Assertions.assertEquals(+0L, MathUtil.clamp(-1L, 0L, 10L));
        Assertions.assertEquals(+5L, MathUtil.clamp(+5L, 0L, 10L));
        Assertions.assertEquals(10L, MathUtil.clamp(11L, 0L, 10L));
        
        Assertions.assertEquals(+0.0F, MathUtil.clamp(-1.0F, 10.0F));
        Assertions.assertEquals(+5.0F, MathUtil.clamp(+5.0F, 10.0F));
        Assertions.assertEquals(10.0F, MathUtil.clamp(11.0F, 10.0F));
        Assertions.assertEquals(+0.0F, MathUtil.clamp(-1.0F, 0.0F, 10.0F));
        Assertions.assertEquals(+5.0F, MathUtil.clamp(+5.0F, 0.0F, 10.0F));
        Assertions.assertEquals(10.0F, MathUtil.clamp(11.0F, 0.0F, 10.0F));
        
        Assertions.assertEquals(+0.0, MathUtil.clamp(-1.0, 10.0));
        Assertions.assertEquals(+5.0, MathUtil.clamp(+5.0, 10.0));
        Assertions.assertEquals(10.0, MathUtil.clamp(11.0, 10.0));
        Assertions.assertEquals(+0.0, MathUtil.clamp(-1.0, 0.0, 10.0));
        Assertions.assertEquals(+5.0, MathUtil.clamp(+5.0, 0.0, 10.0));
        Assertions.assertEquals(10.0, MathUtil.clamp(11.0, 0.0, 10.0));
    }
    
    @Test
    void cycle()
    {
        int    valueI = 0;
        long   valueL = 0L;
        float  valueF = 0.0F;
        double valueD = 0.0D;
        
        for (int i = 0; i < this.iterations; i++)
        {
            valueI = MathUtil.cycle(valueI + 1, 0, 10);
            Assertions.assertTrue(0 <= valueI && valueI <= 10);
            
            valueL = MathUtil.cycle(valueL + 1L, 0L, 10L);
            Assertions.assertTrue(0L <= valueL && valueL <= 10L);
            
            valueF = MathUtil.cycle(valueF + 1.0F, 0.0F, 10.0F);
            Assertions.assertTrue(0.0F <= valueF && valueF <= 10.0F);
            
            valueD = MathUtil.cycle(valueD + 1.0, 0.0, 10.0);
            Assertions.assertTrue(0.0 <= valueD && valueD <= 10.0);
        }
        
        for (int i = 0; i < this.iterations; i++)
        {
            valueI = MathUtil.cycle(valueI + 1, 10);
            Assertions.assertTrue(0 <= valueI && valueI <= 10);
            
            valueL = MathUtil.cycle(valueL + 1L, 10L);
            Assertions.assertTrue(0L <= valueL && valueL <= 10L);
            
            valueF = MathUtil.cycle(valueF + 1.0F, 10.0F);
            Assertions.assertTrue(0.0F <= valueF && valueF <= 10.0F);
            
            valueD = MathUtil.cycle(valueD + 1.0, 10.0);
            Assertions.assertTrue(0.0 <= valueD && valueD <= 10.0);
        }
        
        for (int i = 0; i < this.iterations; i++)
        {
            valueI = MathUtil.cycle(valueI - 1, 0, 10);
            Assertions.assertTrue(0 <= valueI && valueI <= 10);
            
            valueL = MathUtil.cycle(valueL - 1L, 0L, 10L);
            Assertions.assertTrue(0L <= valueL && valueL <= 10L);
            
            valueF = MathUtil.cycle(valueF - 1.0F, 0.0F, 10.0F);
            Assertions.assertTrue(0.0F <= valueF && valueF <= 10.0F);
            
            valueD = MathUtil.cycle(valueD - 1.0, 0.0, 10.0);
            Assertions.assertTrue(0.0 <= valueD && valueD <= 10.0);
        }
        
        for (int i = 0; i < this.iterations; i++)
        {
            valueI = MathUtil.cycle(valueI - 1, 10);
            Assertions.assertTrue(0 <= valueI && valueI <= 10);
            
            valueL = MathUtil.cycle(valueL - 1L, 10L);
            Assertions.assertTrue(0L <= valueL && valueL <= 10L);
            
            valueF = MathUtil.cycle(valueF - 1.0F, 10.0F);
            Assertions.assertTrue(0.0F <= valueF && valueF <= 10.0F);
            
            valueD = MathUtil.cycle(valueD - 1.0, 10.0);
            Assertions.assertTrue(0.0 <= valueD && valueD <= 10.0);
        }
    }
    
    @Test
    void index()
    {
        int size = 16;
        
        Assertions.assertEquals(14, MathUtil.index(-2, size));
        Assertions.assertEquals(15, MathUtil.index(-1, size));
        Assertions.assertEquals(0, MathUtil.index(0, size));
        Assertions.assertEquals(1, MathUtil.index(1, size));
        Assertions.assertEquals(14, MathUtil.index(14, size));
        Assertions.assertEquals(15, MathUtil.index(15, size));
        Assertions.assertEquals(0, MathUtil.index(16, size));
        Assertions.assertEquals(1, MathUtil.index(17, size));
    }
    
    @Test
    void fma()
    {
        Assertions.assertEquals(110, MathUtil.fma(10, 10, 10));
        
        Assertions.assertEquals(110L, MathUtil.fma(10L, 10L, 10L));
        
        Assertions.assertEquals(110.0F, MathUtil.fma(10.0F, 10.0F, 10.0F));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(Float.NaN, 10.0F, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(10.0F, Float.NaN, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(10.0F, 10.0F, Float.NaN)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(Float.POSITIVE_INFINITY, 0.0F, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(Float.NEGATIVE_INFINITY, 0.0F, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(0.0F, Float.POSITIVE_INFINITY, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(0.0F, Float.NEGATIVE_INFINITY, 10.0F)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(Float.POSITIVE_INFINITY, 1.0F, Float.NEGATIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(1.0F, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(Float.NEGATIVE_INFINITY, 1.0F, Float.POSITIVE_INFINITY)));
        Assertions.assertTrue(Float.isNaN(MathUtil.fma(1.0F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)));
        
        Assertions.assertEquals(110.0, MathUtil.fma(10.0, 10.0, 10.0));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(Double.NaN, 10.0, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(10.0, Double.NaN, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(10.0, 10.0, Double.NaN)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(Double.POSITIVE_INFINITY, 0.0, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(Double.NEGATIVE_INFINITY, 0.0, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(0.0, Double.POSITIVE_INFINITY, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(0.0, Double.NEGATIVE_INFINITY, 10.0)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(Double.POSITIVE_INFINITY, 1.0, Double.NEGATIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(Double.NEGATIVE_INFINITY, 1.0, Double.POSITIVE_INFINITY)));
        Assertions.assertTrue(Double.isNaN(MathUtil.fma(1.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)));
    }
    
    @Test
    void signum()
    {
        Assertions.assertEquals(-1, MathUtil.signum(-1));
        Assertions.assertEquals(+0, MathUtil.signum(+0));
        Assertions.assertEquals(+1, MathUtil.signum(+1));
        
        Assertions.assertEquals(-1L, MathUtil.signum(-1L));
        Assertions.assertEquals(+0L, MathUtil.signum(+0L));
        Assertions.assertEquals(+1L, MathUtil.signum(+1L));
        
        Assertions.assertEquals(-1.0F, MathUtil.signum(-1.0F));
        Assertions.assertEquals(-0.0F, MathUtil.signum(-0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.signum(+0.0F));
        Assertions.assertEquals(+1.0F, MathUtil.signum(+1.0F));
        Assertions.assertTrue(Float.isNaN(MathUtil.signum(Float.NaN)));
        
        Assertions.assertEquals(-1.0, MathUtil.signum(-1.0));
        Assertions.assertEquals(-0.0, MathUtil.signum(-0.0));
        Assertions.assertEquals(+0.0, MathUtil.signum(+0.0));
        Assertions.assertEquals(+1.0, MathUtil.signum(+1.0));
        Assertions.assertTrue(Double.isNaN(MathUtil.signum(Double.NaN)));
    }
    
    @Test
    void copySign()
    {
        Assertions.assertEquals(+69, MathUtil.copySign(+69, +1));
        Assertions.assertEquals(-69, MathUtil.copySign(+69, -1));
        Assertions.assertEquals(+69, MathUtil.copySign(-69, +1));
        Assertions.assertEquals(-69, MathUtil.copySign(-69, -1));
        
        Assertions.assertEquals(+69L, MathUtil.copySign(+69L, +1L));
        Assertions.assertEquals(-69L, MathUtil.copySign(+69L, -1L));
        Assertions.assertEquals(+69L, MathUtil.copySign(-69L, +1L));
        Assertions.assertEquals(-69L, MathUtil.copySign(-69L, -1L));
        
        Assertions.assertEquals(+69.0F, MathUtil.copySign(+69.0F, +1.0F));
        Assertions.assertEquals(-69.0F, MathUtil.copySign(+69.0F, -1.0F));
        Assertions.assertEquals(+69.0F, MathUtil.copySign(-69.0F, +1.0F));
        Assertions.assertEquals(-69.0F, MathUtil.copySign(-69.0F, -1.0F));
        
        Assertions.assertEquals(+69.0, MathUtil.copySign(+69.0, +1.0));
        Assertions.assertEquals(-69.0, MathUtil.copySign(+69.0, -1.0));
        Assertions.assertEquals(+69.0, MathUtil.copySign(-69.0, +1.0));
        Assertions.assertEquals(-69.0, MathUtil.copySign(-69.0, -1.0));
    }
    
    @Test
    void getExponent()
    {
        Assertions.assertEquals(Float.MAX_EXPONENT + 1.0F, MathUtil.getExponent(Float.NaN));
        Assertions.assertEquals(Float.MAX_EXPONENT + 1.0F, MathUtil.getExponent(Float.POSITIVE_INFINITY));
        Assertions.assertEquals(Float.MAX_EXPONENT + 1.0F, MathUtil.getExponent(Float.NEGATIVE_INFINITY));
        Assertions.assertEquals(Float.MIN_EXPONENT - 1.0F, MathUtil.getExponent(+0.0F));
        Assertions.assertEquals(Float.MIN_EXPONENT - 1.0F, MathUtil.getExponent(-0.0F));
        
        Assertions.assertEquals(Double.MAX_EXPONENT + 1.0, MathUtil.getExponent(Double.NaN));
        Assertions.assertEquals(Double.MAX_EXPONENT + 1.0, MathUtil.getExponent(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.MAX_EXPONENT + 1.0, MathUtil.getExponent(Double.NEGATIVE_INFINITY));
        Assertions.assertEquals(Double.MIN_EXPONENT - 1.0, MathUtil.getExponent(+0.0));
        Assertions.assertEquals(Double.MIN_EXPONENT - 1.0, MathUtil.getExponent(-0.0));
    }
    
    @Test
    void map()
    {
        Assertions.assertEquals(0.0, MathUtil.map(0.0, 0.0, 1.0, 0.0, 10.0));
        Assertions.assertEquals(5.0, MathUtil.map(0.5, 0.0, 1.0, 0.0, 10.0));
        Assertions.assertEquals(10.0, MathUtil.map(1.0, 0.0, 1.0, 0.0, 10.0));
        Assertions.assertEquals(20.0, MathUtil.map(2.0, 0.0, 1.0, 0.0, 10.0));
    }
    
    @Test
    void getDecimal()
    {
        Assertions.assertEquals(0.14159012F, MathUtil.getDecimal(3.14159F));
        Assertions.assertEquals(0.14158999999999988, MathUtil.getDecimal(3.14159));
    }
    
    @Test
    void isEven()
    {
        Assertions.assertTrue(MathUtil.isEven(0));
        Assertions.assertFalse(MathUtil.isEven(1));
        Assertions.assertTrue(MathUtil.isEven(2));
        
        Assertions.assertTrue(MathUtil.isEven(0.0));
        Assertions.assertFalse(MathUtil.isEven(1.0));
        Assertions.assertTrue(MathUtil.isEven(2.0));
    }
    
    @Test
    void isOdd()
    {
        Assertions.assertFalse(MathUtil.isOdd(0));
        Assertions.assertTrue(MathUtil.isOdd(1));
        Assertions.assertFalse(MathUtil.isOdd(2));
        
        Assertions.assertFalse(MathUtil.isOdd(0.0));
        Assertions.assertTrue(MathUtil.isOdd(1.0));
        Assertions.assertFalse(MathUtil.isOdd(2.0));
    }
    
    @Test
    void mean()
    {
        Assertions.assertEquals(10.0F / 4.0F, MathUtil.mean(1, 2, 3, 4));
        Assertions.assertEquals(10.0 / 4.0, MathUtil.mean(1L, 2L, 3L, 4L));
        Assertions.assertEquals(10.0F / 4.0F, MathUtil.mean(1.0F, 2.0F, 3.0F, 4.0F));
        Assertions.assertEquals(10.0 / 4.0, MathUtil.mean(1.0, 2.0, 3.0, 4.0));
    }
    
    @Test
    void stdDev()
    {
        Assertions.assertEquals(1.118034F, MathUtil.stdDev(1, 2, 3, 4));
        Assertions.assertEquals(1.118033988749895, MathUtil.stdDev(1L, 2L, 3L, 4L));
        Assertions.assertEquals(1.118034F, MathUtil.stdDev(1.0F, 2.0F, 3.0F, 4.0F));
        Assertions.assertEquals(1.118033988749895, MathUtil.stdDev(1.0, 2.0, 3.0, 4.0));
    }
    
    @Test
    void lerp()
    {
        Assertions.assertEquals(+0, MathUtil.lerp(0, 10, -1.0F));
        Assertions.assertEquals(+0, MathUtil.lerp(0, 10, -0.0F));
        Assertions.assertEquals(+0, MathUtil.lerp(0, 10, +0.0F));
        Assertions.assertEquals(+5, MathUtil.lerp(0, 10, +0.5F));
        Assertions.assertEquals(10, MathUtil.lerp(0, 10, +1.0F));
        Assertions.assertEquals(10, MathUtil.lerp(0, 10, +2.0F));
        
        Assertions.assertEquals(+0L, MathUtil.lerp(0L, 10L, -1.0));
        Assertions.assertEquals(+0L, MathUtil.lerp(0L, 10L, -0.0));
        Assertions.assertEquals(+0L, MathUtil.lerp(0L, 10L, +0.0));
        Assertions.assertEquals(+5L, MathUtil.lerp(0L, 10L, +0.5));
        Assertions.assertEquals(10L, MathUtil.lerp(0L, 10L, +1.0));
        Assertions.assertEquals(10L, MathUtil.lerp(0L, 10L, +2.0));
        
        Assertions.assertEquals(+0.0F, MathUtil.lerp(0.0F, 10.0F, -1.0F));
        Assertions.assertEquals(+0.0F, MathUtil.lerp(0.0F, 10.0F, -0.0F));
        Assertions.assertEquals(+0.0F, MathUtil.lerp(0.0F, 10.0F, +0.0F));
        Assertions.assertEquals(+5.0F, MathUtil.lerp(0.0F, 10.0F, +0.5F));
        Assertions.assertEquals(10.0F, MathUtil.lerp(0.0F, 10.0F, +1.0F));
        Assertions.assertEquals(10.0F, MathUtil.lerp(0.0F, 10.0F, +2.0F));
        
        Assertions.assertEquals(+0.0, MathUtil.lerp(0.0, 10.0, -1.0));
        Assertions.assertEquals(+0.0, MathUtil.lerp(0.0, 10.0, -0.0));
        Assertions.assertEquals(+0.0, MathUtil.lerp(0.0, 10.0, +0.0));
        Assertions.assertEquals(+5.0, MathUtil.lerp(0.0, 10.0, +0.5));
        Assertions.assertEquals(10.0, MathUtil.lerp(0.0, 10.0, +1.0));
        Assertions.assertEquals(10.0, MathUtil.lerp(0.0, 10.0, +2.0));
    }
    
    @Test
    void smoothstep()
    {
        Assertions.assertEquals(+0.0, MathUtil.smoothstep(+0.0));
        Assertions.assertEquals(+1.0, MathUtil.smoothstep(+1.0));
    }
    
    @Test
    void smootherstep()
    {
        Assertions.assertEquals(+0.0, MathUtil.smootherstep(+0.0));
        Assertions.assertEquals(+1.0, MathUtil.smootherstep(+1.0));
    }
    
    @Test
    void testEquals()
    {
        Assertions.assertTrue(MathUtil.equals(0.0F, 0.0F, 0.000001F));
        Assertions.assertFalse(MathUtil.equals(0.0F, +1.0F, 0.000001F));
        Assertions.assertFalse(MathUtil.equals(0.0F, -1.0F, 0.000001F));
        Assertions.assertFalse(MathUtil.equals(+1.0F, 0.0F, 0.000001F));
        Assertions.assertFalse(MathUtil.equals(-1.0F, 0.0F, 0.000001F));
        Assertions.assertTrue(MathUtil.equals(0.0F, 0.00000001F, 0.000001F));
        Assertions.assertTrue(MathUtil.equals(0.00000001F, 0.0F, 0.000001F));
        
        Assertions.assertTrue(MathUtil.equals(0.0, 0.0, 0.000001));
        Assertions.assertFalse(MathUtil.equals(0.0, +1.0, 0.000001));
        Assertions.assertFalse(MathUtil.equals(0.0, -1.0, 0.000001));
        Assertions.assertFalse(MathUtil.equals(+1.0, 0.0, 0.000001));
        Assertions.assertFalse(MathUtil.equals(-1.0, 0.0, 0.000001));
        Assertions.assertTrue(MathUtil.equals(0.0, 0.00000001, 0.000001));
        Assertions.assertTrue(MathUtil.equals(0.00000001, 0.0, 0.000001));
    }
}