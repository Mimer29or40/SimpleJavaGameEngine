package engine;

import engine.color.Color;
import engine.color.Colorc;
import engine.font.CharData;
import engine.font.Font;
import engine.font.GlyphData;
import engine.font.TextAlign;
import engine.gl.Framebuffer;
import engine.gl.GLType;
import engine.gl.buffer.BufferUniform;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // ---------- Global State ---------- //
    
    private static int vertexCount;
    
    private static FloatBuffer positionBuffer; // (XYZ)  (shader-location = 0)
    private static FloatBuffer normalBuffer;   // (XYZ)  (shader-location = 1)
    private static FloatBuffer tangentBuffer;  // (XYZ)  (shader-location = 2)
    private static FloatBuffer texCoordBuffer; // (UV)   (shader-location = 3)
    private static ByteBuffer  colorBuffer;    // (RGBA) (shader-location = 4)
    
    private static VertexArray vertexArray;
    
    private static BufferUniform viewBuffer;
    
    // ---------- Lines State ---------- //
    
    private static Program linesProgram;
    
    private static double linesThickness;
    private static Color  linesColorStart;
    private static Color  linesColorEnd;
    
    // ---------- Text State ---------- //
    
    private static Font DEFAULT_FONT;
    
    private static Program textProgram;
    
    private static double    textSize;
    private static Color     textColor;
    private static TextAlign textAlign;
    private static Font      textFont;
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
        
        int quadCount = 8192;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
        
        Renderer.vertexCount = 0;
        
        Renderer.positionBuffer = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 0)
        Renderer.normalBuffer   = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 1)
        Renderer.tangentBuffer  = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 2)
        Renderer.texCoordBuffer = MemoryUtil.memAllocFloat(vertexCount * 2); // (UV)   (shader-location = 3)
        Renderer.colorBuffer    = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 4)
        
        VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute normal   = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute tangent  = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
        VertexAttribute color    = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
        
        Renderer.vertexArray = VertexArray.builder()
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.positionBuffer.clear(), position)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.normalBuffer.clear(), normal)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.tangentBuffer.clear(), tangent)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.texCoordBuffer.clear(), texCoord)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.colorBuffer.clear(), color)
                                          .build();
        
        Renderer.viewBuffer = new BufferUniform(BufferUsage.DYNAMIC_DRAW, Float.BYTES * 16);
        Renderer.viewBuffer.base(0);
        rendererView(new Matrix4d());
        
        // ----- Lines ----- //
        {
            Shader linesVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/lines.vert"));
            Shader linesGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/lines.geom"));
            Shader linesFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/lines.frag"));
            
            Renderer.linesProgram = new Program(linesVert, linesGeom, linesFrag);
            
            linesVert.delete();
            linesGeom.delete();
            linesFrag.delete();
            
            Renderer.linesThickness  = 10.0;
            Renderer.linesColorStart = new Color(Color.WHITE);
            Renderer.linesColorEnd   = new Color(Color.WHITE);
        }
        
        // ----- Text ----- //
        {
            Shader textVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/text.vert"));
            Shader textFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/text.frag"));
            
            Renderer.textProgram = new Program(textVert, textFrag);
            
            textVert.delete();
            textFrag.delete();
            
            Renderer.DEFAULT_FONT = new Font(IOUtil.getPath("font/PressStart2P/PressStart2P.ttf"), true);
            
            Renderer.textSize  = 24.0;
            Renderer.textColor = new Color(Color.WHITE);
            Renderer.textAlign = TextAlign.TOP_LEFT;
            Renderer.textFont  = Renderer.DEFAULT_FONT;
        }
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
        
        MemoryUtil.memFree(Renderer.positionBuffer);
        MemoryUtil.memFree(Renderer.normalBuffer);
        MemoryUtil.memFree(Renderer.tangentBuffer);
        MemoryUtil.memFree(Renderer.texCoordBuffer);
        MemoryUtil.memFree(Renderer.colorBuffer);
        Renderer.vertexArray.delete();
        
        Renderer.linesProgram.delete();
        
        Renderer.textProgram.delete();
        Renderer.DEFAULT_FONT.delete();
    }
    
    // -------------------- Global -------------------- //
    
    public static void rendererView(@NotNull Matrix4dc view)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            Renderer.viewBuffer.set(0, view.get(stack.mallocFloat(16)));
        }
    }
    
    // -------------------- Lines -------------------- //
    
    public static void linesThickness(double thickness)
    {
        Renderer.linesThickness = thickness;
    }
    
    public static void linesColor(@NotNull Colorc color)
    {
        Renderer.linesColorStart.set(color);
        Renderer.linesColorEnd.set(color);
    }
    
    public static void linesColorStart(@NotNull Colorc color)
    {
        Renderer.linesColorStart.set(color);
    }
    
    public static void linesColorEnd(@NotNull Colorc color)
    {
        Renderer.linesColorEnd.set(color);
    }
    
    public static void linesDraw(double @NotNull ... points)
    {
        int pointCount = points.length / 3;
        
        // First coordinate get repeated.
        linesVertex(points[0], points[1], points[2], Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int index = 0; index < pointCount; index++)
        {
            double t = (double) index / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            int i = index * 3;
            linesVertex(points[i], points[i + 1], points[i + 2], lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 3], points[points.length - 2], points[points.length - 1], Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDraw(Vector3d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, points[0].z, Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, points[i].z, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, points[points.length - 1].z, Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDraw(Vector2d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, 0, Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, 0, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, 0, Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    private static void linesVertex(double x, double y, double z, @NotNull Colorc color)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put((float) z);
        
        Renderer.colorBuffer.put((byte) color.r());
        Renderer.colorBuffer.put((byte) color.g());
        Renderer.colorBuffer.put((byte) color.b());
        Renderer.colorBuffer.put((byte) color.a());
        
        Renderer.vertexCount++;
    }
    
    private static void linesDraw()
    {
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.linesProgram);
        Program.uniformBlock("View", 0);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        Program.uniformFloat("thickness", Renderer.linesThickness);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(4).set(0, Renderer.colorBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.LINE_STRIP_ADJACENCY, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.colorBuffer.clear();
    }
    
    // -------------------- Text -------------------- //
    
    public static void textSize(double size)
    {
        Renderer.textSize = size;
    }
    
    public static void textColor(@NotNull Colorc color)
    {
        Renderer.textColor.set(color);
    }
    
    public static void textAlign(@NotNull TextAlign align)
    {
        Renderer.textAlign = align;
    }
    
    public static void textFont(@NotNull Font font)
    {
        Renderer.textFont = font;
    }
    
    public static double textWidth(@NotNull String text, int maxWidth)
    {
        return Renderer.textFont.textWidth(text, Renderer.textSize, maxWidth);
    }
    
    public static double textWidth(@NotNull String text)
    {
        return Renderer.textFont.textWidth(text, Renderer.textSize);
    }
    
    public static double textHeight(@NotNull String text, int maxWidth)
    {
        return Renderer.textFont.textHeight(text, Renderer.textSize, maxWidth);
    }
    
    public static double textHeight(@NotNull String text)
    {
        return Renderer.textFont.textHeight(text, Renderer.textSize);
    }
    
    // TODO - textDrawBillboard
    
    public static void textDraw(@NotNull String text, double x, double y, double width, double height)
    {
        List<String> lines = Renderer.textFont.splitText(text, Renderer.textSize, width);
        
        double actualHeight = textHeight(text);
        
        int hPos = Renderer.textAlign.getH(), vPos = Renderer.textAlign.getV();
        
        double yOffset = vPos == -1 ? 0 : vPos == 0 ? 0.5 * (height - actualHeight) : height - actualHeight;
        for (String line : lines)
        {
            double lineWidth  = textWidth(line);
            double lineHeight = textHeight(line);
            
            double xOffset = hPos == -1 ? 0 : hPos == 0 ? 0.5 * (width - lineWidth) : width - lineWidth;
            
            textBuildVertices(line, x + xOffset, y + yOffset);
            
            yOffset += lineHeight;
        }
        
        textDraw();
    }
    
    public static void textDraw(@NotNull String text, double x, double y)
    {
        textDraw(text, x, y, 0, 0);
    }
    
    private static void textBuildVertices(@NotNull String line, double x, double y)
    {
        double scale = Renderer.textFont.scale(Renderer.textSize);
        
        GlyphData prevGlyph = null, currGlyph;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            CharData charData = Renderer.textFont.charData.get(line.charAt(i));
            currGlyph = Renderer.textFont.glyphData.get(charData.glyph());
            
            x += Renderer.textFont.kerningAdvanceUnscaled(prevGlyph, currGlyph) * scale;
            
            double x0 = x + currGlyph.x0() * scale;
            double y0 = y + currGlyph.y0() * scale;
            double x1 = x + currGlyph.x1() * scale;
            double y1 = y + currGlyph.y1() * scale;
            
            textVertex(x0, y0, 0, currGlyph.u0(), currGlyph.v0()); // Vertex 0
            textVertex(x0, y1, 0, currGlyph.u0(), currGlyph.v1()); // Vertex 1
            textVertex(x1, y1, 0, currGlyph.u1(), currGlyph.v1()); // Vertex 2
            
            textVertex(x0, y0, 0, currGlyph.u0(), currGlyph.v0()); // Vertex 0
            textVertex(x1, y1, 0, currGlyph.u1(), currGlyph.v1()); // Vertex 2
            textVertex(x1, y0, 0, currGlyph.u1(), currGlyph.v0()); // Vertex 3
            
            x += currGlyph.advance() * scale;
            
            prevGlyph = currGlyph;
        }
    }
    
    private static void textVertex(double x, double y, double z, double u, double v)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put((float) z);
        
        Renderer.texCoordBuffer.put((float) u);
        Renderer.texCoordBuffer.put((float) v);
        
        Renderer.vertexCount++;
    }
    
    private static void textDraw()
    {
        Texture.bind(Renderer.textFont.texture, 0);
        
        Program.bind(Renderer.textProgram);
        Program.uniformBlock("View", 0);
        Program.uniformInt("fontTexture", 0);
        Program.uniformColor("fontColor", Renderer.textColor);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(3).set(0, Renderer.texCoordBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.TRIANGLES, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.texCoordBuffer.clear();
    }
    
    private Renderer() {}
}
