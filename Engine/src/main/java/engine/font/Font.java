package engine.font;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureFilter;
import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.stb.STBTruetype.*;

public class Font
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    private static @Nullable String nameString(@NotNull STBTTFontinfo info, int nameID)
    {
        ByteBuffer value = stbtt_GetFontNameString(info, STBTT_PLATFORM_ID_MICROSOFT, STBTT_MS_EID_UNICODE_BMP, STBTT_MS_LANG_ENGLISH, nameID);
        if (value != null)
        {
            ByteBuffer string = MemoryUtil.memAlloc(value.order(ByteOrder.BIG_ENDIAN).capacity() >> 1);
            while (value.hasRemaining())
            {
                value.get();
                string.put(value.get());
            }
            string.flip();
            String result = MemoryUtil.memUTF8(string);
            MemoryUtil.memFree(string);
            return result;
        }
        return null;
    }
    
    // -------------------- Instance -------------------- //
    
    final STBTTFontinfo info;
    final ByteBuffer    fileData;
    
    public final String name;
    
    public final Weight  weight;
    public final boolean italicized;
    
    public final boolean kerning;
    public final boolean alignToInt;
    
    public final int ascentUnscaled;
    public final int descentUnscaled;
    public final int lineGapUnscaled;
    
    public final @UnmodifiableView @NotNull List<@NotNull CharData> charData;
    
    public final Texture2D texture;
    
    public Font(@NotNull Path filePath, boolean kerning, boolean alignToInt, boolean interpolated)
    {
        this.info     = STBTTFontinfo.malloc();
        this.fileData = IOUtil.readFromFile(filePath, new int[1], MemoryUtil::memAlloc);
        
        if (this.fileData == null || !stbtt_InitFont(this.info, this.fileData)) throw new RuntimeException("Font Data could not be loaded: " + filePath);
        
        String fontFamilyName    = nameString(info, 1);  // Font Family name
        String fontSubfamilyName = nameString(info, 2);  // Font Subfamily name
        String typoFamilyName    = nameString(info, 16); // Typographic Family name
        String typoSubfamilyName = nameString(info, 17); // Typographic Subfamily name
        
        this.name = typoFamilyName != null ? typoFamilyName : fontFamilyName != null ? fontFamilyName : "Unknown";
        
        String subfamily = typoSubfamilyName != null ? typoSubfamilyName : fontSubfamilyName;
        if (subfamily != null)
        {
            subfamily = subfamily.toLowerCase();
            
            Weight possibleWeight = Weight.get(subfamily);
            this.weight     = possibleWeight != null ? possibleWeight : Weight.REGULAR;
            this.italicized = subfamily.contains("italic");
        }
        else
        {
            this.weight     = Weight.REGULAR;
            this.italicized = false;
        }
        
        this.kerning    = kerning;
        this.alignToInt = alignToInt;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer ascent  = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);
            
            stbtt_GetFontVMetrics(this.info, ascent, descent, lineGap);
            
            this.ascentUnscaled  = ascent.get(0);
            this.descentUnscaled = descent.get(0);
            this.lineGapUnscaled = lineGap.get(0);
            
            int baseSize = 96;
            
            STBTTPackedchar.Buffer packedChars = STBTTPackedchar.malloc(0xFFFF);
            
            int width;
            int height;
            
            ByteBuffer buffer;
            
            boolean success;
            
            int textureSize = 32;
            int samples     = 1;
            while (true)
            {
                width  = baseSize * textureSize;
                height = baseSize * (textureSize >> 1);
                
                buffer = MemoryUtil.memAlloc(width * height);
                
                packedChars.position(32);
                try (STBTTPackContext pc = STBTTPackContext.malloc())
                {
                    stbtt_PackBegin(pc, buffer, width, height, 0, 2, MemoryUtil.NULL);
                    stbtt_PackSetOversampling(pc, samples, samples);
                    success = stbtt_PackFontRange(pc, this.fileData, 0, baseSize, packedChars.position(), packedChars);
                    stbtt_PackEnd(pc);
                }
                packedChars.clear();
                buffer.clear();
                
                textureSize <<= 1;
                
                if (success || textureSize >= 1000) break;
                MemoryUtil.memFree(buffer);
            }
            
            IntBuffer advanceWidth    = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);
            
            IntBuffer x0 = stack.mallocInt(1);
            IntBuffer y0 = stack.mallocInt(1);
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            
            FloatBuffer x = stack.floats(0);
            FloatBuffer y = stack.floats(0);
            
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            int size = 0xFFFF;
            
            List<CharData> charData = new ArrayList<>(size);
            for (int i = 0; i < size; i++)
            {
                int index = stbtt_FindGlyphIndex(this.info, i);
                
                stbtt_GetGlyphHMetrics(this.info, index, advanceWidth, leftSideBearing);
                stbtt_GetGlyphBox(this.info, index, x0, y0, x1, y1);
                stbtt_GetPackedQuad(packedChars, width, height, i, x, y, quad, this.alignToInt);
                
                int x0Unscaled = x0.get(0);
                int y0Unscaled = this.ascentUnscaled - y1.get(0);
                int x1Unscaled = x1.get(0);
                int y1Unscaled = this.ascentUnscaled - y0.get(0);
                
                charData.add(new CharData((char) i,
                                          index,
                                          advanceWidth.get(0),
                                          leftSideBearing.get(0),
                                          x0Unscaled,
                                          y0Unscaled,
                                          x1Unscaled,
                                          y1Unscaled,
                                          quad.s0(),
                                          quad.t0(),
                                          quad.s1(),
                                          quad.t1()));
            }
            this.charData = Collections.unmodifiableList(charData);
            
            packedChars.free();
            
            ColorBuffer data = ColorBuffer.wrap(ColorFormat.RED, buffer);
            this.texture = new Texture2D(data, width, height);
            if (interpolated) this.texture.filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
            
            MemoryUtil.memFree(buffer);
        }
    }
    
    @Override
    public String toString()
    {
        return "Font{" + this.name + '}';
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Font font = (Font) o;
        return this.name.equals(font.name);
    }
    
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }
    
    // -------------------- Properties -------------------- //
    
    public double scale(double size)
    {
        return stbtt_ScaleForPixelHeight(this.info, (float) size);
    }
    
    public double kerningAdvanceUnscaled(CharData ch1, CharData ch2)
    {
        if (ch1 == null) return 0.0;
        if (ch2 == null) return 0.0;
        if (!this.kerning) return 0.0;
        return stbtt_GetGlyphKernAdvance(this.info, ch1.index(), ch2.index());
    }
    
    // -------------------- Functions -------------------- //
    
    public void delete()
    {
        Font.LOGGER.debug("Deleting:", this);
        
        this.info.free();
        MemoryUtil.memFree(this.fileData);
        
        this.texture.delete();
    }
    
    public List<String> splitText(String text, double size, double maxWidth)
    {
        if (maxWidth <= 0) return List.of(text.split("\n|\r\n|\n\r"));
        
        List<String> lines = new ArrayList<>();
        
        for (String line : text.split("\n|\r\n|\n\r"))
        {
            if (lineWidth(line, size) <= maxWidth)
            {
                lines.add(line);
                continue;
            }
            String[]      lineWords = line.split(" ");
            StringBuilder newLine   = new StringBuilder(lineWords[0]);
            for (int j = 1, n = lineWords.length; j < n; j++)
            {
                String word        = lineWords[j];
                String nextSegment = " " + word;
                if (lineWidth(newLine + nextSegment, size) <= maxWidth)
                {
                    newLine.append(nextSegment);
                    continue;
                }
                lines.add(newLine.toString());
                newLine.setLength(0);
                newLine.append(word);
            }
        }
        return lines;
    }
    
    public double textWidth(@NotNull String text, double size, double maxWidth)
    {
        List<String> lines = splitText(text, size, maxWidth);
        
        double width = 0;
        for (String line : lines) width = Math.max(width, lineWidth(line, size));
        return width;
    }
    
    public double textWidth(@NotNull String text, double size)
    {
        return textWidth(text, size, 0.0);
    }
    
    public double textHeight(@NotNull String text, double size, double maxWidth)
    {
        List<String> lines = splitText(text, size, maxWidth);
        
        double height = 0;
        for (String line : lines) height += lineHeight(line, size);
        return height;
    }
    
    public double textHeight(@NotNull String text, double size)
    {
        return textHeight(text, size, 0.0);
    }
    
    public double lineWidth(@NotNull String line, double size)
    {
        double scale = scale(size);
        
        double width = 0.0;
        
        CharData prevChar = null, currChar;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            currChar = this.charData.get(line.charAt(i));
            
            double charWidth = currChar.advanceWidthUnscaled() + kerningAdvanceUnscaled(prevChar, currChar);
            
            width += charWidth * scale;
            
            prevChar = currChar;
        }
        return width;
    }
    
    public double lineHeight(@NotNull String line, double size)
    {
        double scale = scale(size);
        
        double height = 0;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            double charHeight = this.ascentUnscaled - this.descentUnscaled + this.lineGapUnscaled;
            
            height = Math.max(height, charHeight * scale);
        }
        return height;
    }
}
