package engine;

import engine.color.Color;
import engine.gl.GL;
import engine.gl.ScreenBuffer;
import engine.util.Logger;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import static engine.IO.*;
import static engine.Renderer.*;

public class RenderDemo extends Engine
{
    Vector2d pos;
    double   angle;
    double   scale;
    
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
        
        pos   = new Vector2d();
        angle = 0.0;
        scale = 1.0;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
        if (mouseOnButtonDragged().fired())
        {
            Vector2dc downPos = mouseButtonDragged().getOrDefault(Button.LEFT, null);
            if (downPos != null)
            {
                pos.add(mousePosDelta());
            }
        }
        if (mouseOnScrollChange().fired())
        {
            scale *= Math.pow(0.1, mouseScroll().y() / -50.0);
        }
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        GL.clearBuffers(ScreenBuffer.COLOR);
        
        viewTranslate(pos);
        viewScale(scale);
        //viewTranslate(w, h);
        //viewRotate(time);
        //viewTranslate(-w, -h);
        
        pointsDemo();
        
        linesDemo(time);
        
        ellipseDemo();
        
        viewIdentity();
        
        textDemo();
        
        //Matrix4d projection = new Matrix4d();
        //Matrix4d view       = new Matrix4d();
        //
        //double angle = mousePos().x() / windowSize().x() * Math.PI * 2.0;
        //double y = mousePos().y() - windowSize().y() / 2.0;
        //
        //Vector3d pos = new Vector3d(Math.cos(angle) * 80, y, Math.sin(angle) * 80);
        //
        //GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        //
        //Framebuffer fb = Framebuffer.get();
        //
        //int w = fb.width();
        //int h = fb.height();
        //
        //// World Space
        //projection.setPerspective(Math.toRadians(45), (double) w / h, 0.1, 1000.0);
        //rendererProjection(projection);
        //view.setLookAt(pos, new Vector3d(), new Vector3d(0, 1, 0));
        //rendererView(view);
        //
        //pointsDemo();
        //
        //linesDemo(time);
        //
        //ellipseDemo();
        //
        //// Screen Space
        //projection.setOrtho(-w / 2.0, w / 2.0, h / 2.0, -h / 2.0, -10, 1000.0);
        //rendererProjection(projection);
        //view.identity().setTranslation(-w / 2.0, -h / 2.0, 0);
        //rendererView(view);
        //
        //textDemo();
    }
    
    double[] pointPoints = null;
    
    void pointsDemo()
    {
        if (pointPoints == null)
        {
            int w = windowSize().x();
            int h = windowSize().y();
            
            pointPoints = new double[100 * 2];
            for (int i = 0, n = pointPoints.length >> 1, index = 0; i < n; i++)
            {
                pointPoints[index++] = Math.random() * w;
                pointPoints[index++] = Math.random() * h;
            }
        }
        
        pointsSize(10);
        pointsDraw(pointPoints);
    }
    
    void linesDemo(double time)
    {
        linesThickness(2.0);
        
        linesColor(Color.DARK_RED);
        linesDraw(0, 0, 100, 0);
        
        linesColor(Color.DARK_GREEN);
        linesDraw(0, 0, 0, 100);
        
        linesThickness(5.0);
        
        linesColorStart(Color.DARK_GREEN);
        linesColorEnd(Color.DARK_RED);
        linesDraw(100, 100, 200, 100, 200, 200, 0, 200);
        
        int steps = Math.max((int) (Math.sin(time / 2) * 50 + 55), 2);
        
        double[] points = new double[steps * 2];
        
        double x, y;
        for (int i = 0; i < steps; i++)
        {
            x = (double) i / (steps - 1);
            // y = 4.0 * x * (x - 1) + 1;
            // y = x < 0.5 ? 4 * x * x * x : 1 - 4 * (1 - x) * (1 - x) * (1 - x);
            y = 8 * (x) * (x - 0.5) * (x - 1) + 0.5;
            
            points[(i << 1)]     = x * 500;
            points[(i << 1) + 1] = y * 200;
        }
        
        linesColorStart(Color.CYAN);
        linesColorEnd(Color.MAGENTA);
        linesDraw(points);
        
        for (int i = 0; i < steps; i++)
        {
            double angle = (double) i / (steps - 1) * Math.PI * 5;
            
            points[(i << 1)]     = Math.cos(angle) * i * 2 + 500;
            points[(i << 1) + 1] = Math.sin(angle) * i * 2 + 300;
        }
        
        linesColorStart(Color.LIGHTEST_YELLOW);
        linesColorEnd(Color.DARKEST_YELLOW);
        linesDrawEnclosed(points);
        
        linesColor(Color.BLUE);
        linesDrawBezier(0, 0, 0, 500, 200, 0, 200, 100);
    }
    
    void ellipseDemo()
    {
        //Matrix4d view = new Matrix4d();
        //view.setLookAlong(0, 0, -1, 0, -1, 0);
        //rendererView(view);
        
        ellipseColorInner(Color.GRAY);
        ellipseColorOuter(Color.BLANK);
        ellipseDraw(200, 200);
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
