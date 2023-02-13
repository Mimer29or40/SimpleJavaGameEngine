package engine.gif;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NeuQuantize
{
    /**
     * Number of Colors Used
     */
    private static final int NET_SIZE = 256;
    
    /**
     * Four Primes near 500. Assume no image has a length so large that it is
     * divisible by all four primes.
     */
    private static final int PRIME1 = 499, PRIME2 = 491, PRIME3 = 487, PRIME4 = 503;
    
    /**
     * Minimum Size for Input Image.
     */
    private static final int MIN_PICTURE_BYTES = 3 * PRIME4;
    
    /**
     * Network Definitions
     *
     * <ul>
     *     <li>{@link #MAX_NET_POS}</li>
     *     <li>{@link #NET_BIAS_SHIFT}: Bias for Color Values</li>
     *     <li>{@link #N_CYCLES}: Number of Learning Cycles</li>
     * </ul>
     *
     * @noinspection JavaDoc
     */
    private static final int MAX_NET_POS = NET_SIZE - 1, NET_BIAS_SHIFT = 4, N_CYCLES = 100;
    
    /**
     * Frequency and Bias Definitions
     *
     * <ul>
     *     <li>{@link #INT_BIAS_SHIFT}</li>
     *     <li>{@link #INT_BIAS}: Bias for Fractions</li>
     *     <li>{@link #GAMMA_SHIFT}</li>
     *     <li>{@link #GAMMA} {@code = 1024}</li>
     *     <li>{@link #BETA_SHIFT}</li>
     *     <li>{@link #BETA} {@code = 1/1024}</li>
     *     <li>{@link #BETA_GAMMA}</li>
     * </ul>
     *
     * @noinspection JavaDoc
     */
    @SuppressWarnings("unused")
    private static final int INT_BIAS_SHIFT = 16, INT_BIAS = 1 << INT_BIAS_SHIFT, GAMMA_SHIFT = 10, GAMMA = 1 << GAMMA_SHIFT, BETA_SHIFT = 10, BETA = INT_BIAS >>
                                                                                                                                                      BETA_SHIFT, BETA_GAMMA =
            INT_BIAS <<
            (GAMMA_SHIFT - BETA_SHIFT);
    
    /**
     * Decreasing Radius Factor Definitions
     * <p>
     * For 256 columns, radius starts at {@code 32.0} bias by {@code 6} bits
     * and decreases by a factor of {@code 1/30} each cycle
     *
     * <ul>
     *     <li{@link #INIT_RAD}</li>
     *     <li{@link #RADIUS_BIAS_SHIFT}</li>
     *     <li{@link #RADIUS_BIAS}</li>
     *     <li{@link #INIT_RADIUS}</li>
     *     <li{@link #RADIUS_DEC}</li>
     * </ul>
     *
     * @noinspection JavaDoc
     */
    private static final int INIT_RAD = NET_SIZE >> 3, RADIUS_BIAS_SHIFT = 6, RADIUS_BIAS = 1 << RADIUS_BIAS_SHIFT, INIT_RADIUS = INIT_RAD * RADIUS_BIAS, RADIUS_DEC = 30;
    
    /**
     * Decreasing Alpha Factor Definitions
     * <p>
     * For 256 columns, radius starts at {@code 32.0} bias by {@code 6} bits
     * and decreases by a factor of {@code 1/30} each cycle
     *
     * <ul>
     *     <li{@link #ALPHA_BIAS_SHIFT}: Alpha starts at {@code 1.0}</li>
     *     <li{@link #INIT_ALPHA}</li>
     * </ul>
     *
     * @noinspection JavaDoc
     */
    private static final int ALPHA_BIAS_SHIFT = 10, INIT_ALPHA = 1 << ALPHA_BIAS_SHIFT;
    
    /**
     * {@link #RAD_BIAS} and {@link #ALPHA_RAD_BIAS} used for {@link #radPower} calculation
     *
     * @noinspection JavaDoc, RedundantSuppression
     */
    private static final int RAD_BIAS_SHIFT = 8, RAD_BIAS = 1 << RAD_BIAS_SHIFT, ALPHA_RAD_BIAS_SHIFT = ALPHA_BIAS_SHIFT + RAD_BIAS_SHIFT, ALPHA_RAD_BIAS = 1 <<
                                                                                                                                                            ALPHA_RAD_BIAS_SHIFT;
    
    /*
     * Types and Global Variables
     * --------------------------
     */
    
    /**
     * The Network Itself
     * <p>
     * {@code network = new int[}{@link #NET_SIZE}{@code ][4]}
     */
    private static int[][] network;
    
    /**
     * For Network Lookup - Really 256
     * <p>
     * {@code netIndex = new int[256]}
     */
    private static int[] netIndex;
    
    /**
     * Bias and Frequency Arrays for Learning
     * <p>
     * {@code bias = new int[}{@link #NET_SIZE}{@code ]}
     * <p>
     * {@code freq = new int[}{@link #NET_SIZE}{@code ]}
     */
    private static int[] bias, freq;
    
    /**
     * radPower for pre-computation.
     * <p>
     * {@code radPower = new int[}{@link #INIT_RAD}{@code ]}
     */
    private static int[] radPower;
    
    /**
     * biased by 10 bits
     */
    private static int alphaDec;
    
    /**
     * Initialise network in range (0,0,0) to (255,255,255), set parameters,
     * and create reduced palette
     *
     * @param pixels       The input image.
     * @param sampleFactor Sampling Factor {@code [1..30]}
     */
    public static byte @NotNull [] quantize(byte @NotNull [] pixels, int sampleFactor)
    {
        sampleFactor = pixels.length < NeuQuantize.MIN_PICTURE_BYTES ? 1 : sampleFactor;
        
        NeuQuantize.network  = new int[NeuQuantize.NET_SIZE][];
        NeuQuantize.netIndex = new int[256];
        NeuQuantize.bias     = new int[NeuQuantize.NET_SIZE];
        NeuQuantize.freq     = new int[NeuQuantize.NET_SIZE];
        NeuQuantize.radPower = new int[NeuQuantize.INIT_RAD];
        
        NeuQuantize.alphaDec = 30 + ((sampleFactor - 1) / 3);
        
        for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
        {
            int initial = (i << (NeuQuantize.NET_BIAS_SHIFT + 8)) / NeuQuantize.NET_SIZE;
            
            NeuQuantize.network[i] = new int[] {initial, initial, initial, 0};
        }
        Arrays.fill(NeuQuantize.netIndex, 0);
        Arrays.fill(NeuQuantize.bias, 0);
        Arrays.fill(NeuQuantize.freq, NeuQuantize.INT_BIAS / NeuQuantize.NET_SIZE); // 1/NET_SIZE
        Arrays.fill(NeuQuantize.radPower, 0);
        
        learn(pixels, sampleFactor);
        
        // Unbias network to give byte values 0..255 and record position i to prepare for sort
        for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
        {
            NeuQuantize.network[i][0] >>= NeuQuantize.NET_BIAS_SHIFT;
            NeuQuantize.network[i][1] >>= NeuQuantize.NET_BIAS_SHIFT;
            NeuQuantize.network[i][2] >>= NeuQuantize.NET_BIAS_SHIFT;
            NeuQuantize.network[i][3] =   i; /* record color no */
        }
        
        inxBuild();
        
        int[] index = new int[NeuQuantize.NET_SIZE];
        for (int i = 0; i < NeuQuantize.NET_SIZE; i++) index[NeuQuantize.network[i][3]] = i;
        
        byte[] map = new byte[3 * NeuQuantize.NET_SIZE];
        for (int i = 0, k = 0; i < NeuQuantize.NET_SIZE; i++)
        {
            int j = index[i];
            map[k++] = (byte) (NeuQuantize.network[j][0]);
            map[k++] = (byte) (NeuQuantize.network[j][1]);
            map[k++] = (byte) (NeuQuantize.network[j][2]);
        }
        return map;
    }
    
    /**
     * Search for BGR values 0..255 (after net is unbiased) and return color
     * index
     */
    public static int map(int b, int g, int r)
    {
        int bestDist = 1000; /* biggest possible dist is 256*3 */
        int best     = -1;
        
        // i: index on g
        // j: start at netIndex[g] and work outwards
        for (int i = NeuQuantize.netIndex[g], j = i - 1; i < NeuQuantize.NET_SIZE || j >= 0; )
        {
            if (i < NeuQuantize.NET_SIZE)
            {
                int[] p    = NeuQuantize.network[i];
                int   dist = p[1] - g; /* inx key */
                if (dist >= bestDist)
                {
                    i = NeuQuantize.NET_SIZE; /* stop iter */
                }
                else
                {
                    i++;
                    dist = Math.abs(dist) + Math.abs(p[0] - b);
                    if (dist < bestDist)
                    {
                        dist += Math.abs(p[2] - r);
                        if (dist < bestDist)
                        {
                            bestDist = dist;
                            best     = p[3];
                        }
                    }
                }
            }
            if (j >= 0)
            {
                int[] p    = NeuQuantize.network[j];
                int   dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestDist)
                {
                    j = -1; /* stop iter */
                }
                else
                {
                    j--;
                    dist = Math.abs(dist) + Math.abs(p[0] - b);
                    if (dist < bestDist)
                    {
                        dist += Math.abs(p[2] - r);
                        if (dist < bestDist)
                        {
                            bestDist = dist;
                            best     = p[3];
                        }
                    }
                }
            }
        }
        return best;
    }
    
    /**
     * Main Learning Loop
     * ------------------
     */
    private static void learn(byte @NotNull [] pixels, int sampleFactor)
    {
        int samplePixels = pixels.length / (3 * sampleFactor);
        int alpha        = NeuQuantize.INIT_ALPHA;
        int radius       = NeuQuantize.INIT_RADIUS;
        int rad          = radius >> NeuQuantize.RADIUS_BIAS_SHIFT;
        
        // if (rad <= 1) rad = 0;
        
        for (int i = 0; i < rad; i++)
        {
            NeuQuantize.radPower[i] = alpha * (((rad * rad - i * i) * NeuQuantize.RAD_BIAS) / (rad * rad));
        }
        
        int step;
        if (pixels.length < NeuQuantize.MIN_PICTURE_BYTES)
        {
            step = 3;
        }
        else if ((pixels.length % NeuQuantize.PRIME1) != 0)
        {
            step = 3 * NeuQuantize.PRIME1;
        }
        else if ((pixels.length % NeuQuantize.PRIME2) != 0)
        {
            step = 3 * NeuQuantize.PRIME2;
        }
        else if ((pixels.length % NeuQuantize.PRIME3) != 0)
        {
            step = 3 * NeuQuantize.PRIME3;
        }
        else
        {
            step = 3 * NeuQuantize.PRIME4;
        }
        
        int delta = samplePixels / NeuQuantize.N_CYCLES;
        if (delta == 0) delta = 1;
        for (int i = 0, pix = 0; i < samplePixels; )
        {
            int b = (pixels[pix] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
            int g = (pixels[pix + 1] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
            int r = (pixels[pix + 2] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
            
            int bestBiasPos = contest(b, g, r);
            
            alterSingle(alpha, bestBiasPos, b, g, r);
            if (rad != 0) alterNeighbours(rad, bestBiasPos, b, g, r); /* alter neighbours */
            
            pix += step;
            if (pix >= pixels.length) pix -= pixels.length;
            
            i++;
            
            if (i % delta == 0)
            {
                alpha -= alpha / NeuQuantize.alphaDec;
                radius -= radius / NeuQuantize.RADIUS_DEC;
                rad = radius >> NeuQuantize.RADIUS_BIAS_SHIFT;
                if (rad <= 1) rad = 0;
                for (bestBiasPos = 0; bestBiasPos < rad; bestBiasPos++)
                {
                    NeuQuantize.radPower[bestBiasPos] = alpha * (((rad * rad - bestBiasPos * bestBiasPos) * NeuQuantize.RAD_BIAS) / (rad * rad));
                }
            }
        }
    }
    
    /**
     * Insertion sort of network and building of netIndex[0..255] (to do after unbias)
     */
    private static void inxBuild()
    {
        int prevCol  = 0;
        int startPos = 0;
        for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
        {
            int[] p        = NeuQuantize.network[i];
            int   smallPos = i;
            int   smallVal = p[1]; /* index on g */
            /* find smallest in [i..NET_SIZE-1] */
            for (int j = i + 1; j < NeuQuantize.NET_SIZE; j++)
            {
                int[] q = NeuQuantize.network[j];
                if (q[1] < smallVal)
                { /* index on g */
                    smallPos = j;
                    smallVal = q[1]; /* index on g */
                }
            }
            int[] q = NeuQuantize.network[smallPos];
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
                NeuQuantize.netIndex[prevCol] = (startPos + i) >> 1;
                for (int j = prevCol + 1; j < smallVal; j++) NeuQuantize.netIndex[j] = i;
                prevCol  = smallVal;
                startPos = i;
            }
        }
        
        NeuQuantize.netIndex[prevCol] = (startPos + NeuQuantize.MAX_NET_POS) >> 1;
        Arrays.fill(NeuQuantize.netIndex, prevCol + 1, NeuQuantize.netIndex.length, NeuQuantize.MAX_NET_POS); // really 256
    }
    
    /**
     * Move adjacent neurons by precomputed {@code alpha*(1-((i-j)^2/[r]^2))}
     * in {@link #radPower}{@code [|i-j|]}
     */
    private static void alterNeighbours(int rad, int bestBiasPos, int b, int g, int r)
    {
        int lo = Math.max(bestBiasPos - rad, -1);
        int hi = Math.min(bestBiasPos + rad, NeuQuantize.NET_SIZE);
        
        int j = bestBiasPos + 1;
        int k = bestBiasPos - 1;
        int m = 1;
        while (j < hi || k > lo)
        {
            int a = NeuQuantize.radPower[m++];
            if (j < hi)
            {
                int[] p = NeuQuantize.network[j++];
                try
                {
                    p[0] -= (a * (p[0] - b)) / NeuQuantize.ALPHA_RAD_BIAS;
                    p[1] -= (a * (p[1] - g)) / NeuQuantize.ALPHA_RAD_BIAS;
                    p[2] -= (a * (p[2] - r)) / NeuQuantize.ALPHA_RAD_BIAS;
                }
                catch (Exception ignored) {}
            }
            if (k > lo)
            {
                int[] p = NeuQuantize.network[k--];
                try
                {
                    p[0] -= (a * (p[0] - b)) / NeuQuantize.ALPHA_RAD_BIAS;
                    p[1] -= (a * (p[1] - g)) / NeuQuantize.ALPHA_RAD_BIAS;
                    p[2] -= (a * (p[2] - r)) / NeuQuantize.ALPHA_RAD_BIAS;
                }
                catch (Exception ignored) {}
            }
        }
    }
    
    /**
     * Move neuron i towards biased (b,g,r) by factor alpha
     */
    private static void alterSingle(int alpha, int bestBiasPos, int b, int g, int r)
    {
        // alter hit neuron
        int[] n = NeuQuantize.network[bestBiasPos];
        n[0] -= (alpha * (n[0] - b)) / NeuQuantize.INIT_ALPHA;
        n[1] -= (alpha * (n[1] - g)) / NeuQuantize.INIT_ALPHA;
        n[2] -= (alpha * (n[2] - r)) / NeuQuantize.INIT_ALPHA;
    }
    
    /**
     * Search for biased BGR values
     * <p>
     * Finds the closest neuron (minimum distance) and updates frequency.
     * Finds the best neuron (minimum distance-bias) and returns position.
     * For frequently chosen neurons, {@code freq[i]} is high and
     * {@code bias[i]} is negative.
     * <p>
     * {@code bias[i] = }{@link #GAMMA}{@code *((1/}{@link #NET_SIZE}{@code )-freq[i])}
     */
    private static int contest(int b, int g, int r)
    {
        int bestDist     = Integer.MAX_VALUE;
        int bestBiasDist = bestDist;
        int bestPos      = -1;
        int bestBiasPos  = bestPos;
        
        for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
        {
            int[] n = NeuQuantize.network[i];
            
            int dist = Math.abs(n[0] - b) + Math.abs(n[1] - g) + Math.abs(n[2] - r);
            
            if (dist < bestDist)
            {
                bestDist = dist;
                bestPos  = i;
            }
            
            int biasDist = dist - (NeuQuantize.bias[i] >> (NeuQuantize.INT_BIAS_SHIFT - NeuQuantize.NET_BIAS_SHIFT));
            if (biasDist < bestBiasDist)
            {
                bestBiasDist = biasDist;
                bestBiasPos  = i;
            }
            
            int betaFreq = NeuQuantize.freq[i] >> NeuQuantize.BETA_SHIFT;
            NeuQuantize.bias[i] += betaFreq << NeuQuantize.GAMMA_SHIFT;
            NeuQuantize.freq[i] -= betaFreq;
        }
        NeuQuantize.bias[bestPos] -= NeuQuantize.BETA_GAMMA;
        NeuQuantize.freq[bestPos] += NeuQuantize.BETA;
        return bestBiasPos;
    }
}
