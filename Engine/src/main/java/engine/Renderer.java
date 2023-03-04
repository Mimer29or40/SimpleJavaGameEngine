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
import org.joml.Runtime;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // ---------- Global State ---------- //
    
    private static int vertexCount;
    
    private static FloatBuffer positionBuffer;  // (XYZ)  (shader-location = 0)
    private static FloatBuffer texCoordBuffer;  // (UV)   (shader-location = 1)
    private static ByteBuffer  colorBuffer;     // (RGBA) (shader-location = 2)
    
    private static VertexArray vertexArray;
    
    // -------------------- View -------------------- //
    
    private static Matrix4d      view;
    private static boolean       updateViewBuffer;
    private static BufferUniform viewBuffer;
    
    // ---------- Points State ---------- //
    
    private static Program pointsProgram;
    
    private static double pointsSize;
    private static Color  pointsColor;
    
    // ---------- Lines State ---------- //
    
    private static Program linesProgram;
    
    private static double linesThickness;
    private static Color  linesColorStart;
    private static Color  linesColorEnd;
    private static int    linesBezierDivisions;
    
    // ---------- Ellipse State ---------- //
    
    private static Program ellipseProgram;
    
    private static Color ellipseColorInner;
    private static Color ellipseColorOuter;
    
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
        Renderer.texCoordBuffer = MemoryUtil.memAllocFloat(vertexCount * 2); // (UV)   (shader-location = 1)
        Renderer.colorBuffer    = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 2)
        
        VertexAttribute position = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
        VertexAttribute color    = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
        
        Renderer.vertexArray = VertexArray.builder()
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.positionBuffer.clear(), position)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.texCoordBuffer.clear(), texCoord)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.colorBuffer.clear(), color)
                                          .build();
        
        Renderer.view             = new Matrix4d();
        Renderer.updateViewBuffer = true;
        Renderer.viewBuffer       = new BufferUniform(BufferUsage.DYNAMIC_DRAW, Float.BYTES * 16);
        Renderer.viewBuffer.base(0);
        
        // ----- Point ----- //
        {
            Shader pointVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/point.vert"));
            Shader pointGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/point.geom"));
            Shader pointFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/point.frag"));
            
            Program.bind(Renderer.pointsProgram = new Program(pointVert, pointGeom, pointFrag));
            Program.uniformBlock("View", 0);
            
            pointVert.delete();
            pointGeom.delete();
            pointFrag.delete();
            
            Renderer.pointsSize  = 10.0;
            Renderer.pointsColor = new Color(Color.WHITE);
        }
        
        // ----- Lines ----- //
        {
            Shader linesVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/lines.vert"));
            Shader linesGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/lines.geom"));
            Shader linesFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/lines.frag"));
            
            Program.bind(Renderer.linesProgram = new Program(linesVert, linesGeom, linesFrag));
            Program.uniformBlock("View", 0);
            
            linesVert.delete();
            linesGeom.delete();
            linesFrag.delete();
            
            Renderer.linesThickness       = 10.0;
            Renderer.linesColorStart      = new Color(Color.WHITE);
            Renderer.linesColorEnd        = new Color(Color.WHITE);
            Renderer.linesBezierDivisions = 24;
        }
        
        // ----- Ellipse ----- //
        {
            Shader ellipseVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/ellipse.vert"));
            Shader ellipseGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/ellipse.geom"));
            Shader ellipseFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/ellipse.frag"));
            
            Program.bind(Renderer.ellipseProgram = new Program(ellipseVert, ellipseGeom, ellipseFrag));
            Program.uniformBlock("View", 0);
            
            ellipseVert.delete();
            ellipseGeom.delete();
            ellipseFrag.delete();
            
            Renderer.ellipseColorInner = new Color(Color.WHITE);
            Renderer.ellipseColorOuter = new Color(Color.WHITE);
        }
        
        // ----- Text ----- //
        {
            Shader textVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/text.vert"));
            Shader textFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/text.frag"));
            
            Program.bind(Renderer.textProgram = new Program(textVert, textFrag));
            Program.uniformBlock("View", 0);
            Program.uniformInt("fontTexture", 0);
            
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
        MemoryUtil.memFree(Renderer.texCoordBuffer);
        MemoryUtil.memFree(Renderer.colorBuffer);
        Renderer.vertexArray.delete();
        
        Renderer.pointsProgram.delete();
        
        Renderer.linesProgram.delete();
        
        Renderer.ellipseProgram.delete();
        
        Renderer.textProgram.delete();
        Renderer.DEFAULT_FONT.delete();
    }
    
    // -------------------- View -------------------- //
    
    public static void viewIdentity()
    {
        Framebuffer fb = Framebuffer.get();
        
        Renderer.view.setOrtho(0.0, fb.width(), fb.height(), 0.0, -1.0, 1.0);
        Renderer.updateViewBuffer = true;
    }
    
    public static void viewTranslate(double x, double y)
    {
        if (!Runtime.equals(x, 0.0, 1e-9) || !Runtime.equals(y, 0.0, 1e-9))
        {
            Renderer.view.translate(x, y, 0.0);
            Renderer.updateViewBuffer = true;
        }
    }
    
    public static void viewTranslate(@NotNull Vector2dc pos)
    {
        viewTranslate(pos.x(), pos.y());
    }
    
    public static void viewRotate(double angle)
    {
        if (!Runtime.equals(angle, 0.0, 1e-9))
        {
            Renderer.view.rotate(angle, 0, 0, 1);
            Renderer.updateViewBuffer = true;
        }
    }
    
    public static void viewScale(double sx, double sy)
    {
        if (!Runtime.equals(sx, 1.0, 1e-9) || !Runtime.equals(sy, 1.0, 1e-9))
        {
            Renderer.view.scale(sx, sy, 1.0);
            Renderer.updateViewBuffer = true;
        }
    }
    
    public static void viewScale(double scale)
    {
        viewScale(scale, scale);
    }
    
    public static void viewScale(@NotNull Vector2dc scale)
    {
        viewScale(scale.x(), scale.y());
    }
    
    private static void viewUpdateBuffer()
    {
        if (Renderer.updateViewBuffer)
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                Renderer.viewBuffer.set(0, Renderer.view.get(stack.mallocFloat(16)));
            }
            Renderer.updateViewBuffer = false;
        }
    }
    
    // -------------------- Point -------------------- //
    
    public static void pointsSize(double size)
    {
        Renderer.pointsSize = size;
    }
    
    public static void pointsColor(@NotNull Colorc color)
    {
        Renderer.pointsColor.set(color);
    }
    
    public static void pointsDraw(double @NotNull ... points)
    {
        int count = points.length >> 1;
        for (int i = 0, index = 0; i < count; i++)
        {
            pointsVertex(points[index++], points[index++]);
        }
        
        pointsDraw();
    }
    
    private static void pointsVertex(double x, double y)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put(0);
        
        Renderer.vertexCount++;
    }
    
    private static void pointsDraw()
    {
        viewUpdateBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.pointsProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        Program.uniformFloat("size", Renderer.pointsSize);
        Program.uniformColor("color", Renderer.pointsColor);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.POINTS, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
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
    
    public static void linesBezierDivisions(int divisions)
    {
        Renderer.linesBezierDivisions = divisions;
    }
    
    public static void linesDraw(double @NotNull ... points)
    {
        int pointCount = points.length >> 1;
        
        // First coordinate get repeated.
        linesVertex(points[0], points[1], Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0, index = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[index++], points[index++], lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 2], points[points.length - 1], Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDraw(Vector2d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawEnclosed(double @NotNull ... points)
    {
        int pointCount = points.length >> 1;
        
        // Last coordinate is first.
        linesVertex(points[points.length - 2], points[points.length - 1], Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0, index = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[index++], points[index++], lerp);
        }
        
        // First coordinate is last.
        linesVertex(points[0], points[1], Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawEnclosed(Vector2d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // Last coordinate is first.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, lerp);
        }
        
        // First coordinate is last.
        linesVertex(points[0].x, points[0].y, Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawBezier(double @NotNull ... points)
    {
        int count = points.length >> 1;
        int order = count - 1;
        
        // First coordinate get repeated.
        linesVertex(points[0], points[1], Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i <= Renderer.linesBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer.linesBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * points[(j << 1)];
                y += coeff * points[(j << 1) + 1];
            }
            linesVertex(x, y, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 2], points[points.length - 1], Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawBezier(Vector2d @NotNull ... points)
    {
        int count = points.length;
        int order = count - 1;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, Renderer.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i <= Renderer.linesBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer.linesBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            Renderer.linesColorStart.interpolate(Renderer.linesColorEnd, t, lerp);
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * points[j].x;
                y += coeff * points[j].y;
            }
            linesVertex(x, y, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, Renderer.linesColorEnd);
        
        linesDraw();
    }
    
    private static void linesVertex(double x, double y, @NotNull Colorc color)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put(0);
        
        Renderer.colorBuffer.put((byte) color.r());
        Renderer.colorBuffer.put((byte) color.g());
        Renderer.colorBuffer.put((byte) color.b());
        Renderer.colorBuffer.put((byte) color.a());
        
        Renderer.vertexCount++;
    }
    
    private static void linesDraw()
    {
        viewUpdateBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.linesProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        Program.uniformFloat("thickness", Renderer.linesThickness);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(2).set(0, Renderer.colorBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.LINE_STRIP_ADJACENCY, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.colorBuffer.clear();
    }
    
    // -------------------- Ellipse -------------------- //
    
    public static void ellipseColor(@NotNull Colorc color)
    {
        Renderer.ellipseColorInner.set(color);
        Renderer.ellipseColorOuter.set(color);
    }
    
    public static void ellipseColorInner(@NotNull Colorc color)
    {
        Renderer.ellipseColorInner.set(color);
    }
    
    public static void ellipseColorOuter(@NotNull Colorc color)
    {
        Renderer.ellipseColorOuter.set(color);
    }
    
    public static void ellipseDraw(double x, double y)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put(0);
        
        Renderer.vertexCount++;
        
        ellipseDraw();
    }
    
    private static void ellipseDraw()
    {
        viewUpdateBuffer();
        
        Program.bind(Renderer.ellipseProgram);
        Program.uniformFloat2("size", 200, 100);
        Program.uniformColor("colorInner", Renderer.ellipseColorInner);
        Program.uniformColor("colorOuter", Renderer.ellipseColorOuter);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.POINTS, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
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
            
            textVertex(x0, y0, currGlyph.u0(), currGlyph.v0()); // Vertex 0
            textVertex(x0, y1, currGlyph.u0(), currGlyph.v1()); // Vertex 1
            textVertex(x1, y1, currGlyph.u1(), currGlyph.v1()); // Vertex 2
            
            textVertex(x0, y0, currGlyph.u0(), currGlyph.v0()); // Vertex 0
            textVertex(x1, y1, currGlyph.u1(), currGlyph.v1()); // Vertex 2
            textVertex(x1, y0, currGlyph.u1(), currGlyph.v0()); // Vertex 3
            
            x += currGlyph.advance() * scale;
            
            prevGlyph = currGlyph;
        }
    }
    
    private static void textVertex(double x, double y, double u, double v)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put(0);
        
        Renderer.texCoordBuffer.put((float) u);
        Renderer.texCoordBuffer.put((float) v);
        
        Renderer.vertexCount++;
    }
    
    private static void textDraw()
    {
        viewUpdateBuffer();
        
        Texture.bind(Renderer.textFont.texture, 0);
        
        Program.bind(Renderer.textProgram);
        Program.uniformColor("fontColor", Renderer.textColor);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(1).set(0, Renderer.texCoordBuffer.flip());
        Renderer.vertexArray.draw(DrawMode.TRIANGLES, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.texCoordBuffer.clear();
    }
    
    private static final Map<Long, Long> BINOMIAL_CACHE = new HashMap<>();
    
    private static long binomial(int order, int k)
    {
        long hash = (((long) order) << 32) | k;
        return Renderer.BINOMIAL_CACHE.computeIfAbsent(hash, i -> _binomial(order, k));
    }
    
    private static long _binomial(int order, int k)
    {
        if (k > order - k) k = order - k;
        long b = 1;
        for (int i = 1, m = order; i <= k; i++, m--) b = (b * m) / i;
        return b;
    }
    
    private Renderer() {}
}
