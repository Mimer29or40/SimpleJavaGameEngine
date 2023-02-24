package stbtt;

import engine.util.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.libc.LibCString.memset;

public class SDFDemo
{
    static final boolean anti_aliases = false;
    
    static final class fontchar
    {
        float advance;
        int   xoff;
        int   yoff;
        int   w, h;
        ByteBuffer data;
    }
    
    // used both to compute SDF and in 'shader'
    static float sdf_size         = 32.F;          // the larger this is, the better large font sizes look
    static float pixel_dist_scale = 64.F;  // trades off precision w/ ability to handle *smaller* sizes
    static int   onedge_value     = 128;
    static int   padding          = 3; // not used in shader
    
    static fontchar[] fdata = new fontchar[128];
    
    public static final int        BITMAP_W = 1200;
    public static final int        BITMAP_H = 800;
    static              ByteBuffer bitmap   = MemoryUtil.memAlloc(BITMAP_H * BITMAP_W * 3);
    
    static String sample       = "This is goofy text, size %d!";
    static String small_sample = "This is goofy text, size %d! Really needs in-shader supersampling to look good.";
    
    static void blend_pixel(int x, int y, int color, float alpha)
    {
        int index = (y * BITMAP_W * 3) + (x * 3);
        for (int i = 0; i < 3; ++i)
        {
            bitmap.put(index + i, (byte) (stb_lerp(alpha, bitmap.get(index + i), (byte) color) + 0.5)); // round
        }
    }
    
    static void draw_char(float px, float py, char c, float relative_scale)
    {
        int      x, y;
        fontchar fc  = fdata[c];
        float    fx0 = px + fc.xoff * relative_scale;
        float    fy0 = py + fc.yoff * relative_scale;
        float    fx1 = fx0 + fc.w * relative_scale;
        float    fy1 = fy0 + fc.h * relative_scale;
        int      ix0 = (int) Math.floor(fx0);
        int      iy0 = (int) Math.floor(fy0);
        int      ix1 = (int) Math.ceil(fx1);
        int      iy1 = (int) Math.ceil(fy1);
        // clamp to viewport
        if (ix0 < 0) ix0 = 0;
        if (iy0 < 0) iy0 = 0;
        if (ix1 > BITMAP_W) ix1 = BITMAP_W;
        if (iy1 > BITMAP_H) iy1 = BITMAP_H;
        
        for (y = iy0; y < iy1; ++y)
        {
            for (x = ix0; x < ix1; ++x)
            {
                float sdf_dist, pix_dist;
                float bmx = stb_linear_remap(x, fx0, fx1, 0, fc.w);
                float bmy = stb_linear_remap(y, fy0, fy1, 0, fc.h);
                int   v00, v01, v10, v11;
                float v0, v1, v;
                int   sx0 = (int) bmx;
                int   sx1 = sx0 + 1;
                int   sy0 = (int) bmy;
                int   sy1 = sy0 + 1;
                // compute lerp weights
                bmx = bmx - sx0;
                bmy = bmy - sy0;
                // clamp to edge
                sx0 = (int) stb_clamp(sx0, 0, fc.w - 1);
                sx1 = (int) stb_clamp(sx1, 0, fc.w - 1);
                sy0 = (int) stb_clamp(sy0, 0, fc.h - 1);
                sy1 = (int) stb_clamp(sy1, 0, fc.h - 1);
                // bilinear texture sample
                v00 = fc.data.get(sy0 * fc.w + sx0) & 0xFF;
                v01 = fc.data.get(sy0 * fc.w + sx1) & 0xFF;
                v10 = fc.data.get(sy1 * fc.w + sx0) & 0xFF;
                v11 = fc.data.get(sy1 * fc.w + sx1) & 0xFF;
                v0  = stb_lerp(bmx, v00, v01);
                v1  = stb_lerp(bmx, v10, v11);
                v   = stb_lerp(bmy, v0, v1);
                if (anti_aliases)
                {
                    // Following math can be greatly simplified
                    
                    // convert distance in SDF value to distance in SDF bitmap
                    sdf_dist = stb_linear_remap(v, onedge_value, onedge_value + pixel_dist_scale, 0, 1);
                    // convert distance in SDF bitmap to distance in output bitmap
                    pix_dist = sdf_dist * relative_scale;
                    // anti-alias by mapping 1/2 pixel around contour from 0..1 alpha
                    v = stb_linear_remap(pix_dist, -0.5f, 0.5f, 0, 1);
                    if (v > 1) v = 1;
                    if (v > 0) blend_pixel(x, y, 0, v);
                }
                else
                {
                    // non-anti-aliased
                    if (v > onedge_value) blend_pixel(x, y, 0, 1.F);
                }
            }
        }
    }
    
    
    static void print_text(float x, float y, @NotNull String text, float scale)
    {
        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (fdata[c].data != null) draw_char(x, y, c, scale);
            x += fdata[c].advance * scale;
        }
    }
    
    static float stb_lerp(float t, float a, float b)
    {
        return a + t * (b - a);
    }
    
    static float stb_unlerp(float t, float a, float b)
    {
        return (t - a) / (b - a);
    }
    
    static float stb_linear_remap(float x, float x_min, float x_max, float out_min, float out_max)
    {
        return stb_lerp(stb_unlerp(x, x_min, x_max), out_min, out_max);
    }
    
    static float stb_clamp(float t, float a, float b)
    {
        return t < a ? a : Math.min(t, b);
    }
    
    public static void main(String[] args)
    {
        int           ch;
        float         scale, ypos;
        STBTTFontinfo font = STBTTFontinfo.malloc();
        ByteBuffer    data = IOUtil.readFromFile(Path.of("c:/windows/fonts/times.ttf"), new int[1], MemoryUtil::memAlloc);
        stbtt_InitFont(font, data, 0);
        
        scale = stbtt_ScaleForPixelHeight(font, sdf_size);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer xoff    = stack.mallocInt(1);
            IntBuffer yoff    = stack.mallocInt(1);
            IntBuffer w       = stack.mallocInt(1);
            IntBuffer h       = stack.mallocInt(1);
            IntBuffer advance = stack.mallocInt(1);
            IntBuffer bearing = stack.mallocInt(1);
            
            for (ch = 32; ch < 127; ++ch)
            {
                fontchar fc = new fontchar();
                fc.data = stbtt_GetCodepointSDF(font, scale, ch, padding, (byte) onedge_value, pixel_dist_scale, w, h, xoff, yoff);
                fc.xoff = xoff.get(0);
                fc.yoff = yoff.get(0);
                fc.w    = w.get(0);
                fc.h    = h.get(0);
                stbtt_GetCodepointHMetrics(font, ch, advance, bearing);
                fc.advance = advance.get(0) * scale;
                fdata[ch]  = fc;
            }
        }
        
        ypos = 60;
        memset(bitmap, 255);
        print_text(400, ypos + 30, String.format("sdf bitmap height %d", (int) sdf_size), 30 / sdf_size);
        ypos += 80;
        for (scale = 8.F; scale < 120.0; scale *= 1.33f)
        {
            print_text(80, ypos + scale, String.format(scale == 8.0 ? small_sample : sample, (int) scale), scale / sdf_size);
            ypos += scale * 1.05f + 20;
        }
        
        stbi_write_png("sdf_test.png", BITMAP_W, BITMAP_H, 3, bitmap, 0);
        
        font.free();
    }
}
