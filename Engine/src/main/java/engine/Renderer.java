package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.color.Colorc;
import engine.gl.*;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferArray;
import engine.gl.buffer.BufferUsage;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.vertex.Attribute;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.util.IOUtil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.*;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
        Renderer.LOGGER.debug("OpenGL Version:", GL44.glGetString(GL44.GL_VERSION));
        
        setupBinding();
        setupState();
        setupDefaults();
        setupDraw();
        
        bind(Renderer.defaultProgram);
        bind(Renderer.defaultFramebuffer);
        activeTexture(0);
        bind(Renderer.defaultTexture);
        bind(BufferArray.NULL);
        bind(Renderer.drawVertexArray);
        
        clearScreenBuffers();
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
        
        destroyDraw();
        destroyDefaults();
        destroyState();
        destroyBinding();
    }
    
    // -------------------- Binding State -------------------- //
    
    static int boundActiveTexture;
    
    static       Program     boundProgram;
    static       Framebuffer boundFramebuffer;
    static final Texture[]   boundTextures = new Texture[32];
    static       Buffer      boundBuffer;
    static       VertexArray boundVertexArray;
    
    private static void setupBinding()
    {
        Renderer.boundActiveTexture = -1;
        Renderer.boundProgram       = null;
        Renderer.boundFramebuffer   = null;
        Arrays.fill(Renderer.boundTextures, null);
        Renderer.boundBuffer      = null;
        Renderer.boundVertexArray = null;
        
        bind(Program.NULL);
        bind(Framebuffer.NULL);
        activeTexture(0);
        bind(Texture2D.NULL);
        bind(BufferArray.NULL);
        bind(VertexArray.NULL);
    }
    
    private static void destroyBinding()
    {
    
    }
    
    public static void bind(@NotNull Program program)
    {
        if (Renderer.boundProgram != program)
        {
            Renderer.LOGGER.trace("Binding:", program);
            
            Renderer.boundProgram = program;
            
            GL44.glUseProgram(program.id());
        }
    }
    
    public static void bind(@NotNull Framebuffer framebuffer)
    {
        if (Renderer.boundFramebuffer != framebuffer || framebuffer == Framebuffer.NULL)
        {
            Renderer.LOGGER.trace("Binding:", framebuffer);
            
            Renderer.boundFramebuffer = framebuffer;
            
            GL44.glBindFramebuffer(GL44.GL_FRAMEBUFFER, framebuffer.id());
            GL44.glViewport(0, 0, framebuffer.width(), framebuffer.height());
        }
    }
    
    public static void activeTexture(@Range(from = 0, to = 31) int index)
    {
        if (Renderer.boundActiveTexture != index)
        {
            Renderer.LOGGER.trace("Setting Active Texture:", index);
            
            Renderer.boundActiveTexture = index;
            
            GL44.glActiveTexture(GL44.GL_TEXTURE0 + index);
        }
    }
    
    public static void bind(@NotNull Texture texture)
    {
        if (Renderer.boundTextures[Renderer.boundActiveTexture] != texture)
        {
            Renderer.LOGGER.trace("Binding: %s to index=%s", texture, Renderer.boundActiveTexture);
            
            Renderer.boundTextures[Renderer.boundActiveTexture] = texture;
            
            GL44.glBindTexture(texture.type, texture.id());
        }
    }
    
    public static void bind(@NotNull Buffer buffer)
    {
        if (Renderer.boundBuffer != buffer)
        {
            Renderer.LOGGER.trace("Binding:", buffer);
            
            Renderer.boundBuffer = buffer;
            
            GL44.glBindBuffer(buffer.type, buffer.id());
        }
    }
    
    public static void bind(@NotNull VertexArray vertexArray)
    {
        if (Renderer.boundVertexArray != vertexArray)
        {
            Renderer.LOGGER.trace("Binding:", vertexArray);
            
            Renderer.boundVertexArray = vertexArray;
            
            GL44.glBindVertexArray(vertexArray.id());
        }
    }
    
    // -------------------- State -------------------- //
    
    public static final int STACK_SIZE = 32;
    
    static int stateIndex;
    
    static final boolean[] stateDepthClamp             = new boolean[Renderer.DRAW_CALLS_COUNT];
    static final boolean[] stateLineSmooth             = new boolean[Renderer.DRAW_CALLS_COUNT];
    static final boolean[] stateTextureCubeMapSeamless = new boolean[Renderer.DRAW_CALLS_COUNT];
    
    static final boolean[] stateWireframe = new boolean[Renderer.DRAW_CALLS_COUNT];
    
    static final BlendMode[]   stateBlendMode   = new BlendMode[Renderer.DRAW_CALLS_COUNT];
    static final DepthMode[]   stateDepthMode   = new DepthMode[Renderer.DRAW_CALLS_COUNT];
    static final StencilMode[] stateStencilMode = new StencilMode[Renderer.DRAW_CALLS_COUNT];
    static final ScissorMode[] stateScissorMode = new ScissorMode[Renderer.DRAW_CALLS_COUNT];
    
    static final boolean[][] stateColorMask   = new boolean[Renderer.DRAW_CALLS_COUNT][4];
    static final boolean[]   stateDepthMask   = new boolean[Renderer.DRAW_CALLS_COUNT];
    static final int[]       stateStencilMask = new int[Renderer.DRAW_CALLS_COUNT];
    
    static final double[][] stateClearColor   = new double[Renderer.DRAW_CALLS_COUNT][4];
    static final double[]   stateClearDepth   = new double[Renderer.DRAW_CALLS_COUNT];
    static final int[]      stateClearStencil = new int[Renderer.DRAW_CALLS_COUNT];
    
    static final CullFace[] stateCullFace = new CullFace[Renderer.DRAW_CALLS_COUNT];
    static final Winding[]  stateWinding  = new Winding[Renderer.DRAW_CALLS_COUNT];
    
    static final Matrix4d[] stateProjection = new Matrix4d[Renderer.DRAW_CALLS_COUNT];
    static final Matrix4d[] stateView       = new Matrix4d[Renderer.DRAW_CALLS_COUNT];
    static final Matrix4d[] stateModel      = new Matrix4d[Renderer.DRAW_CALLS_COUNT];
    static final Matrix4d[] stateNormal     = new Matrix4d[Renderer.DRAW_CALLS_COUNT];
    
    static final Color[] stateDiffuse  = new Color[Renderer.DRAW_CALLS_COUNT];
    static final Color[] stateSpecular = new Color[Renderer.DRAW_CALLS_COUNT];
    static final Color[] stateAmbient  = new Color[Renderer.DRAW_CALLS_COUNT];
    
    private static final ScissorMode scissorModeCustom = new ScissorMode(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    private static void setupState()
    {
        for (int i = 0; i < Renderer.DRAW_CALLS_COUNT; i++)
        {
            Renderer.stateDepthClamp[i]             = false;
            Renderer.stateLineSmooth[i]             = true;
            Renderer.stateTextureCubeMapSeamless[i] = false;
            
            Renderer.stateWireframe[i] = GL44.glGetInteger(GL44.GL_FRONT_AND_BACK) == GL44.GL_FILL;
            
            Renderer.stateBlendMode[i]   = null;
            Renderer.stateDepthMode[i]   = null;
            Renderer.stateStencilMode[i] = null;
            Renderer.stateScissorMode[i] = null;
            
            Renderer.stateColorMask[i]   = new boolean[] {false, false, false, false};
            Renderer.stateDepthMask[i]   = false;
            Renderer.stateStencilMask[i] = 0x00;
            
            Renderer.stateClearColor[i]   = new double[] {0.0, 0.0, 0.0, 0.0};
            Renderer.stateClearDepth[i]   = 0.0;
            Renderer.stateClearStencil[i] = 0xFF;
            
            Renderer.stateCullFace[i] = null;
            Renderer.stateWinding[i]  = null;
            
            Renderer.stateProjection[i] = new Matrix4d();
            Renderer.stateView[i]       = new Matrix4d();
            Renderer.stateModel[i]      = new Matrix4d();
            Renderer.stateNormal[i]     = new Matrix4d();
            
            Renderer.stateDiffuse[i]  = new Color();
            Renderer.stateSpecular[i] = new Color();
            Renderer.stateAmbient[i]  = new Color();
        }
        
        stateDefault();
    }
    
    private static void destroyState()
    {
    
    }
    
    public static void stateDefault()
    {
        stateDepthClamp(true);
        stateLineSmooth(false);
        stateTextureCubeMapSeamless(true);
        
        stateWireframe(false);
        
        stateBlendMode(BlendMode.DEFAULT);
        stateDepthMode(DepthMode.DEFAULT);
        stateStencilMode(StencilMode.DEFAULT);
        stateScissorMode(ScissorMode.DEFAULT);
        
        stateColorMask(true, true, true, true);
        stateDepthMask(true);
        stateStencilMask(0xFF);
        
        stateClearColor(0.0, 0.0, 0.0, 1.0);
        stateClearDepth(1.0);
        stateClearStencil(0x00);
        
        stateCullFace(CullFace.DEFAULT);
        stateWinding(Winding.DEFAULT);
        
        stateProjection().identity();
        stateView().identity();
        stateModel().identity();
        stateNormal().identity();
        
        stateDiffuse().set(255, 255);
        stateSpecular().set(255, 255);
        stateAmbient().set(255, 255);
    }
    
    public static void statePush()
    {
        final int idx     = Renderer.stateIndex;
        final int nextIdx = idx + 1;
        
        Renderer.stateDepthClamp[nextIdx]             = Renderer.stateDepthClamp[idx];
        Renderer.stateLineSmooth[nextIdx]             = Renderer.stateLineSmooth[idx];
        Renderer.stateTextureCubeMapSeamless[nextIdx] = Renderer.stateTextureCubeMapSeamless[idx];
        
        Renderer.stateWireframe[nextIdx] = Renderer.stateWireframe[idx];
        
        Renderer.stateBlendMode[nextIdx]   = Renderer.stateBlendMode[idx];
        Renderer.stateDepthMode[nextIdx]   = Renderer.stateDepthMode[idx];
        Renderer.stateStencilMode[nextIdx] = Renderer.stateStencilMode[idx];
        Renderer.stateScissorMode[nextIdx] = Renderer.stateScissorMode[idx];
        
        Renderer.stateColorMask[nextIdx][0] = Renderer.stateColorMask[idx][0];
        Renderer.stateColorMask[nextIdx][1] = Renderer.stateColorMask[idx][1];
        Renderer.stateColorMask[nextIdx][2] = Renderer.stateColorMask[idx][2];
        Renderer.stateColorMask[nextIdx][3] = Renderer.stateColorMask[idx][3];
        Renderer.stateDepthMask[nextIdx]    = Renderer.stateDepthMask[idx];
        Renderer.stateStencilMask[nextIdx]  = Renderer.stateStencilMask[idx];
        
        Renderer.stateClearColor[nextIdx][0] = Renderer.stateClearColor[idx][0];
        Renderer.stateClearColor[nextIdx][1] = Renderer.stateClearColor[idx][1];
        Renderer.stateClearColor[nextIdx][2] = Renderer.stateClearColor[idx][2];
        Renderer.stateClearColor[nextIdx][3] = Renderer.stateClearColor[idx][3];
        Renderer.stateClearDepth[nextIdx]    = Renderer.stateClearDepth[idx];
        Renderer.stateClearStencil[nextIdx]  = Renderer.stateClearStencil[idx];
        
        Renderer.stateCullFace[nextIdx] = Renderer.stateCullFace[idx];
        Renderer.stateWinding[nextIdx]  = Renderer.stateWinding[idx];
        
        Renderer.stateProjection[nextIdx].set(Renderer.stateProjection[idx]);
        Renderer.stateView[nextIdx].set(Renderer.stateView[idx]);
        Renderer.stateModel[nextIdx].set(Renderer.stateModel[idx]);
        Renderer.stateNormal[nextIdx].set(Renderer.stateNormal[idx]);
        
        Renderer.stateDiffuse[nextIdx].set(Renderer.stateDiffuse[idx]);
        Renderer.stateSpecular[nextIdx].set(Renderer.stateSpecular[idx]);
        Renderer.stateAmbient[nextIdx].set(Renderer.stateAmbient[idx]);
        
        Renderer.stateIndex = nextIdx;
    }
    
    public static void statePop()
    {
        stateSet(Renderer.stateIndex - 1);
    }
    
    static void stateReset()
    {
        stateSet(0);
    }
    
    private static void stateSet(int index)
    {
        stateDepthClamp(Renderer.stateDepthClamp[index]);
        stateLineSmooth(Renderer.stateLineSmooth[index]);
        stateTextureCubeMapSeamless(Renderer.stateTextureCubeMapSeamless[index]);
        
        stateWireframe(Renderer.stateWireframe[index]);
        
        stateBlendMode(Renderer.stateBlendMode[index]);
        stateDepthMode(Renderer.stateDepthMode[index]);
        stateStencilMode(Renderer.stateStencilMode[index]);
        stateScissorMode(Renderer.stateScissorMode[index]);
        
        boolean[] colorMask = Renderer.stateColorMask[index];
        stateColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
        stateDepthMask(Renderer.stateDepthMask[index]);
        stateStencilMask(Renderer.stateStencilMask[index]);
        
        double[] clearColor = Renderer.stateClearColor[index];
        stateClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        stateClearDepth(Renderer.stateClearDepth[index]);
        stateClearStencil(Renderer.stateClearStencil[index]);
        
        stateCullFace(Renderer.stateCullFace[index]);
        stateWinding(Renderer.stateWinding[index]);
        
        // Nn need to set matrix or color stack
        
        Renderer.stateIndex = index;
    }
    
    public static void stateDepthClamp(boolean depthClamp)
    {
        if (Renderer.stateDepthClamp[Renderer.stateIndex] != depthClamp)
        {
            Renderer.LOGGER.trace("Setting Depth Clamp Flag:", depthClamp);
            
            Renderer.stateDepthClamp[Renderer.stateIndex] = depthClamp;
            
            if (depthClamp)
            {
                GL44.glEnable(GL44.GL_DEPTH_CLAMP);
            }
            else
            {
                GL44.glDisable(GL44.GL_DEPTH_CLAMP);
            }
        }
    }
    
    public static void stateLineSmooth(boolean lineSmooth)
    {
        if (Renderer.stateLineSmooth[Renderer.stateIndex] != lineSmooth)
        {
            Renderer.LOGGER.trace("Setting Line Smooth Flag:", lineSmooth);
            
            Renderer.stateLineSmooth[Renderer.stateIndex] = lineSmooth;
            
            if (lineSmooth)
            {
                GL44.glEnable(GL44.GL_LINE_SMOOTH);
            }
            else
            {
                GL44.glDisable(GL44.GL_LINE_SMOOTH);
            }
        }
    }
    
    public static void stateTextureCubeMapSeamless(boolean textureCubeMapSeamless)
    {
        if (Renderer.stateTextureCubeMapSeamless[Renderer.stateIndex] != textureCubeMapSeamless)
        {
            Renderer.LOGGER.trace("Setting Texture Cube Map Seamless Flag:", textureCubeMapSeamless);
            
            Renderer.stateTextureCubeMapSeamless[Renderer.stateIndex] = textureCubeMapSeamless;
            
            if (textureCubeMapSeamless)
            {
                GL44.glEnable(GL44.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
            else
            {
                GL44.glDisable(GL44.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
        }
    }
    
    public static void stateWireframe(boolean wireframe)
    {
        if (Renderer.stateWireframe[Renderer.stateIndex] != wireframe)
        {
            Renderer.LOGGER.trace("Setting Wireframe Flag:", wireframe);
            
            Renderer.stateWireframe[Renderer.stateIndex] = wireframe;
            
            GL44.glPolygonMode(GL44.GL_FRONT_AND_BACK, wireframe ? GL44.GL_LINE : GL44.GL_FILL);
        }
    }
    
    public static void stateBlendMode(@Nullable BlendMode mode)
    {
        if (mode == null) mode = BlendMode.DEFAULT;
        
        if (Renderer.stateBlendMode[Renderer.stateIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Blend Mode:", mode);
            
            Renderer.stateBlendMode[Renderer.stateIndex] = mode;
            
            if (mode == BlendMode.NONE)
            {
                GL44.glDisable(GL44.GL_BLEND);
            }
            else
            {
                GL44.glEnable(GL44.GL_BLEND);
                GL44.glBlendFunc(mode.srcFunc().ref, mode.dstFunc().ref);
                GL44.glBlendEquation(mode.blendEqn().ref);
            }
        }
    }
    
    public static void stateDepthMode(@Nullable DepthMode mode)
    {
        if (mode == null) mode = DepthMode.DEFAULT;
        
        if (Renderer.stateDepthMode[Renderer.stateIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Depth Mode:", mode);
            
            Renderer.stateDepthMode[Renderer.stateIndex] = mode;
            
            if (mode == DepthMode.NONE)
            {
                GL44.glDisable(GL44.GL_DEPTH_TEST);
            }
            else
            {
                GL44.glEnable(GL44.GL_DEPTH_TEST);
                GL44.glDepthFunc(mode.ref);
            }
        }
    }
    
    public static void stateStencilMode(@Nullable StencilMode mode)
    {
        if (mode == null) mode = StencilMode.DEFAULT;
        
        if (Renderer.stateStencilMode[Renderer.stateIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Stencil Mode:", mode);
            
            Renderer.stateStencilMode[Renderer.stateIndex] = mode;
            
            if (mode == StencilMode.NONE)
            {
                GL44.glDisable(GL44.GL_STENCIL_TEST);
            }
            else
            {
                GL44.glEnable(GL44.GL_STENCIL_TEST);
                GL44.glStencilFunc(mode.func().ref, mode.ref(), mode.mask());
                GL44.glStencilOp(mode.sFail().ref, mode.dpFail().ref, mode.dpPass().ref);
            }
        }
    }
    
    public static void stateScissorMode(@Nullable ScissorMode mode)
    {
        if (mode == null) mode = ScissorMode.DEFAULT;
        
        if (Renderer.stateScissorMode[Renderer.stateIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting ScissorMode:", mode);
            
            Renderer.stateScissorMode[Renderer.stateIndex] = mode;
            
            if (mode == ScissorMode.NONE)
            {
                GL44.glDisable(GL44.GL_SCISSOR_TEST);
            }
            else
            {
                GL44.glEnable(GL44.GL_SCISSOR_TEST);
                GL44.glScissor(mode.x(), mode.y(), mode.width(), mode.height());
            }
        }
    }
    
    public static void stateScissorMode(int x, int y, int width, int height)
    {
        Renderer.LOGGER.trace("Setting Custom Scissor: [%s, %s, %s, %s]", x, y, width, height);
        
        Renderer.stateScissorMode[Renderer.stateIndex] = Renderer.scissorModeCustom;
        
        GL44.glEnable(GL44.GL_SCISSOR_TEST);
        GL44.glScissor(x, y, width, height);
    }
    
    public static void stateColorMask(boolean r, boolean g, boolean b, boolean a)
    {
        boolean[] colorMask = Renderer.stateColorMask[Renderer.stateIndex];
        if (Boolean.compare(colorMask[0], r) != 0 || Boolean.compare(colorMask[1], g) != 0 || Boolean.compare(colorMask[2], b) != 0 || Boolean.compare(colorMask[3], a) != 0)
        {
            Renderer.LOGGER.trace("Setting Color Mask: r=%s g=%s b=%s a=%s", r, g, b, a);
            
            colorMask[0] = r;
            colorMask[1] = g;
            colorMask[2] = b;
            colorMask[3] = a;
            
            GL44.glColorMask(r, g, b, a);
        }
    }
    
    public static void stateDepthMask(boolean flag)
    {
        if (Renderer.stateDepthMask[Renderer.stateIndex] != flag)
        {
            Renderer.LOGGER.trace("Setting Depth Mask:", flag);
            
            Renderer.stateDepthMask[Renderer.stateIndex] = flag;
            
            GL44.glDepthMask(flag);
        }
    }
    
    public static void stateStencilMask(int mask)
    {
        if (Renderer.stateStencilMask[Renderer.stateIndex] != mask)
        {
            Renderer.LOGGER.trace("Setting Stencil Mask: 0x%02X", mask);
            
            Renderer.stateStencilMask[Renderer.stateIndex] = mask;
            
            GL44.glStencilMask(mask);
        }
    }
    
    public static void stateClearColor(double r, double g, double b, double a)
    {
        double[] clearColor = Renderer.stateClearColor[Renderer.stateIndex];
        if (Double.compare(clearColor[0], r) != 0 || Double.compare(clearColor[1], g) != 0 || Double.compare(clearColor[2], b) != 0 || Double.compare(clearColor[3], a) != 0)
        {
            Renderer.LOGGER.trace("Setting Clear Color: (%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
            
            clearColor[0] = r;
            clearColor[1] = g;
            clearColor[2] = b;
            clearColor[3] = a;
            
            GL44.glClearColor((float) r, (float) g, (float) b, (float) a);
        }
    }
    
    public static void stateClearDepth(double depth)
    {
        if (Double.compare(Renderer.stateClearDepth[Renderer.stateIndex], depth) != 0)
        {
            Renderer.LOGGER.trace("Setting Clear Depth: %.3f", depth);
            
            Renderer.stateClearDepth[Renderer.stateIndex] = depth;
            
            GL44.glClearDepth(depth);
        }
    }
    
    public static void stateClearStencil(int stencil)
    {
        if (Renderer.stateClearStencil[Renderer.stateIndex] != stencil)
        {
            Renderer.LOGGER.trace("Setting Clear Stencil: 0x%02X", stencil);
            
            Renderer.stateClearStencil[Renderer.stateIndex] = stencil;
            
            GL44.glClearStencil(stencil);
        }
    }
    
    public static void stateCullFace(@Nullable CullFace cullFace)
    {
        if (cullFace == null) cullFace = CullFace.DEFAULT;
        
        if (Renderer.stateCullFace[Renderer.stateIndex] != cullFace)
        {
            Renderer.LOGGER.trace("Setting Cull Face:", cullFace);
            
            Renderer.stateCullFace[Renderer.stateIndex] = cullFace;
            
            if (cullFace == CullFace.NONE)
            {
                GL44.glDisable(GL44.GL_CULL_FACE);
            }
            else
            {
                GL44.glEnable(GL44.GL_CULL_FACE);
                GL44.glCullFace(cullFace.ref);
            }
        }
    }
    
    public static void stateWinding(@Nullable Winding winding)
    {
        if (winding == null) winding = Winding.DEFAULT;
        
        if (Renderer.stateWinding[Renderer.stateIndex] != winding)
        {
            Renderer.LOGGER.trace("Setting Winding:", winding);
            
            Renderer.stateWinding[Renderer.stateIndex] = winding;
            
            GL44.glFrontFace(winding.ref);
        }
    }
    
    public static @NotNull Matrix4d stateProjection()
    {
        return Renderer.stateProjection[Renderer.stateIndex];
    }
    
    public static @NotNull Matrix4d stateView()
    {
        return Renderer.stateView[Renderer.stateIndex];
    }
    
    public static @NotNull Matrix4d stateModel()
    {
        return Renderer.stateModel[Renderer.stateIndex];
    }
    
    public static @NotNull Matrix4d stateNormal()
    {
        return Renderer.stateNormal[Renderer.stateIndex];
    }
    
    public static @NotNull Color stateDiffuse()
    {
        return Renderer.stateDiffuse[Renderer.stateIndex];
    }
    
    public static @NotNull Color stateSpecular()
    {
        return Renderer.stateSpecular[Renderer.stateIndex];
    }
    
    public static @NotNull Color stateAmbient()
    {
        return Renderer.stateAmbient[Renderer.stateIndex];
    }
    
    // -------------------- Attribute -------------------- //
    
    public static void attributeShort(@NotNull String name, short value)
    {
        Renderer.LOGGER.trace("attributeShort(%s, %s)", name, value);
        
        GL44.glVertexAttrib1s(Renderer.boundProgram.getUniform(name), value);
    }
    
    public static void attributeInt(@NotNull String name, int value)
    {
        Renderer.LOGGER.trace("attributeInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1i(Renderer.boundProgram.getUniform(name), value);
    }
    
    public static void attributeUInt(@NotNull String name, long value)
    {
        Renderer.LOGGER.trace("attributeUInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1ui(Renderer.boundProgram.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public static void attributeFloat(@NotNull String name, double value)
    {
        Renderer.LOGGER.trace("attributeFloat(%s, %s)", name, value);
        
        GL44.glVertexAttrib1f(Renderer.boundProgram.getUniform(name), (float) value);
    }
    
    public static void attributeShort2(@NotNull String name, short x, short y)
    {
        Renderer.LOGGER.trace("attributeShort2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2s(Renderer.boundProgram.getUniform(name), x, y);
    }
    
    public static void attributeInt2(@NotNull String name, int x, int y)
    {
        Renderer.LOGGER.trace("attributeInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2i(Renderer.boundProgram.getUniform(name), x, y);
    }
    
    public static void attributeInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeInt2(name, vec.x(), vec.y());
    }
    
    public static void attributeUInt2(@NotNull String name, long x, long y)
    {
        Renderer.LOGGER.trace("attributeUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeUInt2(name, vec.x(), vec.y());
    }
    
    public static void attributeFloat2(@NotNull String name, double x, double y)
    {
        Renderer.LOGGER.trace("attributeFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2f(Renderer.boundProgram.getUniform(name), (float) x, (float) y);
    }
    
    public static void attributeFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        attributeFloat2(name, vec.x(), vec.y());
    }
    
    public static void attributeShort3(@NotNull String name, short x, short y, short z)
    {
        Renderer.LOGGER.trace("attributeShort3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3s(Renderer.boundProgram.getUniform(name), x, y, z);
    }
    
    public static void attributeInt3(@NotNull String name, int x, int y, int z)
    {
        Renderer.LOGGER.trace("attributeInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3i(Renderer.boundProgram.getUniform(name), x, y, z);
    }
    
    public static void attributeInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeUInt3(@NotNull String name, long x, long y, long z)
    {
        Renderer.LOGGER.trace("attributeUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeFloat3(@NotNull String name, double x, double y, double z)
    {
        Renderer.LOGGER.trace("attributeFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3f(Renderer.boundProgram.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public static void attributeFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        attributeFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeShort4(@NotNull String name, short x, short y, short z, short w)
    {
        Renderer.LOGGER.trace("attributeShort4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4s(Renderer.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public static void attributeInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("attributeInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4i(Renderer.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public static void attributeInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Renderer.LOGGER.trace("attributeUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Renderer.LOGGER.trace("attributeFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4f(Renderer.boundProgram.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public static void attributeFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        attributeFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeNormalizedUByte4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("attributeNormalizedUByte4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4Nub(Renderer.boundProgram.getUniform(name), (byte) (x & 0xFF), (byte) (y & 0xFF), (byte) (z & 0xFF), (byte) (w & 0xFF));
    }
    
    // -------------------- Uniform -------------------- //
    
    public static void uniformBool(@NotNull String name, boolean value)
    {
        Renderer.LOGGER.trace("uniformBool(%s, %s)", name, value);
        
        GL44.glUniform1i(Renderer.boundProgram.getUniform(name), value ? 1 : 0);
    }
    
    public static void uniformInt(@NotNull String name, int value)
    {
        Renderer.LOGGER.trace("uniformInt(%s, %s)", name, value);
        
        GL44.glUniform1i(Renderer.boundProgram.getUniform(name), value);
    }
    
    public static void uniformUInt(@NotNull String name, long value)
    {
        Renderer.LOGGER.trace("uniformUInt(%s, %s)", name, value);
        
        GL44.glUniform1ui(Renderer.boundProgram.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public static void uniformFloat(@NotNull String name, double value)
    {
        Renderer.LOGGER.trace("uniformFloat(%s, %s)", name, value);
        
        GL44.glUniform1f(Renderer.boundProgram.getUniform(name), (float) value);
    }
    
    public static void uniformBool2(@NotNull String name, boolean x, boolean y)
    {
        Renderer.LOGGER.trace("uniformBool2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Renderer.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0);
    }
    
    public static void uniformInt2(@NotNull String name, int x, int y)
    {
        Renderer.LOGGER.trace("uniformInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Renderer.boundProgram.getUniform(name), x, y);
    }
    
    public static void uniformInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformUInt2(@NotNull String name, long x, long y)
    {
        Renderer.LOGGER.trace("uniformUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformUInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformFloat2(@NotNull String name, double x, double y)
    {
        Renderer.LOGGER.trace("uniformFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2f(Renderer.boundProgram.getUniform(name), (float) x, (float) y);
    }
    
    public static void uniformFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        uniformFloat2(name, vec.x(), vec.y());
    }
    
    public static void uniformBool3(@NotNull String name, boolean x, boolean y, boolean z)
    {
        Renderer.LOGGER.trace("uniformBool3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Renderer.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
    }
    
    public static void uniformInt3(@NotNull String name, int x, int y, int z)
    {
        Renderer.LOGGER.trace("uniformInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Renderer.boundProgram.getUniform(name), x, y, z);
    }
    
    public static void uniformInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformUInt3(@NotNull String name, long x, long y, long z)
    {
        Renderer.LOGGER.trace("uniformUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformFloat3(@NotNull String name, double x, double y, double z)
    {
        Renderer.LOGGER.trace("uniformFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3f(Renderer.boundProgram.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public static void uniformFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        uniformFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformBool4(@NotNull String name, boolean x, boolean y, boolean z, boolean w)
    {
        Renderer.LOGGER.trace("uniformBool3(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Renderer.boundProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
    }
    
    public static void uniformInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("uniformInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Renderer.boundProgram.getUniform(name), x, y, z, w);
    }
    
    public static void uniformInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Renderer.LOGGER.trace("uniformUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4ui(Renderer.boundProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Renderer.LOGGER.trace("uniformFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4f(Renderer.boundProgram.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public static void uniformFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        uniformFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformMatrix2(@NotNull String name, boolean transpose, @NotNull Matrix2dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix2(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats((float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11());
            GL44.glUniformMatrix2fv(Renderer.boundProgram.getUniform(name), transpose, buffer);
        }
    }
    
    public static void uniformMatrix3(@NotNull String name, boolean transpose, @NotNull Matrix3dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix3(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix3fv(Renderer.boundProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
        }
    }
    
    public static void uniformMatrix4(@NotNull String name, boolean transpose, @NotNull Matrix4dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix4(%s, %s, %n%s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix4fv(Renderer.boundProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
        }
    }
    
    public static void uniformColor(@NotNull String name, @NotNull Colorc color)
    {
        Renderer.LOGGER.trace("uniformColor(%s, %s", name, color);
        
        GL44.glUniform4f(Renderer.boundProgram.getUniform(name), color.rf(), color.gf(), color.bf(), color.af());
    }
    
    // -------------------- Defaults -------------------- //
    
    static Shader      defaultVertexShader;
    static Shader      defaultFragmentShader;
    static Program     defaultProgram;
    static Texture     defaultTexture;
    static Framebuffer defaultFramebuffer;
    
    private static void setupDefaults()
    {
        Renderer.defaultVertexShader   = new Shader(Shader.Type.VERTEX, IOUtil.getPath("shader/default.vert"));
        Renderer.defaultFragmentShader = new Shader(Shader.Type.FRAGMENT, IOUtil.getPath("shader/default.frag"));
        
        Renderer.defaultProgram = Program.builder().shader(Renderer.defaultVertexShader).shader(Renderer.defaultFragmentShader).build();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ColorBuffer data = ColorBuffer.malloc(ColorFormat.RGBA, 1, stack);
            Renderer.defaultTexture = new Texture2D(data.put(0, 255, 255, 255, 255), 1, 1);
        }
        
        Renderer.defaultFramebuffer = Framebuffer.NULL; // TODO
    }
    
    private static void destroyDefaults()
    {
        Renderer.defaultFramebuffer.delete();
        Renderer.defaultFramebuffer = null;
        
        Renderer.defaultTexture.delete();
        Renderer.defaultTexture = null;
        
        Renderer.defaultProgram.delete();
        Renderer.defaultProgram = null;
        
        Renderer.defaultFragmentShader.delete();
        Renderer.defaultFragmentShader = null;
        Renderer.defaultVertexShader.delete();
        Renderer.defaultVertexShader = null;
    }
    
    // -------------------- Draw -------------------- //
    
    public static final int DRAW_ELEMENTS_COUNT = 8192;
    public static final int DRAW_MAX_TEXTURES   = 16;
    public static final int DRAW_CALLS_COUNT    = 32;
    
    static FloatBuffer drawVertexPos;    // (XYZ)  (shader-location = 0)
    static FloatBuffer drawVertexTex1;   // (UVQ)  (shader-location = 1)
    static FloatBuffer drawVertexNorm;   // (XYZ)  (shader-location = 2)
    static FloatBuffer drawVertexTan;    // (XYZ)  (shader-location = 3)
    static ByteBuffer  drawVertexColor;  // (RGBA) (shader-location = 4)
    static FloatBuffer drawVertexTex2;   // (UVQ)  (shader-location = 5)
    
    static VertexArray drawVertexArray;
    
    static int       drawTextureIndex;
    static String[]  drawTextureNames;
    static Texture[] drawTextures;
    
    static boolean drawVertexHasBegun;
    
    static int        drawCallIndex;
    static DrawCall[] drawCalls;
    
    static double drawCurrentDepth;
    
    static final Matrix4d drawMVP = new Matrix4d();
    
    static final Vector3d drawViewX = new Vector3d();
    static final Vector3d drawViewY = new Vector3d();
    static final Vector3d drawViewZ = new Vector3d();
    
    private static void setupDraw()
    {
        int vertexCount = Renderer.DRAW_ELEMENTS_COUNT * 4; // 4 vertices per quad
        
        Renderer.drawVertexPos   = MemoryUtil.memCallocFloat(vertexCount * 3); // 3 floats per position
        Renderer.drawVertexTex1  = MemoryUtil.memCallocFloat(vertexCount * 3); // 3 floats per texcoord
        Renderer.drawVertexNorm  = MemoryUtil.memCallocFloat(vertexCount * 3); // 3 floats per normal
        Renderer.drawVertexTan   = MemoryUtil.memCallocFloat(vertexCount * 3); // 3 floats per tangent
        Renderer.drawVertexColor = MemoryUtil.memCalloc(vertexCount * 4);      // 4 bytes  per color
        Renderer.drawVertexTex2  = MemoryUtil.memCallocFloat(vertexCount * 3); // 3 floats per texcoord2
        
        IntBuffer indices = MemoryUtil.memCallocInt(Renderer.DRAW_ELEMENTS_COUNT * 6); // 6 indices per quad
        for (int i = 0; i < Renderer.DRAW_ELEMENTS_COUNT; ++i)
        {
            indices.put(4 * i);
            indices.put(4 * i + 1);
            indices.put(4 * i + 2);
            indices.put(4 * i);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
        }
        
        Renderer.drawVertexArray = VertexArray.builder()
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.FLOAT, 3, false))
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.FLOAT, 3, false))
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.FLOAT, 3, false))
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.FLOAT, 3, false))
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.UNSIGNED_BYTE, 4, true))
                                              .buffer(BufferUsage.DYNAMIC_DRAW, vertexCount, new Attribute(GLType.FLOAT, 3, false))
                                              .indexBuffer(BufferUsage.STATIC_DRAW, indices.clear())
                                              .build();
        MemoryUtil.memFree(indices);
        
        Renderer.drawTextureIndex = 0;
        Renderer.drawTextureNames = new String[Renderer.DRAW_MAX_TEXTURES];
        Renderer.drawTextures     = new Texture[Renderer.DRAW_MAX_TEXTURES];
        
        Renderer.drawVertexHasBegun = false;
        
        Renderer.drawCallIndex = 0;
        Renderer.drawCalls     = new DrawCall[Renderer.DRAW_CALLS_COUNT];
        for (int i = 0; i < Renderer.DRAW_CALLS_COUNT; i++) Renderer.drawCalls[i] = new DrawCall();
        
        Renderer.drawCurrentDepth = 0.99995;
    }
    
    private static void destroyDraw()
    {
        Renderer.drawCallIndex = -1;
        for (int i = 0; i < Renderer.DRAW_CALLS_COUNT; i++)
        {
            Renderer.drawCalls[i].reset();
            Renderer.drawCalls[i] = null;
        }
        Renderer.drawCalls = null;
        
        Renderer.drawVertexArray.delete();
        Renderer.drawVertexArray = null;
        
        // Free vertex arrays memory from CPU (RAM)
        MemoryUtil.memFree(Renderer.drawVertexPos);
        MemoryUtil.memFree(Renderer.drawVertexTex1);
        MemoryUtil.memFree(Renderer.drawVertexNorm);
        MemoryUtil.memFree(Renderer.drawVertexTan);
        MemoryUtil.memFree(Renderer.drawVertexColor);
        MemoryUtil.memFree(Renderer.drawVertexTex2);
    }
    
    public static void drawSetTexture(@NotNull Texture texture)
    {
        Renderer.LOGGER.trace("drawSetTexture(%s)", texture);
        
        if (Renderer.drawCalls[Renderer.drawCallIndex].texture != texture)
        {
            Renderer.drawIncrementCall();
            
            Renderer.drawCalls[Renderer.drawCallIndex].texture = texture;
        }
    }
    
    public static void drawAddTexture(@NotNull Texture texture)
    {
        Renderer.LOGGER.trace("drawSetTexture(%s)", texture);
        
        if (Renderer.drawCalls[Renderer.drawCallIndex].texture != texture)
        {
            Renderer.drawIncrementCall();
            
            Renderer.drawCalls[Renderer.drawCallIndex].texture = texture;
        }
    }
    
    public static void drawVertexBegin(@NotNull DrawMode mode)
    {
        if (Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not ended");
        Renderer.drawVertexHasBegun = true;
        
        Renderer.LOGGER.trace("vertexBegin(%s)", mode);
        
        if (Renderer.drawCalls[Renderer.drawCallIndex].mode != mode)
        {
            Renderer.drawIncrementCall();
            
            Renderer.drawCalls[Renderer.drawCallIndex].mode = mode;
        }
    }
    
    public static void drawVertexEnd()
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        Renderer.drawVertexHasBegun = false;
        
        Renderer.LOGGER.trace("drawEnd()");
        
        int limit;
        
        // Make sure drawVertexTex1 count match vertex count
        limit = Renderer.drawVertexPos.position() / 3 - Renderer.drawVertexTex1.position() / 3;
        for (int i = 0; i < limit; i++) Renderer.drawVertexTex1.put(0F).put(0F).put(1F);
        
        // Make sure drawVertexNorm count match vertex count
        limit = Renderer.drawVertexPos.position() / 3 - Renderer.drawVertexNorm.position() / 3;
        for (int i = 0; i < limit; i++) Renderer.drawVertexNorm.put(0F).put(0F).put(1F);
        
        // Make sure drawVertexTan count match vertex count
        limit = Renderer.drawVertexPos.position() / 3 - Renderer.drawVertexTan.position() / 3;
        for (int i = 0; i < limit; i++) Renderer.drawVertexTan.put(1F).put(0F).put(0F);
        
        // Make sure drawVertexColor count match vertex count
        limit = Renderer.drawVertexPos.position() / 3 - Renderer.drawVertexColor.position() / 4;
        for (int i = 0, index; i < limit; i++)
        {
            index = Renderer.drawVertexColor.position() - 4;
            Renderer.drawVertexColor.put(Renderer.drawVertexColor.get(index));
            Renderer.drawVertexColor.put(Renderer.drawVertexColor.get(index + 1));
            Renderer.drawVertexColor.put(Renderer.drawVertexColor.get(index + 2));
            Renderer.drawVertexColor.put(Renderer.drawVertexColor.get(index + 3));
        }
        
        // Make sure drawVertexTex2 count match vertex count
        limit = Renderer.drawVertexPos.position() / 3 - Renderer.drawVertexTex2.position() / 3;
        for (int i = 0; i < limit; i++) Renderer.drawVertexTex2.put(0F).put(0F).put(1F);
        
        // Correct increment formula would be: depthInc = (zFar - zNear)/pow(2, bits)
        Renderer.drawCurrentDepth -= 0.00005;
    }
    
    public static void drawVertexPos(double x, double y, double z)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        // Verify that current vertex buffer elements limit has not been reached
        if (Renderer.drawVertexPos.position() / 4 < Renderer.DRAW_ELEMENTS_COUNT * 4)
        {
            Renderer.LOGGER.trace("drawVertexPos(%s, %s, %s)", x, y, z);
            
            Renderer.drawVertexPos.put((float) x).put((float) y).put((float) z);
            
            Renderer.drawCalls[Renderer.drawCallIndex].vertexCount++;
        }
        else
        {
            Renderer.LOGGER.severe("Vertex Buffer Overflow");
        }
    }
    
    public static void drawVertexPos(double x, double y)
    {
        drawVertexPos(x, y, Renderer.drawCurrentDepth);
    }
    
    public static void drawVertexTexCoord(double u, double v, double q)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        Renderer.LOGGER.trace("drawVertexTexCoord(%s, %s, %s)", u, v, q);
        
        Renderer.drawVertexTex1.put((float) u).put((float) v).put((float) q);
    }
    
    public static void drawVertexTexCoord(double u, double v)
    {
        drawVertexTexCoord(u, v, 1.0);
    }
    
    public static void drawVertexNormal(double x, double y, double z)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        Renderer.LOGGER.trace("drawVertexNormal(%s, %s, %s)", x, y, z);
        
        Renderer.drawVertexNorm.put((float) x).put((float) y).put((float) z);
    }
    
    public static void drawVertexTangent(double x, double y, double z)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        Renderer.LOGGER.trace("drawVertexTangent(%s, %s, %s)", x, y, z);
        
        Renderer.drawVertexTan.put((float) x).put((float) y).put((float) z);
    }
    
    public static void drawVertexColor(int r, int g, int b, int a)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        Renderer.LOGGER.trace("drawVertexColor(%s, %s, %s, %s)", r, g, b, a);
        
        Renderer.drawVertexColor.put((byte) (r & 0xFF)).put((byte) (g & 0xFF)).put((byte) (b & 0xFF)).put((byte) (a & 0xFF));
    }
    
    public static void drawVertexTexCoord2(double u, double v, double q)
    {
        if (!Renderer.drawVertexHasBegun) throw new IllegalStateException("Drawing was not stared");
        
        Renderer.LOGGER.trace("drawVertexTexCoord2(%s, %s, %s)", u, v, q);
        
        Renderer.drawVertexTex2.put((float) u).put((float) v).put((float) q);
    }
    
    public static void drawVertexTexCoord2(double u, double v)
    {
        drawVertexTexCoord2(u, v, 1.0);
    }
    
    public static void drawVertices()
    {
        // Check to see if the vertex array was updated.
        if (Renderer.drawVertexPos.position() > 0)
        {
            //Renderer.internalStats.vertices += Renderer.drawVertexPos.position(); // TODO
            
            bind(Renderer.boundVertexArray);
            
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_POSITION)).set(0, Renderer.drawVertexPos.flip());
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_TEXCOORD)).set(0, Renderer.drawVertexTex1.flip());
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_NORMAL)).set(0, Renderer.drawVertexNorm.flip());
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_TANGENT)).set(0, Renderer.drawVertexTan.flip());
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_COLOR)).set(0, Renderer.drawVertexColor.flip());
            Renderer.boundVertexArray.buffers.get(Program.DEFAULT_ATTRIBUTES.indexOf(Program.ATTRIBUTE_TEXCOORD2)).set(0, Renderer.drawVertexTex2.flip());
            
            // Get the values at the stack location
            Matrix4d projection = stateProjection();
            Matrix4d view       = stateView();
            Matrix4d model      = stateModel();
            Matrix4d normal     = stateNormal();
            
            Color diffuse  = stateDiffuse();
            Color specular = stateSpecular();
            Color ambient  = stateAmbient();
            
            // Create modelView-projection matrix
            Renderer.drawMVP.set(projection);
            Renderer.drawMVP.mul(view);
            Renderer.drawMVP.mul(model);
            
            view.transformDirection(Renderer.drawViewX.set(1, 0, 0));
            view.transformDirection(Renderer.drawViewY.set(0, 1, 0));
            view.transformDirection(Renderer.drawViewZ.set(0, 0, 1));
            
            // Upload to Shader
            uniformMatrix4(Program.UNIFORM_MATRIX_MVP, false, Renderer.drawMVP);
            uniformMatrix4(Program.UNIFORM_MATRIX_PROJECTION, false, projection);
            uniformMatrix4(Program.UNIFORM_MATRIX_VIEW, false, view);
            uniformMatrix4(Program.UNIFORM_MATRIX_MODEL, false, model);
            uniformMatrix4(Program.UNIFORM_MATRIX_NORMAL, false, normal);
            uniformFloat3(Program.UNIFORM_VECTOR_VIEW_X, Renderer.drawViewX);
            uniformFloat3(Program.UNIFORM_VECTOR_VIEW_Y, Renderer.drawViewY);
            uniformFloat3(Program.UNIFORM_VECTOR_VIEW_Z, Renderer.drawViewZ);
            uniformColor(Program.UNIFORM_COLOR_DIFFUSE, diffuse);
            uniformColor(Program.UNIFORM_COLOR_SPECULAR, specular);
            uniformColor(Program.UNIFORM_COLOR_AMBIENT, ambient);
            
            // TODO - Is this needed?
            uniformInt(Program.MAP_DIFFUSE, 0);
            uniformInt(Program.MAP_SPECULAR, 1);
            uniformInt(Program.MAP_NORMAL, 2);
            uniformInt(Program.MAP_ROUGHNESS, 3);
            uniformInt(Program.MAP_OCCLUSION, 4);
            uniformInt(Program.MAP_EMISSION, 5);
            uniformInt(Program.MAP_HEIGHT, 6);
            uniformInt(Program.MAP_CUBEMAP, 7);
            uniformInt(Program.MAP_IRRADIANCE, 8);
            uniformInt(Program.MAP_PREFILTER, 9);
            uniformInt(Program.MAP_BRDF, 10);
            
            for (int i = 0; i < Renderer.drawTextureIndex; i++)
            {
                activeTexture(i + 1);
                bind(Renderer.drawTextures[i]);
                uniformInt(Renderer.drawTextureNames[i], i + 1);
            }
            
            for (int i = 0, offset = 0; i <= Renderer.drawCallIndex; i++)
            {
                //Renderer.internalStats.draws++; // TODO
                
                DrawCall drawCall = Renderer.drawCalls[i];
                
                activeTexture(0);
                bind(drawCall.texture);
                
                if (drawCall.mode == DrawMode.QUADS)
                {
                    Renderer.boundVertexArray.drawElements(DrawMode.TRIANGLES, Integer.toUnsignedLong(offset / 4 * 6), drawCall.vertexCount / 4 * 6);
                }
                else
                {
                    Renderer.boundVertexArray.draw(drawCall.mode, offset, drawCall.vertexCount);
                }
                
                offset += drawCall.vertexCount + drawCall.alignment;
            }
            
            for (int i = 0; i < Renderer.drawTextureIndex; i++)
            {
                activeTexture(i + 1);
                bind(Texture2D.NULL);
                
                Renderer.drawTextureNames[i] = null;
                Renderer.drawTextures[i]     = null;
            }
            Renderer.drawTextureIndex = 0;
            
            // Reset Vertex Array and increment buffer objects (in case of multi-buffering)
            Renderer.drawVertexPos.clear();
            Renderer.drawVertexTex1.clear();
            Renderer.drawVertexNorm.clear();
            Renderer.drawVertexTan.clear();
            Renderer.drawVertexColor.clear();
            Renderer.drawVertexTex2.clear();
            
            // Reset Draw Calls
            Renderer.drawCallIndex = 0;
            // This doesn't need to happen because the draw call is reset when it is incremented.
            // This happens to get rid of the reference to the Texture
            for (DrawCall drawCall : Renderer.drawCalls) drawCall.reset();
            
            // Reset Depth
            Renderer.drawCurrentDepth = 0.99995;
        }
    }
    
    public static void drawIfOverflow(int count)
    {
        if (Renderer.drawVertexPos.position() / 3 + count >= Renderer.DRAW_ELEMENTS_COUNT * 4) drawVertices();
    }
    
    private static void drawIncrementCall()
    {
        DrawCall drawCall = Renderer.drawCalls[Renderer.drawCallIndex];
        
        // Check to see if DrawCall is empty
        if (drawCall.vertexCount > 0)
        {
            // Make sure current this.draw.count is aligned a multiple of 4,
            // that way, following QUADS drawing will keep aligned with index processing
            // It implies adding some extra alignment vertex at the end of the draw,
            // those vertex are not processed, but they are considered as an additional offset
            // for the next set of vertex to be drawn
            
            int offset = drawCall.vertexCount % 4;
            drawCall.alignment = offset != 0 ? 4 - offset : 0;
            
            if (drawCall.alignment > 0)
            {
                drawIfOverflow(drawCall.alignment);
                
                Renderer.drawVertexPos.position(Renderer.drawVertexPos.position() + drawCall.alignment);
                Renderer.drawVertexTex1.position(Renderer.drawVertexTex1.position() + drawCall.alignment);
                Renderer.drawVertexNorm.position(Renderer.drawVertexNorm.position() + drawCall.alignment);
                Renderer.drawVertexTan.position(Renderer.drawVertexTan.position() + drawCall.alignment);
                Renderer.drawVertexColor.position(Renderer.drawVertexColor.position() + drawCall.alignment);
                Renderer.drawVertexTex2.position(Renderer.drawVertexTex2.position() + drawCall.alignment);
            }
            
            if (++Renderer.drawCallIndex >= Renderer.drawCalls.length) drawVertices();
        }
    }
    
    // -------------------- Functions -------------------- //
    
    public static void clearScreenBuffers()
    {
        Renderer.LOGGER.trace("Clearing All Buffers");
        
        GL44.glClear(GL44.GL_COLOR_BUFFER_BIT | GL44.GL_DEPTH_BUFFER_BIT | GL44.GL_STENCIL_BUFFER_BIT);
    }
    
    public static void clearScreenBuffers(@NotNull ScreenBuffer... buffers)
    {
        Renderer.LOGGER.trace("Clearing Buffers:", buffers);
        
        int mask = 0;
        for (ScreenBuffer buffer : buffers) mask |= buffer.ref;
        GL44.glClear(mask);
    }
    
    private static @NotNull ColorBuffer readBuffer(int buffer, int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        ByteBuffer data = MemoryUtil.memAlloc(width * height * format.sizeof);
        
        GL44.glReadBuffer(buffer);
        GL44.glReadPixels(x, y, width, height, format.format, GL44.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(data));
        
        // Flip data vertically
        int    s    = width * format.sizeof;
        byte[] tmp1 = new byte[s], tmp2 = new byte[s];
        for (int i = 0, n = height >> 1, col1, col2; i < n; i++)
        {
            col1 = i * s;
            col2 = (height - i - 1) * s;
            data.get(col1, tmp1);
            data.get(col2, tmp2);
            data.put(col1, tmp2);
            data.put(col2, tmp1);
        }
        
        return ColorBuffer.wrap(format, data);
    }
    
    public static @NotNull ColorBuffer readFrontBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        return readBuffer(GL44.GL_FRONT, x, y, width, height, format);
    }
    
    public static @NotNull ColorBuffer readBackBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        return readBuffer(GL44.GL_BACK, x, y, width, height, format);
    }
    
    // -------------------- Sub Classes -------------------- //
    
    private static final class DrawCall
    {
        private DrawMode mode;
        
        private int vertexCount;
        private int alignment;
        
        private Texture texture;
        
        private DrawCall()
        {
            reset();
        }
        
        private void reset()
        {
            this.mode = DrawMode.DEFAULT;
            
            this.vertexCount = 0;
            this.alignment   = 0;
            
            this.texture = Renderer.defaultTexture;
        }
    }
    
    private Renderer() {}
}
