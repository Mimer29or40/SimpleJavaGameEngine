package engine.gif;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class LZW
{
    /**
     * General Defines
     *
     * <ul>
     *     <li>{@link #HASH_SIZE}: 80% occupancy</li>
     *     <li>{@link #MAX_BIT_DEPTH}</li>
     *     <li>{@link #MAX_MAX_CODE}: Should <b>NEVER</b> generate this code</li>
     * </ul>
     *
     * @noinspection JavaDoc
     */
    private static final int HASH_SIZE = 5003, MAX_BIT_DEPTH = 12, MAX_MAX_CODE = 1 << MAX_BIT_DEPTH;
    
    private static final int[] MASKS = {
            0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
    };
    
    /**
     * Initial Number of Bits
     */
    private static int initBitDepth;
    
    /**
     * Number of bits/code
     */
    private static int bitDepth;
    
    /**
     * Maximum Code, given n_bits
     */
    private static int maxCode;
    
    private static int[] hashTable;
    private static int[] codeTable;
    
    /**
     * Block Compression Parameters. After all codes are used up, and
     * compression rate changes, start over.
     */
    private static boolean clearFlag = false;
    
    private static int clearCode;
    private static int eofCode;
    
    /**
     * First unused entry
     */
    private static int freeEntry;
    
    private static int curBitDepth;
    private static int curAccum;
    
    /**
     * Number of characters so far in this 'packet'
     */
    private static int accumulatorIndex;
    
    /**
     * Define the storage for the packet accumulator
     */
    private static byte[] accumulator;
    
    /**
     * Algorithm: Use open addressing double hashing (no chaining) on the
     * prefix code / next character combination. We do a variant of Knuth's
     * algorithm D (vol. 3, sec. 6.4) along with G. Knott's
     * relatively-prime secondary probe.  Here, the modular division first
     * probe is gives way to a faster exclusive-or manipulation. Also do
     * block compression with an adaptive reset, whereby the code table is
     * cleared when the compression ratio decreases, but after the table
     * fills. The variable-length output codes are re-sized at this point,
     * and a special CLEAR code is generated for the decompressor. Late
     * addition: construct the table according to file size for noticeable
     * speed improvement on small files. Please direct questions about this
     * implementation to ames!jaw.
     * <p>
     * Maintain a BITS character long buffer (so that 8 codes will fit in
     * it exactly). Use the VAX insv instruction to insert each code in
     * turn. When the buffer fills up empty it and start over.
     *
     * @param data     The array of data formatted to encode.
     * @param bitDepth The bit depth. Usually 8.
     * @param stream   The stream to output the data.
     *
     * @throws IOException Writing to the stream failed.
     */
    public static void encode(byte[] data, int bitDepth, OutputStream stream) throws IOException
    {
        LZW.initBitDepth = Math.max(2, bitDepth) + 1;
        
        LZW.bitDepth = LZW.initBitDepth;
        LZW.maxCode  = (1 << LZW.bitDepth) - 1;
        
        LZW.hashTable = new int[LZW.HASH_SIZE];
        LZW.codeTable = new int[LZW.HASH_SIZE];
        
        // Set up the necessary values
        LZW.clearFlag = false;
        
        LZW.clearCode = 1 << (LZW.initBitDepth - 1);
        LZW.eofCode   = LZW.clearCode + 1;
        LZW.freeEntry = LZW.clearCode + 2;
        
        LZW.curAccum    = 0;
        LZW.curBitDepth = 0;
        
        LZW.accumulatorIndex = 0; // clear packet
        LZW.accumulator      = new byte[256];
        
        // compress and write the pixel data
        
        int hashShift = 0;
        for (int i = LZW.HASH_SIZE; i < 65536; i *= 2) ++hashShift;
        hashShift = 8 - hashShift; // set hash code range bound
        
        Arrays.fill(LZW.hashTable, -1); // clear hash table
        
        stream.write(LZW.initBitDepth - 1); // write "initial code size" byte
        
        output(LZW.clearCode, stream);
        
        int byteIndex = 0;
        int prevByte  = data[byteIndex] & 0xFF;
        outer_loop:
        for (int n = data.length; byteIndex < n; ++byteIndex)
        {
            int curByte = data[byteIndex] & 0xFF;
            
            int hash      = (curByte << LZW.MAX_BIT_DEPTH) + prevByte;
            int hashIndex = (curByte << hashShift) ^ prevByte; // xor hashing
            
            if (LZW.hashTable[hashIndex] == hash)
            {
                prevByte = LZW.codeTable[hashIndex];
                continue;
            }
            else if (LZW.hashTable[hashIndex] >= 0) // non-empty slot
            {
                int secondaryHash = hashIndex == 0 ? 1 : LZW.HASH_SIZE - hashIndex; // secondary hash (after G. Knott)
                do
                {
                    if ((hashIndex -= secondaryHash) < 0) hashIndex += LZW.HASH_SIZE;
                    
                    if (LZW.hashTable[hashIndex] == hash)
                    {
                        prevByte = LZW.codeTable[hashIndex];
                        continue outer_loop;
                    }
                }
                while (LZW.hashTable[hashIndex] >= 0);
            }
            output(prevByte, stream);
            if (LZW.freeEntry < LZW.MAX_MAX_CODE)
            {
                LZW.codeTable[hashIndex] = LZW.freeEntry++; // code -> hashtable
                LZW.hashTable[hashIndex] = hash;
            }
            else
            {
                Arrays.fill(LZW.hashTable, -1);
                LZW.freeEntry = LZW.clearCode + 2;
                LZW.clearFlag = true;
                
                output(LZW.clearCode, stream);
            }
            prevByte = curByte;
        }
        // Put out the final code.
        output(prevByte, stream);
        output(LZW.eofCode, stream);
        
        // write block terminator
        stream.write(0);
    }
    
    /**
     * Add a character to the end of the current packet, and if it is 254
     * characters, flush the packet to disk.
     */
    private static void writeAccumulator(byte c, OutputStream stream) throws IOException
    {
        LZW.accumulator[LZW.accumulatorIndex++] = c;
        if (LZW.accumulatorIndex >= 254) flushAccumulator(stream);
    }
    
    /**
     * Flush the packet to disk, and reset the accumulator.
     */
    private static void flushAccumulator(OutputStream stream) throws IOException
    {
        if (LZW.accumulatorIndex > 0)
        {
            stream.write(LZW.accumulatorIndex);
            stream.write(LZW.accumulator, 0, LZW.accumulatorIndex);
            LZW.accumulatorIndex = 0;
        }
    }
    
    private static void output(int code, OutputStream stream) throws IOException
    {
        LZW.curAccum &= LZW.MASKS[LZW.curBitDepth];
        LZW.curAccum = LZW.curBitDepth > 0 ? LZW.curAccum | (code << LZW.curBitDepth) : code;
        LZW.curBitDepth += LZW.bitDepth;
        
        while (LZW.curBitDepth >= 8)
        {
            writeAccumulator((byte) (LZW.curAccum & 0xFF), stream);
            LZW.curAccum >>= 8;
            LZW.curBitDepth -= 8;
        }
        
        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (LZW.freeEntry > LZW.maxCode || LZW.clearFlag)
        {
            if (LZW.clearFlag)
            {
                LZW.bitDepth  = LZW.initBitDepth;
                LZW.maxCode   = (1 << LZW.bitDepth) - 1;
                LZW.clearFlag = false;
            }
            else
            {
                ++LZW.bitDepth;
                LZW.maxCode = LZW.bitDepth == LZW.MAX_BIT_DEPTH ? LZW.MAX_MAX_CODE : (1 << LZW.bitDepth) - 1;
            }
        }
        
        if (code == LZW.eofCode)
        {
            // At EOF, write the rest of the buffer.
            while (LZW.curBitDepth > 0)
            {
                writeAccumulator((byte) (LZW.curAccum & 0xFF), stream);
                LZW.curAccum >>= 8;
                LZW.curBitDepth -= 8;
            }
            
            flushAccumulator(stream);
        }
    }
}
