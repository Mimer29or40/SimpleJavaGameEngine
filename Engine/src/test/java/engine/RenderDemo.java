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
        GL.DEFAULT_STATE.depthMode = DepthMode.LESS;
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardKeyDown(Key.ESCAPE)) stop();
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        double angle = mousePos().x() / windowSize().x() * Math.PI * 2.0;
        Vector3d pos = new Vector3d(Math.cos(angle) * 200, 20, Math.sin(angle) * 200);
        
        GL.clearBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH);
        
        Framebuffer fb = Framebuffer.get();
        
        double w = fb.width() / 4.0;
        double h = fb.height() / 4.0;
        
        Matrix4d view = new Matrix4d();
        view.setPerspective(Math.toRadians(45), w / h, 0.1, 1000.0);
        view.lookAt(pos, new Vector3d(), new Vector3d(0, 1, 0));
        rendererView(view);
        
        linesThickness(5.0);
        
        linesColor(Color.DARK_RED);
        linesDraw(0, 0, 0, 10, 0, 0);
        
        linesColor(Color.DARK_GREEN);
        linesDraw(0, 0, 0, 0, 10, 0);
        
        linesColor(Color.DARK_BLUE);
        linesDraw(0, 0, 0, 0, 0, 10);
        
        linesColorStart(Color.DARK_GREEN);
        linesColorEnd(Color.DARK_RED);
        linesDraw(10, 10, 0, 20, 10, 0, 20, 20, 0, 0, 20, 0);
        
        int steps = Math.max((int) (Math.sin(time / 2) * 50 + 50), 2);
        
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
        
        //lineRenderer().startColor.set(Color.CYAN);
        //lineRenderer().endColor.set(Color.MAGENTA);
        //lineRenderer().drawLines(points);
        
        linesColorStart(Color.CYAN);
        linesColorEnd(Color.MAGENTA);
        linesDraw(points);
        
        //Matrix4d view = new Matrix4d().setOrtho(0, fb.width(), fb.height(), 0, -1, 1);
        //
        //Program.bind(this.shader);
        //Program.uniformMatrix4("view", false, view);
        //Program.uniformInt2("viewport", fb.width(), fb.height());
        //Program.uniformFloat("thickness", 10);
        //
        //vao.draw(DrawMode.LINE_STRIP_ADJACENCY, 6);
        
        view.setOrtho(0, fb.width(), fb.height(), 0, -1, 1);
        rendererView(view);
    
        textColor(Color.DARK_GREEN);
        //textSize(mousePos().x());
        textDraw("ABCDEF\nGHIJK\nLMNOP\nQRSTU\nVWXYZ", 0, 0);
    
        //textRenderer().color.set(Color.DARK_GREEN);
        ////textRenderer().size = mousePos().x();
        //textRenderer().drawText("ABCDEF\nGHIJK\nLMNOP\nQRSTU\nVWXYZ", 0, 0);
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
