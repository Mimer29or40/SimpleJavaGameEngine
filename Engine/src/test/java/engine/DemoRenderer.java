package engine;

import engine.color.Color;
import engine.font.TextAlign;
import engine.gl.texture.Texture2D;
import engine.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import static engine.IO.*;
import static engine.Renderer.*;

public class DemoRenderer extends Engine
{
    private Key state = Key.F1;
    
    // DrawPoint2D
    private Vector2d[] draggablePoint;
    private int        draggingPoint = -1;
    
    // DrawLine2D
    private Vector2d[] draggableLine;
    private int        draggingLine = -1;
    
    // DrawLines2D
    private Vector2d[] draggableLines;
    private int        draggingLines = -1;
    
    // DrawBezier2D
    private Vector2d[] draggableBezier;
    private int        draggingBezier = -1;
    
    // DrawTriangle2D
    private Vector2d[] draggableTriangle;
    private int        draggingTriangle = -1;
    
    // DrawQuad2D
    private Vector2d[] draggableQuad;
    private int        draggingQuad = -1;
    
    // DrawRect2D
    private Vector2d[] draggableRect;
    private int        draggingRect = -1;
    
    // DrawEllipse2D
    private Vector2d[] draggableEllipse;
    private int        draggingEllipse = -1;
    
    // DrawRing2D
    private Vector2d[] draggableRing;
    private int        draggingRing = -1;
    
    // DrawQuad2D
    private Vector2d[] draggableTexture;
    private int        draggingTexture = -1;
    
    // DrawTexture2D
    private Texture2D texture;
    
    // DrawText2D
    private Vector2d[] draggableText;
    private int        draggingText = -1;
    
    private boolean toggle;
    
    private int hValue = 1, vValue = 1;
    
    protected DemoRenderer()
    {
        super(800, 800);
    }
    
    @Override
    protected void setup()
    {
        int width  = windowSize().x();
        int height = windowSize().y();
        
        draggablePoint = new Vector2d[16];
        for (int i = 0, n = draggablePoint.length; i < n; i++)
        {
            double angle = (double) i / (n - 1) * Math.PI * 2.0;
            
            double cos = Math.cos(angle) * width * 0.25;
            double sin = Math.sin(angle) * height * 0.25;
            
            draggablePoint[i] = new Vector2d(cos + width * 0.5, sin + height * 0.5);
        }
        
        draggableLine = new Vector2d[] {
                new Vector2d(width * 0.5, height * 0.5), new Vector2d(10, 10), new Vector2d(10, height - 10), new Vector2d(width - 10, height - 10), new Vector2d(width - 10, 10)
        };
        
        draggableLines = new Vector2d[] {
                new Vector2d(10, height * 0.5), new Vector2d(width * 0.5 - 10, 10), new Vector2d(width * 0.5 + 10, height - 10), new Vector2d(width - 10, height - 10)
        };
        
        draggableBezier = new Vector2d[] {
                new Vector2d(10, height * 0.5), new Vector2d(width * 0.5 - 10, 10), new Vector2d(width * 0.5 + 10, height - 10), new Vector2d(width - 10, height - 10)
        };
        
        draggableTriangle = new Vector2d[] {
                new Vector2d(10, height * 0.5), new Vector2d(width * 0.5 - 10, 10), new Vector2d(width * 0.5 + 10, height - 10)
        };
        
        draggableQuad = new Vector2d[] {
                new Vector2d(10, 10), new Vector2d(10, height - 10), new Vector2d(width - 10, height - 10), new Vector2d(width - 10, 10)
        };
        
        draggableRect = new Vector2d[] {
                new Vector2d(width - 10, height - 10)
        };
        
        draggableEllipse = new Vector2d[] {
                new Vector2d(width - 10, height - 10)
        };
        
        draggableRing = new Vector2d[] {
                new Vector2d(width * 0.5 + 10, height * 0.5 + 10),
                new Vector2d(width - 10, height - 10),
                new Vector2d(width * 0.5, height * 0.5),
                new Vector2d(width - 10, height * 0.5),
                };
        
        draggableTexture = new Vector2d[] {
                new Vector2d(10, 10), new Vector2d(10, height - 10), new Vector2d(width - 10, height - 10), new Vector2d(width - 10, 10)
        };
        
        draggableText = new Vector2d[] {
                new Vector2d(width >> 1, height >> 1), new Vector2d(width - 10, height - 10), new Vector2d(10, height >> 1)
        };
        
        //Image image = Image.genColorGradient(30, 30, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE);
        //texture = Texture.load(image);
        //image.delete();
        
        //Font.registerFamily("demo/FiraSans", "FiraSans", true, false, true);
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (keyboardOnKeyDown().fired())
        {
            for (Key key : keyboardKeyDown())
            {
                switch (key)
                {
                    case LEFT -> --hValue;
                    case RIGHT -> ++hValue;
                    case DOWN -> --vValue;
                    case UP -> ++vValue;
                    case SPACE -> toggle = !toggle;
                    case ESCAPE -> stop();
                    case L_SHIFT, ENTER -> {}
                    default -> {if (modifierOnly(Modifier.NONE)) state = key;}
                }
            }
        }
        if (keyboardOnKeyRepeated().fired())
        {
            if (!modifierOnly(Modifier.CONTROL))
            {
                for (Key key : keyboardKeyRepeated())
                {
                    switch (key)
                    {
                        case LEFT -> --hValue;
                        case RIGHT -> ++hValue;
                        case DOWN -> --vValue;
                        case UP -> ++vValue;
                    }
                }
            }
        }
        if (keyboardOnKeyHeld().fired())
        {
            if (modifierOnly(Modifier.CONTROL))
            {
                for (Key key : keyboardKeyHeld())
                {
                    switch (key)
                    {
                        case LEFT -> --hValue;
                        case RIGHT -> ++hValue;
                        case DOWN -> --vValue;
                        case UP -> ++vValue;
                        case ENTER ->
                        {
                            try
                            {
                                Thread.sleep((int) (Math.random() * 9) + 1);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if (mouseOnButtonDown().fired())
        {
            draggingPoint    = -1;
            draggingLine     = -1;
            draggingLines    = -1;
            draggingBezier   = -1;
            draggingTriangle = -1;
            draggingQuad     = -1;
            draggingRect     = -1;
            draggingEllipse  = -1;
            draggingRing     = -1;
            draggingTexture  = -1;
            draggingText     = -1;
            switch (state)
            {
                case F1 -> draggingPoint = mouseDown(draggablePoint, mousePos());
                case F2 -> draggingLine = mouseDown(draggableLine, mousePos());
                case F3 -> draggingLines = mouseDown(draggableLines, mousePos());
                case F5 -> draggingBezier = mouseDown(draggableBezier, mousePos());
                case F6 -> draggingTriangle = mouseDown(draggableTriangle, mousePos());
                case F7 -> draggingQuad = mouseDown(draggableQuad, mousePos());
                case F8 -> draggingRect = mouseDown(draggableRect, mousePos());
                case F9 -> draggingEllipse = mouseDown(draggableEllipse, mousePos());
                case F10 -> draggingRing = mouseDown(draggableRing, mousePos());
                case F11 -> draggingTexture = mouseDown(draggableTexture, mousePos());
                case F12 -> draggingText = mouseDown(draggableText, mousePos());
            }
        }
        if (mouseOnButtonUp().fired())
        {
            draggingPoint    = -1;
            draggingLine     = -1;
            draggingLines    = -1;
            draggingBezier   = -1;
            draggingTriangle = -1;
            draggingQuad     = -1;
            draggingRect     = -1;
            draggingEllipse  = -1;
            draggingRing     = -1;
            draggingTexture  = -1;
            draggingText     = -1;
        }
        if (mouseOnButtonDragged().fired())
        {
            switch (state)
            {
                case F1 -> mouseMoved(draggablePoint, draggingPoint, mousePos());
                case F2 -> mouseMoved(draggableLine, draggingLine, mousePos());
                case F3 -> mouseMoved(draggableLines, draggingLines, mousePos());
                case F5 -> mouseMoved(draggableBezier, draggingBezier, mousePos());
                case F6 -> mouseMoved(draggableTriangle, draggingTriangle, mousePos());
                case F7 -> mouseMoved(draggableQuad, draggingQuad, mousePos());
                case F8 -> mouseMoved(draggableRect, draggingRect, mousePos());
                case F9 -> mouseMoved(draggableEllipse, draggingEllipse, mousePos());
                case F10 -> mouseMoved(draggableRing, draggingRing, mousePos());
                case F11 -> mouseMoved(draggableTexture, draggingTexture, mousePos());
                case F12 -> mouseMoved(draggableText, draggingText, mousePos());
            }
        }
    }
    
    private int mouseDown(Vector2d @NotNull [] draggable, @NotNull Vector2dc pos)
    {
        for (int i = 0; i < draggable.length; i++)
        {
            double x = pos.x() - draggable[i].x;
            double y = pos.y() - draggable[i].y;
            
            if (Math.sqrt(x * x + y * y) < 10) return i;
        }
        return -1;
    }
    
    private void mouseMoved(Vector2d @NotNull [] draggable, int dragging, @NotNull Vector2dc pos)
    {
        if (dragging != -1)
        {
            //Debug.notification("Pos: " + pos);
            draggable[dragging].set(pos);
        }
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
        int width  = windowSize().x();
        int height = windowSize().y();
        
        clear(Color.BLACK);
        
        double thickness = MathUtil.map(Math.max(hValue, 1), 0, 100, 0, 10);
        
        switch (state)
        {
            case F1 ->
            {
                pointBatchBegin();
                int r, g, b, a;
                for (Vector2d p : draggablePoint)
                {
                    r = (int) (Math.random() * 255);
                    g = (int) (Math.random() * 255);
                    b = (int) (Math.random() * 255);
                    a = (int) (Math.random() * 255);
                    
                    pointSize(thickness);
                    pointColor(r, g, b, a);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F2 ->
            {
                lineBatchBegin();
                
                lineThicknessStart(thickness);
                lineThicknessEnd(5);
                lineColorStart(Color.BLANK);
                lineColorEnd(Color.GREEN);
                lineDraw(draggableLine[0].x, draggableLine[0].y, draggableLine[1].x, draggableLine[1].y);
                
                lineColorEnd(Color.RED);
                lineDraw(draggableLine[0].x, draggableLine[0].y, draggableLine[2].x, draggableLine[2].y);
                
                lineColorEnd(Color.BLUE);
                lineDraw(draggableLine[0].x, draggableLine[0].y, draggableLine[3].x, draggableLine[3].y);
                
                lineColorEnd(Color.WHITE);
                lineDraw(draggableLine[0].x, draggableLine[0].y, draggableLine[4].x, draggableLine[4].y);
                
                lineBatchEnd();
                
                pointBatchBegin();
                for (Vector2d p : draggableLine)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F3 ->
            {
                if (toggle)
                {
                    int steps = Math.max(vValue, 2);
                    
                    double[] points = new double[steps * 2];
                    
                    double x, y;
                    for (int i = 0; i < steps; i++)
                    {
                        x = (double) i / (steps - 1);
                        // y = 4.0 * x * (x - 1) + 1;
                        // y = x < 0.5 ? 4 * x * x * x : 1 - 4 * (1 - x) * (1 - x) * (1 - x);
                        y = 8 * (x) * (x - 0.5) * (x - 1) + 0.5;
                        
                        points[(i << 1)]     = x * width;
                        points[(i << 1) + 1] = y * height;
                    }
                    
                    lineThicknessStart(thickness);
                    lineThicknessEnd(5);
                    lineColorStart(Color.CYAN);
                    lineColorEnd(Color.MAGENTA);
                    lineDraw(points);
                }
                else
                {
                    double[] points = new double[draggableLines.length << 1];
                    for (int i = 0, index = 0; i < draggableLines.length; i++)
                    {
                        points[index++] = draggableLines[i].x;
                        points[index++] = draggableLines[i].y;
                    }
                    
                    lineThicknessStart(thickness);
                    lineThicknessEnd(5);
                    lineColorStart(Color.RED);
                    lineColorEnd(Color.GREEN);
                    lineDraw(points);
                    
                    pointBatchBegin();
                    for (Vector2d p : draggableLines)
                    {
                        pointSize(5);
                        pointColor(Color.WHITE);
                        pointDraw(p.x, p.y);
                    }
                    pointBatchEnd();
                }
            }
            case F4 ->
            {
                int steps = Math.max(vValue, 2);
                
                double[] points = new double[steps * 2];
                
                double angle, dist;
                for (int i = 0; i < steps; i++)
                {
                    angle = (i + 0.5) / steps * Math.PI * 2;
                    dist  = Math.cos(time + 9 * angle) * 25 + width / 5.0;
                    
                    points[(i << 1)]     = width / 2.0 + Math.cos(angle) * dist;
                    points[(i << 1) + 1] = height / 2.0 + Math.sin(angle) * dist;
                }
                
                lineThicknessStart(thickness);
                lineThicknessEnd(5);
                lineColorStart(Color.LIGHTEST_YELLOW);
                lineColorEnd(Color.DARKEST_YELLOW);
                lineDrawEnclosed(points);
            }
            case F5 ->
            {
                double[] points = new double[draggableBezier.length << 1];
                for (int i = 0, index = 0; i < draggableBezier.length; i++)
                {
                    points[index++] = draggableBezier[i].x;
                    points[index++] = draggableBezier[i].y;
                }
                
                lineThicknessStart(thickness);
                lineThicknessEnd(5);
                lineColorStart(Color.GREEN);
                lineColorEnd(Color.YELLOW);
                lineDrawBezier(points);
                
                pointBatchBegin();
                for (Vector2d p : draggableBezier)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F6 ->
            {
                double x0 = draggableTriangle[0].x, y0 = draggableTriangle[0].y;
                double x1 = draggableTriangle[1].x, y1 = draggableTriangle[1].y;
                double x2 = draggableTriangle[2].x, y2 = draggableTriangle[2].y;
                if (toggle)
                {
                    triangleColor(Color.YELLOW);
                    triangleDraw(x0, y0, x1, y1, x2, y2);
                }
                else
                {
                    triangleColor0(Color.RED);
                    triangleColor1(Color.GREEN);
                    triangleColor2(Color.BLUE);
                    triangleDraw(x0, y0, x1, y1, x2, y2);
                }
                
                lineColor(Color.GRAY);
                lineThickness(thickness);
                lineDraw(x0, y0, x1, y1, x2, y2);
                
                pointBatchBegin();
                for (Vector2d p : draggableTriangle)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F7 ->
            {
                double x0 = draggableQuad[0].x, y0 = draggableQuad[0].y;
                double x1 = draggableQuad[1].x, y1 = draggableQuad[1].y;
                double x2 = draggableQuad[2].x, y2 = draggableQuad[2].y;
                double x3 = draggableQuad[3].x, y3 = draggableQuad[3].y;
                if (toggle)
                {
                    quadColor(Color.YELLOW);
                    quadDraw(x0, y0, x1, y1, x2, y2, x3, y3);
                }
                else
                {
                    quadColor0(Color.RED);
                    quadColor1(Color.GREEN);
                    quadColor2(Color.BLUE);
                    quadColor3(Color.WHITE);
                    quadDraw(x0, y0, x1, y1, x2, y2, x3, y3);
                }
                lineColor(Color.GRAY);
                lineThickness(thickness);
                lineDraw(x0, y0, x1, y1, x2, y2, x3, y3);
                
                pointBatchBegin();
                for (Vector2d p : draggableQuad)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F8 ->
            {
                double cx = width * 0.5;
                double cy = height * 0.5;
                
                double w = (draggableRect[0].x - cx) * 2;
                double h = (draggableRect[0].y - cy) * 2;
                
                double cornerRadius = MathUtil.map(vValue, 0, 100, 0, 10);
                
                rectColor(Color.DARKER_RED);
                //rectCornerRadius(cornerRadius); // TODO
                rectDraw(cx, cy, w, h);
                
                if (toggle)
                {
                    rectColorTopLeft(Color.RED);
                    rectColorTopRight(Color.GREEN);
                    rectColorBottomLeft(Color.WHITE);
                    rectColorBottomRight(Color.BLUE);
                    //rectCornerRadius(cornerRadius); // TODO
                    rectDraw(cx, cy, w - thickness, h - thickness);
                }
                else
                {
                    rectColorGradientH(Color.BLUE, Color.LIGHT_GREEN);
                    //rectCornerRadius(cornerRadius); // TODO
                    rectDraw(cx, cy, w - thickness, h - thickness);
                }
                
                pointBatchBegin();
                for (Vector2d p : draggableRect)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            case F9 ->
            {
                double cx = width * 0.5;
                double cy = height * 0.5;
                
                double rx = (draggableEllipse[0].x - cx) * 2;
                double ry = (draggableEllipse[0].y - cy) * 2;
                
                //ellipseStartAngle(0.0); // TODO
                //ellipseStopAngle(Math.toRadians(vValue)); // TODO
                ellipseColor(Color.DARKER_BLUE);
                ellipseDraw(cx, cy, rx, ry);
                
                if (toggle)
                {
                    //ellipseStartAngle(0.0); // TODO
                    //ellipseStopAngle(Math.toRadians(vValue)); // TODO
                    ellipseColorInner(Color.GRAY);
                    ellipseColorOuter(Color.YELLOW);
                    ellipseDraw(cx, cy, rx - thickness, ry - thickness);
                }
                else
                {
                    //ellipseStartAngle(0.0); // TODO
                    //ellipseStopAngle(Math.toRadians(vValue)); // TODO
                    ellipseColor(Color.YELLOW);
                    ellipseDraw(cx, cy, rx - thickness, ry - thickness);
                }
                
                pointBatchBegin();
                for (Vector2d p : draggableEllipse)
                {
                    pointSize(5);
                    pointColor(Color.WHITE);
                    pointDraw(p.x, p.y);
                }
                pointBatchEnd();
            }
            //case F10 ->
            //{
            //    double cx = width * 0.5;
            //    double cy = height * 0.5;
            //
            //    double rxi = draggableRing[0].x - cx;
            //    double ryi = draggableRing[0].y - cy;
            //
            //    double rxo = draggableRing[1].x - cx;
            //    double ryo = draggableRing[1].y - cy;
            //
            //    double ox = draggableRing[2].x - cx;
            //    double oy = draggableRing[2].y - cy;
            //
            //    double rotation = Math.atan2(draggableRing[3].y - draggableRing[2].y, draggableRing[3].x - draggableRing[2].x);
            //
            //    if (toggle)
            //    {
            //        Draw.fillRing2D()
            //            .point(cx, cy)
            //            .innerRadius(rxi, ryi)
            //            .outerRadius(rxo, ryo)
            //            .rotationOrigin(ox, oy)
            //            .stopAngle(Math.toRadians(vValue))
            //            .rotationAngle(rotation)
            //            .startColor(Color.DARK_RED)
            //            .stopColor(Color.LIGHT_GREEN)
            //            .draw();
            //    }
            //    else
            //    {
            //        Draw.fillRing2D()
            //            .point(cx, cy)
            //            .innerRadius(rxi, ryi)
            //            .outerRadius(rxo, ryo)
            //            .rotationOrigin(ox, oy)
            //            .stopAngle(Math.toRadians(vValue))
            //            .rotationAngle(rotation)
            //            .innerColor(Color.DARK_RED)
            //            .outerColor(Color.LIGHT_GREEN)
            //            .draw();
            //    }
            //
            //    Draw.drawRing2D()
            //        .point(cx, cy)
            //        .innerRadius(rxi, ryi)
            //        .outerRadius(rxo, ryo)
            //        .thickness(thickness)
            //        .rotationOrigin(ox, oy)
            //        .stopAngle(Math.toRadians(vValue))
            //        .rotationAngle(rotation)
            //        .color(Color.DARKER_BLUE)
            //        .draw();
            //
            //    for (Vector2d point : draggableRing)
            //    {
            //        Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
            //    }
            //}
            //case F11 ->
            //{
            //    if (toggle)
            //    {
            //        double rotation = Math.toRadians(hValue);
            //
            //        double cx = width >> 1;
            //        double cy = height >> 1;
            //
            //        double mul = Math.map(vValue, 0, 1000, 0, 10);
            //
            //        double w = texture.width() * mul;
            //        double h = texture.height() * mul;
            //
            //        Draw.drawTexture2D().texture(texture).dst(cx, cy, w, h).rotationOrigin(w * 0.5, h * 0.5).rotationAngle(rotation).draw();
            //    }
            //    else
            //    {
            //        Draw.drawTextureWarped2D()
            //            .texture(texture)
            //            .point0(draggableTexture[0])
            //            .point1(draggableTexture[1])
            //            .point2(draggableTexture[2])
            //            .point3(draggableTexture[3])
            //            .draw();
            //
            //        for (Vector2d point : draggableTexture)
            //        {
            //            Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
            //        }
            //    }
            //}
            //case F12 -> drawText(thickness);
        }
        
        textSize(12);
        textAlign(TextAlign.TOP_LEFT);
        textColor(Color.DARK_GREEN);
        textDraw(String.format("Update: %.3f%nDraw:   %.3f%nhValue: %s%nvValue: %s", updateTimeActual(), drawTimeActual(), hValue, vValue), 0, 0);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Engine instance = new DemoRenderer();
        
        start(instance);
    }
}
