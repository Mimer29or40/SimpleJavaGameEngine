package CodingTrain;

import engine.Engine;
import engine.color.Color;
import engine.gl.GL;
import engine.gl.ScreenBuffer;

import static CodingTrain.CTUtils.map;
import static engine.IO.*;
import static engine.Renderer.*;

public class CT_001_StarField extends Engine
{
    Star[] stars = new Star[400];
    
    double speed;
    
    protected CT_001_StarField()
    {
        super(600, 600);
    }
    
    @Override
    protected void setup()
    {
        windowTitle("Coding Train - 001 - StarField");
        
        for (int i = 0; i < stars.length; i++) stars[i] = new Star();
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        speed = map(mousePos().x(), 0, windowSize().x(), 0, 50);
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearBuffers(ScreenBuffer.COLOR);
        
        clear(Color.BLACK);
        viewTranslate(windowSize().x() / 2.0, windowSize().y() / 2.0);
        
        for (Star star : stars)
        {
            star.update();
            star.show();
        }
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    class Star
    {
        double x, y, z;
        
        double pz;
        
        Star()
        {
            double w = windowSize().x() * 2.0;
            double h = windowSize().y() * 2.0;
            
            x  = Math.random() * 2 * w - w;
            y  = Math.random() * 2 * h - h;
            pz = z = Math.random() * w;
        }
        
        void update()
        {
            z -= speed;
            if (this.z < 1)
            {
                double w = windowSize().x() * 2.0;
                double h = windowSize().y() * 2.0;
                
                x  = Math.random() * 2 * w - w;
                y  = Math.random() * 2 * h - h;
                pz = z = Math.random() * w;
            }
        }
        
        void show()
        {
            double sx = map(x / z, 0.0, 1.0, 0.0, windowSize().x() / 2.0);
            double sy = map(y / z, 0.0, 1.0, 0.0, windowSize().y() / 2.0);
            
            double r = map(z, windowSize().x() * 2.0, 0.0, 0.0, 16.0);
            ellipseColor(Color.WHITE);
            ellipseDraw(sx, sy, r, r);
            
            double px = map(x / pz, 0.0, 1.0, 0.0, windowSize().x() / 2.0);
            double py = map(y / pz, 0.0, 1.0, 0.0, windowSize().y() / 2.0);
            
            pz = z;
            
            lineColor(Color.WHITE);
            lineDraw(px, py, sx, sy);
            
        }
    }
    
    public static void main(String[] args)
    {
        Engine instance = new CT_001_StarField();
        
        start(instance);
    }
}
