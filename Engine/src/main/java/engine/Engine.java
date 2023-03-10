package engine;

import engine.gl.vertex.DrawMode;
import engine.util.Logger;
import engine.util.ThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import static engine.Renderer.*;
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
        
        Renderer.setup();
        
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
        
        Renderer.destroy();
        
        IO.destroy();
    }
    
    private static void renderThread()
    {
        long currentTime = Engine.nanoseconds();
        
        Engine.updateTimeLast = currentTime;
        Engine.drawTimeLast   = currentTime;
        
        try
        {
            IO.windowMakeCurrent();
            
            while (Engine.shouldRenderThreadRun)
            {
                currentTime            = Engine.nanoseconds();
                Engine.updateTimeDelta = currentTime - Engine.updateTimeLast;
                if (Engine.updateTimeDelta >= Engine.updateFreqInv)
                {
                    Engine.updateTimeLast = currentTime;
                    
                    updateRenderThread(Engine.updateFrame++, currentTime, Engine.updateTimeDelta);
                }
                
                currentTime          = Engine.nanoseconds();
                Engine.drawTimeDelta = currentTime - Engine.drawTimeLast;
                if (Engine.drawTimeDelta >= Engine.drawFreqInv)
                {
                    Engine.drawTimeLast = currentTime;
                    
                    drawRenderThread(Engine.drawFrame++, currentTime, Engine.drawTimeDelta);
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
        IO.update(time, deltaTime);
        
        double timeD      = time / 1_000_000_000D;
        double deltaTimeD = deltaTime / 1_000_000_000D;
        
        Engine.instance.update(frame, timeD, deltaTimeD);
        
        if (IO.windowOnClose().fired() && !IO.windowOnClose().consumed()) stop();
    }
    
    private static void drawRenderThread(int frame, long time, long deltaTime)
    {
        double timeD      = time / 1_000_000_000D;
        double deltaTimeD = deltaTime / 1_000_000_000D;
        
        bind(Renderer.defaultProgram);
        bind(Renderer.defaultFramebuffer);
    
        stateReset();
        stateDefault();
    
        //int r = Renderer.defaultFramebuffer.width() >> 1;
        //int l = -r;
        //int b = Renderer.defaultFramebuffer.height() >> 1;
        //int t = -b;
        //
        //stateProjection().setOrtho(l, r, b, t, 1.0, -1.0);
        //stateView().translate(l, t, 0.0);
        
        drawVertexBegin(DrawMode.TRIANGLES);
        
        drawVertexPos(-1.0, 1.0);
        drawVertexColor(255, 0, 0, 255);
    
        drawVertexPos(-1.0, 1.0);
        drawVertexColor(0, 0, 255, 255);
        
        //drawVertexPos(1.0, 1.0);
        //drawVertexColor(0, 0, 0, 0);
    
        drawVertexPos(1.0, -1.0);
        drawVertexColor(0, 255, 0, 255);
        
        drawVertexEnd();
        
        Engine.instance.draw(frame, timeD, deltaTimeD);
        
        drawVertices();
        
        IO.windowSwap();
    }
    
    // -------------------- Time -------------------- //
    
    private static long start;
    
    private static int  updateFrame     = 0;
    private static int  updateFreq      = 0;
    private static long updateFreqInv   = 0L;
    private static long updateTimeDelta = 0L;
    private static long updateTimeLast  = 0L;
    
    private static int  drawFrame     = 0;
    private static int  drawFreq      = 0;
    private static long drawFreqInv   = 0L;
    private static long drawTimeDelta = 0L;
    private static long drawTimeLast  = 0L;
    
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
    
    public static int updateFrequencyTarget()
    {
        return Engine.updateFreq;
    }
    
    public static void updateFrequencyTarget(int frequency)
    {
        Engine.updateFreq    = Math.max(0, frequency);
        Engine.updateFreqInv = frequency > 0 ? 1_000_000_000L / (long) frequency : 0L;
    }
    
    public static int drawFrequencyTarget()
    {
        return Engine.drawFreq;
    }
    
    public static void drawFrequencyTarget(int frequency)
    {
        Engine.drawFreq    = Math.max(0, frequency);
        Engine.drawFreqInv = frequency > 0 ? 1_000_000_000L / (long) frequency : 0L;
    }
    
    // -------------------- Instance -------------------- //
    
    public final String    name;
    public final Vector2ic size;
    
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
