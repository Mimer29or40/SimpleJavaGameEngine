package engine.font;

import engine.Image;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureFilter;
import engine.util.BinaryPacker;
import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.*;

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
    
    public final int ascentUnscaled;
    public final int descentUnscaled;
    public final int lineGapUnscaled;
    
    public final @UnmodifiableView @NotNull List<@NotNull CharData>          charData;
    public final @UnmodifiableView @NotNull Map<Integer, @NotNull GlyphData> glyphData;
    
    public final Texture2D texture;
    
    public Font(@NotNull Path filePath, boolean kerning)
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
        
        this.kerning = kerning;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer ascent  = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);
            
            stbtt_GetFontVMetrics(this.info, ascent, descent, lineGap);
            
            this.ascentUnscaled  = ascent.get(0);
            this.descentUnscaled = descent.get(0);
            this.lineGapUnscaled = lineGap.get(0);
            
            IntBuffer w    = stack.mallocInt(1);
            IntBuffer h    = stack.mallocInt(1);
            IntBuffer xoff = stack.mallocInt(1);
            IntBuffer yoff = stack.mallocInt(1);
            
            // TODO - Class members
            float scale            = stbtt_ScaleForPixelHeight(this.info, 32F);
            int   padding          = 4;
            int   edge_value       = 128;
            float pixel_dist_scale = 64F;
            
            // ---------- CharData ---------- //
            int maxChar = 0xFFFF;
            
            List<CharData>          charData  = new ArrayList<>();
            Map<Integer, GlyphData> glyphData = new HashMap<>();
            
            ByteBuffer nullBuffer = stack.malloc(0);
            
            Map<Integer, BinaryPacker.Container<ByteBuffer>> containers = new HashMap<>();
            for (int i = 0; i <= maxChar; i++)
            {
                int glyph = stbtt_FindGlyphIndex(this.info, i);
                charData.add(new CharData((char) i, glyph));
                
                containers.computeIfAbsent(glyph, g -> {
                    w.put(0, 0);
                    h.put(0, 0);
                    ByteBuffer bitmap = stbtt_GetGlyphSDF(this.info, scale, g, padding, (byte) edge_value, pixel_dist_scale, w, h, xoff, yoff);
                    if (bitmap == null) bitmap = nullBuffer;
                    return new BinaryPacker.Container<>(bitmap, w.get(0), h.get(0));
                });
            }
            
            BinaryPacker packer = new BinaryPacker();
            packer.sortFunc = BinaryPacker.Sort.HEIGHT;
            BinaryPacker.Result result = packer.fit(new ArrayList<>(containers.values()));
            
            ColorBuffer data      = ColorBuffer.calloc(ColorFormat.RED, result.width() * result.height());
            Image       image     = new Image(data, result.width(), result.height());
            ByteBuffer  imageData = MemoryUtil.memByteBuffer(Objects.requireNonNull(image.data()));
            
            IntBuffer advance = stack.mallocInt(1);
            IntBuffer bearing = stack.mallocInt(1);
            
            IntBuffer x0 = stack.mallocInt(1);
            IntBuffer y0 = stack.mallocInt(1);
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            
            for (Integer glyph : containers.keySet())
            {
                BinaryPacker.Container<ByteBuffer> container = containers.get(glyph);
                
                ByteBuffer bitmap = container.object();
                
                stbtt_GetGlyphHMetrics(this.info, glyph, advance, bearing);
                stbtt_GetGlyphBox(this.info, glyph, x0, y0, x1, y1);
                
                int _x0 = x0.get(0);
                int _y0 = this.ascentUnscaled - y1.get(0);
                int _x1 = x1.get(0);
                int _y1 = this.ascentUnscaled - y0.get(0);
                
                double u0 = (double) container.x() / (double) result.width();
                double v0 = (double) container.y() / (double) result.height();
                double u1 = (double) (container.x() + container.width()) / (double) result.width();
                double v1 = (double) (container.y() + container.height()) / (double) result.height();
                
                if (bitmap != nullBuffer)
                {
                    for (int y = 0; y < container.height(); y++)
                    {
                        for (int x = 0; x < container.width(); x++)
                        {
                            byte value = bitmap.get(y * container.width() + x);
                            
                            int index = (container.y() + y) * result.width() + (container.x() + x);
                            imageData.put(index, value);
                        }
                    }
                }
                
                glyphData.put(glyph, new GlyphData(glyph, advance.get(0), bearing.get(0), _x0, _y0, _x1, _y1, u0, v0, u1, v1));
            }
            
            this.charData  = Collections.unmodifiableList(charData);
            this.glyphData = Collections.unmodifiableMap(glyphData);
            
            image.save(Path.of("SDFFont.png"));
            
            this.texture = new Texture2D(image);
            //this.texture.filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
            
            image.delete();
            
            for (BinaryPacker.Container<ByteBuffer> container : containers.values())
            {
                ByteBuffer bitmap = container.object();
                if (bitmap != nullBuffer) stbtt_FreeSDF(bitmap);
            }
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
    
    public double kerningAdvanceUnscaled(GlyphData glyph1, GlyphData glyph2)
    {
        if (glyph1 == null) return 0.0;
        if (glyph2 == null) return 0.0;
        if (!this.kerning) return 0.0;
        return stbtt_GetGlyphKernAdvance(this.info, glyph1.glyph(), glyph2.glyph());
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
        
        GlyphData prevGlyph = null, currGlyph;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            CharData ch = this.charData.get(line.charAt(i));
            currGlyph = this.glyphData.get(ch.glyph());
            
            double charWidth = currGlyph.advance() + kerningAdvanceUnscaled(prevGlyph, currGlyph);
            
            width += charWidth * scale;
            
            prevGlyph = currGlyph;
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
