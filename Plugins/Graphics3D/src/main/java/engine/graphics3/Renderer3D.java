package engine.graphics3;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer3D
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // ---------- Global State ---------- //
    
    private static int vertexCount;
    
    private static FloatBuffer positionBuffer;  // (XYZ)  (shader-location = 0)
    private static FloatBuffer tangentBuffer;   // (XYZ)  (shader-location = 1)
    private static FloatBuffer bitangentBuffer; // (XYZ)  (shader-location = 2)
    private static FloatBuffer normalBuffer;    // (XYZ)  (shader-location = 3)
    private static FloatBuffer texCoordBuffer;  // (UV)   (shader-location = 4)
    private static ByteBuffer  colorBuffer;     // (RGBA) (shader-location = 5)
    
    private static VertexArray vertexArray;
    
    private static Matrix4d projection;
    private static Matrix4d view;
    private static Matrix4d pv;
    
    private static BufferUniform pvBuffer;
    private static boolean       updatePVBuffer;
    
    private static Matrix4d billboardFront;
    private static boolean  billboardEnabled;
    
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
        Renderer3D.LOGGER.debug("Setup");
        
        int quadCount = 8192;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
    
        Renderer3D.vertexCount = 0;
    
        Renderer3D.positionBuffer  = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 0)
        Renderer3D.tangentBuffer   = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 1)
        Renderer3D.bitangentBuffer = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 2)
        Renderer3D.normalBuffer    = MemoryUtil.memAllocFloat(vertexCount * 3); // (XYZ)  (shader-location = 3)
        Renderer3D.texCoordBuffer  = MemoryUtil.memAllocFloat(vertexCount * 2); // (UV)   (shader-location = 4)
        Renderer3D.colorBuffer     = MemoryUtil.memAlloc(vertexCount * 4);      // (RGBA) (shader-location = 5)
        
        VertexAttribute position  = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute tangent   = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute bitangent = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute normal    = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute texCoord  = new VertexAttribute(GLType.FLOAT, 2, false);
        VertexAttribute color     = new VertexAttribute(GLType.UNSIGNED_BYTE, 4, true);
    
        Renderer3D.vertexArray = VertexArray.builder()
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.positionBuffer.clear(), position)
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.tangentBuffer.clear(), tangent)
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.bitangentBuffer.clear(), bitangent)
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.normalBuffer.clear(), normal)
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.texCoordBuffer.clear(), texCoord)
                                            .buffer(BufferUsage.DYNAMIC_DRAW, Renderer3D.colorBuffer.clear(), color)
                                            .build();
    
        Renderer3D.projection = new Matrix4d();
        Renderer3D.view       = new Matrix4d();
        Renderer3D.pv         = new Matrix4d();
    
        Renderer3D.pvBuffer = new BufferUniform(BufferUsage.DYNAMIC_DRAW, Float.BYTES * 16);
        Renderer3D.pvBuffer.base(0);
        Renderer3D.updatePVBuffer = true;
    
        Renderer3D.billboardFront   = new Matrix4d();
        Renderer3D.billboardEnabled = false;
        
        // ----- Point ----- //
        {
            Shader pointVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/point.vert"));
            Shader pointGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/point.geom"));
            Shader pointFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/point.frag"));
            
            Program.bind(Renderer3D.pointsProgram = new Program(pointVert, pointGeom, pointFrag));
            Program.uniformBlock("View", 0);
            
            pointVert.delete();
            pointGeom.delete();
            pointFrag.delete();
    
            Renderer3D.pointsSize  = 10.0;
            Renderer3D.pointsColor = new Color(Color.WHITE);
        }
        
        // ----- Lines ----- //
        {
            Shader linesVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/lines.vert"));
            Shader linesGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/lines.geom"));
            Shader linesFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/lines.frag"));
            
            Program.bind(Renderer3D.linesProgram = new Program(linesVert, linesGeom, linesFrag));
            Program.uniformBlock("View", 0);
            
            linesVert.delete();
            linesGeom.delete();
            linesFrag.delete();
    
            Renderer3D.linesThickness       = 10.0;
            Renderer3D.linesColorStart      = new Color(Color.WHITE);
            Renderer3D.linesColorEnd        = new Color(Color.WHITE);
            Renderer3D.linesBezierDivisions = 24;
        }
        
        // ----- Ellipse ----- //
        {
            Shader ellipseVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/ellipse.vert"));
            Shader ellipseGeom = new Shader(ShaderType.GEOMETRY, IOUtil.getPath("shader/ellipse.geom"));
            Shader ellipseFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/ellipse.frag"));
            
            Program.bind(Renderer3D.ellipseProgram = new Program(ellipseVert, ellipseGeom, ellipseFrag));
            Program.uniformBlock("View", 0);
            
            ellipseVert.delete();
            ellipseGeom.delete();
            ellipseFrag.delete();
    
            Renderer3D.ellipseColorInner = new Color(Color.WHITE);
            Renderer3D.ellipseColorOuter = new Color(Color.WHITE);
        }
        
        // ----- Text ----- //
        {
            Shader textVert = new Shader(ShaderType.VERTEX, IOUtil.getPath("shader/text.vert"));
            Shader textFrag = new Shader(ShaderType.FRAGMENT, IOUtil.getPath("shader/text.frag"));
            
            Program.bind(Renderer3D.textProgram = new Program(textVert, textFrag));
            Program.uniformBlock("View", 0);
            Program.uniformInt("fontTexture", 0);
            
            textVert.delete();
            textFrag.delete();
    
            Renderer3D.DEFAULT_FONT = new Font(IOUtil.getPath("font/PressStart2P/PressStart2P.ttf"), true);
    
            Renderer3D.textSize  = 24.0;
            Renderer3D.textColor = new Color(Color.WHITE);
            Renderer3D.textAlign = TextAlign.TOP_LEFT;
            Renderer3D.textFont  = Renderer3D.DEFAULT_FONT;
        }
    }
    
    static void destroy()
    {
        Renderer3D.LOGGER.debug("Destroy");
        
        MemoryUtil.memFree(Renderer3D.positionBuffer);
        MemoryUtil.memFree(Renderer3D.tangentBuffer);
        MemoryUtil.memFree(Renderer3D.bitangentBuffer);
        MemoryUtil.memFree(Renderer3D.normalBuffer);
        MemoryUtil.memFree(Renderer3D.texCoordBuffer);
        MemoryUtil.memFree(Renderer3D.colorBuffer);
        Renderer3D.vertexArray.delete();
        
        Renderer3D.pointsProgram.delete();
        
        Renderer3D.linesProgram.delete();
        
        Renderer3D.ellipseProgram.delete();
        
        Renderer3D.textProgram.delete();
        Renderer3D.DEFAULT_FONT.delete();
    }
    
    // -------------------- Global -------------------- //
    
    public static void rendererProjection(@NotNull Matrix4dc projection)
    {
        Renderer3D.projection.set(projection);
        Renderer3D.updatePVBuffer = true;
    }
    
    public static void rendererView(@NotNull Matrix4dc view)
    {
        Renderer3D.view.set(view);
        Renderer3D.updatePVBuffer = true;
    }
    
    private static void updatePVBuffer()
    {
        if (Renderer3D.updatePVBuffer)
        {
            Renderer3D.projection.mul(Renderer3D.view, Renderer3D.pv);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                Renderer3D.pvBuffer.set(0, Renderer3D.pv.get(stack.mallocFloat(16)));
            }
    
            Renderer3D.updatePVBuffer = false;
        }
    }
    
    // -------------------- Point -------------------- //
    
    public static void pointsSize(double size)
    {
        Renderer3D.pointsSize = size;
    }
    
    public static void pointsColor(@NotNull Colorc color)
    {
        Renderer3D.pointsColor.set(color);
    }
    
    public static void pointsDraw(double @NotNull ... points)
    {
        int count = points.length / 3;
        for (int i = 0, index = 0; i < count; i++)
        {
            pointsVertex(points[index++], points[index++], points[index++]);
        }
        
        pointsDraw();
    }
    
    private static void pointsVertex(double x, double y, double z)
    {
        Renderer3D.positionBuffer.put((float) x);
        Renderer3D.positionBuffer.put((float) y);
        Renderer3D.positionBuffer.put((float) z);
        
        Renderer3D.vertexCount++;
    }
    
    private static void pointsDraw()
    {
        updatePVBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer3D.pointsProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        Program.uniformFloat("size", Renderer3D.pointsSize);
        Program.uniformColor("color", Renderer3D.pointsColor);
        
        VertexArray.bind(Renderer3D.vertexArray);
        Renderer3D.vertexArray.buffer(0).set(0, Renderer3D.positionBuffer.flip());
        Renderer3D.vertexArray.draw(DrawMode.POINTS, Renderer3D.vertexCount);
    
        Renderer3D.vertexCount = 0;
        Renderer3D.positionBuffer.clear();
    }
    
    // -------------------- Lines -------------------- //
    
    public static void linesThickness(double thickness)
    {
        Renderer3D.linesThickness = thickness;
    }
    
    public static void linesColor(@NotNull Colorc color)
    {
        Renderer3D.linesColorStart.set(color);
        Renderer3D.linesColorEnd.set(color);
    }
    
    public static void linesColorStart(@NotNull Colorc color)
    {
        Renderer3D.linesColorStart.set(color);
    }
    
    public static void linesColorEnd(@NotNull Colorc color)
    {
        Renderer3D.linesColorEnd.set(color);
    }
    
    public static void linesBezierDivisions(int divisions)
    {
        Renderer3D.linesBezierDivisions = divisions;
    }
    
    public static void linesDraw(double @NotNull ... points)
    {
        int pointCount = points.length / 3;
        
        // First coordinate get repeated.
        linesVertex(points[0], points[1], points[2], Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int index = 0; index < pointCount; index++)
        {
            double t = (double) index / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            int i = index * 3;
            linesVertex(points[i], points[i + 1], points[i + 2], lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 3], points[points.length - 2], points[points.length - 1], Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDraw(Vector2d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, 0, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, 0, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, 0, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDraw(Vector3d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, points[0].z, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, points[i].z, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, points[points.length - 1].z, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawEnclosed(double @NotNull ... points)
    {
        int pointCount = points.length / 3;
        
        // Last coordinate is first.
        linesVertex(points[points.length - 3], points[points.length - 2], points[points.length - 1], Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int index = 0; index < pointCount; index++)
        {
            double t = (double) index / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            int i = index * 3;
            linesVertex(points[i], points[i + 1], points[i + 2], lerp);
        }
        
        // First coordinate is last.
        linesVertex(points[0], points[1], points[2], Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawEnclosed(Vector2d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // Last coordinate is first.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, 0, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, 0, lerp);
        }
        
        // First coordinate is last.
        linesVertex(points[0].x, points[0].y, 0, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawEnclosed(Vector3d @NotNull ... points)
    {
        int pointCount = points.length;
        
        // Last coordinate is first.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, points[points.length - 1].z, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i < pointCount; i++)
        {
            double t = (double) i / (pointCount - 1);
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            linesVertex(points[i].x, points[i].y, points[i].z, lerp);
        }
        
        // First coordinate is last.
        linesVertex(points[0].x, points[0].y, points[0].z, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawBezier(double @NotNull ... points)
    {
        int count = points.length / 3;
        int order = count - 1;
        
        // First coordinate get repeated.
        linesVertex(points[0], points[1], points[2], Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i <= Renderer3D.linesBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer3D.linesBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0, z = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * points[(j * 3)];
                y += coeff * points[(j * 3) + 1];
                z += coeff * points[(j * 3) + 2];
            }
            linesVertex(x, y, z, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 3], points[points.length - 2], points[points.length - 1], Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawBezier(Vector2d @NotNull ... points)
    {
        int count = points.length;
        int order = count - 1;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, 0.0, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i <= Renderer3D.linesBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer3D.linesBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0, z = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * points[(j * 3)].x;
                y += coeff * points[(j * 3)].y;
            }
            linesVertex(x, y, z, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, 0.0, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    public static void linesDrawBezier(Vector3d @NotNull ... points)
    {
        int count = points.length;
        int order = count - 1;
        
        // First coordinate get repeated.
        linesVertex(points[0].x, points[0].y, points[0].z, Renderer3D.linesColorStart);
        
        Color lerp = new Color();
        for (int i = 0; i <= Renderer3D.linesBezierDivisions; i++)
        {
            double t    = (double) i / (Renderer3D.linesBezierDivisions - 1);
            double tInv = 1.0 - t;
            
            Renderer3D.linesColorStart.interpolate(Renderer3D.linesColorEnd, t, lerp);
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0.0, y = 0.0, z = 0.0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * points[(j * 3)].x;
                y += coeff * points[(j * 3)].y;
                z += coeff * points[(j * 3)].z;
            }
            linesVertex(x, y, z, lerp);
        }
        
        // Last coordinate get repeated.
        linesVertex(points[points.length - 1].x, points[points.length - 1].y, points[points.length - 1].z, Renderer3D.linesColorEnd);
        
        linesDraw();
    }
    
    private static void linesVertex(double x, double y, double z, @NotNull Colorc color)
    {
        Renderer3D.positionBuffer.put((float) x);
        Renderer3D.positionBuffer.put((float) y);
        Renderer3D.positionBuffer.put((float) z);
        
        Renderer3D.colorBuffer.put((byte) color.r());
        Renderer3D.colorBuffer.put((byte) color.g());
        Renderer3D.colorBuffer.put((byte) color.b());
        Renderer3D.colorBuffer.put((byte) color.a());
        
        Renderer3D.vertexCount++;
    }
    
    private static void linesDraw()
    {
        updatePVBuffer();
        
        Framebuffer fb = Framebuffer.get();
        
        Program.bind(Renderer3D.linesProgram);
        Program.uniformInt2("viewport", fb.width(), fb.height());
        Program.uniformFloat("thickness", Renderer3D.linesThickness);
        
        VertexArray.bind(Renderer3D.vertexArray);
        Renderer3D.vertexArray.buffer(0).set(0, Renderer3D.positionBuffer.flip());
        Renderer3D.vertexArray.buffer(5).set(0, Renderer3D.colorBuffer.flip());
        Renderer3D.vertexArray.draw(DrawMode.LINE_STRIP_ADJACENCY, Renderer3D.vertexCount);
    
        Renderer3D.vertexCount = 0;
        Renderer3D.positionBuffer.clear();
        Renderer3D.colorBuffer.clear();
    }
    
    // -------------------- Ellipse -------------------- //
    
    public static void ellipseColor(@NotNull Colorc color)
    {
        Renderer3D.ellipseColorInner.set(color);
        Renderer3D.ellipseColorOuter.set(color);
    }
    
    public static void ellipseColorInner(@NotNull Colorc color)
    {
        Renderer3D.ellipseColorInner.set(color);
    }
    
    public static void ellipseColorOuter(@NotNull Colorc color)
    {
        Renderer3D.ellipseColorOuter.set(color);
    }
    
    public static void ellipseDraw(double x, double y, double z)
    {
        Renderer3D.positionBuffer.put((float) x);
        Renderer3D.positionBuffer.put((float) y);
        Renderer3D.positionBuffer.put((float) z);
        
        if (Renderer3D.billboardEnabled)
        {
            Renderer3D.tangentBuffer.put((float) Renderer3D.view.m00());
            Renderer3D.tangentBuffer.put((float) Renderer3D.view.m10());
            Renderer3D.tangentBuffer.put((float) Renderer3D.view.m20());
    
            Renderer3D.bitangentBuffer.put((float) Renderer3D.view.m01());
            Renderer3D.bitangentBuffer.put((float) Renderer3D.view.m11());
            Renderer3D.bitangentBuffer.put((float) Renderer3D.view.m21());
        }
        else
        {
            Renderer3D.tangentBuffer.put((float) Renderer3D.billboardFront.m00());
            Renderer3D.tangentBuffer.put((float) Renderer3D.billboardFront.m01());
            Renderer3D.tangentBuffer.put((float) Renderer3D.billboardFront.m02());
    
            Renderer3D.bitangentBuffer.put((float) Renderer3D.billboardFront.m10());
            Renderer3D.bitangentBuffer.put((float) Renderer3D.billboardFront.m11());
            Renderer3D.bitangentBuffer.put((float) Renderer3D.billboardFront.m12());
        }
        
        Renderer3D.vertexCount++;
        
        ellipseDraw();
    }
    
    private static void ellipseDraw()
    {
        updatePVBuffer();
        
        Program.bind(Renderer3D.ellipseProgram);
        Program.uniformFloat2("size", 20, 10);
        Program.uniformColor("colorInner", Renderer3D.ellipseColorInner);
        Program.uniformColor("colorOuter", Renderer3D.ellipseColorOuter);
        
        VertexArray.bind(Renderer3D.vertexArray);
        Renderer3D.vertexArray.buffer(0).set(0, Renderer3D.positionBuffer.flip());
        Renderer3D.vertexArray.buffer(1).set(0, Renderer3D.tangentBuffer.flip());
        Renderer3D.vertexArray.buffer(2).set(0, Renderer3D.bitangentBuffer.flip());
        Renderer3D.vertexArray.draw(DrawMode.POINTS, Renderer3D.vertexCount);
    
        Renderer3D.vertexCount = 0;
        Renderer3D.positionBuffer.clear();
        Renderer3D.tangentBuffer.clear();
        Renderer3D.bitangentBuffer.clear();
    }
    
    // -------------------- Text -------------------- //
    
    public static void textSize(double size)
    {
        Renderer3D.textSize = size;
    }
    
    public static void textColor(@NotNull Colorc color)
    {
        Renderer3D.textColor.set(color);
    }
    
    public static void textAlign(@NotNull TextAlign align)
    {
        Renderer3D.textAlign = align;
    }
    
    public static void textFont(@NotNull Font font)
    {
        Renderer3D.textFont = font;
    }
    
    public static double textWidth(@NotNull String text, int maxWidth)
    {
        return Renderer3D.textFont.textWidth(text, Renderer3D.textSize, maxWidth);
    }
    
    public static double textWidth(@NotNull String text)
    {
        return Renderer3D.textFont.textWidth(text, Renderer3D.textSize);
    }
    
    public static double textHeight(@NotNull String text, int maxWidth)
    {
        return Renderer3D.textFont.textHeight(text, Renderer3D.textSize, maxWidth);
    }
    
    public static double textHeight(@NotNull String text)
    {
        return Renderer3D.textFont.textHeight(text, Renderer3D.textSize);
    }
    
    // TODO - textDrawBillboard
    
    public static void textDraw(@NotNull String text, double x, double y, double width, double height)
    {
        List<String> lines = Renderer3D.textFont.splitText(text, Renderer3D.textSize, width);
        
        double actualHeight = textHeight(text);
        
        int hPos = Renderer3D.textAlign.getH(), vPos = Renderer3D.textAlign.getV();
        
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
        double scale = Renderer3D.textFont.scale(Renderer3D.textSize);
        
        GlyphData prevGlyph = null, currGlyph;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            CharData charData = Renderer3D.textFont.charData.get(line.charAt(i));
            currGlyph = Renderer3D.textFont.glyphData.get(charData.glyph());
            
            x += Renderer3D.textFont.kerningAdvanceUnscaled(prevGlyph, currGlyph) * scale;
            
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
        Renderer3D.positionBuffer.put((float) x);
        Renderer3D.positionBuffer.put((float) y);
        Renderer3D.positionBuffer.put((float) z);
        
        Renderer3D.texCoordBuffer.put((float) u);
        Renderer3D.texCoordBuffer.put((float) v);
        
        Renderer3D.vertexCount++;
    }
    
    private static void textDraw()
    {
        updatePVBuffer();
        
        Texture.bind(Renderer3D.textFont.texture, 0);
        
        Program.bind(Renderer3D.textProgram);
        Program.uniformColor("fontColor", Renderer3D.textColor);
        
        VertexArray.bind(Renderer3D.vertexArray);
        Renderer3D.vertexArray.buffer(0).set(0, Renderer3D.positionBuffer.flip());
        Renderer3D.vertexArray.buffer(4).set(0, Renderer3D.texCoordBuffer.flip());
        Renderer3D.vertexArray.draw(DrawMode.TRIANGLES, Renderer3D.vertexCount);
    
        Renderer3D.vertexCount = 0;
        Renderer3D.positionBuffer.clear();
        Renderer3D.texCoordBuffer.clear();
    }
    
    private static final Map<Long, Long> BINOMIAL_CACHE = new HashMap<>();
    
    private static long binomial(int order, int k)
    {
        long hash = (((long) order) << 32) | k;
        return Renderer3D.BINOMIAL_CACHE.computeIfAbsent(hash, i -> _binomial(order, k));
    }
    
    private static long _binomial(int order, int k)
    {
        if (k > order - k) k = order - k;
        long b = 1;
        for (int i = 1, m = order; i <= k; i++, m--) b = (b * m) / i;
        return b;
    }
    
    private Renderer3D() {}
}
