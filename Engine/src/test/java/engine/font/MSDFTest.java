package engine.font;

import engine.util.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBTruetype.*;

public class MSDFTest
{
    STBTTFontinfo info;
    ByteBuffer    fileData;
    
    void setup()
    {
        Path filePath = IOUtil.getPath("font/PressStart2P/PressStart2P.ttf");
        //Path filePath = IOUtil.getPath("c:/windows/fonts/times.ttf");
        
        this.info     = STBTTFontinfo.malloc();
        this.fileData = IOUtil.readFromFile(filePath, new int[1], MemoryUtil::memAlloc);
        
        if (this.fileData == null || !stbtt_InitFont(this.info, this.fileData)) throw new RuntimeException("Font Data could not be loaded: " + filePath);
    }
    
    void run(@NotNull MemoryStack stack)
    {
        int glyph = stbtt_FindGlyphIndex(this.info, 'A');
        
        PointerBuffer      verticesPointer = stack.mallocPointer(1);
        int                vertexCount     = stbtt_GetGlyphShape(this.info, glyph, verticesPointer);
        STBTTVertex.Buffer vertices        = STBTTVertex.create(verticesPointer.get(0), vertexCount);
        
        vertices.forEach(v -> System.out.printf("STBTTVertex{x=%s, y=%s, cx=%s, cy=%s, cx1=%s, cy1=%s, type=%s}%n", v.x(), v.y(), v.cx(), v.cy(), v.cx1(), v.cy1(), v.type()));
    }
    
    void destroy()
    {
        MemoryUtil.memFree(this.fileData);
        this.info.free();
    }
    
    public static void main(String[] args)
    {
        MSDFTest test = new MSDFTest();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            test.setup();
            test.run(stack);
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.out);
        }
        finally
        {
            test.destroy();
        }
    }
}
