package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.color.Colorc;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8;

public class MutableImage extends Image
{
    // -------------------- Instance -------------------- //
    
    public MutableImage(@NotNull Image image)
    {
        super(image.data.copy(), image.width, image.height);
    }
    
    public MutableImage(@NotNull ColorFormat format, int width, int height)
    {
        super(format, width, height);
    }
    
    public MutableImage(@NotNull String filePath)
    {
        super(filePath);
    }
    
    public @NotNull MutableImage reformat(@NotNull ColorFormat format)
    {
        if (format == ColorFormat.UNKNOWN) throw new UnsupportedOperationException("Invalid format: " + format);
        
        if (format == this.data.format) return this;
        
        ColorBuffer newData = ColorBuffer.malloc(format, this.width * this.height);
        
        Color color = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, color);
            newData.put(i, color);
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    public @NotNull MutableImage resize(int width, int height)
    {
        if (width == this.width && height == this.height) return this;
        
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, width * height);
        
        ByteBuffer src = MemoryUtil.memByteBuffer(this.data);
        ByteBuffer dst = MemoryUtil.memByteBuffer(newData);
        
        stbir_resize_uint8(src, this.width, this.height, 0, dst, width, height, 0, this.data.format.sizeof);
        
        MemoryUtil.memFree(this.data);
        this.data   = newData;
        this.width  = width;
        this.height = height;
        
        return this;
    }
    
    public @NotNull MutableImage resizeNN(int width, int height)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, width * height);
        
        // EDIT: added +1 to account for an early rounding problem
        int xRatio = (this.width << 16) / width + 1;
        int yRatio = (this.height << 16) / height + 1;
        
        long srcPtr = this.data.address();
        long dstPtr = newData.address();
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int x2 = x * xRatio >> 16;
                int y2 = y * yRatio >> 16;
                
                long src = Integer.toUnsignedLong(y2 * this.width + x2) * this.data.format.sizeof;
                long dst = Integer.toUnsignedLong(y * width + x) * this.data.format.sizeof;
                
                MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, this.data.format.sizeof);
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data   = newData;
        this.width  = width;
        this.height = height;
        
        return this;
    }
    
    public @NotNull MutableImage toPOT()
    {
        // Calculate next power-of-two values
        // NOTE: Just add the required amount of pixels at the right and bottom sides of image...
        int potWidth  = (int) Math.pow(2, Math.ceil(Math.log(this.width) / Math.log(2)));
        int potHeight = (int) Math.pow(2, Math.ceil(Math.log(this.height) / Math.log(2)));
        
        return resize(potWidth, potHeight);
    }
    
    /**
     * Crop an image to area defined by a rectangle
     * <p>
     * Mipmap data will be lost.
     *
     * @param x      The offset x coordinate of the sub-region
     * @param y      The offset y coordinate of the sub-region
     * @param width  The width of the sub-region
     * @param height The height of the sub-region
     *
     * @return this
     *
     * @throws IllegalArgumentException if rectangle exceeds image bounds
     */
    @NotNull
    public MutableImage crop(int x, int y, int width, int height)
    {
        validateRect(x, y, width, height);
        
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, width * height);
        
        long srcPtr = this.data.address();
        long dstPtr = newData.address();
        
        long bytesPerLine = Integer.toUnsignedLong(width * this.data.format.sizeof);
        for (int j = 0; j < height; j++)
        {
            int srcIdx = (j + y) * this.width + x;
            int dstIdx = j * width;
            
            long src = srcPtr + Integer.toUnsignedLong(srcIdx * this.data.format.sizeof);
            long dst = dstPtr + Integer.toUnsignedLong(dstIdx * this.data.format.sizeof);
            
            MemoryUtil.memCopy(src, dst, bytesPerLine);
        }
        
        MemoryUtil.memFree(this.data);
        this.data   = newData;
        this.width  = width;
        this.height = height;
        
        return this;
    }
    
    /**
     * Quantize the image by decreasing the bits per pixels to the values
     * specified.
     *
     * @param rBpp The number of bits in the {@code r} channel [0-8]
     * @param gBpp The number of bits in the {@code g} channel [0-8]
     * @param bBpp The number of bits in the {@code b} channel [0-8]
     * @param aBpp The number of bits in the {@code a} channel [0-8]
     *
     * @return This
     */
    @NotNull
    public MutableImage quantize(int rBpp, int gBpp, int bBpp, int aBpp)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        int rs = (1 << rBpp) - 1;
        int gs = (1 << gBpp) - 1;
        int bs = (1 << bBpp) - 1;
        int as = (1 << aBpp) - 1;
        
        Color color = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, color);
            
            // This "CAN" be simplified, but we need the precision to be lost.
            color.r(rs > 0 ? ((((color.r() + (127 / rs)) * rs) / 255) * 255) / rs : 0);
            color.g(gs > 0 ? ((((color.g() + (127 / gs)) * gs) / 255) * 255) / gs : 0);
            color.b(bs > 0 ? ((((color.b() + (127 / bs)) * bs) / 255) * 255) / bs : 0);
            color.a(as > 0 ? ((((color.a() + (127 / as)) * as) / 255) * 255) / as : 255);
            
            newData.put(i, color);
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Quantize the image to only 256 colors.
     *
     * @param sampleFactor Sampling Factor {@code [1..30]}
     *
     * @return This
     */
    @NotNull
    public MutableImage neuQuantize(int sampleFactor)
    {
        int sizeof = this.data.format.sizeof;
        
        final int NET_SIZE          = 256;
        final int PRIME1            = 499;
        final int PRIME2            = 491;
        final int PRIME3            = 487;
        final int PRIME4            = 503;
        final int MIN_PICTURE_BYTES = sizeof * PRIME4;
        final int MAX_NET_POS       = NET_SIZE - 1;
        final int NET_BIAS_SHIFT    = 4;
        final int N_CYCLES          = 100;
        final int INT_BIAS_SHIFT    = 16;
        final int INT_BIAS          = 1 << INT_BIAS_SHIFT;
        final int GAMMA_SHIFT       = 10;
        //final int GAMMA                = 1 << GAMMA_SHIFT;
        final int BETA_SHIFT           = 10;
        final int BETA                 = INT_BIAS >> BETA_SHIFT;
        final int BETA_GAMMA           = INT_BIAS << (GAMMA_SHIFT - BETA_SHIFT);
        final int INIT_RAD             = NET_SIZE >> 3;
        final int RADIUS_BIAS_SHIFT    = 6;
        final int RADIUS_BIAS          = 1 << RADIUS_BIAS_SHIFT;
        final int INIT_RADIUS          = INIT_RAD * RADIUS_BIAS;
        final int RADIUS_DEC           = 30;
        final int ALPHA_BIAS_SHIFT     = 10;
        final int INIT_ALPHA           = 1 << ALPHA_BIAS_SHIFT;
        final int RAD_BIAS_SHIFT       = 8;
        final int RAD_BIAS             = 1 << RAD_BIAS_SHIFT;
        final int ALPHA_RAD_BIAS_SHIFT = ALPHA_BIAS_SHIFT + RAD_BIAS_SHIFT;
        final int ALPHA_RAD_BIAS       = 1 << ALPHA_RAD_BIAS_SHIFT;
        
        int dataSize = this.data.capacity() * sizeof;
        
        sampleFactor = dataSize < MIN_PICTURE_BYTES ? 1 : sampleFactor;
        
        int[][] network  = new int[NET_SIZE][];
        int[]   netIndex = new int[256];
        int[]   bias     = new int[NET_SIZE];
        int[]   freq     = new int[NET_SIZE];
        int[]   radPower = new int[INIT_RAD];
        
        int alphaDec = 30 + ((sampleFactor - 1) / sizeof);
        
        for (int i = 0; i < NET_SIZE; i++)
        {
            int initial = (i << (NET_BIAS_SHIFT + 8)) / NET_SIZE;
            
            network[i] = new int[] {initial, initial, initial, initial, 0};
        }
        Arrays.fill(netIndex, 0);
        Arrays.fill(bias, 0);
        Arrays.fill(freq, INT_BIAS / NET_SIZE); // 1/NET_SIZE
        Arrays.fill(radPower, 0);
        
        int samplePixels = dataSize / (sizeof * sampleFactor);
        int alpha        = INIT_ALPHA;
        int radius       = INIT_RADIUS;
        int rad          = radius >> RADIUS_BIAS_SHIFT;
        
        // if (rad <= 1) rad = 0;
        
        for (int i = 0, rad2 = rad * rad; i < rad; i++) radPower[i] = alpha * (((rad2 - i * i) * RAD_BIAS) / rad2);
        
        int step;
        if (dataSize < MIN_PICTURE_BYTES)
        {
            step = 1;
        }
        else if ((dataSize % PRIME1) != 0)
        {
            step = PRIME1;
        }
        else if ((dataSize % PRIME2) != 0)
        {
            step = PRIME2;
        }
        else if ((dataSize % PRIME3) != 0)
        {
            step = PRIME3;
        }
        else
        {
            step = PRIME4;
        }
    
        Color color = new Color();
        
        int r, g, b, a;
        int delta = samplePixels / N_CYCLES;
        if (delta == 0) delta = 1;
        for (int i = 0, pix = 0; i < samplePixels; )
        {
            this.data.get(pix, color);
    
            r = color.r() << NET_BIAS_SHIFT;
            g = color.g() << NET_BIAS_SHIFT;
            b = color.b() << NET_BIAS_SHIFT;
            a = color.a() << NET_BIAS_SHIFT;
            
            int bestDist     = Integer.MAX_VALUE;
            int bestBiasDist = Integer.MAX_VALUE;
            int bestPos      = -1;
            int bestBiasPos  = -1;
            
            for (int j = 0; j < NET_SIZE; j++)
            {
                int[] n = network[j];
                
                int dist = Math.abs(n[0] - r) + Math.abs(n[1] - g) + Math.abs(n[2] - b) + Math.abs(n[3] - a);
                
                if (dist < bestDist)
                {
                    bestDist = dist;
                    bestPos  = j;
                }
                
                int biasDist = dist - (bias[j] >> (INT_BIAS_SHIFT - NET_BIAS_SHIFT));
                if (biasDist < bestBiasDist)
                {
                    bestBiasDist = biasDist;
                    bestBiasPos  = j;
                }
                
                int betaFreq = freq[j] >> BETA_SHIFT;
                bias[j] += betaFreq << GAMMA_SHIFT;
                freq[j] -= betaFreq;
            }
            bias[bestPos] -= BETA_GAMMA;
            freq[bestPos] += BETA;
            
            // alter hit neuron
            int[] n = network[bestBiasPos];
            n[0] -= (alpha * (n[0] - r)) / INIT_ALPHA;
            n[1] -= (alpha * (n[1] - g)) / INIT_ALPHA;
            n[2] -= (alpha * (n[2] - b)) / INIT_ALPHA;
            n[3] -= (alpha * (n[3] - a)) / INIT_ALPHA;
            if (rad != 0)
            {
                // alter neighbours
                int lo = Math.max(bestBiasPos - rad, -1);
                int hi = Math.min(bestBiasPos + rad, NET_SIZE);
                
                int j = bestBiasPos + 1;
                int k = bestBiasPos - 1;
                int m = 1;
                while (j < hi || k > lo)
                {
                    int radP = radPower[m++];
                    if (j < hi)
                    {
                        int[] p = network[j++];
                        try
                        {
                            p[0] -= (radP * (p[0] - r)) / ALPHA_RAD_BIAS;
                            p[1] -= (radP * (p[1] - g)) / ALPHA_RAD_BIAS;
                            p[2] -= (radP * (p[2] - b)) / ALPHA_RAD_BIAS;
                            p[3] -= (radP * (p[3] - a)) / ALPHA_RAD_BIAS;
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                    if (k > lo)
                    {
                        int[] p = network[k--];
                        try
                        {
                            p[0] -= (radP * (p[0] - r)) / ALPHA_RAD_BIAS;
                            p[1] -= (radP * (p[1] - g)) / ALPHA_RAD_BIAS;
                            p[2] -= (radP * (p[2] - b)) / ALPHA_RAD_BIAS;
                            p[3] -= (radP * (p[3] - a)) / ALPHA_RAD_BIAS;
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
            
            pix += step;
            if (pix >= this.width * this.height) pix -= this.width * this.height;
            
            i++;
            
            if (i % delta == 0)
            {
                alpha -= alpha / alphaDec;
                radius -= radius / RADIUS_DEC;
                rad = radius >> RADIUS_BIAS_SHIFT;
                if (rad <= 1) rad = 0;
                for (bestBiasPos = 0; bestBiasPos < rad; bestBiasPos++)
                {
                    radPower[bestBiasPos] = alpha * (((rad * rad - bestBiasPos * bestBiasPos) * RAD_BIAS) / (rad * rad));
                }
            }
        }
        
        // Unbias network to give byte values 0..255 and record position i to prepare for sort
        for (int i = 0; i < NET_SIZE; i++)
        {
            network[i][0] >>= NET_BIAS_SHIFT;
            network[i][1] >>= NET_BIAS_SHIFT;
            network[i][2] >>= NET_BIAS_SHIFT;
            network[i][3] >>= NET_BIAS_SHIFT;
            network[i][4] =   i; /* record color no */
        }
        
        int prevCol  = 0;
        int startPos = 0;
        for (int i = 0; i < NET_SIZE; i++)
        {
            int[] p        = network[i];
            int   smallPos = i;
            int   smallVal = p[1]; /* index on g */
            /* find smallest in [i..NET_SIZE-1] */
            for (int j = i + 1; j < NET_SIZE; j++)
            {
                int[] q = network[j];
                if (q[1] < smallVal)
                { /* index on g */
                    smallPos = j;
                    smallVal = q[1]; /* index on g */
                }
            }
            int[] q = network[smallPos];
            /* swap p(i) and q(smallPos) entries */
            if (i != smallPos)
            {
                int temp;
                
                temp = q[0];
                q[0] = p[0];
                p[0] = temp;
                temp = q[1];
                q[1] = p[1];
                p[1] = temp;
                temp = q[2];
                q[2] = p[2];
                p[2] = temp;
                temp = q[3];
                q[3] = p[3];
                p[3] = temp;
            }
            /* smallVal entry is now in position i */
            if (smallVal != prevCol)
            {
                netIndex[prevCol] = (startPos + i) >> 1;
                for (int j = prevCol + 1; j < smallVal; j++) netIndex[j] = i;
                prevCol  = smallVal;
                startPos = i;
            }
        }
        
        netIndex[prevCol] = (startPos + MAX_NET_POS) >> 1;
        Arrays.fill(netIndex, prevCol + 1, netIndex.length, MAX_NET_POS); // really 256
        
        int[] index = new int[NET_SIZE];
        for (int i = 0; i < NET_SIZE; i++) index[network[i][4]] = i;
        
        byte[] colorTable = new byte[sizeof * NET_SIZE];
        for (int i = 0, k = 0; i < NET_SIZE; i++)
        {
            int j = index[i];
            colorTable[k++] = (byte) network[j][0];
            colorTable[k++] = (byte) network[j][1];
            colorTable[k++] = (byte) network[j][2];
            colorTable[k++] = (byte) network[j][3];
        }
        
        ColorBuffer newData = ColorBuffer.malloc(this.width * this.height * this.data.format.sizeof);
        for (int idx = 0, n = this.width * this.height; idx < n; idx++)
        {
            this.data.get(idx, color);
            
            int bestDist = 1000; /* biggest possible dist is 256*3 */
            int best     = -1;
            
            // i: index on g
            // j: start at netIndex[g] and work outwards
            for (int i = netIndex[color.g()], j = i - 1; i < NET_SIZE || j >= 0; )
            {
                if (i < NET_SIZE)
                {
                    int[] p    = network[i];
                    int   dist = p[1] - color.g(); /* inx key */
                    if (dist >= bestDist)
                    {
                        i = NET_SIZE; /* stop iter */
                    }
                    else
                    {
                        i++;
                        dist = Math.abs(dist) + Math.abs(p[0] - color.r());
                        if (dist < bestDist)
                        {
                            dist += Math.abs(p[2] - color.b());
                            if (dist < bestDist)
                            {
                                dist += Math.abs(p[3] - color.a());
                                if (dist < bestDist)
                                {
                                    bestDist = dist;
                                    best     = p[4];
                                }
                            }
                        }
                    }
                }
                if (j >= 0)
                {
                    int[] p    = network[j];
                    int   dist = color.g() - p[1]; /* inx key - reverse dif */
                    if (dist >= bestDist)
                    {
                        j = -1; /* stop iter */
                    }
                    else
                    {
                        j--;
                        dist = Math.abs(dist) + Math.abs(p[0] - color.r());
                        if (dist < bestDist)
                        {
                            dist += Math.abs(p[2] - color.b());
                            if (dist < bestDist)
                            {
                                dist += Math.abs(p[3] - color.a());
                                if (dist < bestDist)
                                {
                                    bestDist = dist;
                                    best     = p[4];
                                }
                            }
                        }
                    }
                }
            }
            
            color.r(colorTable[(4 * best)] & 0xFF);
            color.g(colorTable[(4 * best) + 1] & 0xFF);
            color.b(colorTable[(4 * best) + 2] & 0xFF);
            color.a(colorTable[(4 * best) + 3] & 0xFF);
            
            newData.put(idx, color);
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Dither image data (Floyd-Steinberg dithering)
     * <p>
     * Mipmap data will be lost.
     *
     * @param rBpp The number of bits in the {@code r} channel
     * @param gBpp The number of bits in the {@code g} channel
     * @param bBpp The number of bits in the {@code b} channel
     * @param aBpp The number of bits in the {@code a} channel
     *
     * @return This
     */
    @NotNull
    public MutableImage dither(int rBpp, int gBpp, int bBpp, int aBpp)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.width * this.height * this.data.format.sizeof);
        //ColorBuffer copy    = this.data.copy();
        
        Color color = new Color();
        
        int index;
        int rErr, gErr, bErr, aErr;
        int rNew, gNew, bNew, aNew;
        
        int rs = (1 << rBpp) - 1;
        int gs = (1 << gBpp) - 1;
        int bs = (1 << bBpp) - 1;
        int as = (1 << aBpp) - 1;
        
        for (int y = 0; y < this.height; y++)
        {
            for (int x = 0; x < this.width; x++)
            {
                index = y * this.width + x;
                this.data.get(index, color);
                
                // NOTE: New pixel obtained by bits truncate, it would be better to round values (check ImageFormat())
                rNew = rs > 0 ? ((((color.r() + (127 / rs)) * rs) / 255) * 255) / rs : 0;
                gNew = gs > 0 ? ((((color.g() + (127 / gs)) * gs) / 255) * 255) / gs : 0;
                bNew = bs > 0 ? ((((color.b() + (127 / bs)) * bs) / 255) * 255) / bs : 0;
                aNew = as > 0 ? ((((color.a() + (127 / as)) * as) / 255) * 255) / as : 255;
                
                // NOTE: Error must be computed between new and old pixel but using same number of bits!
                // We want to know how much color precision we have lost...
                rErr = color.r() - rNew;
                gErr = color.g() - gNew;
                bErr = color.b() - bNew;
                aErr = color.a() - aNew;
                
                newData.put(index, rNew, gNew, bNew, aNew);
                
                // NOTE: Some cases are out of the array and should be ignored
                if (x < this.width - 1)
                {
                    index = y * this.width + x + 1;
                    this.data.get(index, color)
                             .put(index,
                                  Color.toInt(color.r() + (int) (rErr * 7F / 16F)),
                                  Color.toInt(color.g() + (int) (gErr * 7F / 16F)),
                                  Color.toInt(color.b() + (int) (bErr * 7F / 16F)),
                                  Color.toInt(color.a() + (int) (aErr * 7F / 16F)));
                }
                
                if (x > 0 && y < this.height - 1)
                {
                    index = (y + 1) * this.width + x - 1;
                    this.data.get(index, color)
                             .put(index,
                                  Color.toInt(color.r() + (int) (rErr * 3F / 16F)),
                                  Color.toInt(color.g() + (int) (gErr * 3F / 16F)),
                                  Color.toInt(color.b() + (int) (bErr * 3F / 16F)),
                                  Color.toInt(color.a() + (int) (aErr * 3F / 16F)));
                }
                
                if (y < this.height - 1)
                {
                    index = (y + 1) * this.width + x;
                    this.data.get(index, color)
                             .put(index,
                                  Color.toInt(color.r() + (int) (rErr * 5F / 16F)),
                                  Color.toInt(color.g() + (int) (gErr * 5F / 16F)),
                                  Color.toInt(color.b() + (int) (bErr * 5F / 16F)),
                                  Color.toInt(color.a() + (int) (aErr * 5F / 16F)));
                }
                
                if (x < this.width - 1 && y < this.height - 1)
                {
                    index = (y + 1) * this.width + x + 1;
                    this.data.get(index, color)
                             .put(index,
                                  Color.toInt(color.r() + (int) (rErr * 1F / 16F)),
                                  Color.toInt(color.g() + (int) (gErr * 1F / 16F)),
                                  Color.toInt(color.b() + (int) (bErr * 1F / 16F)),
                                  Color.toInt(color.a() + (int) (aErr * 1F / 16F)));
                }
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Flips this image vertically.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    @NotNull
    public MutableImage flipV()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        long srcPtr = this.data.address();
        long dstPtr = newData.address();
        
        long bytesPerLine = Integer.toUnsignedLong(this.width) * this.data.format.sizeof;
        for (int i = this.height - 1, offsetSize = 0; i >= 0; i--)
        {
            long src = srcPtr + Integer.toUnsignedLong(i * this.width) * this.data.format.sizeof;
            
            MemoryUtil.memCopy(src, dstPtr + offsetSize, bytesPerLine);
            offsetSize += bytesPerLine;
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Flips this image horizontally.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    @NotNull
    public MutableImage flipH()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        if (this.data.format != ColorFormat.RGBA)
        {
            long srcPtr = this.data.address();
            long dstPtr = newData.address();
            
            long bytesPerLine = Integer.toUnsignedLong(this.width) * this.data.format.sizeof;
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    // OPTION 1: Move pixels with memCopy()
                    long src = Integer.toUnsignedLong(y * this.width + this.width - 1 - x) * this.data.format.sizeof;
                    long dst = Integer.toUnsignedLong(y * this.width + x) * this.data.format.sizeof;
                    
                    MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, bytesPerLine);
                    
                    // OPTION 2: Just copy data pixel by pixel
                    // newData.put(y * this.width + x, this.data.getBytes(y * this.width + (this.width - 1 - x)));
                }
            }
        }
        else
        {
            // OPTION 3: Faster implementation (specific for 32bit pixels)
            // NOTE: It does not require additional allocations
            IntBuffer srcPtr = MemoryUtil.memByteBuffer(this.data).asIntBuffer();
            IntBuffer dstPtr = MemoryUtil.memByteBuffer(newData).asIntBuffer();
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width / 2; x++)
                {
                    int backup = srcPtr.get(y * this.width + x);
                    dstPtr.put(y * this.width + x, srcPtr.get(y * this.width + this.width - 1 - x));
                    dstPtr.put(y * this.width + this.width - 1 - x, backup);
                }
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Rotates this image 90 degrees clockwise.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    @NotNull
    public MutableImage rotateCW()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        long srcPtr = this.data.address();
        long dstPtr = newData.address();
        
        for (int y = 0; y < this.height; y++)
        {
            for (int x = 0; x < this.width; x++)
            {
                long src = Integer.toUnsignedLong(y * this.width + x) * this.data.format.sizeof;
                long dst = Integer.toUnsignedLong(x * this.height + this.height - y - 1) * this.data.format.sizeof;
                
                MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, this.data.format.sizeof);
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Rotates this image 90 degrees counter-clockwise.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    @NotNull
    public MutableImage rotateCCW()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        long srcPtr = this.data.address();
        long dstPtr = newData.address();
        
        for (int y = 0; y < this.height; y++)
        {
            for (int x = 0; x < this.width; x++)
            {
                long src = Integer.toUnsignedLong(y * this.width + this.width - x - 1) * this.data.format.sizeof;
                long dst = Integer.toUnsignedLong(x * this.height + y) * this.data.format.sizeof;
                
                MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, this.data.format.sizeof);
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Tints this image using the specified {@link Colorc}.
     *
     * @param color The color to tint by.
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorTint(@NotNull Colorc color)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.tint(color));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Converts the image to grayscale.
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorGrayscale()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.grayscale());
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Changes the brightness of this image by a specified amount.
     *
     * @param brightness The amount to change the brightness by [{@code -1.0 - +1.0}]
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorBrightness(double brightness)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.brightness(brightness));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Changes the contrast of this image by a specified amount.
     *
     * @param contrast The amount to change the contrast by [{@code -1.0 - +1.0}]
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorContrast(double contrast)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.contrast(contrast));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Changes the gamma of this image by a specified amount.
     *
     * @param gamma The amount to change the gamma
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorGamma(double gamma)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.gamma(gamma));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Inverts the color of this image.
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorInvert()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.invert());
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Makes the image brighter by a specified amount.
     *
     * @param percentage the percentage to make the color brighter [{@code 0.0 - 1.0}]
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorBrighter(double percentage)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.brighter(percentage));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Makes the image darker by a specified amount.
     *
     * @param percentage the percentage to make the color darker [{@code 0.0 - 1.0}]
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorDarker(double percentage)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            newData.put(i, c.darker(percentage));
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Replaces all pixels that are close enough to {@code color} with {@code replace}
     *
     * @param find     The color to find.
     * @param distance The distance to the color.
     * @param replace  The color to replace.
     *
     * @return this;
     */
    @NotNull
    public MutableImage colorReplace(@NotNull Colorc find, @NotNull Colorc replace, double distance)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color c = new Color();
        
        if (distance <= 0.0)
        {
            for (int i = 0, n = this.width * this.height; i < n; i++)
            {
                this.data.get(i, c);
                if (c.equals(find)) c.set(replace);
                newData.put(i, c);
            }
        }
        else
        {
            int r = find.r();
            int g = find.g();
            int b = find.b();
            int a = find.a();
            
            double rc, gc, bc, ac, dist;
            
            for (int i = 0, n = this.width * this.height; i < n; i++)
            {
                this.data.get(i, c);
                rc   = Math.abs(c.r() - r) / 255.0;
                gc   = Math.abs(c.g() - g) / 255.0;
                bc   = Math.abs(c.b() - b) / 255.0;
                ac   = Math.abs(c.a() - a) / 255.0;
                dist = rc * rc + gc * gc + bc * bc + ac * ac;
                if (dist <= distance) c.set(replace);
                newData.put(i, c);
            }
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Replaces any pixel whose alpha value is within the threshold with the
     * provided color.
     *
     * @param color     The color to use.
     * @param threshold The threshold.
     *
     * @return this
     */
    @NotNull
    public MutableImage alphaClear(@NotNull Colorc color, int threshold)
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        threshold = Color.toInt(threshold);
        
        Color c = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, c);
            if (c.a() <= threshold) c.set(color);
            newData.put(i, c);
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Apply alpha mask to image
     * <p>
     * NOTE 1: Returned image is GRAY_ALPHA (16bit) or R
     * NOTE 2: alphaMask should be same size as image
     * <p>
     * Mipmap data will be lost.
     *
     * @param alphaMask The mask image
     *
     * @return this
     */
    @NotNull
    public MutableImage alphaMask(@NotNull Image alphaMask)
    {
        if (this.width != alphaMask.width || this.height != alphaMask.height) throw new IllegalArgumentException("Alpha mask must be same size as image");
        
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Image   mask       = alphaMask;
        boolean deleteMask = false;
        
        // Force mask to be Grayscale
        if (alphaMask.data.format != ColorFormat.RED)
        {
            mask       = new MutableImage(alphaMask).reformat(ColorFormat.RED);
            deleteMask = true;
        }
        
        Color color0 = new Color();
        Color color1 = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, color0);
            mask.data.get(i, color1);
            color0.a(color1.r());
            newData.put(i, color0);
        }
        
        if (deleteMask) mask.delete();
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    /**
     * Pre-multiply alpha channel
     *
     * @return this
     */
    public @NotNull MutableImage alphaPreMultiply()
    {
        ColorBuffer newData = ColorBuffer.malloc(this.data.format, this.width * this.height);
        
        Color color = new Color();
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            this.data.get(i, color);
            if (color.a() == 0)
            {
                color.r(0);
                color.g(0);
                color.b(0);
            }
            else
            {
                color.r(color.r() * color.a() / 255);
                color.g(color.g() * color.a() / 255);
                color.b(color.b() * color.a() / 255);
            }
            newData.put(i, color);
        }
        
        MemoryUtil.memFree(this.data);
        this.data = newData;
        
        return this;
    }
    
    // -------------------- Utility Functions -------------------- //
    
    private void validateRect(int x, int y, int width, int height)
    {
        if (x < 0) throw new IllegalArgumentException("subregion x exceeds image bounds");
        if (y < 0) throw new IllegalArgumentException("subregion y exceeds image bounds");
        if (x + width - 1 >= this.width) throw new IllegalArgumentException("subregion width exceeds image bounds");
        if (y + height - 1 >= this.height) throw new IllegalArgumentException("subregion height exceeds image bounds");
    }
}
