package engine;

import engine.color.Color;
import engine.color.ColorBuffer;
import engine.color.ColorFormat;
import engine.color.Colorc;
import engine.gl.*;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferArray;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.*;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Renderer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public static final int STACK_SIZE = 32;
    
    private static final ScissorMode scissorModeCustom = new ScissorMode(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    static Shader      defaultVertexShader;
    static Shader      defaultFragmentShader;
    static Program     defaultProgram;
    static Texture     defaultTexture;
    static Framebuffer defaultFramebuffer;
    
    static void setup()
    {
        Renderer.LOGGER.debug("Setup");
        Renderer.LOGGER.debug("OpenGL Version:", GL44.glGetString(GL44.GL_VERSION));
        
        final int index = Renderer.stackIndex;
        
        Renderer.depthClamp[index]             = GL44.glIsEnabled(GL44.GL_DEPTH_CLAMP);
        Renderer.lineSmooth[index]             = GL44.glIsEnabled(GL44.GL_LINE_SMOOTH);
        Renderer.textureCubeMapSeamless[index] = GL44.glIsEnabled(GL44.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        
        Renderer.wireframe[index] = GL44.glGetInteger(GL44.GL_FRONT_AND_BACK) == GL44.GL_FILL;
        
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
        
        Renderer.activeTexture = -1;
        Renderer.program       = null;
        Renderer.framebuffer   = null;
        Arrays.fill(Renderer.textures, null);
        Renderer.buffer = null;
    
        bind(Program.NULL);
        bind(Framebuffer.NULL);
        activeTexture(0);
        bind(Texture2D.NULL);
        bind(BufferArray.NULL);
        
        // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-1/
        // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-2/
        String vert = """
                      #version 330
                      in vec3 POSITION;
                      in vec3 TEXCOORD;
                      in vec4 COLOR;
                      out vec3 fragTexCoord;
                      out vec4 fragColor;
                      uniform mat4 MATRIX_MVP;
                      void main()
                      {
                          gl_Position = MATRIX_MVP * vec4(POSITION, 1.0);
                          fragTexCoord = TEXCOORD;
                          fragColor = COLOR;
                      }
                      """;
        String frag = """
                      #version 330
                      in vec3 fragTexCoord;
                      in vec4 fragColor;
                      out vec4 finalColor;
                      uniform sampler2D texture0;
                      void main()
                      {
                          vec4 texelColor = textureProj(texture0, fragTexCoord);
                          finalColor = texelColor * fragColor;
                      }
                      """;
        Renderer.defaultVertexShader   = new Shader(Shader.Type.VERTEX, vert);
        Renderer.defaultFragmentShader = new Shader(Shader.Type.FRAGMENT, frag);
        
        Renderer.defaultProgram = Program.builder().shader(Renderer.defaultVertexShader).shader(Renderer.defaultFragmentShader).build();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ColorBuffer data = ColorBuffer.malloc(ColorFormat.RGBA, 1, stack);
            Renderer.defaultTexture = new Texture2D(data.put(0, 255, 255, 255, 255), 1, 1);
        }
        
        Renderer.defaultFramebuffer = Framebuffer.NULL; // TODO
        
        stateDefault();
        
        bind(Renderer.defaultProgram);
        bind(Renderer.defaultFramebuffer);
        activeTexture(0);
        bind(Renderer.defaultTexture);
        bind(BufferArray.NULL);
        
        clearScreenBuffers();
    }
    
    static void destroy()
    {
        Renderer.LOGGER.debug("Destroy");
        
        Renderer.defaultTexture.delete();
        Renderer.defaultTexture = null;
        
        Renderer.defaultProgram.delete();
        Renderer.defaultProgram = null;
        
        Renderer.defaultFragmentShader.delete();
        Renderer.defaultFragmentShader = null;
        Renderer.defaultVertexShader.delete();
        Renderer.defaultVertexShader = null;
    }
    
    // -------------------- Stack State -------------------- //
    
    static int stackIndex;
    
    static final boolean[] depthClamp             = new boolean[Renderer.STACK_SIZE];
    static final boolean[] lineSmooth             = new boolean[Renderer.STACK_SIZE];
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
    
    // -------------------- Non-Stack State -------------------- //
    
    static int activeTexture;
    
    static       Program     program;
    static       Framebuffer framebuffer;
    static final Texture[]   textures = new Texture[32];
    static       Buffer      buffer;
    
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
    
    // -------------------- Stack State Functions -------------------- //
    
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
        final int idx     = Renderer.stackIndex;
        final int nextIdx = idx + 1;
        
        Renderer.depthClamp[nextIdx]             = Renderer.depthClamp[idx];
        Renderer.lineSmooth[nextIdx]             = Renderer.lineSmooth[idx];
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
        
        stateDepthClamp(Renderer.depthClamp[prevIdx]);
        stateLineSmooth(Renderer.lineSmooth[prevIdx]);
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
        
        // Nn need to set matrix or color stack
        
        Renderer.stackIndex = prevIdx;
    }
    
    public static void stateDepthClamp(boolean depthClamp)
    {
        if (Renderer.depthClamp[Renderer.stackIndex] != depthClamp)
        {
            Renderer.LOGGER.trace("Setting Depth Clamp Flag:", depthClamp);
            
            Renderer.depthClamp[Renderer.stackIndex] = depthClamp;
            
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
        if (Renderer.lineSmooth[Renderer.stackIndex] != lineSmooth)
        {
            Renderer.LOGGER.trace("Setting Line Smooth Flag:", lineSmooth);
            
            Renderer.lineSmooth[Renderer.stackIndex] = lineSmooth;
            
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
        if (Renderer.textureCubeMapSeamless[Renderer.stackIndex] != textureCubeMapSeamless)
        {
            Renderer.LOGGER.trace("Setting Texture Cube Map Seamless Flag:", textureCubeMapSeamless);
            
            Renderer.textureCubeMapSeamless[Renderer.stackIndex] = textureCubeMapSeamless;
            
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
        if (Renderer.wireframe[Renderer.stackIndex] != wireframe)
        {
            Renderer.LOGGER.trace("Setting Wireframe Flag:", wireframe);
            
            Renderer.wireframe[Renderer.stackIndex] = wireframe;
            
            GL44.glPolygonMode(GL44.GL_FRONT_AND_BACK, wireframe ? GL44.GL_LINE : GL44.GL_FILL);
        }
    }
    
    public static void stateBlendMode(@Nullable BlendMode mode)
    {
        if (mode == null) mode = BlendMode.DEFAULT;
        
        if (Renderer.blendMode[Renderer.stackIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Blend Mode:", mode);
            
            Renderer.blendMode[Renderer.stackIndex] = mode;
            
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
        
        if (Renderer.depthMode[Renderer.stackIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Depth Mode:", mode);
            
            Renderer.depthMode[Renderer.stackIndex] = mode;
            
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
        
        if (Renderer.stencilMode[Renderer.stackIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting Stencil Mode:", mode);
            
            Renderer.stencilMode[Renderer.stackIndex] = mode;
            
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
        
        if (Renderer.scissorMode[Renderer.stackIndex] != mode)
        {
            Renderer.LOGGER.trace("Setting ScissorMode:", mode);
            
            Renderer.scissorMode[Renderer.stackIndex] = mode;
            
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
        
        Renderer.scissorMode[Renderer.stackIndex] = Renderer.scissorModeCustom;
        
        GL44.glEnable(GL44.GL_SCISSOR_TEST);
        GL44.glScissor(x, y, width, height);
    }
    
    public static void stateColorMask(boolean r, boolean g, boolean b, boolean a)
    {
        boolean[] colorMask = Renderer.colorMask[Renderer.stackIndex];
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
        if (Renderer.depthMask[Renderer.stackIndex] != flag)
        {
            Renderer.LOGGER.trace("Setting Depth Mask:", flag);
            
            Renderer.depthMask[Renderer.stackIndex] = flag;
            
            GL44.glDepthMask(flag);
        }
    }
    
    public static void stateStencilMask(int mask)
    {
        if (Renderer.stencilMask[Renderer.stackIndex] != mask)
        {
            Renderer.LOGGER.trace("Setting Stencil Mask: 0x%02X", mask);
            
            Renderer.stencilMask[Renderer.stackIndex] = mask;
            
            GL44.glStencilMask(mask);
        }
    }
    
    public static void stateClearColor(double r, double g, double b, double a)
    {
        double[] clearColor = Renderer.clearColor[Renderer.stackIndex];
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
        if (Double.compare(Renderer.clearDepth[Renderer.stackIndex], depth) != 0)
        {
            Renderer.LOGGER.trace("Setting Clear Depth: %.3f", depth);
            
            Renderer.clearDepth[Renderer.stackIndex] = depth;
            
            GL44.glClearDepth(depth);
        }
    }
    
    public static void stateClearStencil(int stencil)
    {
        if (Renderer.clearStencil[Renderer.stackIndex] != stencil)
        {
            Renderer.LOGGER.trace("Setting Clear Stencil: 0x%02X", stencil);
            
            Renderer.clearStencil[Renderer.stackIndex] = stencil;
            
            GL44.glClearStencil(stencil);
        }
    }
    
    public static void stateCullFace(@Nullable CullFace cullFace)
    {
        if (cullFace == null) cullFace = CullFace.DEFAULT;
        
        if (Renderer.cullFace[Renderer.stackIndex] != cullFace)
        {
            Renderer.LOGGER.trace("Setting Cull Face:", cullFace);
            
            Renderer.cullFace[Renderer.stackIndex] = cullFace;
            
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
        
        if (Renderer.winding[Renderer.stackIndex] != winding)
        {
            Renderer.LOGGER.trace("Setting Winding:", winding);
            
            Renderer.winding[Renderer.stackIndex] = winding;
            
            GL44.glFrontFace(winding.ref);
        }
    }
    
    // -------------------- Non-Stack State Functions -------------------- //
    
    public static void bind(@NotNull Program program)
    {
        if (Renderer.program != program)
        {
            Renderer.LOGGER.trace("Binding:", program);
            
            Renderer.program = program;
            
            GL44.glUseProgram(program.id());
        }
    }
    
    public static void bind(@NotNull Framebuffer framebuffer)
    {
        if (Renderer.framebuffer != framebuffer)
        {
            Renderer.LOGGER.trace("Binding:", framebuffer);
            
            Renderer.framebuffer = framebuffer;
            
            GL44.glBindFramebuffer(GL40.GL_FRAMEBUFFER, framebuffer.id());
            GL44.glViewport(0, 0, framebuffer.width(), framebuffer.height());
        }
    }
    
    public static void activeTexture(@Range(from = 0, to = 31) int index)
    {
        if (Renderer.activeTexture != index)
        {
            Renderer.LOGGER.trace("Setting Active Texture:", index);
            
            Renderer.activeTexture = index;
            
            GL44.glActiveTexture(GL44.GL_TEXTURE0 + index);
        }
    }
    
    public static void bind(@NotNull Texture texture)
    {
        if (Renderer.textures[Renderer.activeTexture] != texture)
        {
            Renderer.LOGGER.trace("Binding: %s to index=%s", texture, Renderer.activeTexture);
            
            Renderer.textures[Renderer.activeTexture] = texture;
            
            GL44.glBindTexture(texture.type, texture.id());
        }
    }
    
    public static void bind(@NotNull Buffer buffer)
    {
        if (Renderer.buffer != buffer)
        {
            Renderer.LOGGER.trace("Binding:", buffer);
            
            Renderer.buffer = buffer;
            
            GL44.glBindBuffer(buffer.type, buffer.id());
        }
    }
    
    //public static void bind(@NotNull VertexArray vertexArray)  // TODO
    //{
    //    Renderer.LOGGER.trace("Binding", vertexArray);
    //
    //    GL44.glBindVertexArray(vertexArray.id);
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
    
    // -------------------- Attribute -------------------- //
    
    public static void attributeShort(@NotNull String name, short value)
    {
        Renderer.LOGGER.trace("attributeShort(%s, %s)", name, value);
        
        GL44.glVertexAttrib1s(Renderer.program.getUniform(name), value);
    }
    
    public static void attributeInt(@NotNull String name, int value)
    {
        Renderer.LOGGER.trace("attributeInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1i(Renderer.program.getUniform(name), value);
    }
    
    public static void attributeUInt(@NotNull String name, long value)
    {
        Renderer.LOGGER.trace("attributeUInt(%s, %s)", name, value);
        
        GL44.glVertexAttribI1ui(Renderer.program.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public static void attributeFloat(@NotNull String name, double value)
    {
        Renderer.LOGGER.trace("attributeFloat(%s, %s)", name, value);
        
        GL44.glVertexAttrib1f(Renderer.program.getUniform(name), (float) value);
    }
    
    public static void attributeShort2(@NotNull String name, short x, short y)
    {
        Renderer.LOGGER.trace("attributeShort2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2s(Renderer.program.getUniform(name), x, y);
    }
    
    public static void attributeInt2(@NotNull String name, int x, int y)
    {
        Renderer.LOGGER.trace("attributeInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2i(Renderer.program.getUniform(name), x, y);
    }
    
    public static void attributeInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeInt2(name, vec.x(), vec.y());
    }
    
    public static void attributeUInt2(@NotNull String name, long x, long y)
    {
        Renderer.LOGGER.trace("attributeUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttribI2ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        attributeUInt2(name, vec.x(), vec.y());
    }
    
    public static void attributeFloat2(@NotNull String name, double x, double y)
    {
        Renderer.LOGGER.trace("attributeFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glVertexAttrib2f(Renderer.program.getUniform(name), (float) x, (float) y);
    }
    
    public static void attributeFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        attributeFloat2(name, vec.x(), vec.y());
    }
    
    public static void attributeShort3(@NotNull String name, short x, short y, short z)
    {
        Renderer.LOGGER.trace("attributeShort3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3s(Renderer.program.getUniform(name), x, y, z);
    }
    
    public static void attributeInt3(@NotNull String name, int x, int y, int z)
    {
        Renderer.LOGGER.trace("attributeInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3i(Renderer.program.getUniform(name), x, y, z);
    }
    
    public static void attributeInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeUInt3(@NotNull String name, long x, long y, long z)
    {
        Renderer.LOGGER.trace("attributeUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttribI3ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        attributeUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeFloat3(@NotNull String name, double x, double y, double z)
    {
        Renderer.LOGGER.trace("attributeFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glVertexAttrib3f(Renderer.program.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public static void attributeFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        attributeFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void attributeShort4(@NotNull String name, short x, short y, short z, short w)
    {
        Renderer.LOGGER.trace("attributeShort4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4s(Renderer.program.getUniform(name), x, y, z, w);
    }
    
    public static void attributeInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("attributeInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4i(Renderer.program.getUniform(name), x, y, z, w);
    }
    
    public static void attributeInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Renderer.LOGGER.trace("attributeUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttribI4ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public static void attributeUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        attributeUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Renderer.LOGGER.trace("attributeFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4f(Renderer.program.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public static void attributeFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        attributeFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void attributeNormalizedUByte4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("attributeNormalizedUByte4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glVertexAttrib4Nub(Renderer.program.getUniform(name), (byte) (x & 0xFF), (byte) (y & 0xFF), (byte) (z & 0xFF), (byte) (w & 0xFF));
    }
    
    // -------------------- Uniform -------------------- //
    
    public static void uniformBool(@NotNull String name, boolean value)
    {
        Renderer.LOGGER.trace("uniformBool(%s, %s)", name, value);
        
        GL44.glUniform1i(Renderer.program.getUniform(name), value ? 1 : 0);
    }
    
    public static void uniformInt(@NotNull String name, int value)
    {
        Renderer.LOGGER.trace("uniformInt(%s, %s)", name, value);
        
        GL44.glUniform1i(Renderer.program.getUniform(name), value);
    }
    
    public static void uniformUInt(@NotNull String name, long value)
    {
        Renderer.LOGGER.trace("uniformUInt(%s, %s)", name, value);
        
        GL44.glUniform1ui(Renderer.program.getUniform(name), (int) (value & 0xFFFFFFFFL));
    }
    
    public static void uniformFloat(@NotNull String name, double value)
    {
        Renderer.LOGGER.trace("uniformFloat(%s, %s)", name, value);
        
        GL44.glUniform1f(Renderer.program.getUniform(name), (float) value);
    }
    
    public static void uniformBool2(@NotNull String name, boolean x, boolean y)
    {
        Renderer.LOGGER.trace("uniformBool2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Renderer.program.getUniform(name), x ? 1 : 0, y ? 1 : 0);
    }
    
    public static void uniformInt2(@NotNull String name, int x, int y)
    {
        Renderer.LOGGER.trace("uniformInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2i(Renderer.program.getUniform(name), x, y);
    }
    
    public static void uniformInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformUInt2(@NotNull String name, long x, long y)
    {
        Renderer.LOGGER.trace("uniformUInt2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt2(@NotNull String name, @NotNull Vector2ic vec)
    {
        uniformUInt2(name, vec.x(), vec.y());
    }
    
    public static void uniformFloat2(@NotNull String name, double x, double y)
    {
        Renderer.LOGGER.trace("uniformFloat2(%s, %s, %s)", name, x, y);
        
        GL44.glUniform2f(Renderer.program.getUniform(name), (float) x, (float) y);
    }
    
    public static void uniformFloat2(@NotNull String name, @NotNull Vector2dc vec)
    {
        uniformFloat2(name, vec.x(), vec.y());
    }
    
    public static void uniformBool3(@NotNull String name, boolean x, boolean y, boolean z)
    {
        Renderer.LOGGER.trace("uniformBool3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Renderer.program.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
    }
    
    public static void uniformInt3(@NotNull String name, int x, int y, int z)
    {
        Renderer.LOGGER.trace("uniformInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3i(Renderer.program.getUniform(name), x, y, z);
    }
    
    public static void uniformInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformUInt3(@NotNull String name, long x, long y, long z)
    {
        Renderer.LOGGER.trace("uniformUInt3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt3(@NotNull String name, @NotNull Vector3ic vec)
    {
        uniformUInt3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformFloat3(@NotNull String name, double x, double y, double z)
    {
        Renderer.LOGGER.trace("uniformFloat3(%s, %s, %s, %s)", name, x, y, z);
        
        GL44.glUniform3f(Renderer.program.getUniform(name), (float) x, (float) y, (float) z);
    }
    
    public static void uniformFloat3(@NotNull String name, @NotNull Vector3dc vec)
    {
        uniformFloat3(name, vec.x(), vec.y(), vec.z());
    }
    
    public static void uniformBool4(@NotNull String name, boolean x, boolean y, boolean z, boolean w)
    {
        Renderer.LOGGER.trace("uniformBool3(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Renderer.program.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
    }
    
    public static void uniformInt4(@NotNull String name, int x, int y, int z, int w)
    {
        Renderer.LOGGER.trace("uniformInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4i(Renderer.program.getUniform(name), x, y, z, w);
    }
    
    public static void uniformInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformUInt4(@NotNull String name, long x, long y, long z, long w)
    {
        Renderer.LOGGER.trace("uniformUInt4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4ui(Renderer.program.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
    }
    
    public static void uniformUInt4(@NotNull String name, @NotNull Vector4ic vec)
    {
        uniformUInt4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformFloat4(@NotNull String name, double x, double y, double z, double w)
    {
        Renderer.LOGGER.trace("uniformFloat4(%s, %s, %s, %s, %s)", name, x, y, z, w);
        
        GL44.glUniform4f(Renderer.program.getUniform(name), (float) x, (float) y, (float) z, (float) w);
    }
    
    public static void uniformFloat4(@NotNull String name, @NotNull Vector4dc vec)
    {
        uniformFloat4(name, vec.x(), vec.y(), vec.z(), vec.w());
    }
    
    public static void uniformMatrix2(@NotNull String name, boolean transpose, @NotNull Matrix2dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix2(%s, %s, %s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.floats((float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11());
            GL44.glUniformMatrix2fv(Renderer.program.getUniform(name), transpose, buffer);
        }
    }
    
    public static void uniformMatrix3(@NotNull String name, boolean transpose, @NotNull Matrix3dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix3(%s, %s, %s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix3fv(Renderer.program.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
        }
    }
    
    public static void uniformMatrix4(@NotNull String name, boolean transpose, @NotNull Matrix4dc mat)
    {
        Renderer.LOGGER.trace("uniformMatrix4(%s, %s, %s)", name, transpose, mat);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            GL44.glUniformMatrix4fv(Renderer.program.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
        }
    }
    
    public static void uniformColor(@NotNull String name, @NotNull Colorc color)
    {
        Renderer.LOGGER.trace("uniformColor(%s, %s", name, color);
        
        GL44.glUniform4f(Renderer.program.getUniform(name), color.rf(), color.gf(), color.bf(), color.af());
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
    
    private Renderer() {}
}