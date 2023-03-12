package engine.gui;

import engine.Button;
import engine.IO;
import engine.color.Color;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import static engine.IO.*;
import static engine.Renderer.ellipseColor;
import static engine.Renderer.ellipseDraw;

public class Slider
{
    public final Vector2d pos  = new Vector2d();
    public final Vector2d size = new Vector2d();
    
    public double min = 0.0;
    public double max = 1.0;
    
    protected double value = 0.5;
    
    public final Color lineColor        = new Color(200, 200, 200, 255);
    public final Color ballColor        = new Color(200, 200, 200, 255);
    public final Color ballOutlineColor = new Color(128, 128, 128, 255);
    
    public Slider() {}
    
    public Slider(double min, double max)
    {
        this.min = min;
        this.max = max;
        
        this.value = (max - min) * 0.5;
    }
    
    public double get()
    {
        return this.value;
    }
    
    public void set(double value)
    {
        this.value = value <= this.min ? this.min : Math.min(value, this.max);
    }
    
    protected boolean containsPoint(@NotNull Vector2dc point)
    {
        double x = point.x();
        double y = point.y();
        return !(this.pos.x > x || this.pos.y > y || x > this.pos.x + this.size.x || y > this.pos.y + this.size.y);
    }
    
    public void update()
    {
        Vector2dc mouse = mousePos();
        
        IO.Event dragEvent = mouseOnButtonDragged();
        if (dragEvent.fired() && !dragEvent.consumed())
        {
            Vector2dc downPos = mouseButtonDragged().getOrDefault(Button.LEFT, null);
            if (downPos != null && containsPoint(downPos))
            {
                dragEvent.consume();
                set(map(mouse.x(), this.pos.x, this.pos.x + this.size.x, this.min, this.max));
            }
        }
        
        if (containsPoint(mouse) && mouseOnScrollChange().fired() && !mouseOnScrollChange().consumed())
        {
            mouseOnScrollChange().consume();
            set(get() + (this.max - this.min) * 0.01 * mouseScroll().y());
        }
    }
    
    public void draw()
    {
        double line_thickness = this.size.y * 0.25;
        
        double linePosX = this.pos.x;
        double linePosY = this.pos.y + (this.size.y - line_thickness) * 0.5;
        
        double lineSizeX = this.size.x;
        double lineSizeY = this.size.y * 0.25;
        
        //draw_color(this.lineColor);
        //draw_rect_mode(RectMode.CORNER);
        //fill_rect(line_pos, line_size); // TODO
        
        //noinspection SuspiciousNameCombination
        double ballPosX = this.pos.x + map(this.value, this.min, this.max, 0, this.size.x);
        double ballPosY = this.pos.y + this.size.y * 0.5;
        
        ellipseColor(this.ballOutlineColor);
        ellipseDraw(ballPosX, ballPosY, this.size.y, this.size.y);
        
        double thickness = 2;
        
        ellipseColor(this.ballColor);
        ellipseDraw(ballPosX, ballPosY, this.size.y - thickness, this.size.y - thickness);
    }
    
    private static double map(double value, double x0, double x1, double y0, double y1)
    {
        return (value - x0) * (y1 - y0) / (x1 - x0) + y0;
    }
}
