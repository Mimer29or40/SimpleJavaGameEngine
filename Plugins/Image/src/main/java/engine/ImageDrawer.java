package engine;

import engine.color.Color;
import engine.color.ColorBlend;
import engine.color.Colorc;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

public class ImageDrawer
{
    protected final Image image;
    
    public ImageDrawer(@NotNull Image image)
    {
        this.image = image;
    }
    
    // TODO - Create Rectangle/Shape2d Versions
    
    public void clear(Colorc color)
    {
        this.image.data.forEach(c -> c.set(color));
    }
    
    public void drawPixel(int x, int y, @NotNull Colorc color, @NotNull ColorBlend blendMode)
    {
        if (x < 0 || this.image.width <= x) return;
        if (y < 0 || this.image.height <= y) return;
        this.image.data.apply(y * this.image.width + x, c -> blendMode.blend(color, c, c));
    }
    
    public void drawPixel(int x, int y, Colorc color)
    {
        drawPixel(x, y, color, ColorBlend.NONE);
    }
    
    public void drawLine(int startX, int startY, int endX, int endY, Colorc color, ColorBlend blendMode)
    {
        int x, y, xe, ye, temp;
        int dx = endX - startX;
        int dy = endY - startY;
        
        if (dx == 0) // Line is vertical
        {
            if (endY < startY)
            {
                temp   = startY;
                startY = endY;
                endY   = temp;
            }
            for (y = startY; y <= endY; y++) drawPixel(startX, y, color, blendMode);
            return;
        }
        
        if (dy == 0) // Line is horizontal
        {
            if (endX < startX)
            {
                temp   = startX;
                startX = endX;
                endX   = temp;
            }
            for (x = startX; x <= endX; x++) drawPixel(x, startY, color, blendMode);
            return;
        }
        
        // Line is Funk-aye
        int dx1 = Math.abs(dx);
        int dy1 = Math.abs(dy);
        int px  = 2 * dy1 - dx1;
        int py  = 2 * dx1 - dy1;
        if (dy1 <= dx1)
        {
            if (dx >= 0)
            {
                x  = startX;
                y  = startY;
                xe = endX;
            }
            else
            {
                x  = endX;
                y  = endY;
                xe = startX;
            }
            
            drawPixel(x, y, color, blendMode);
            
            while (x < xe)
            {
                x = x + 1;
                if (px < 0)
                {
                    px = px + 2 * dy1;
                }
                else
                {
                    y  = dx < 0 && dy < 0 || dx > 0 && dy > 0 ? y + 1 : y - 1;
                    px = px + 2 * (dy1 - dx1);
                }
                drawPixel(x, y, color, blendMode);
            }
        }
        else
        {
            if (dy >= 0)
            {
                x  = startX;
                y  = startY;
                ye = endY;
            }
            else
            {
                x  = endX;
                y  = endY;
                ye = startY;
            }
            
            drawPixel(x, y, color, blendMode);
            
            while (y < ye)
            {
                y = y + 1;
                if (py <= 0)
                {
                    py = py + 2 * dx1;
                }
                else
                {
                    x  = dx < 0 && dy < 0 || dx > 0 && dy > 0 ? x + 1 : x - 1;
                    py = py + 2 * (dx1 - dy1);
                }
                drawPixel(x, y, color, blendMode);
            }
        }
    }
    
    public void drawLine(int startX, int startY, int endX, int endY, Colorc color)
    {
        drawLine(startX, startY, endX, endY, color, ColorBlend.NONE);
    }
    
    public void drawRectangle(int x, int y, int width, int height, int thickness, Colorc color, ColorBlend blendMode)
    {
        fillRectangle(x, y, width, thickness, color, blendMode);
        fillRectangle(x, y + thickness, thickness, height - thickness * 2, color, blendMode);
        fillRectangle(x + width - thickness, y + thickness, thickness, height - thickness * 2, color, blendMode);
        fillRectangle(x, y + height - thickness, width, thickness, color, blendMode);
    }
    
    public void drawRectangle(int x, int y, int width, int height, int thickness, Colorc color)
    {
        drawRectangle(x, y, width, height, thickness, color, ColorBlend.NONE);
    }
    
    public void fillRectangle(int x, int y, int width, int height, Colorc color, ColorBlend blendMode)
    {
        for (int j = y; j < y + height; j++)
        {
            for (int i = x; i < x + width; i++)
            {
                drawPixel(i, j, color, blendMode);
            }
        }
    }
    
    public void fillRectangle(int x, int y, int width, int height, Colorc color)
    {
        fillRectangle(x, y, width, height, color, ColorBlend.NONE);
    }
    
    public void drawCircle(int centerX, int centerY, int radius, Colorc color, ColorBlend blendMode)
    {
        if (radius < 0 || centerX < -radius || centerY < -radius || centerX - this.image.width > radius || centerY - this.image.height > radius) return;
        
        if (radius > 0)
        {
            int x0 = 0;
            int y0 = radius;
            int d  = 3 - 2 * radius;
            
            while (y0 >= x0) // only formulate 1/8 of circle
            {
                // DrawCall even octant's
                drawPixel(centerX + x0, centerY - y0, color, blendMode); // Q6 - upper -> right -> right
                drawPixel(centerX + y0, centerY + x0, color, blendMode); // Q4 - lower -> lower -> right
                drawPixel(centerX - x0, centerY + y0, color, blendMode); // Q2 - lower -> left -> left
                drawPixel(centerX - y0, centerY - x0, color, blendMode); // Q0 - upper -> upper -> left
                if (x0 != 0 && x0 != y0)
                {
                    drawPixel(centerX + y0, centerY - x0, color, blendMode); // Q7 - upper -> upper -> right
                    drawPixel(centerX + x0, centerY + y0, color, blendMode); // Q5 - lower -> right -> right
                    drawPixel(centerX - y0, centerY + x0, color, blendMode); // Q3 - lower -> lower -> left
                    drawPixel(centerX - x0, centerY - y0, color, blendMode); // Q1 - upper -> left -> left
                }
                
                d += d < 0 ? 4 * x0++ + 6 : 4 * (x0++ - y0--) + 10;
            }
        }
        else
        {
            drawPixel(centerX, centerY, color, blendMode);
        }
    }
    
    public void drawCircle(int centerX, int centerY, int radius, Colorc color)
    {
        drawCircle(centerX, centerY, radius, color, ColorBlend.NONE);
    }
    
    public void fillCircle(int centerX, int centerY, int radius, Colorc color, ColorBlend blendMode)
    {
        if (radius < 0 || centerX < -radius || centerY < -radius || centerX - this.image.width > radius || centerY - this.image.height > radius) return;
        
        if (radius > 0)
        {
            int x0 = 0;
            int y0 = radius;
            int d  = 3 - 2 * radius;
            
            while (y0 >= x0)
            {
                for (int x = centerX - y0; x <= centerX + y0; x++) drawPixel(x, centerY - x0, color, blendMode);
                if (x0 > 0) for (int x = centerX - y0; x <= centerX + y0; x++) drawPixel(x, centerY + x0, color, blendMode);
                
                if (d < 0)
                {
                    d += 4 * x0++ + 6;
                }
                else
                {
                    if (x0 != y0)
                    {
                        for (int x = centerX - x0; x <= centerX + x0; x++) drawPixel(x, centerY - y0, color, blendMode);
                        for (int x = centerX - x0; x <= centerX + x0; x++) drawPixel(x, centerY + y0, color, blendMode);
                    }
                    d += 4 * (x0++ - y0--) + 10;
                }
            }
        }
        else
        {
            drawPixel(centerX, centerY, color, blendMode);
        }
    }
    
    public void fillCircle(int centerX, int centerY, int radius, Colorc color)
    {
        fillCircle(centerX, centerY, radius, color, ColorBlend.NONE);
    }
    
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color, ColorBlend blendMode)
    {
        drawLine(x1, y1, x2, y2, color, blendMode);
        drawLine(x2, y2, x3, y3, color, blendMode);
        drawLine(x3, y3, x1, y1, color, blendMode);
    }
    
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color)
    {
        drawTriangle(x1, y1, x2, y2, x3, y3, color, ColorBlend.NONE);
    }
    
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color, ColorBlend blendMode)
    {
        int     t1x, t2x, y, minX, maxX, t1xp, t2xp, temp;
        boolean changed1 = false;
        boolean changed2 = false;
        int     signX1, signX2, dx1, dy1, dx2, dy2;
        int     e1, e2;
        // Sort vertices
        if (y1 > y2)
        {
            temp = y1;
            y1   = y2;
            y2   = temp;
            
            temp = x1;
            x1   = x2;
            x2   = temp;
        }
        if (y1 > y3)
        {
            temp = y1;
            y1   = y3;
            y3   = temp;
            
            temp = x1;
            x1   = x3;
            x3   = temp;
        }
        if (y2 > y3)
        {
            temp = y2;
            y2   = y3;
            y3   = temp;
            
            temp = x2;
            x2   = x3;
            x3   = temp;
        }
        
        t1x = t2x = x1;
        y   = y1; // Starting points
        dx1 = x2 - x1;
        if (dx1 < 0)
        {
            dx1    = -dx1;
            signX1 = -1;
        }
        else
        {
            signX1 = 1;
        }
        dy1 = y2 - y1;
        
        dx2 = x3 - x1;
        if (dx2 < 0)
        {
            dx2    = -dx2;
            signX2 = -1;
        }
        else
        {
            signX2 = 1;
        }
        dy2 = y3 - y1;
        
        if (dy1 > dx1)
        {
            temp     = dx1;
            dx1      = dy1;
            dy1      = temp;
            changed1 = true;
        }
        if (dy2 > dx2)
        {
            temp     = dx2;
            dx2      = dy2;
            dy2      = temp;
            changed2 = true;
        }
        
        e2 = dx2 >> 1;
        // Flat top, just process the second half
        if (y1 != y2)
        {
            e1 = dx1 >> 1;
            
            for (int i = 0; i < dx1; )
            {
                t1xp = 0;
                t2xp = 0;
                if (t1x < t2x)
                {
                    minX = t1x;
                    maxX = t2x;
                }
                else
                {
                    minX = t2x;
                    maxX = t1x;
                }
                // process first line until y value is about to change
                next1:
                while (i < dx1)
                {
                    i++;
                    e1 += dy1;
                    while (e1 >= dx1)
                    {
                        e1 -= dx1;
                        if (changed1)
                        {
                            t1xp = signX1; //t1x += signX1;
                        }
                        else
                        {
                            break next1;
                        }
                    }
                    if (changed1)
                    {
                        break;
                    }
                    else
                    {
                        t1x += signX1;
                    }
                }
                // Move line
                // process second line until y value is about to change
                next2:
                while (true)
                {
                    e2 += dy2;
                    while (e2 >= dx2)
                    {
                        e2 -= dx2;
                        if (changed2)
                        {
                            t2xp = signX2; // t2x += signX2;
                        }
                        else
                        {
                            break next2;
                        }
                    }
                    if (changed2)
                    {
                        break;
                    }
                    else
                    {
                        t2x += signX2;
                    }
                }
                if (minX > t1x) minX = t1x;
                if (minX > t2x) minX = t2x;
                if (maxX < t1x) maxX = t1x;
                if (maxX < t2x) maxX = t2x;
                for (int j = minX; j <= maxX; j++) drawPixel(j, y, color, blendMode); // DrawCall line from min to max points found on the y
                // Now increase y
                if (!changed1) t1x += signX1;
                t1x += t1xp;
                if (!changed2) t2x += signX2;
                t2x += t2xp;
                y += 1;
                if (y == y2) break;
            }
        }
        // Second half
        dx1 = x3 - x2;
        if (dx1 < 0)
        {
            dx1    = -dx1;
            signX1 = -1;
        }
        else
        {
            signX1 = 1;
        }
        dy1 = y3 - y2;
        t1x = x2;
        
        if (dy1 > dx1)
        {   // swap values
            temp     = dx1;
            dx1      = dy1;
            dy1      = temp;
            changed1 = true;
        }
        else
        {
            changed1 = false;
        }
        
        e1 = dx1 >> 1;
        
        for (int i = 0; i <= dx1; i++)
        {
            t1xp = 0;
            t2xp = 0;
            if (t1x < t2x)
            {
                minX = t1x;
                maxX = t2x;
            }
            else
            {
                minX = t2x;
                maxX = t1x;
            }
            // process first line until y value is about to change
            next3:
            while (i < dx1)
            {
                e1 += dy1;
                while (e1 >= dx1)
                {
                    e1 -= dx1;
                    if (changed1)
                    {
                        t1xp = signX1;//t1x += signX1;
                        break;
                    }
                    else
                    {
                        break next3;
                    }
                }
                if (changed1)
                {
                    break;
                }
                else
                {
                    t1x += signX1;
                }
                if (i < dx1) i++;
            }
            // process second line until y value is about to change
            next4:
            while (t2x != x3)
            {
                e2 += dy2;
                while (e2 >= dx2)
                {
                    e2 -= dx2;
                    if (changed2)
                    {
                        t2xp = signX2;
                    }
                    else
                    {
                        break next4;
                    }
                }
                if (changed2)
                {
                    break;
                }
                else
                {
                    t2x += signX2;
                }
            }
            
            if (minX > t1x) minX = t1x;
            if (minX > t2x) minX = t2x;
            if (maxX < t1x) maxX = t1x;
            if (maxX < t2x) maxX = t2x;
            for (int j = minX; j <= maxX; j++) drawPixel(j, y, color, blendMode);
            if (!changed1) t1x += signX1;
            t1x += t1xp;
            if (!changed2) t2x += signX2;
            t2x += t2xp;
            y += 1;
            if (y > y3) return;
        }
    }
    
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color)
    {
        fillTriangle(x1, y1, x2, y2, x3, y3, color, ColorBlend.NONE);
    }
    
    public void drawImage(@NotNull Image src, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH, ColorBlend blendMode)
    {
        Image   srcImage  = src;   // Pointer to source image
        boolean copiedSrc = false; // Track source copy required
        
        // Source rectangle out-of-bounds security checks
        srcImage.validateRect(srcX, srcY, srcW, srcH);
        
        // Check if source rectangle needs to be resized to destination rectangle
        // In that case, we make a copy of source and apply all required transform
        if (srcW != dstW || srcH != dstH)
        {
            srcImage = new MutableImage(src.copy()).crop(srcX, srcY, srcW, srcH).resize(dstW, dstH); // Create image from another image
            srcX     = 0;
            srcY     = 0;
            srcW     = srcImage.width;
            srcH     = srcImage.height;
            
            copiedSrc = true;
        }
        
        // Destination rectangle out-of-bounds security checks
        this.image.validateRect(dstX, dstY, dstW, dstH);
        
        // Fast path: Avoid blendMode if source has no alpha to blendMode
        boolean blendRequired = blendMode != ColorBlend.NONE && srcImage.format().alpha;
        
        if (!blendRequired && srcImage.format() == this.image.format())
        {
            int srcSizeof = srcImage.format().sizeof;
            int srcStride = srcImage.width * srcSizeof;
            
            int dstSizeof = this.image.format().sizeof;
            int dstStride = this.image.width * dstSizeof;
            
            long srcPtr = srcImage.data.address() + Integer.toUnsignedLong(srcY * srcImage.width + srcX) * srcSizeof;
            long dstPtr = this.image.data.address() + Integer.toUnsignedLong(dstY * this.image.width + dstX) * dstSizeof;
            
            for (int j = 0; j < srcH; j++)
            {
                MemoryUtil.memCopy(srcPtr, dstPtr, srcStride);
                
                srcPtr += srcStride;
                dstPtr += dstStride;
            }
        }
        else
        {
            Color srcColor = new Color();
            for (int j = 0; j < srcH; j++)
            {
                for (int i = 0; i < srcW; i++)
                {
                    srcImage.data.get((i + srcX) * srcImage.width + (j + srcY), srcColor);
                    this.image.data.apply((i + dstX) * this.image.width + (j + dstY), c -> blendMode.blend(srcColor, c, c));
                }
            }
        }
        
        if (copiedSrc) srcImage.delete(); // Unload source modified image
    }
    
    public void drawImage(@NotNull Image src, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH)
    {
        drawImage(src, srcX, srcY, srcW, srcH, dstX, dstY, dstW, dstH, ColorBlend.NONE);
    }
}
