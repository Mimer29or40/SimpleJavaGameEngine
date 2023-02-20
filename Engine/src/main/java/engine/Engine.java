package engine;

import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.buffer.Buffer;
import engine.gl.buffer.BufferArray;
import engine.gl.shader.Program;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.vertex.VertexArray;
import engine.util.Logger;
import engine.util.ThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwWaitEvents;

public abstract class Engine
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Engine -------------------- //
    
    private static Engine instance;
    
    private static final Thread mainThread   = Thread.currentThread();
    private static final Thread renderThread = new Thread(Engine::renderThread, "render");
    
    private static boolean shouldMainThreadRun   = false;
    private static boolean shouldRenderThreadRun = false;
    
    public static final ThreadExecutor executor = new ThreadExecutor(Engine.mainThread);
    
    protected static void start(@NotNull Engine instance)
    {
        try
        {
            if (Engine.instance != null) throw new IllegalStateException("Engine was already started.");
            Engine.instance = instance;
            
            Engine.LOGGER.info("Starting");
            
            setupEngine();
            
            runEngine();
        }
        catch (Throwable e)
        {
            Engine.LOGGER.severe(e);
        }
        finally
        {
            destroyEngine();
            
            Engine.LOGGER.info("Finished");
            
            Engine.instance = null;
        }
    }
    
    public static void stop()
    {
        Engine.shouldRenderThreadRun = false;
    }
    
    private static void setupEngine()
    {
        Engine.shouldMainThreadRun   = true;
        Engine.shouldRenderThreadRun = true;
        
        Engine.executor.onTaskReceived = GLFW::glfwPostEmptyEvent;
        
        Engine.start = System.nanoTime();
        
        IO.setup(Engine.instance.size, "Engine - " + Engine.instance.name);
        
        setupRenderers();
        
        Engine.LOGGER.debug("Instance Setup");
        Engine.instance.setup();
        
        IO.windowUnmakeCurrent();
    }
    
    private static void runEngine() throws InterruptedException
    {
        Engine.renderThread.start();
        
        while (Engine.shouldMainThreadRun)
        {
            glfwWaitEvents();
            
            Engine.executor.processQueue();
            
            //Thread.yield();
        }
        
        //noinspection DataFlowIssue
        Engine.shouldMainThreadRun   = false;
        Engine.shouldRenderThreadRun = false;
        
        Engine.renderThread.join();
    }
    
    private static void destroyEngine()
    {
        IO.windowMakeCurrent();
        
        Engine.LOGGER.debug("Instance Destroy");
        Engine.instance.destroy();
        
        destroyRenderers();
        
        IO.destroy();
    }
    
    private static void setupRenderers()
    {
        GL.defaultState();
        
        Framebuffer.bind(Framebuffer.NULL);
        Program.bind(Program.NULL);
        VertexArray.bind(VertexArray.NULL);
        Buffer.bind(BufferArray.NULL);
        Texture.bind(Texture2D.NULL);
        
        Renderer.setup();
    }
    
    private static void destroyRenderers()
    {
        Renderer.destroy();
    }
    
    private static void renderThread()
    {
        long currentTime = Engine.nanoseconds();
        
        int  updateFrame    = 0;
        int  updateFreq     = -1;
        long updateFreqInv  = 0L;
        long updateTimeLast = currentTime;
        long updateTimeDelta;
        
        int  drawFrame    = 0;
        int  drawFreq     = -1;
        long drawFreqInv  = 0L;
        long drawTimeLast = currentTime;
        long drawTimeDelta;
        
        try
        {
            IO.windowMakeCurrent();
            
            while (Engine.shouldRenderThreadRun)
            {
                if (updateFreq != Engine.instance.updateFreq)
                {
                    updateFreq    = Math.max(0, Engine.instance.updateFreq);
                    updateFreqInv = updateFreq > 0 ? 1_000_000_000L / (long) updateFreq : 0L;
                    
                    Engine.instance.updateFreq = updateFreq;
                }
                
                currentTime     = Engine.nanoseconds();
                updateTimeDelta = currentTime - updateTimeLast;
                if (updateTimeDelta >= updateFreqInv)
                {
                    updateTimeLast = currentTime;
                    
                    updateRenderThread(updateFrame++, currentTime, updateTimeDelta);
                }
                
                if (drawFreq != Engine.instance.drawFreq)
                {
                    drawFreq    = Math.max(0, Engine.instance.drawFreq);
                    drawFreqInv = drawFreq > 0 ? 1_000_000_000L / (long) drawFreq : 0L;
                    
                    Engine.instance.drawFreq = drawFreq;
                }
                
                currentTime   = Engine.nanoseconds();
                drawTimeDelta = currentTime - drawTimeLast;
                if (drawTimeDelta >= drawFreqInv)
                {
                    drawTimeLast = currentTime;
                    
                    drawRenderThread(drawFrame++, currentTime, drawTimeDelta);
                }
                
                Thread.yield();
            }
        }
        catch (Throwable e)
        {
            Engine.LOGGER.severe(e);
        }
        finally
        {
            Engine.shouldMainThreadRun = false;
            
            IO.windowUnmakeCurrent();
        }
    }
    
    private static void updateRenderThread(int frame, long time, long deltaTime)
    {
        IO.update(time);
        
        double timeD      = time / 1_000_000_000D;
        double deltaTimeD = deltaTime / 1_000_000_000D;
        
        Engine.instance.update(frame, timeD, deltaTimeD);
        
        if (IO.windowOnClose().fired() && !IO.windowOnClose().consumed()) stop();
    }
    
    private static void drawRenderThread(int frame, long time, long deltaTime)
    {
        double timeD      = time / 1_000_000_000D;
        double deltaTimeD = deltaTime / 1_000_000_000D;
        
        Renderer.beforeDraw();
        
        Engine.instance.draw(frame, timeD, deltaTimeD);
        
        Renderer.afterDraw();
        
        IO.windowSwap();
    }
    
    // -------------------- Time -------------------- //
    
    private static long start;
    
    public static double seconds()
    {
        return (double) nanoseconds() / 1_000_000_000D;
    }
    
    public static double milliseconds()
    {
        return (double) nanoseconds() / 1_000_000D;
    }
    
    public static double microseconds()
    {
        return (double) nanoseconds() / 1_000D;
    }
    
    public static long nanoseconds()
    {
        return Engine.start > 0 ? System.nanoTime() - Engine.start : 0L;
    }
    
    // -------------------- Instance -------------------- //
    
    public final String    name;
    public final Vector2ic size;
    
    protected int updateFreq = 0;
    protected int drawFreq   = 60;
    
    protected Engine(String name, int width, int height)
    {
        this.name = name;
        this.size = new Vector2i(width, height);
    }
    
    protected Engine(int width, int height)
    {
        String className = getClass().getSimpleName();
        
        StringBuilder name = new StringBuilder();
        for (int i = 0, n = className.length(); i < n; i++)
        {
            char ch = className.charAt(i);
            if (i > 0 && (Character.isDigit(ch) || Character.isUpperCase(ch))) name.append(' ');
            name.append(ch == '_' ? " - " : ch);
        }
        this.name = name.toString();
        this.size = new Vector2i(width, height);
    }
    
    @Override
    public String toString()
    {
        return "Engine{" + "name='" + this.name + '\'' + '}';
    }
    
    protected abstract void setup();
    
    protected abstract void update(int frame, double time, double deltaTime);
    
    protected abstract void draw(int frame, double time, double deltaTime);
    
    protected abstract void destroy();
}
