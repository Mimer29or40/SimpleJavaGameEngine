package engine.gl;

import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL44.*;

public class GL
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public static void defaultState()
    {
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        depthClamp(true);
        lineSmooth(false);
        textureCubeMapSeamless(true);
        
        polygonMode(PolygonMode.DEFAULT);
        
        blendMode(BlendMode.DEFAULT);
        depthMode(DepthMode.DEFAULT);
        stencilMode(StencilMode.DEFAULT);
        scissorMode(ScissorMode.DEFAULT);
        
        colorMask(true, true, true, true);
        depthMask(true);
        stencilMask(0xFF);
        
        clearColor(0.0, 0.0, 0.0, 1.0);
        clearDepth(1.0);
        clearStencil(0x00);
        
        cullFace(CullFace.DEFAULT);
        winding(Winding.DEFAULT);
    }
    
    private static void setFlag(int flag, boolean value)
    {
        if (value) {glEnable(flag);}
        else {glDisable(flag);}
    }
    
    public static void depthClamp(boolean depthClamp)
    {
        GL.LOGGER.trace("depthClamp(%s)", depthClamp);
        
        setFlag(GL_DEPTH_CLAMP, depthClamp);
    }
    
    public static void lineSmooth(boolean lineSmooth)
    {
        GL.LOGGER.trace("lineSmooth(%s)", lineSmooth);
        
        setFlag(GL_LINE_SMOOTH, lineSmooth);
    }
    
    public static void textureCubeMapSeamless(boolean textureCubeMapSeamless)
    {
        GL.LOGGER.trace("textureCubeMapSeamless(%s)", textureCubeMapSeamless);
        
        setFlag(GL_TEXTURE_CUBE_MAP_SEAMLESS, textureCubeMapSeamless);
    }
    
    public static void polygonMode(@NotNull PolygonMode mode)
    {
        GL.LOGGER.trace("polygonMode(%s)", mode);
        
        glPolygonMode(GL_FRONT_AND_BACK, mode.ref);
    }
    
    public static void blendMode(@NotNull BlendMode mode)
    {
        GL.LOGGER.trace("blendMode(%s)", mode);
        
        if (mode == BlendMode.NONE)
        {
            glDisable(GL_BLEND);
        }
        else
        {
            glEnable(GL_BLEND);
            glBlendFunc(mode.srcFunc().ref, mode.dstFunc().ref);
            glBlendEquation(mode.blendEqn().ref);
        }
    }
    
    public static void depthMode(@NotNull DepthMode mode)
    {
        GL.LOGGER.trace("depthMode(%s)", mode);
        
        if (mode == DepthMode.NONE)
        {
            glDisable(GL_DEPTH_TEST);
        }
        else
        {
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(mode.ref);
        }
    }
    
    public static void stencilMode(@NotNull StencilMode mode)
    {
        GL.LOGGER.trace("stencilMode(%s)", mode);
        
        if (mode == StencilMode.NONE)
        {
            glDisable(GL_STENCIL_TEST);
        }
        else
        {
            glEnable(GL_STENCIL_TEST);
            glStencilFunc(mode.func().ref, mode.ref(), mode.mask());
            glStencilOp(mode.sFail().ref, mode.dpFail().ref, mode.dpPass().ref);
        }
    }
    
    public static void scissorMode(@NotNull ScissorMode mode)
    {
        GL.LOGGER.trace("scissorMode(%s)", mode);
        
        if (mode == ScissorMode.NONE)
        {
            glDisable(GL_SCISSOR_TEST);
        }
        else
        {
            glEnable(GL_SCISSOR_TEST);
            glScissor(mode.x(), mode.y(), mode.width(), mode.height());
        }
    }
    
    public static void scissorMode(int x, int y, int width, int height)
    {
        GL.LOGGER.trace("scissorMode(%s, %s, %s, %s)", x, y, width, height);
        
        glEnable(GL_SCISSOR_TEST);
        glScissor(x, y, width, height);
    }
    
    public static void colorMask(boolean r, boolean g, boolean b, boolean a)
    {
        GL.LOGGER.trace("colorMask(%s, %s, %s, %s)", r, g, b, a);
        
        glColorMask(r, g, b, a);
    }
    
    public static void depthMask(boolean mask)
    {
        GL.LOGGER.trace("depthMask(%s)", mask);
        
        glDepthMask(mask);
    }
    
    public static void stencilMask(int mask)
    {
        GL.LOGGER.trace("stencilMask(0x%02X)", mask);
        
        glStencilMask(mask);
    }
    
    public static void clearColor(double r, double g, double b, double a)
    {
        GL.LOGGER.trace("clearColor(%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
        
        glClearColor((float) r, (float) g, (float) b, (float) a);
    }
    
    public static void clearDepth(double depth)
    {
        GL.LOGGER.trace("clearDepth(%.3f)", depth);
        
        glClearDepth(depth);
    }
    
    public static void clearStencil(int stencil)
    {
        GL.LOGGER.trace("clearStencil(0x%02X)", stencil);
        
        glClearStencil(stencil);
    }
    
    public static void clearBuffers()
    {
        GL.LOGGER.trace("clearBuffers()");
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }
    
    public static void clearBuffers(@NotNull ScreenBuffer buffer, @NotNull ScreenBuffer @NotNull ... others)
    {
        if (others.length == 0)
        {
            GL.LOGGER.trace("clearBuffers(%s)", buffer);
            glClear(buffer.ref);
            return;
        }
        
        GL.LOGGER.trace("clearBuffers(%s, %s)", buffer, others);
        
        int mask = buffer.ref;
        for (ScreenBuffer b : others) mask |= b.ref;
        glClear(mask);
    }
    
    public static void cullFace(@NotNull CullFace cullFace)
    {
        GL.LOGGER.trace("cullFace(%s)", cullFace);
        
        if (cullFace == CullFace.NONE)
        {
            glDisable(GL_CULL_FACE);
        }
        else
        {
            glEnable(GL_CULL_FACE);
            glCullFace(cullFace.ref);
        }
    }
    
    public static void winding(@NotNull Winding winding)
    {
        GL.LOGGER.trace("winding(%s)", winding);
        
        glFrontFace(winding.ref);
    }
    
    private static @NotNull ColorBuffer readBuffer(int buffer, int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        ByteBuffer data = MemoryUtil.memAlloc(width * height * format.sizeof);
        
        glReadBuffer(buffer);
        glReadPixels(x, y, width, height, format.format, GL_UNSIGNED_BYTE, MemoryUtil.memAddress(data));
        
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
        GL.LOGGER.trace("readFrontBuffer(%s, %s, %s, %s, %s)", x, y, width, height, format);
        
        return readBuffer(GL_FRONT, x, y, width, height, format);
    }
    
    public static @NotNull ColorBuffer readBackBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        GL.LOGGER.trace("readBackBuffer(%s, %s, %s, %s, %s)", x, y, width, height, format);
        
        return readBuffer(GL_BACK, x, y, width, height, format);
    }
    
    private GL() {}
}
