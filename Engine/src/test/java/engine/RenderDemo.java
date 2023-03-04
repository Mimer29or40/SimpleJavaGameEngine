package engine;

import engine.color.Color;
import engine.gl.DepthMode;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.ScreenBuffer;
import engine.util.Logger;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import static engine.IO.*;
import static engine.Renderer.*;

public class RenderDemo extends Engine
{
    protected RenderDemo()
    {
        super(640, 400);
        
        this.updateFreq = 120;
        this.drawFreq   = 60;
    }
    
    @Override
    protected void setup()
    {
        GL.DEFAULT_STATE.clearColor = new double[] {0.1, 0.1, 0.1, 1.0};
        GL.DEFAULT_STATE.depthMode  = DepthMode.LESS;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        Matrix4d projection = new Matrix4d();
        Matrix4d view       = new Matrix4d();
        
        double angle = mousePos().x() / windowSize().x() * Math.PI * 2.0;
        double y = mousePos().y() - windowSize().y() / 2.0;
        
        Vector3d pos = new Vector3d(Math.cos(angle) * 80, y, Math.sin(angle) * 80);
        
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Framebuffer fb = Framebuffer.get();
        
        int w = fb.width();
        int h = fb.height();
        
        // World Space
        projection.setPerspective(Math.toRadians(45), (double) w / h, 0.1, 1000.0);
        rendererProjection(projection);
        view.setLookAt(pos, new Vector3d(), new Vector3d(0, 1, 0));
        rendererView(view);
        
        pointsDemo();
        
        linesDemo(time);
        
        ellipseDemo();
    
        // Screen Space
        projection.setOrtho(-w / 2.0, w / 2.0, h / 2.0, -h / 2.0, -10, 1000.0);
        rendererProjection(projection);
        view.identity().setTranslation(-w / 2.0, -h / 2.0, 0);
        rendererView(view);
        
        textDemo();
    }
    
    double[] pointPoints = null;
    
    void pointsDemo()
    {
        if (pointPoints == null)
        {
            pointPoints = new double[100 * 3];
            for (int i = 0; i < pointPoints.length; i++)
            {
                pointPoints[i] = Math.random() * 60.0 - 30.0;
            }
        }
        
        pointsSize(10);
        pointsDraw(pointPoints);
    }
    
    void linesDemo(double time)
    {
        linesThickness(2.0);
        
        linesColor(Color.DARK_RED);
        linesDraw(0, 0, 0, 10, 0, 0);
        
        linesColor(Color.DARK_GREEN);
        linesDraw(0, 0, 0, 0, 10, 0);
        
        linesColor(Color.DARK_BLUE);
        linesDraw(0, 0, 0, 0, 0, 10);
        
        linesThickness(5.0);
        
        linesColorStart(Color.DARK_GREEN);
        linesColorEnd(Color.DARK_RED);
        linesDraw(10, 10, 0, 20, 10, 0, 20, 20, 0, 0, 20, 0);
        
        int steps = Math.max((int) (Math.sin(time / 2) * 50 + 60), 2);
        
        double[] points = new double[steps * 3];
        
        double x, y;
        for (int i = 0; i < steps; i++)
        {
            x = (double) i / (steps - 1);
            // y = 4.0 * x * (x - 1) + 1;
            // y = x < 0.5 ? 4 * x * x * x : 1 - 4 * (1 - x) * (1 - x) * (1 - x);
            y = 8 * (x) * (x - 0.5) * (x - 1) + 0.5;
            
            points[(i * 3)]     = x * 50;
            points[(i * 3) + 1] = y * 20;
            points[(i * 3) + 2] = Math.sin(x * 50) * 5;
        }
        
        linesColorStart(Color.CYAN);
        linesColorEnd(Color.MAGENTA);
        linesDraw(points);
        
        for (int i = 0; i < steps; i++)
        {
            double angle = (double) i / (steps - 1) * Math.PI * 10;
            
            points[(i * 3)]     = Math.cos(angle) * i / 8.0;
            points[(i * 3) + 1] = Math.sin(angle) * i / 8.0;
            points[(i * 3) + 2] = i / 2.0;
        }
        
        linesColorStart(Color.LIGHTEST_YELLOW);
        linesColorEnd(Color.DARKEST_YELLOW);
        linesDrawEnclosed(points);
        
        linesColor(Color.BLUE);
        linesDrawBezier(0, 0, 0, 0, 50, -20, 20, 0, 20, -20, 10, 20);
    }
    
    void ellipseDemo()
    {
        //Matrix4d view = new Matrix4d();
        //view.setLookAlong(0, 0, -1, 0, -1, 0);
        //rendererView(view);
    
        ellipseColorInner(Color.GRAY);
        ellipseColorOuter(Color.BLANK);
        ellipseDraw(20, 20, 0);
    }
    
    void textDemo()
    {
        textColor(Color.DARK_GREEN);
        //textSize(mousePos().x());
        textDraw(String.format("Update: %.3f\nDraw:   %.3f", updateTimeActual(), drawTimeActual()), 0, 0);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Logger.LEVEL = Logger.Level.DEBUG;
        
        Engine instance = new RenderDemo();
        start(instance);
    }
}
