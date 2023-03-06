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
    
    private static FloatBuffer positionBuffer;  // (XYZW) (shader-location = 0)
    private static FloatBuffer texCoordBuffer;  // (UV)   (shader-location = 1)
    private static ByteBuffer  color0Buffer;    // (RGBA) (shader-location = 2)
    private static ByteBuffer  color1Buffer;    // (RGBA) (shader-location = 3)
    
    private static VertexArray vertexArray;
    
    private static Batch batch = Batch.NONE;
    
    // -------------------- View -------------------- //
    
    private static Matrix4d      view;
    private static boolean       updateViewBuffer;
    private static BufferUniform viewBuffer;
    
    // ---------- Points State ---------- //
    
    private static Program pointProgram;
    
    private static double pointSize;
    private static Color  pointColor;
    
    // ---------- Lines State ---------- //
    
    private static Program lineProgram;
    
    private static double lineThicknessStart;
    private static double lineThicknessEnd;
    private static Color  lineColorStart;
    private static Color  lineColorEnd;
    private static int    lineBezierDivisions;
    
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
        
        Renderer.positionBuffer = MemoryUtil.memAllocFloat(vertexCount * 4); // (XYZW) (shader-location = 0)
        Renderer.texCoordBuffer = MemoryUtil.memAllocFloat(vertexCount * 2); // (UV)   (shader-location = 1)
        Renderer.color0Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 2)
        Renderer.color1Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 3)
        
        VertexAttribute position = new VertexAttribute(GLType.FLOAT, 4, false);
        VertexAttribute texCoord = new VertexAttribute(GLType.FLOAT, 2, false);
        VertexAttribute color0   = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
        VertexAttribute color1   = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
        
        Renderer.vertexArray = VertexArray.builder()
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.positionBuffer.clear(), position)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.texCoordBuffer.clear(), texCoord)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color0Buffer.clear(), color0)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color1Buffer.clear(), color1)
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
            
            Program.bind(Renderer.pointProgram = new Program(pointVert, pointGeom, pointFrag));
            Program.uniformBlock("View", 0);
            
            pointVert.delete();
            pointGeom.delete();
            pointFrag.delete();
            
            Renderer.pointSize  = 10.0;
            Renderer.pointColor = new Color(Color.WHITE);
        }
        
        // ----- Lines ----- //
        {
            Shader linesVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/lines.vert"));
            Shader linesGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/lines.geom"));
            Shader linesFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/lines.frag"));
            
            Program.bind(Renderer.lineProgram = new Program(linesVert, linesGeom, linesFrag));
            Program.uniformBlock("View", 0);
            
            linesVert.delete();
            linesGeom.delete();
            linesFrag.delete();
            
            Renderer.lineThicknessStart  = 10.0;
            Renderer.lineThicknessEnd    = 10.0;
            Renderer.lineColorStart      = new Color(Color.WHITE);
            Renderer.lineColorEnd        = new Color(Color.WHITE);
            Renderer.lineBezierDivisions = 24;
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
        MemoryUtil.memFree(Renderer.color0Buffer);
        MemoryUtil.memFree(Renderer.color1Buffer);
        Renderer.vertexArray.delete();
        
        Renderer.pointProgram.delete();
        
        Renderer.lineProgram.delete();
        
        Renderer.ellipseProgram.delete();
        
        Renderer.textProgram.delete();
        Renderer.DEFAULT_FONT.delete();
    }
    
    // -------------------- Vertices -------------------- //
    
    private static void position(double x, double y, double z, double w)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put((float) z);
        Renderer.positionBuffer.put((float) w);
    }
    
    private static void texCoord(double u, double V)
    {
        Renderer.texCoordBuffer.put((float) u);
        Renderer.texCoordBuffer.put((float) V);
    }
    
    private static void color0(@NotNull Colorc color)
    {
        Renderer.color0Buffer.put((byte) color.r());
        Renderer.color0Buffer.put((byte) color.g());
        Renderer.color0Buffer.put((byte) color.b());
        Renderer.color0Buffer.put((byte) color.a());
    }
    
    private static void color1(@NotNull Colorc color)
    {
        Renderer.color1Buffer.put((byte) color.r());
        Renderer.color1Buffer.put((byte) color.g());
        Renderer.color1Buffer.put((byte) color.b());
        Renderer.color1Buffer.put((byte) color.a());
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
    
    public static void pointSize(double size)
    {
        Renderer.pointSize = size;
    }
    
    public static void pointColor(@NotNull Colorc color)
    {
        Renderer.pointColor.set(color);
    }
    
    public static void pointBatchBegin()
    {
        if (Renderer.batch != Batch.NONE) throw new IllegalStateException("Batch was never ended: " + Renderer.batch);
        Renderer.batch = Batch.POINT;
    }
    
    public static void pointBatchEnd()
    {
        if (Renderer.batch != Batch.POINT) throw new IllegalStateException("Point Batch was not started");
        Renderer.batch = Batch.NONE;
        
        pointDrawBuffer();
    }
    
    public static void pointDraw(double x, double y)
    {
        if (Renderer.batch != Batch.NONE && Renderer.batch != Batch.POINT) throw new IllegalStateException(Renderer.batch + " is active");
        pointVertex(x, y, Renderer.pointSize, Renderer.pointColor);
        
        if (Renderer.batch == Batch.NONE) pointDrawBuffer();
    }
    
    private static void pointVertex(double x, double y, double thickness, @NotNull Colorc color)
    {
        position(x, y, thickness, 0);
        color0(color);
        
        Renderer.vertexCount++;
    }
    
    private static void pointDrawBuffer()
    {
        viewUpdateBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.pointProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(2).set(0, Renderer.color0Buffer.flip());
        Renderer.vertexArray.draw(DrawMode.POINTS, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.color0Buffer.clear();
    }
    
    // -------------------- Lines -------------------- //
    
    public static void lineThickness(double thickness)
    {
        Renderer.lineThicknessStart = thickness;
        Renderer.lineThicknessEnd   = thickness;
    }
    
    public static void lineThicknessStart(double thickness)
    {
        Renderer.lineThicknessStart = thickness;
    }
    
    public static void lineThicknessEnd(double thickness)
    {
        Renderer.lineThicknessEnd = thickness;
    }
    
    public static void lineColor(@NotNull Colorc color)
    {
        Renderer.lineColorStart.set(color);
        Renderer.lineColorEnd.set(color);
    }
    
    public static void lineColorStart(@NotNull Colorc color)
    {
        Renderer.lineColorStart.set(color);
    }
    
    public static void lineColorEnd(@NotNull Colorc color)
    {
        Renderer.lineColorEnd.set(color);
    }
    
    public static void lineBezierDivisions(int divisions)
    {
        Renderer.lineBezierDivisions = divisions;
    }
    
    public static void lineBatchBegin()
    {
        if (Renderer.batch != Batch.NONE) throw new IllegalStateException("Batch was never ended: " + Renderer.batch);
        Renderer.batch = Batch.LINE;
    }
    
    public static void lineBatchEnd()
    {
        if (Renderer.batch != Batch.LINE) throw new IllegalStateException("Line Batch was not started");
        Renderer.batch = Batch.NONE;
        
        lineDrawBuffer();
    }
    
    public static void lineDraw(double @NotNull ... points)
    {
        int count = points.length >> 1;
        
        double[] thickness = new double[count];
        Color[]  color     = new Color[count];
        
        for (int i = 0; i < count; i++)
        {
            double t = (double) i / (count - 1);
            
            thickness[i] = Renderer.lineThicknessStart * (1.0 - t) + Renderer.lineThicknessEnd * t;
            color[i]     = Renderer.lineColorStart.interpolate(Renderer.lineColorEnd, t, new Color());
        }
        
        for (int p1 = 0; p1 < count - 1; p1++)
        {
            int p0 = Math.max(p1 - 1, 0);
            int p2 = Math.min(p1 + 1, count - 1);
            int p3 = Math.min(p1 + 2, count - 1);
            
            lineVertex(points[p0 << 1], points[(p0 << 1) + 1], thickness[p0], color[p0]);
            lineVertex(points[p1 << 1], points[(p1 << 1) + 1], thickness[p1], color[p1]);
            lineVertex(points[p2 << 1], points[(p2 << 1) + 1], thickness[p2], color[p2]);
            lineVertex(points[p3 << 1], points[(p3 << 1) + 1], thickness[p3], color[p3]);
        }
        
        if (Renderer.batch == Batch.NONE) lineDrawBuffer();
    }
    
    public static void lineDrawEnclosed(double @NotNull ... points)
    {
        int count = points.length >> 1;
        
        double[] thickness = new double[count];
        Color[]  color     = new Color[count];
        
        for (int i = 0; i < count; i++)
        {
            double t = (double) i / (count - 1);
            
            thickness[i] = Renderer.lineThicknessStart * (1.0 - t) + Renderer.lineThicknessEnd * t;
            color[i]     = Renderer.lineColorStart.interpolate(Renderer.lineColorEnd, t, new Color());
        }
        
        for (int p1 = 0; p1 < count; p1++)
        {
            int p0 = (p1 - 1 + count) % count;
            int p2 = (p1 + 1 + count) % count;
            int p3 = (p1 + 2 + count) % count;
            
            lineVertex(points[p0 << 1], points[(p0 << 1) + 1], thickness[p0], color[p0]);
            lineVertex(points[p1 << 1], points[(p1 << 1) + 1], thickness[p1], color[p1]);
            lineVertex(points[p2 << 1], points[(p2 << 1) + 1], thickness[p2], color[p2]);
            lineVertex(points[p3 << 1], points[(p3 << 1) + 1], thickness[p3], color[p3]);
        }
        
        if (Renderer.batch == Batch.NONE) lineDrawBuffer();
    }
    
    public static void lineDrawBezier(double @NotNull ... controlPoints)
    {
        int count = controlPoints.length >> 1;
        int order = count - 1;
        
        double[] points = new double[Renderer.lineBezierDivisions * 2];
        for (int i = 0, index = 0; i < Renderer.lineBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer.lineBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * controlPoints[(j << 1)];
                y += coeff * controlPoints[(j << 1) + 1];
            }
            points[index++] = x;
            points[index++] = y;
        }
        lineDraw(points);
    }
    
    private static void lineVertex(double x, double y, double thickness, @NotNull Colorc color)
    {
        position(x, y, thickness, 0);
        color0(color);
        
        Renderer.vertexCount++;
    }
    
    private static void lineDrawBuffer()
    {
        viewUpdateBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.lineProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(2).set(0, Renderer.color0Buffer.flip());
        Renderer.vertexArray.draw(DrawMode.LINES_ADJACENCY, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.color0Buffer.clear();
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
    
    public static void ellipseBatchBegin()
    {
        if (Renderer.batch != Batch.NONE) throw new IllegalStateException("Batch was never ended: " + Renderer.batch);
        Renderer.batch = Batch.ELLIPSE;
    }
    
    public static void ellipseBatchEnd()
    {
        if (Renderer.batch != Batch.ELLIPSE) throw new IllegalStateException("Ellipse Batch was not started");
        Renderer.batch = Batch.NONE;
        
        ellipseDrawBuffer();
    }
    
    public static void ellipseDraw(double x, double y, double w, double h)
    {
        if (Renderer.batch != Batch.NONE && Renderer.batch != Batch.ELLIPSE) throw new IllegalStateException(Renderer.batch + " is active");
        ellipseVertex(x, y, w, h, Renderer.ellipseColorInner, Renderer.ellipseColorOuter);
        
        if (Renderer.batch == Batch.NONE) ellipseDrawBuffer();
    }
    
    private static void ellipseVertex(double x, double y, double w, double h, @NotNull Colorc inner, @NotNull Colorc outer)
    {
        position(x, y, w, h);
        color0(inner);
        color1(outer);
        
        Renderer.vertexCount++;
    }
    
    private static void ellipseDrawBuffer()
    {
        viewUpdateBuffer();
        
        Program.bind(Renderer.ellipseProgram);
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(2).set(0, Renderer.color0Buffer.flip());
        Renderer.vertexArray.buffer(3).set(0, Renderer.color1Buffer.flip());
        Renderer.vertexArray.draw(DrawMode.POINTS, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.color0Buffer.clear();
        Renderer.color1Buffer.clear();
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
        
        textDrawBuffer();
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
        position(x, y, 0, 0);
        texCoord(u, v);
        
        Renderer.vertexCount++;
    }
    
    private static void textDrawBuffer()
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
    
    private enum Batch
    {
        NONE,
        POINT,
        LINE,
        ELLIPSE,
        TEXT
    }
}
