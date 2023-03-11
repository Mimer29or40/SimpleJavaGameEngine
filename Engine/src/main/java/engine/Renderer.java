package engine;

import engine.color.Color;
import engine.color.Colorc;
import engine.font.CharData;
import engine.font.Font;
import engine.font.GlyphData;
import engine.font.TextAlign;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.GLType;
import engine.gl.ScreenBuffer;
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
    private static FloatBuffer texCoordBuffer;  // (UVST) (shader-location = 1)
    private static ByteBuffer  color0Buffer;    // (RGBA) (shader-location = 2)
    private static ByteBuffer  color1Buffer;    // (RGBA) (shader-location = 3)
    private static ByteBuffer  color2Buffer;    // (RGBA) (shader-location = 4)
    private static ByteBuffer  color3Buffer;    // (RGBA) (shader-location = 5)
    
    private static VertexArray vertexArray;
    
    private static Batch batch = Batch.NONE;
    
    private static int stackIndex = 0;
    
    // -------------------- View -------------------- //
    
    private static Matrix4d[]    view;
    private static boolean       updateViewBuffer;
    private static BufferUniform viewBuffer;
    
    // ---------- Points State ---------- //
    
    private static Program pointProgram;
    
    private static double[] pointSize;
    private static Color[]  pointColor;
    
    // ---------- Lines State ---------- //
    
    private static Program lineProgram;
    
    private static double[] lineThicknessStart;
    private static double[] lineThicknessEnd;
    private static Color[]  lineColorStart;
    private static Color[]  lineColorEnd;
    private static int[]    lineBezierDivisions;
    
    // ---------- Rect State ---------- //
    
    private static Program rectProgram;
    
    private static Color[] rectColorTL;
    private static Color[] rectColorTR;
    private static Color[] rectColorBL;
    private static Color[] rectColorBR;
    
    // ---------- Ellipse State ---------- //
    
    private static Program ellipseProgram;
    
    private static Color[] ellipseColorInner;
    private static Color[] ellipseColorOuter;
    
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
        int stackSize = 32;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
        
        Renderer.vertexCount = 0;
        
        Renderer.positionBuffer = MemoryUtil.memAllocFloat(vertexCount * 4); // (XYZW) (shader-location = 0)
        Renderer.texCoordBuffer = MemoryUtil.memAllocFloat(vertexCount * 4); // (UV)   (shader-location = 1)
        Renderer.color0Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 2)
        Renderer.color1Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 3)
        Renderer.color2Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 3)
        Renderer.color3Buffer   = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 3)
        
        VertexAttribute float4 = new VertexAttribute(GLType.FLOAT, 4, false);
        VertexAttribute ubyte4 = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
        
        Renderer.vertexArray = VertexArray.builder()
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.positionBuffer.clear(), float4)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.texCoordBuffer.clear(), float4)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color0Buffer.clear(), ubyte4)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color1Buffer.clear(), ubyte4)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color2Buffer.clear(), ubyte4)
                                          .buffer(BufferUsage.DYNAMIC_DRAW, Renderer.color3Buffer.clear(), ubyte4)
                                          .build();
        
        Renderer.view             = new Matrix4d[stackSize];
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
            
            Renderer.pointSize  = new double[stackSize];
            Renderer.pointColor = new Color[stackSize];
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
            
            Renderer.lineThicknessStart  = new double[stackSize];
            Renderer.lineThicknessEnd    = new double[stackSize];
            Renderer.lineColorStart      = new Color[stackSize];
            Renderer.lineColorEnd        = new Color[stackSize];
            Renderer.lineBezierDivisions = new int[stackSize];
        }
        
        // ----- Rect ----- //
        {
            Shader rectVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/rect.vert"));
            Shader rectGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/rect.geom"));
            Shader rectFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/rect.frag"));
            
            Program.bind(Renderer.rectProgram = new Program(rectVert, rectGeom, rectFrag));
            Program.uniformBlock("View", 0);
            
            rectVert.delete();
            rectGeom.delete();
            rectFrag.delete();
            
            Renderer.rectColorTL = new Color[stackSize];
            Renderer.rectColorTR = new Color[stackSize];
            Renderer.rectColorBL = new Color[stackSize];
            Renderer.rectColorBR = new Color[stackSize];
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
            
            Renderer.ellipseColorInner = new Color[stackSize];
            Renderer.ellipseColorOuter = new Color[stackSize];
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
        
        // ----- Defaults ----- //
        for (int i = 0; i < stackSize; i++)
        {
            Renderer.view[i] = new Matrix4d();
            
            Renderer.pointSize[i]  = 5.0;
            Renderer.pointColor[i] = new Color(Color.WHITE);
            
            Renderer.lineThicknessStart[i]  = 5.0;
            Renderer.lineThicknessEnd[i]    = 5.0;
            Renderer.lineColorStart[i]      = new Color();
            Renderer.lineColorEnd[i]        = new Color();
            Renderer.lineBezierDivisions[i] = 24;
            
            Renderer.rectColorTL[i] = new Color(Color.WHITE);
            Renderer.rectColorTR[i] = new Color(Color.WHITE);
            Renderer.rectColorBL[i] = new Color(Color.WHITE);
            Renderer.rectColorBR[i] = new Color(Color.WHITE);
            
            Renderer.ellipseColorInner[i] = new Color(Color.WHITE);
            Renderer.ellipseColorOuter[i] = new Color(Color.WHITE);
        }
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
        
        MemoryUtil.memFree(Renderer.positionBuffer);
        MemoryUtil.memFree(Renderer.texCoordBuffer);
        MemoryUtil.memFree(Renderer.color0Buffer);
        MemoryUtil.memFree(Renderer.color1Buffer);
        MemoryUtil.memFree(Renderer.color2Buffer);
        MemoryUtil.memFree(Renderer.color3Buffer);
        Renderer.vertexArray.delete();
        
        Renderer.pointProgram.delete();
        
        Renderer.lineProgram.delete();
        
        Renderer.rectProgram.delete();
        
        Renderer.ellipseProgram.delete();
        
        Renderer.textProgram.delete();
        Renderer.DEFAULT_FONT.delete();
    }
    
    public static void statePush()
    {
        int i = Renderer.stackIndex++;
        
        Renderer.view[Renderer.stackIndex].set(Renderer.view[i]);
        
        Renderer.pointSize[Renderer.stackIndex] = Renderer.pointSize[i];
        Renderer.pointColor[Renderer.stackIndex].set(Renderer.pointColor[i]);
        
        Renderer.lineThicknessStart[Renderer.stackIndex] = Renderer.lineThicknessStart[i];
        Renderer.lineThicknessEnd[Renderer.stackIndex]   = Renderer.lineThicknessEnd[i];
        Renderer.lineColorStart[Renderer.stackIndex].set(Renderer.lineColorStart[i]);
        Renderer.lineColorEnd[Renderer.stackIndex].set(Renderer.lineColorEnd[i]);
        Renderer.lineBezierDivisions[Renderer.stackIndex] = Renderer.lineBezierDivisions[i];
        
        Renderer.rectColorTL[Renderer.stackIndex].set(Renderer.rectColorTL[i]);
        Renderer.rectColorTR[Renderer.stackIndex].set(Renderer.rectColorTR[i]);
        Renderer.rectColorBL[Renderer.stackIndex].set(Renderer.rectColorBL[i]);
        Renderer.rectColorBR[Renderer.stackIndex].set(Renderer.rectColorBR[i]);
        
        Renderer.ellipseColorInner[Renderer.stackIndex].set(Renderer.ellipseColorInner[i]);
        Renderer.ellipseColorOuter[Renderer.stackIndex].set(Renderer.ellipseColorOuter[i]);
    }
    
    public static void statePop()
    {
        Renderer.stackIndex--;
        
        Renderer.updateViewBuffer = true;
    }
    
    public static void clear(double r, double g, double b, double a)
    {
        GL.clearColor(r, g, b, a);
        GL.clearBuffers(ScreenBuffer.COLOR);
    }
    
    public static void clear(@NotNull Colorc color)
    {
        clear(color.rf(), color.gf(), color.bf(), color.af());
    }
    
    // -------------------- Vertices -------------------- //
    
    private static void position(double x, double y, double z, double w)
    {
        Renderer.positionBuffer.put((float) x);
        Renderer.positionBuffer.put((float) y);
        Renderer.positionBuffer.put((float) z);
        Renderer.positionBuffer.put((float) w);
    }
    
    private static void texCoord(double u, double v, double s, double t)
    {
        Renderer.texCoordBuffer.put((float) u);
        Renderer.texCoordBuffer.put((float) v);
        Renderer.texCoordBuffer.put((float) s);
        Renderer.texCoordBuffer.put((float) t);
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
    
    private static void color2(@NotNull Colorc color)
    {
        Renderer.color2Buffer.put((byte) color.r());
        Renderer.color2Buffer.put((byte) color.g());
        Renderer.color2Buffer.put((byte) color.b());
        Renderer.color2Buffer.put((byte) color.a());
    }
    
    private static void color3(@NotNull Colorc color)
    {
        Renderer.color3Buffer.put((byte) color.r());
        Renderer.color3Buffer.put((byte) color.g());
        Renderer.color3Buffer.put((byte) color.b());
        Renderer.color3Buffer.put((byte) color.a());
    }
    
    private static void drawVertices(@NotNull DrawMode mode)
    {
        if (Renderer.updateViewBuffer)
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                Renderer.viewBuffer.set(0, Renderer.view[Renderer.stackIndex].get(stack.mallocFloat(16)));
            }
            Renderer.updateViewBuffer = false;
        }
        
        VertexArray.bind(Renderer.vertexArray);
        Renderer.vertexArray.buffer(0).set(0, Renderer.positionBuffer.flip());
        Renderer.vertexArray.buffer(1).set(0, Renderer.texCoordBuffer.flip());
        Renderer.vertexArray.buffer(2).set(0, Renderer.color0Buffer.flip());
        Renderer.vertexArray.buffer(3).set(0, Renderer.color1Buffer.flip());
        Renderer.vertexArray.buffer(4).set(0, Renderer.color2Buffer.flip());
        Renderer.vertexArray.buffer(5).set(0, Renderer.color3Buffer.flip());
        Renderer.vertexArray.draw(mode, 0, Renderer.vertexCount);
        
        Renderer.vertexCount = 0;
        Renderer.positionBuffer.clear();
        Renderer.texCoordBuffer.clear();
        Renderer.color0Buffer.clear();
        Renderer.color1Buffer.clear();
        Renderer.color2Buffer.clear();
        Renderer.color3Buffer.clear();
    }
    
    // -------------------- View -------------------- //
    
    public static void viewIdentity()
    {
        Framebuffer fb = Framebuffer.get();
        
        Renderer.view[Renderer.stackIndex].setOrtho(0.0, fb.width(), fb.height(), 0.0, -1.0, 1.0);
        Renderer.updateViewBuffer = true;
    }
    
    public static void viewTranslate(double x, double y)
    {
        if (!Runtime.equals(x, 0.0, 1e-9) || !Runtime.equals(y, 0.0, 1e-9))
        {
            Renderer.view[Renderer.stackIndex].translate(x, y, 0.0);
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
            Renderer.view[Renderer.stackIndex].rotate(angle, 0, 0, 1);
            Renderer.updateViewBuffer = true;
        }
    }
    
    public static void viewScale(double sx, double sy)
    {
        if (!Runtime.equals(sx, 1.0, 1e-9) || !Runtime.equals(sy, 1.0, 1e-9))
        {
            Renderer.view[Renderer.stackIndex].scale(sx, sy, 1.0);
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
    
    // -------------------- Point -------------------- //
    
    public static void pointSize(double size)
    {
        Renderer.pointSize[Renderer.stackIndex] = size;
    }
    
    public static void pointColor(int r, int g, int b, int a)
    {
        Renderer.pointColor[Renderer.stackIndex].set(r, g, b, a);
    }
    
    public static void pointColor(@NotNull Colorc color)
    {
        Renderer.pointColor[Renderer.stackIndex].set(color);
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
        pointVertex(x, y, Renderer.pointSize[Renderer.stackIndex], Renderer.pointColor[Renderer.stackIndex]);
        
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
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.pointProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        
        drawVertices(DrawMode.POINTS);
    }
    
    // -------------------- Lines -------------------- //
    
    public static void lineThickness(double thickness)
    {
        Renderer.lineThicknessStart[Renderer.stackIndex] = thickness;
        Renderer.lineThicknessEnd[Renderer.stackIndex]   = thickness;
    }
    
    public static void lineThicknessStart(double thickness)
    {
        Renderer.lineThicknessStart[Renderer.stackIndex] = thickness;
    }
    
    public static void lineThicknessEnd(double thickness)
    {
        Renderer.lineThicknessEnd[Renderer.stackIndex] = thickness;
    }
    
    public static void lineColor(@NotNull Colorc color)
    {
        Renderer.lineColorStart[Renderer.stackIndex].set(color);
        Renderer.lineColorEnd[Renderer.stackIndex].set(color);
    }
    
    public static void lineColorStart(@NotNull Colorc color)
    {
        Renderer.lineColorStart[Renderer.stackIndex].set(color);
    }
    
    public static void lineColorEnd(@NotNull Colorc color)
    {
        Renderer.lineColorEnd[Renderer.stackIndex].set(color);
    }
    
    public static void lineBezierDivisions(int divisions)
    {
        Renderer.lineBezierDivisions[Renderer.stackIndex] = divisions;
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
        
        double thicknessStart = Renderer.lineThicknessStart[Renderer.stackIndex];
        double thicknessEnd   = Renderer.lineThicknessEnd[Renderer.stackIndex];
        
        Color colorStart = Renderer.lineColorStart[Renderer.stackIndex];
        Color colorEnd   = Renderer.lineColorEnd[Renderer.stackIndex];
        
        double[] thickness = new double[count];
        Color[]  color     = new Color[count];
        
        for (int i = 0; i < count; i++)
        {
            double t = (double) i / (count - 1);
            
            thickness[i] = thicknessStart * (1.0 - t) + thicknessEnd * t;
            color[i]     = colorStart.interpolate(colorEnd, t, new Color());
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
        
        double thicknessStart = Renderer.lineThicknessStart[Renderer.stackIndex];
        double thicknessEnd   = Renderer.lineThicknessEnd[Renderer.stackIndex];
        
        Color colorStart = Renderer.lineColorStart[Renderer.stackIndex];
        Color colorEnd   = Renderer.lineColorEnd[Renderer.stackIndex];
        
        double[] thickness = new double[count];
        Color[]  color     = new Color[count];
        
        for (int i = 0; i < count; i++)
        {
            double t = (double) i / (count - 1);
            
            thickness[i] = thicknessStart * (1.0 - t) + thicknessEnd * t;
            color[i]     = colorStart.interpolate(colorEnd, t, new Color());
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
        
        int divisions = Renderer.lineBezierDivisions[Renderer.stackIndex];
        
        double[] points = new double[divisions * 2];
        for (int i = 0, index = 0; i < divisions; i++)
        {
            double t    = (double) i / (divisions - 1);
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
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer.lineProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        
        drawVertices(DrawMode.LINES_ADJACENCY);
    }
    
    // -------------------- Rect -------------------- //
    
    public static void rectColor(@NotNull Colorc color)
    {
        Renderer.rectColorTL[Renderer.stackIndex].set(color);
        Renderer.rectColorTR[Renderer.stackIndex].set(color);
        Renderer.rectColorBL[Renderer.stackIndex].set(color);
        Renderer.rectColorBR[Renderer.stackIndex].set(color);
    }
    
    public static void rectColorGradientH(@NotNull Colorc left, @NotNull Colorc right)
    {
        Renderer.rectColorTL[Renderer.stackIndex].set(left);
        Renderer.rectColorTR[Renderer.stackIndex].set(right);
        Renderer.rectColorBL[Renderer.stackIndex].set(left);
        Renderer.rectColorBR[Renderer.stackIndex].set(right);
    }
    
    public static void rectColorGradientV(@NotNull Colorc top, @NotNull Colorc bottom)
    {
        Renderer.rectColorTL[Renderer.stackIndex].set(top);
        Renderer.rectColorTR[Renderer.stackIndex].set(top);
        Renderer.rectColorBL[Renderer.stackIndex].set(bottom);
        Renderer.rectColorBR[Renderer.stackIndex].set(bottom);
    }
    
    public static void rectColorTopLeft(@NotNull Colorc color)
    {
        Renderer.rectColorTL[Renderer.stackIndex].set(color);
    }
    
    public static void rectColorTopRight(@NotNull Colorc color)
    {
        Renderer.rectColorTR[Renderer.stackIndex].set(color);
    }
    
    public static void rectColorBottomLeft(@NotNull Colorc color)
    {
        Renderer.rectColorBL[Renderer.stackIndex].set(color);
    }
    
    public static void rectColorBottomRight(@NotNull Colorc color)
    {
        Renderer.rectColorBR[Renderer.stackIndex].set(color);
    }
    
    public static void rectBatchBegin()
    {
        if (Renderer.batch != Batch.NONE) throw new IllegalStateException("Batch was never ended: " + Renderer.batch);
        Renderer.batch = Batch.RECT;
    }
    
    public static void rectBatchEnd()
    {
        if (Renderer.batch != Batch.RECT) throw new IllegalStateException("Rect Batch was not started");
        Renderer.batch = Batch.NONE;
        
        rectDrawBuffer();
    }
    
    public static void rectDraw(double x, double y, double w, double h)
    {
        if (Renderer.batch != Batch.NONE && Renderer.batch != Batch.RECT) throw new IllegalStateException(Renderer.batch + " is active");
        
        Colorc topLeft     = Renderer.rectColorTL[Renderer.stackIndex];
        Colorc topRight    = Renderer.rectColorTR[Renderer.stackIndex];
        Colorc bottomLeft  = Renderer.rectColorBL[Renderer.stackIndex];
        Colorc bottomRight = Renderer.rectColorBR[Renderer.stackIndex];
        
        rectVertex(x, y, w, h, topLeft, topRight, bottomLeft, bottomRight);
        
        if (Renderer.batch == Batch.NONE) rectDrawBuffer();
    }
    
    private static void rectVertex(double x, double y, double w, double h, @NotNull Colorc colorTL, @NotNull Colorc colorTR, @NotNull Colorc colorBL, @NotNull Colorc colorBR)
    {
        position(x, y, w, h);
        color0(colorTL);
        color1(colorTR);
        color2(colorBL);
        color3(colorBR);
        
        Renderer.vertexCount++;
    }
    
    private static void rectDrawBuffer()
    {
        Program.bind(Renderer.rectProgram);
        
        drawVertices(DrawMode.POINTS);
    }
    
    // -------------------- Ellipse -------------------- //
    
    public static void ellipseColor(@NotNull Colorc color)
    {
        Renderer.ellipseColorInner[Renderer.stackIndex].set(color);
        Renderer.ellipseColorOuter[Renderer.stackIndex].set(color);
    }
    
    public static void ellipseColorInner(@NotNull Colorc color)
    {
        Renderer.ellipseColorInner[Renderer.stackIndex].set(color);
    }
    
    public static void ellipseColorOuter(@NotNull Colorc color)
    {
        Renderer.ellipseColorOuter[Renderer.stackIndex].set(color);
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
        
        Colorc inner = Renderer.ellipseColorInner[Renderer.stackIndex];
        Colorc outer = Renderer.ellipseColorOuter[Renderer.stackIndex];
        ellipseVertex(x, y, w, h, inner, outer);
        
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
        Program.bind(Renderer.ellipseProgram);
        
        drawVertices(DrawMode.POINTS);
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
        texCoord(u, v, 0, 0);
        
        Renderer.vertexCount++;
    }
    
    private static void textDrawBuffer()
    {
        Texture.bind(Renderer.textFont.texture, 0);
        
        Program.bind(Renderer.textProgram);
        Program.uniformColor("fontColor", Renderer.textColor);
        
        drawVertices(DrawMode.TRIANGLES);
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
        RECT,
        ELLIPSE,
        TEXT
    }
}
