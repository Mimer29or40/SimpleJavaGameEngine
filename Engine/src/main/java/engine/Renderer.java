package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.gl.*;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public static final int STACK_SIZE = 32;
    
    private static final ScissorMode scissorModeCustom = new ScissorMode(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
        
        final int index = Renderer.stackIndex;
        
        Renderer.DEPTH_CLAMP[index]            = GL40.glIsEnabled(GL40.GL_DEPTH_CLAMP);
        Renderer.LINE_SMOOTH[index]            = GL40.glIsEnabled(GL40.GL_LINE_SMOOTH);
        Renderer.textureCubeMapSeamless[index] = GL40.glIsEnabled(GL40.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        
        Renderer.wireframe[index] = GL40.glGetInteger(GL40.GL_FRONT_AND_BACK) == GL40.GL_FILL;
        
        Renderer.blendMode[index]   = null;
        Renderer.depthMode[index]   = null;
        Renderer.stencilMode[index] = null;
        Renderer.scissorMode[index] = null;
        
        Renderer.colorMask[index]   = new boolean[] {false, false, false, false};
        Renderer.depthMask[index]   = false;
        Renderer.stencilMask[index] = 0x00;
        
        Renderer.clearColor[index]   = new double[] {0.0, 0.0, 0.0, 0.0};
        Renderer.clearDepth[index]   = 0.0;
        Renderer.clearStencil[index] = 0xFF;
        
        Renderer.cullFace[index] = null;
        Renderer.winding[index]  = null;
        
        Renderer.program[index] = null;
        //Renderer.framebuffer[index] = null;
        
        stateDefault();
        clearScreenBuffers();
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
    }
    
    // -------------------- State -------------------- //
    
    static int stackIndex;
    
    static final boolean[] DEPTH_CLAMP            = new boolean[Renderer.STACK_SIZE];
    static final boolean[] LINE_SMOOTH            = new boolean[Renderer.STACK_SIZE];
    static final boolean[] textureCubeMapSeamless = new boolean[Renderer.STACK_SIZE];
    
    static final boolean[] wireframe = new boolean[Renderer.STACK_SIZE];
    
    static final BlendMode[]   blendMode   = new BlendMode[Renderer.STACK_SIZE];
    static final DepthMode[]   depthMode   = new DepthMode[Renderer.STACK_SIZE];
    static final StencilMode[] stencilMode = new StencilMode[Renderer.STACK_SIZE];
    static final ScissorMode[] scissorMode = new ScissorMode[Renderer.STACK_SIZE];
    
    static final boolean[][] colorMask   = new boolean[Renderer.STACK_SIZE][4];
    static final boolean[]   depthMask   = new boolean[Renderer.STACK_SIZE];
    static final int[]       stencilMask = new int[Renderer.STACK_SIZE];
    
    static final double[][] clearColor   = new double[Renderer.STACK_SIZE][4];
    static final double[]   clearDepth   = new double[Renderer.STACK_SIZE];
    static final int[]      clearStencil = new int[Renderer.STACK_SIZE];
    
    static final CullFace[] cullFace = new CullFace[Renderer.STACK_SIZE];
    static final Winding[]  winding  = new Winding[Renderer.STACK_SIZE];
    
    static final Program[] program = new Program[Renderer.STACK_SIZE];
    //static final Framebuffer[] framebuffer  = new Framebuffer[Renderer.STACK_SIZE];  // TODO
    
    static final Matrix4d[] projection = new Matrix4d[Renderer.STACK_SIZE];
    static final Matrix4d[] view       = new Matrix4d[Renderer.STACK_SIZE];
    static final Matrix4d[] model      = new Matrix4d[Renderer.STACK_SIZE];
    static final Matrix4d[] normal     = new Matrix4d[Renderer.STACK_SIZE];
    
    static final Color[] diffuse  = new Color[Renderer.STACK_SIZE];
    static final Color[] specular = new Color[Renderer.STACK_SIZE];
    static final Color[] ambient  = new Color[Renderer.STACK_SIZE];
    
    //static       int         textureIndex;
    //static final String[]    textureNames;
    //static final GLTexture[] textureActive;
    
    static final Matrix4d mvp = new Matrix4d();
    
    static final Vector3d viewX = new Vector3d();
    static final Vector3d viewY = new Vector3d();
    static final Vector3d viewZ = new Vector3d();
    
    static
    {
        for (int i = 0; i < Renderer.STACK_SIZE; i++)
        {
            Renderer.projection[i] = new Matrix4d();
            Renderer.view[i]       = new Matrix4d();
            Renderer.model[i]      = new Matrix4d();
            Renderer.normal[i]     = new Matrix4d();
            
            Renderer.diffuse[i]  = new Color();
            Renderer.specular[i] = new Color();
            Renderer.ambient[i]  = new Color();
        }
    }
    
    // -------------------- State Functions -------------------- //
    
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
        
        stateProgram(Program.NULL);  // TODO - Set to Default Program
        //Renderer.framebuffer[Renderer.stackIndex];  // TODO
        
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
        final int idx     = Renderer.stackIndex;
        final int nextIdx = idx + 1;
        
        Renderer.DEPTH_CLAMP[nextIdx]            = Renderer.DEPTH_CLAMP[idx];
        Renderer.LINE_SMOOTH[nextIdx]            = Renderer.LINE_SMOOTH[idx];
        Renderer.textureCubeMapSeamless[nextIdx] = Renderer.textureCubeMapSeamless[idx];
        
        Renderer.wireframe[nextIdx] = Renderer.wireframe[idx];
        
        Renderer.blendMode[nextIdx]   = Renderer.blendMode[idx];
        Renderer.depthMode[nextIdx]   = Renderer.depthMode[idx];
        Renderer.stencilMode[nextIdx] = Renderer.stencilMode[idx];
        Renderer.scissorMode[nextIdx] = Renderer.scissorMode[idx];
        
        Renderer.colorMask[nextIdx][0] = Renderer.colorMask[idx][0];
        Renderer.colorMask[nextIdx][1] = Renderer.colorMask[idx][1];
        Renderer.colorMask[nextIdx][2] = Renderer.colorMask[idx][2];
        Renderer.colorMask[nextIdx][3] = Renderer.colorMask[idx][3];
        Renderer.depthMask[nextIdx]    = Renderer.depthMask[idx];
        Renderer.stencilMask[nextIdx]  = Renderer.stencilMask[idx];
        
        Renderer.clearColor[nextIdx][0] = Renderer.clearColor[idx][0];
        Renderer.clearColor[nextIdx][1] = Renderer.clearColor[idx][1];
        Renderer.clearColor[nextIdx][2] = Renderer.clearColor[idx][2];
        Renderer.clearColor[nextIdx][3] = Renderer.clearColor[idx][3];
        Renderer.clearDepth[nextIdx]    = Renderer.clearDepth[idx];
        Renderer.clearStencil[nextIdx]  = Renderer.clearStencil[idx];
        
        Renderer.cullFace[nextIdx] = Renderer.cullFace[idx];
        Renderer.winding[nextIdx]  = Renderer.winding[idx];
        
        Renderer.program[nextIdx] = Renderer.program[idx];
        //Renderer.framebuffer[nextIdx] = Renderer.framebuffer[idx];  // TODO
        
        Renderer.projection[nextIdx].set(Renderer.projection[idx]);
        Renderer.view[nextIdx].set(Renderer.view[idx]);
        Renderer.model[nextIdx].set(Renderer.model[idx]);
        Renderer.normal[nextIdx].set(Renderer.normal[idx]);
        
        Renderer.diffuse[nextIdx].set(Renderer.diffuse[idx]);
        Renderer.specular[nextIdx].set(Renderer.specular[idx]);
        Renderer.ambient[nextIdx].set(Renderer.ambient[idx]);
        
        Renderer.stackIndex = nextIdx;
    }
    
    public static void statePop()
    {
        final int idx     = Renderer.stackIndex;
        final int prevIdx = idx - 1;
        
        stateDepthClamp(Renderer.DEPTH_CLAMP[prevIdx]);
        stateLineSmooth(Renderer.LINE_SMOOTH[prevIdx]);
        stateTextureCubeMapSeamless(Renderer.textureCubeMapSeamless[prevIdx]);
        
        stateWireframe(Renderer.wireframe[prevIdx]);
        
        stateBlendMode(Renderer.blendMode[prevIdx]);
        stateDepthMode(Renderer.depthMode[prevIdx]);
        stateStencilMode(Renderer.stencilMode[prevIdx]);
        stateScissorMode(Renderer.scissorMode[prevIdx]);
        
        boolean[] colorMask = Renderer.colorMask[prevIdx];
        stateColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
        stateDepthMask(Renderer.depthMask[prevIdx]);
        stateStencilMask(Renderer.stencilMask[prevIdx]);
        
        double[] clearColor = Renderer.clearColor[prevIdx];
        stateClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        stateClearDepth(Renderer.clearDepth[prevIdx]);
        stateClearStencil(Renderer.clearStencil[prevIdx]);
        
        stateCullFace(Renderer.cullFace[prevIdx]);
        stateWinding(Renderer.winding[prevIdx]);
        
        stateProgram(Renderer.program[prevIdx]);
        //stateFramebuffer(Renderer.framebuffer[prevIdx]);  // TODO
        
        // Nn need to set matrix or color stack
        
        Renderer.stackIndex = prevIdx;
    }
    
    public static void stateDepthClamp(boolean depthClamp)
    {
        Renderer.LOGGER.trace("Setting Depth Clamp Flag:", depthClamp);
        
        if (Renderer.DEPTH_CLAMP[Renderer.stackIndex] != depthClamp)
        {
            Renderer.DEPTH_CLAMP[Renderer.stackIndex] = depthClamp;
            
            if (depthClamp)
            {
                GL40.glEnable(GL40.GL_DEPTH_CLAMP);
            }
            else
            {
                GL40.glDisable(GL40.GL_DEPTH_CLAMP);
            }
        }
    }
    
    public static void stateLineSmooth(boolean lineSmooth)
    {
        Renderer.LOGGER.trace("Setting Line Smooth Flag:", lineSmooth);
        
        if (Renderer.LINE_SMOOTH[Renderer.stackIndex] != lineSmooth)
        {
            Renderer.LINE_SMOOTH[Renderer.stackIndex] = lineSmooth;
            
            if (lineSmooth)
            {
                GL40.glEnable(GL40.GL_LINE_SMOOTH);
            }
            else
            {
                GL40.glDisable(GL40.GL_LINE_SMOOTH);
            }
        }
    }
    
    public static void stateTextureCubeMapSeamless(boolean textureCubeMapSeamless)
    {
        Renderer.LOGGER.trace("Setting Texture Cube Map Seamless Flag:", textureCubeMapSeamless);
        
        if (Renderer.textureCubeMapSeamless[Renderer.stackIndex] != textureCubeMapSeamless)
        {
            Renderer.textureCubeMapSeamless[Renderer.stackIndex] = textureCubeMapSeamless;
            
            if (textureCubeMapSeamless)
            {
                GL40.glEnable(GL40.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
            else
            {
                GL40.glDisable(GL40.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
        }
    }
    
    public static void stateWireframe(boolean wireframe)
    {
        Renderer.LOGGER.trace("Setting Wireframe Flag:", wireframe);
        
        if (Renderer.wireframe[Renderer.stackIndex] != wireframe)
        {
            Renderer.wireframe[Renderer.stackIndex] = wireframe;
            
            GL40.glPolygonMode(GL40.GL_FRONT_AND_BACK, wireframe ? GL40.GL_LINE : GL40.GL_FILL);
        }
    }
    
    public static void stateBlendMode(@Nullable BlendMode mode)
    {
        if (mode == null) mode = BlendMode.DEFAULT;
        
        Renderer.LOGGER.trace("Setting Blend Mode:", mode);
        
        if (Renderer.blendMode[Renderer.stackIndex] != mode)
        {
            Renderer.blendMode[Renderer.stackIndex] = mode;
            
            if (mode == BlendMode.NONE)
            {
                GL40.glDisable(GL40.GL_BLEND);
            }
            else
            {
                GL40.glEnable(GL40.GL_BLEND);
                GL40.glBlendFunc(mode.srcFunc().ref, mode.dstFunc().ref);
                GL40.glBlendEquation(mode.blendEqn().ref);
            }
        }
    }
    
    public static void stateDepthMode(@Nullable DepthMode mode)
    {
        if (mode == null) mode = DepthMode.DEFAULT;
        
        Renderer.LOGGER.trace("Setting Depth Mode:", mode);
        
        if (Renderer.depthMode[Renderer.stackIndex] != mode)
        {
            Renderer.depthMode[Renderer.stackIndex] = mode;
            
            if (mode == DepthMode.NONE)
            {
                GL40.glDisable(GL40.GL_DEPTH_TEST);
            }
            else
            {
                GL40.glEnable(GL40.GL_DEPTH_TEST);
                GL40.glDepthFunc(mode.ref);
            }
        }
    }
    
    public static void stateStencilMode(@Nullable StencilMode mode)
    {
        if (mode == null) mode = StencilMode.DEFAULT;
        
        Renderer.LOGGER.trace("Setting Stencil Mode:", mode);
        
        if (Renderer.stencilMode[Renderer.stackIndex] != mode)
        {
            Renderer.stencilMode[Renderer.stackIndex] = mode;
            
            if (mode == StencilMode.NONE)
            {
                GL40.glDisable(GL40.GL_STENCIL_TEST);
            }
            else
            {
                GL40.glEnable(GL40.GL_STENCIL_TEST);
                GL40.glStencilFunc(mode.func().ref, mode.ref(), mode.mask());
                GL40.glStencilOp(mode.sFail().ref, mode.dpFail().ref, mode.dpPass().ref);
            }
        }
    }
    
    public static void stateScissorMode(@Nullable ScissorMode mode)
    {
        if (mode == null) mode = ScissorMode.DEFAULT;
        
        Renderer.LOGGER.trace("Setting ScissorMode:", mode);
        
        if (Renderer.scissorMode[Renderer.stackIndex] != mode)
        {
            Renderer.scissorMode[Renderer.stackIndex] = mode;
            
            if (mode == ScissorMode.NONE)
            {
                GL40.glDisable(GL40.GL_SCISSOR_TEST);
            }
            else
            {
                GL40.glEnable(GL40.GL_SCISSOR_TEST);
                GL40.glScissor(mode.x(), mode.y(), mode.width(), mode.height());
            }
        }
    }
    
    public static void stateScissor(int x, int y, int width, int height)
    {
        Renderer.LOGGER.trace("Setting Custom Scissor: [%s, %s, %s, %s]", x, y, width, height);
        
        Renderer.scissorMode[Renderer.stackIndex] = Renderer.scissorModeCustom;
        
        GL40.glEnable(GL40.GL_SCISSOR_TEST);
        GL40.glScissor(x, y, width, height);
    }
    
    public static void stateColorMask(boolean r, boolean g, boolean b, boolean a)
    {
        Renderer.LOGGER.trace("Setting Color Mask: r=%s g=%s b=%s a=%s", r, g, b, a);
        
        boolean[] colorMask = Renderer.colorMask[Renderer.stackIndex];
        if (Boolean.compare(colorMask[0], r) != 0 || Boolean.compare(colorMask[1], g) != 0 || Boolean.compare(colorMask[2], b) != 0 || Boolean.compare(colorMask[3], a) != 0)
        {
            colorMask[0] = r;
            colorMask[1] = g;
            colorMask[2] = b;
            colorMask[3] = a;
            
            GL40.glColorMask(r, g, b, a);
        }
    }
    
    public static void stateDepthMask(boolean flag)
    {
        Renderer.LOGGER.trace("Setting Depth Mask:", flag);
        
        if (Renderer.depthMask[Renderer.stackIndex] != flag)
        {
            Renderer.depthMask[Renderer.stackIndex] = flag;
            
            GL40.glDepthMask(flag);
        }
    }
    
    public static void stateStencilMask(int mask)
    {
        Renderer.LOGGER.trace("Setting Stencil Mask: 0x%02X", mask);
        
        if (Renderer.stencilMask[Renderer.stackIndex] != mask)
        {
            Renderer.stencilMask[Renderer.stackIndex] = mask;
            
            GL40.glStencilMask(mask);
        }
    }
    
    public static void stateClearColor(double r, double g, double b, double a)
    {
        Renderer.LOGGER.trace("Setting Clear Color: (%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
        
        double[] clearColor = Renderer.clearColor[Renderer.stackIndex];
        if (Double.compare(clearColor[0], r) != 0 || Double.compare(clearColor[1], g) != 0 || Double.compare(clearColor[2], b) != 0 || Double.compare(clearColor[3], a) != 0)
        {
            clearColor[0] = r;
            clearColor[1] = g;
            clearColor[2] = b;
            clearColor[3] = a;
            
            GL40.glClearColor((float) r, (float) g, (float) b, (float) a);
        }
    }
    
    public static void stateClearDepth(double depth)
    {
        Renderer.LOGGER.trace("Setting Clear Depth: %.3f", depth);
        
        if (Double.compare(Renderer.clearDepth[Renderer.stackIndex], depth) != 0)
        {
            Renderer.clearDepth[Renderer.stackIndex] = depth;
            
            GL40.glClearDepth(depth);
        }
    }
    
    public static void stateClearStencil(int stencil)
    {
        Renderer.LOGGER.trace("Setting Clear Stencil: 0x%02X", stencil);
        
        if (Renderer.clearStencil[Renderer.stackIndex] != stencil)
        {
            Renderer.clearStencil[Renderer.stackIndex] = stencil;
            
            GL40.glClearStencil(stencil);
        }
    }
    
    public static void stateCullFace(@Nullable CullFace cullFace)
    {
        if (cullFace == null) cullFace = CullFace.DEFAULT;
        
        Renderer.LOGGER.trace("Setting Cull Face:", cullFace);
        
        if (Renderer.cullFace[Renderer.stackIndex] != cullFace)
        {
            Renderer.cullFace[Renderer.stackIndex] = cullFace;
            
            if (cullFace == CullFace.NONE)
            {
                GL40.glDisable(GL40.GL_CULL_FACE);
            }
            else
            {
                GL40.glEnable(GL40.GL_CULL_FACE);
                GL40.glCullFace(cullFace.ref);
            }
        }
    }
    
    public static void stateWinding(@Nullable Winding winding)
    {
        if (winding == null) winding = Winding.DEFAULT;
        
        Renderer.LOGGER.trace("Setting Winding:", winding);
        
        if (Renderer.winding[Renderer.stackIndex] != winding)
        {
            Renderer.winding[Renderer.stackIndex] = winding;
            
            GL40.glFrontFace(winding.ref);
        }
    }
    
    public static void stateProgram(@NotNull Program program)
    {
        Renderer.LOGGER.trace("Setting Program:", program);
        
        if (Renderer.program[Renderer.stackIndex] != program)
        {
            Renderer.program[Renderer.stackIndex] = program;
            
            GL40.glUseProgram(program.id());
        }
    }
    
    //public static @NotNull Framebuffer framebuffer()  // TODO
    //{
    //    return Renderer.framebuffer[Renderer.stackIndex];
    //}
    
    public static @NotNull Matrix4d stateProjection()
    {
        return Renderer.projection[Renderer.stackIndex];
    }
    
    public static @NotNull Matrix4d stateView()
    {
        return Renderer.view[Renderer.stackIndex];
    }
    
    public static @NotNull Matrix4d stateModel()
    {
        return Renderer.model[Renderer.stackIndex];
    }
    
    public static @NotNull Matrix4d stateNormal()
    {
        return Renderer.normal[Renderer.stackIndex];
    }
    
    public static @NotNull Color stateDiffuse()
    {
        return Renderer.diffuse[Renderer.stackIndex];
    }
    
    public static @NotNull Color stateSpecular()
    {
        return Renderer.specular[Renderer.stackIndex];
    }
    
    public static @NotNull Color stateAmbient()
    {
        return Renderer.ambient[Renderer.stackIndex];
    }
    
    // -------------------- Functions -------------------- //
    
    public static void clearScreenBuffers()
    {
        Renderer.LOGGER.trace("Clearing All Buffers");
        
        GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT | GL40.GL_STENCIL_BUFFER_BIT);
    }
    
    public static void clearScreenBuffers(@NotNull ScreenBuffer... buffers)
    {
        Renderer.LOGGER.trace("Clearing Buffers:", buffers);
        
        int mask = 0;
        for (ScreenBuffer buffer : buffers) mask |= buffer.ref;
        GL40.glClear(mask);
    }
    
    private static @NotNull ColorBuffer readBuffer(int buffer, int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        ByteBuffer data = MemoryUtil.memAlloc(width * height * format.sizeof);
        
        GL40.glReadBuffer(buffer);
        GL40.glReadPixels(x, y, width, height, format.format, GL40.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(data));
        
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
        return readBuffer(GL40.GL_FRONT, x, y, width, height, format);
    }
    
    public static @NotNull ColorBuffer readBackBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        return readBuffer(GL40.GL_BACK, x, y, width, height, format);
    }
    
    //public static void bind(@NotNull Texture texture, int index)  // TODO
    //{
    //    Renderer.LOGGER.trace("Binding %s to index=%s", texture, index);
    //
    //    GL40.glActiveTexture(GL40.GL_TEXTURE0 + index);
    //    GL40.glBindTexture(texture.type, texture.id);
    //}
    
    //public static void bind(@NotNull Texture texture)  // TODO
    //{
    //    bind(texture, 0);
    //}
    
    //public static void bind(@NotNull Buffer buffer)  // TODO
    //{
    //    Renderer.LOGGER.trace("Binding", buffer);
    //
    //    GL40.glBindBuffer(buffer.type, buffer.id);
    //}
    
    //public static void bind(@NotNull VertexArray vertexArray)  // TODO
    //{
    //    Renderer.LOGGER.trace("Binding", vertexArray);
    //
    //    GL40.glBindVertexArray(vertexArray.id);
    //}
    
    //public static void bind(@NotNull Framebuffer framebuffer)  // TODO
    //{
    //    Renderer.LOGGER.trace("Binding Framebuffer:", framebuffer);
    //
    //    if (Renderer.framebuffer != framebuffer)
    //    {
    //        Renderer.framebuffer = framebuffer;
    //
    //        GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, framebuffer.id());
    //        GL40.glViewport(0, 0, framebuffer.width(), framebuffer.height());
    //    }
    //}
    
    private Renderer() {}
}
