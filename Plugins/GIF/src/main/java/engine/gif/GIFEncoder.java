package engine.gif;

import engine.Image;
import engine.ImageDrawer;
import engine.ImageGenerator;
import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class GIFEncoder
{
    // -------------------- Per Gif State -------------------- //
    
    protected OutputStream stream;
    
    protected boolean started     = false;
    protected boolean shouldClose = false;
    protected boolean firstFrame  = true;
    
    protected int     width   = 0;
    protected int     height  = 0;
    protected boolean sizeSet = false;
    protected int     repeat  = 0;
    
    // -------------------- Per Frame State -------------------- //
    
    protected int dispose = -1;
    protected int quality = 10;
    
    protected int     transparentColor = -1;
    protected boolean transparentExact = false;
    protected int     backgroundColor  = 0;
    
    // -------------------- Per Gif Methods -------------------- //
    
    /**
     * Initiates GIF file creation on the given stream with the given size and
     * will repeat the given amount of times, or indefinitely. The stream is
     * not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     *
     * @return false if initial write failed.
     */
    public boolean start(@NotNull OutputStream stream, int width, int height, int repeat)
    {
        try
        {
            this.stream = stream;
            
            this.started     = true;
            this.shouldClose = false;
            
            writeString("GIF89a"); // header
            
            this.width   = width < 1 ? 256 : width;
            this.height  = height < 1 ? 256 : height;
            this.sizeSet = true;
            this.repeat  = Math.max(repeat, 0);
        }
        catch (IOException e)
        {
            this.started = false;
        }
        
        return this.started;
    }
    
    /**
     * Initiates GIF file creation on the given stream with the given size and
     * will repeat indefinitely. The stream is not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     *
     * @return false if initial write failed.
     */
    public boolean start(@NotNull OutputStream stream, int width, int height)
    {
        return start(stream, width, height, -1);
    }
    
    /**
     * Initiates GIF file creation on the given stream which will repeat the
     * given amount of times, or indefinitely. The size will be that of the
     * first frame. The stream is not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     *
     * @return false if initial write failed.
     */
    public boolean start(@NotNull OutputStream stream, int repeat)
    {
        boolean result = start(stream, -1, -1, repeat);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates GIF file creation on the given stream which will repeat
     * indefinitely. The size will be that of the first frame. The stream is
     * not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     *
     * @return false if initial write failed.
     */
    public boolean start(@NotNull OutputStream stream)
    {
        boolean result = start(stream, -1, -1, -1);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name with the given
     * size and will repeat the given amount of times, or indefinitely.
     *
     * @param file   String containing output file name.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     *
     * @return false if open or initial write failed.
     */
    public boolean start(@NotNull String file, int width, int height, int repeat)
    {
        try
        {
            boolean result = start(new BufferedOutputStream(new FileOutputStream(file)), width, height, repeat);
            this.shouldClose = true;
            return result;
        }
        catch (IOException ignored) {}
        return false;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name with the given
     * size and will repeat indefinitely.
     *
     * @param file   String containing output file name.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     *
     * @return false if open or initial write failed.
     */
    public boolean start(@NotNull String file, int width, int height)
    {
        return start(file, width, height, -1);
    }
    
    /**
     * Initiates writing of a GIF file with the specified name which will
     * repeat the given amount of times, or indefinitely. The size will be that
     * of the first frame.
     *
     * @param file   String containing output file name.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     *
     * @return false if open or initial write failed.
     */
    public boolean start(@NotNull String file, int repeat)
    {
        boolean result = start(file, -1, -1, repeat);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name which will
     * repeat indefinitely. The size will be that of the first frame.
     *
     * @param file String containing output file name.
     *
     * @return false if open or initial write failed.
     */
    public boolean start(@NotNull String file)
    {
        boolean result = start(file, -1, -1, -1);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Adds next GIF frame.  The frame is not written immediately, but is
     * actually deferred until the next frame is received so that timing
     * data can be inserted.  Invoking <code>finish()</code> flushes all
     * frames.  If <code>setSize</code> was not invoked, the size of the
     * first image is used for all subsequent frames.
     *
     * @param image Image to write.
     * @param delay The time, in milliseconds, before the succeeding frame will be shown
     *
     * @return true if successful.
     */
    public boolean addFrame(Image image, int delay)
    {
        if (image == null || !this.started) return false;
        
        try
        {
            if (!this.sizeSet)
            {
                // use first frame's size
                this.width   = image.width();
                this.height  = image.height();
                this.sizeSet = true;
            }
            
            // convert to correct format if necessary
            byte[] pixels = getImagePixels(image);
            
            // build color table & map pixels
            AnalyzeResults results = analyzePixels(pixels);
            
            int colorDepth = 8;
            int palletSize = 7;
            
            if (this.firstFrame)
            {
                // logical screen descriptior
                writeLSD(palletSize);
                
                // global color table
                writePalette(results.colorTable());
                
                // use NS app extension to indicate reps
                if (this.repeat >= 0) writeNetscapeExt();
            }
            
            // write graphic control extension
            writeGraphicCtrlExt(delay, results.transparentIndex());
            
            // image descriptor
            writeImageDesc(palletSize);
            
            // local color table
            if (!this.firstFrame) writePalette(results.colorTable());
            
            // encode and write pixel data
            LZW.encode(results.pixels(), colorDepth, this.stream);
            
            this.firstFrame = false;
            
            return true;
        }
        catch (IOException ignored) {}
        
        return false;
    }
    
    /**
     * Flushes any pending data and closes output file.
     * If writing to an OutputStream, the stream is not
     * closed.
     */
    public boolean finish()
    {
        if (!this.started) return false;
        
        boolean ok = true;
        try
        {
            // gif trailer
            this.stream.write(0x3B);
            this.stream.flush();
            if (this.shouldClose) this.stream.close();
        }
        catch (IOException e)
        {
            ok = false;
        }
        
        // reset for subsequent use
        this.stream = null;
        
        this.started     = false;
        this.shouldClose = false;
        this.firstFrame  = true;
        
        this.width   = 0;
        this.height  = 0;
        this.sizeSet = false;
        this.repeat  = 0;
        
        this.dispose = -1;
        this.quality = 10;
        
        this.transparentColor = -1;
        this.transparentExact = false;
        this.backgroundColor  = 0;
        
        return ok;
    }
    
    // -------------------- Per Frame Methods -------------------- //
    
    /**
     * Sets the GIF frame disposal code for the last added frame
     * and any subsequent frames.  Default is 0 if no transparent
     * color has been set, otherwise 2.
     *
     * @param code int disposal code.
     */
    public void disposalCode(int code)
    {
        this.dispose = Math.max(code, 0);
    }
    
    /**
     * Sets quality of color quantization (conversion of images
     * to the maximum 256 colors allowed by the GIF specification).
     * Lower values (minimum = 1) produce better colors, but slow
     * processing significantly.  10 is the default, and produces
     * good color mapping at reasonable speeds.  Values greater
     * than 20 do not yield significant improvements in speed.
     *
     * @param quality int greater than 0.
     */
    public void quality(int quality)
    {
        this.quality = Math.max(quality, 1);
    }
    
    /**
     * Sets the transparent color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the quantization
     * process, the color in the final palette for each frame closest to the
     * given color becomes the transparent color for that frame. If exactMatch
     * is set to true, transparent color index is search with exact match, and
     * not looking for the closest one.
     *
     * @param r     The r component of the color to be treated as transparent on display.
     * @param g     The g component of the color to be treated as transparent on display.
     * @param b     The b component of the color to be treated as transparent on display.
     * @param exact If only the exact color should be used.
     */
    public void transparentColor(int r, int g, int b, boolean exact)
    {
        this.transparentColor = toInt(r, g, b);
        this.transparentExact = exact;
    }
    
    /**
     * Sets the transparent color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the quantization
     * process, the color in the final palette for each frame closest to the
     * given color becomes the transparent color for that frame.
     *
     * @param r The r component of the color to be treated as transparent on display.
     * @param g The g component of the color to be treated as transparent on display.
     * @param b The b component of the color to be treated as transparent on display.
     */
    public void transparentColor(int r, int g, int b)
    {
        transparentColor(r, g, b, false);
    }
    
    /**
     * Removes the transparent color for the last added frame and any subsequent
     * frames.
     */
    public void removeTransparentColor()
    {
        this.transparentColor = -1;
    }
    
    /**
     * Sets the background color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the
     * quantization process, the color in the final palette for each frame
     * closest to the given color becomes the background color for that frame.
     *
     * @param r The r component of the color to be treated as background on display.
     * @param g The g component of the color to be treated as background on display.
     * @param b The b component of the color to be treated as background on display.
     */
    public void backgroundColor(int r, int g, int b)
    {
        this.backgroundColor = toInt(r, g, b);
    }
    
    /**
     * Resets the background color to black for the last added frame and any
     * subsequent frames.
     */
    public void removeBackgroundColor()
    {
        this.backgroundColor = 0;
    }
    
    /**
     * Extracts image pixels into byte array "pixels"
     */
    protected byte @NotNull [] getImagePixels(@NotNull Image image)
    {
        int w = image.width();
        int h = image.height();
        
        ColorFormat type = image.format();
        
        ColorBuffer data   = Objects.requireNonNull(image.data());
        boolean     delete = false;
        if (w != this.width || h != this.height || type != ColorFormat.RGB)
        {
            // create new image with right size/format
            Color background = new Color();
            background.setFromInt(this.backgroundColor);
            
            ImageGenerator generator = ImageGenerator.colorSolid(background);
            Image          temp      = generator.generate(ColorFormat.RGB, this.width, this.height);
            ImageDrawer    drawer    = new ImageDrawer(temp);
            drawer.drawImage(image, 0, 0, w, h, 0, 0, this.width, this.height);
            data = Objects.requireNonNull(temp.data()).copy();
            temp.delete();
            delete = true;
        }
        
        byte[] pixels = new byte[data.remaining() * data.sizeof()];
        for (int i = 0; data.hasRemaining(); )
        {
            Color color = data.get();
            pixels[i++] = (byte) color.b();
            pixels[i++] = (byte) color.g();
            pixels[i++] = (byte) color.r();
        }
        if (delete) data.free();
        
        return pixels;
    }
    
    /**
     * Analyzes image colors and creates color map.
     */
    protected @NotNull AnalyzeResults analyzePixels(byte @NotNull [] rawPixels)
    {
        int          len    = rawPixels.length;
        int          nPix   = len / 3;
        final byte[] pixels = new byte[nPix];
        
        // initialize quantizer
        final byte[]    colorTable = NeuQuantize.quantize(rawPixels, this.quality); // create reduced palette
        final boolean[] usedEntry  = new boolean[256];
        // convert map from BGR to RGB
        for (int i = 0; i < colorTable.length; i += 3)
        {
            byte temp = colorTable[i];
            colorTable[i]     = colorTable[i + 2];
            colorTable[i + 2] = temp;
            usedEntry[i / 3]  = false;
        }
        // map image pixels to new palette
        int k = 0;
        for (int i = 0; i < nPix; i++)
        {
            int index = NeuQuantize.map(rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF);
            pixels[i]        = (byte) index;
            usedEntry[index] = true;
        }
        // get the closest match to transparent color if specified
        final int transparentIndex;
        if (this.transparentColor > 0)
        {
            if (this.transparentExact)
            {
                transparentIndex = findExact(this.transparentColor, colorTable, usedEntry);
            }
            else
            {
                transparentIndex = findClosest(this.transparentColor, colorTable, usedEntry);
            }
        }
        else
        {
            transparentIndex = 0;
        }
        return new AnalyzeResults()
        {
            @Override
            public byte[] pixels()
            {
                return pixels;
            }
            
            @Override
            public byte[] colorTable()
            {
                return colorTable;
            }
            
            @Override
            public int transparentIndex()
            {
                return transparentIndex;
            }
        };
    }
    
    // -------------------- Custom Write Methods -------------------- //
    
    /**
     * Writes Graphic Control Extension
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeGraphicCtrlExt(int delay, int transIndex) throws IOException
    {
        // extension introducer
        this.stream.write(0x21);
        
        // GCE label
        this.stream.write(0xF9);
        
        // data block size
        this.stream.write(4);
        
        int transparency, dispose;
        if (this.transparentColor < 0)
        {
            transparency = 0;
            dispose      = 0; // dispose = no action
        }
        else
        {
            transparency = 1;
            dispose      = 2; // force clear if using transparent color
        }
        
        // user override
        if (this.dispose >= 0) dispose = this.dispose & 7;
        
        // packed fields
        this.stream.write((((0 & 0b0111) << 5) |       // 1:3 reserved
                           ((dispose & 0b0111) << 2) | // 4:6 disposal
                           ((0 & 0b0001) << 1) |       // 7   user input - 0 = none
                           (transparency & 0b0001)));  // 8   transparency flag
        
        writeShort(delay / 10); // delay x 1/100 sec
        this.stream.write(transIndex); // transparent color index
        this.stream.write(0); // block terminator
    }
    
    /**
     * Writes Image Descriptor
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeImageDesc(int palletSize) throws IOException
    {
        // image separator
        this.stream.write(0x2C);
        
        // image position x,y = 0,0
        writeShort(0);
        writeShort(0);
        
        // image size
        writeShort(this.width);
        writeShort(this.height);
        
        // packed fields
        if (this.firstFrame)
        {
            // no LCT  - GCT is used for first (or only) frame
            this.stream.write(0);
        }
        else
        {
            // specify normal LCT
            this.stream.write((((1 & 0b0001) << 7) |    // 1 local color table  1=yes
                               ((0 & 0b0001) << 6) |    // 2 interlace - 0=no
                               ((0 & 0b0001) << 5) |    // 3 sorted - 0=no
                               ((0 & 0b0011) << 3) |    // 4-5 reserved
                               (palletSize & 0b0111))); // 6-8 size of color table
        }
    }
    
    /**
     * Writes Logical Screen Descriptor
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeLSD(int palletSize) throws IOException
    {
        // logical screen size
        writeShort(this.width);
        writeShort(this.height);
        
        // packed fields
        this.stream.write((((1 & 0b0001) << 7) |    // 1  : global color table flag = 1 (gct used)
                           ((7 & 0b0111) << 4) |    // 2-4: color resolution = 7
                           ((0 & 0b0001) << 3) |    // 5  : gct sort flag = 0
                           (palletSize & 0b0111))); // 6-8: gct size
        
        // background color index
        this.stream.write(0);
        
        // pixel aspect ratio - assume 1:1
        this.stream.write(0);
    }
    
    /**
     * Writes Netscape application extension to define
     * repeat count.
     */
    protected void writeNetscapeExt() throws IOException
    {
        // extension introducer
        this.stream.write(0x21);
        
        // app extension label
        this.stream.write(0xFF);
        
        // block size
        this.stream.write(11);
        
        // app id + auth code
        writeString("NETSCAPE" + "2.0");
        
        // sub-block size
        this.stream.write(3);
        
        // loop sub-block id
        this.stream.write(1);
        
        // loop count (extra iterations, 0=repeat forever)
        writeShort(this.repeat);
        
        // block terminator
        this.stream.write(0);
    }
    
    /**
     * Writes color table
     */
    protected void writePalette(byte[] colorTab) throws IOException
    {
        this.stream.write(colorTab);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) this.stream.write(0);
    }
    
    /**
     * Write 16-bit value to output stream, LSB first
     */
    protected void writeShort(int value) throws IOException
    {
        this.stream.write(value & 0xFF);
        this.stream.write((value >> 8) & 0xFF);
    }
    
    /**
     * Writes string to output stream
     */
    protected void writeString(@NotNull String s) throws IOException
    {
        for (int i = 0; i < s.length(); i++) this.stream.write((byte) s.charAt(i));
    }
    
    // -------------------- Utility Methods -------------------- //
    
    /**
     * Returns index of palette exactly matching to color c or -1 if there is no exact matching.
     */
    protected static int findExact(int color, byte[] colorTable, boolean[] usedEntry)
    {
        if (colorTable == null || usedEntry == null) return -1;
        
        int[] c = fromInt(color);
        
        int r = c[0];
        int g = c[1];
        int b = c[2];
        for (int i = 0, n = colorTable.length / 3; i < n; ++i)
        {
            int index = i * 3;
            // If the entry is used in colorTab, then check if it is the same exact color we're looking for
            if (usedEntry[i] && r == (colorTable[index] & 0xFF) && g == (colorTable[index + 1] & 0xFF) && b == (colorTable[index + 2] & 0xFF))
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns index of palette color closest to c
     */
    protected static int findClosest(int color, byte[] colorTable, boolean[] usedEntry)
    {
        if (colorTable == null) return -1;
        
        int[] c = fromInt(color);
        
        int r = c[0];
        int g = c[1];
        int b = c[2];
        
        int minpos = 0;
        int dmin   = Integer.MAX_VALUE;
        for (int i = 0, n = colorTable.length / 3; i < n; ++i)
        {
            int index = i * 3;
            
            int dr = r - (colorTable[index] & 0xFF);
            int dg = g - (colorTable[index + 1] & 0xFF);
            int db = b - (colorTable[index + 2] & 0xFF);
            int d  = dr * dr + dg * dg + db * db;
            
            if (usedEntry[i] && (d < dmin))
            {
                dmin   = d;
                minpos = i;
            }
        }
        return minpos;
    }
    
    protected static int toInt(int r, int g, int b)
    {
        return ((r << 16) & 0xFF) | ((g << 8) & 0xFF) | ((b) & 0xFF);
    }
    
    protected static int[] fromInt(int c)
    {
        return new int[] {(c >> 16) & 0xFF, (c >> 8) & 0xFF, (c) & 0xFF};
    }
}
