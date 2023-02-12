package engine;

import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.glfw.GLFW.*;

public class IO
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- State -------------------- //
    
    static long _HOLD_FREQUENCY     = 1_000_000L;
    static long _DOUBLE_PRESS_DELAY = 200_000_000L;
    
    static void setup(@NotNull Vector2ic size, @NotNull String title)
    {
        IO.LOGGER.debug("Setup");
        
        List<Callback> callbacks = new ArrayList<>();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            IntBuffer rev   = stack.mallocInt(1);
            
            glfwGetVersion(major, minor, rev);
            
            IO.LOGGER.debug("GLFW Version: %s.%s.%s", major.get(), minor.get(), rev.get());
        }
        
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        
        callbacks.add(glfwSetErrorCallback(IO::errorCallback));
        
        // ---------- Window ---------- //
        setupWindow(size, title, callbacks);
        
        // ---------- Mouse ---------- //
        setupMouse(callbacks);
        
        // ---------- Keyboard ---------- //
        setupKeyboard(callbacks);
        
        // ---------- Modifier ---------- //
        setupModifier();
        
        for (Callback callback : callbacks) if (callback != null) callback.free();
        
        IO.windowMakeCurrent();
    }
    
    static void update(long time)
    {
        // ---------- Window ---------- //
        updateWindow();
        
        // ---------- Mouse ---------- //
        updateMouse(time);
        
        // ---------- Keyboard ---------- //
        updateKeyboard(time);
    }
    
    static void destroy()
    {
        IO.LOGGER.debug("Destroy");
        
        List<Callback> callbacks = new ArrayList<>();
        
        // ---------- Keyboard ---------- //
        destroyKeyboard(callbacks);
        
        // ---------- Mouse ---------- //
        destroyMouse(callbacks);
        
        // ---------- Window ---------- //
        destroyWindow(callbacks);
        
        callbacks.add(glfwSetErrorCallback(null));
        
        for (Callback callback : callbacks) if (callback != null) callback.free();
        
        org.lwjgl.opengl.GL.destroy();
        glfwTerminate();
    }
    
    // -------------------- Error Handling -------------------- //
    
    static final Map<Integer, String> ERROR_CODES = APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, org.lwjgl.glfw.GLFW.class);
    
    private static void errorCallback(int error, long description)
    {
        StringBuilder message = new StringBuilder();
        message.append("[LWJGL] ").append(IO.ERROR_CODES.get(error)).append(" error\n");
        message.append("\tDescription : ").append(MemoryUtil.memUTF8(description)).append('\n');
        message.append("\tStacktrace  :\n");
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 4; i < stack.length; i++) message.append("\t\t").append(stack[i].toString()).append('\n');
        IO.LOGGER.severe(message.toString());
    }
    
    // -------------------- Window State -------------------- //
    
    static long WINDOW_HANDLE = MemoryUtil.NULL;
    
    static       boolean WINDOW_CLOSE_REQUESTED = false;
    static final Event   WINDOW_ON_CLOSE        = new Event();
    
    static           boolean WINDOW_FOCUSED;
    static @Nullable Boolean WINDOW_FOCUSED_CHANGE = null;
    static final     Event   WINDOW_ON_FOCUSED     = new Event();
    
    static           boolean WINDOW_MINIMIZED;
    static @Nullable Boolean WINDOW_MINIMIZED_CHANGE = null;
    static final     Event   WINDOW_ON_MINIMIZED     = new Event();
    
    static           boolean WINDOW_MAXIMIZED;
    static @Nullable Boolean WINDOW_MAXIMIZED_CHANGE = null;
    static final     Event   WINDOW_ON_MAXIMIZED     = new Event();
    
    static final     Vector2i WINDOW_POS        = new Vector2i();
    static @Nullable Vector2i WINDOW_POS_CHANGE = null;
    static final     Event    WINDOW_ON_POS     = new Event();
    
    static final     Vector2i WINDOW_SIZE        = new Vector2i();
    static @Nullable Vector2i WINDOW_SIZE_CHANGE = null;
    static final     Event    WINDOW_ON_SIZE     = new Event();
    
    static final     Vector2d WINDOW_CONTENT_SCALE        = new Vector2d();
    static @Nullable Vector2d WINDOW_CONTENT_SCALE_CHANGE = null;
    static final     Event    WINDOW_ON_CONTENT_SCALE     = new Event();
    
    static final     Vector2i WINDOW_FRAMEBUFFER_SIZE        = new Vector2i();
    static @Nullable Vector2i WINDOW_FRAMEBUFFER_SIZE_CHANGE = null;
    static final     Event    WINDOW_ON_FRAMEBUFFER_SIZE     = new Event();
    
    static       boolean WINDOW_REFRESH_REQUESTED = false;
    static final Event   WINDOW_ON_REFRESH        = new Event();
    
    static final     List<@NotNull String> WINDOW_DROPPED        = new LinkedList<>();
    static @Nullable PointerBuffer         WINDOW_DROPPED_CHANGE = null;
    static final     Event                 WINDOW_ON_DROPPED     = new Event();
    
    private static void setupWindow(@NotNull Vector2ic size, @NotNull String title, @NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Setup Window");
        
        glfwDefaultWindowHints();
        
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);
        
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        
        IO.WINDOW_HANDLE = glfwCreateWindow(size.x(), size.y(), title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (IO.WINDOW_HANDLE == MemoryUtil.NULL) throw new RuntimeException("Could not create window.");
        
        IO.WINDOW_FOCUSED   = glfwGetWindowAttrib(IO.WINDOW_HANDLE, GLFW_FOCUSED) == GLFW_TRUE;
        IO.WINDOW_MINIMIZED = glfwGetWindowAttrib(IO.WINDOW_HANDLE, GLFW_ICONIFIED) == GLFW_TRUE;
        IO.WINDOW_MAXIMIZED = glfwGetWindowAttrib(IO.WINDOW_HANDLE, GLFW_MAXIMIZED) == GLFW_TRUE;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            
            FloatBuffer xf = stack.mallocFloat(1);
            FloatBuffer yf = stack.mallocFloat(1);
            
            glfwGetWindowPos(IO.WINDOW_HANDLE, x, y);
            IO.WINDOW_POS.set(x.get(0), y.get(0));
            
            glfwGetWindowSize(IO.WINDOW_HANDLE, x, y);
            IO.WINDOW_SIZE.set(x.get(0), y.get(0));
            
            glfwGetWindowContentScale(IO.WINDOW_HANDLE, xf, yf);
            IO.WINDOW_CONTENT_SCALE.set(xf.get(0), yf.get(0));
            
            glfwGetFramebufferSize(IO.WINDOW_HANDLE, x, y);
            IO.WINDOW_FRAMEBUFFER_SIZE.set(x.get(0), y.get(0));
        }
        
        callbacks.add(glfwSetWindowCloseCallback(IO.WINDOW_HANDLE, IO::windowCloseCallback));
        callbacks.add(glfwSetWindowFocusCallback(IO.WINDOW_HANDLE, IO::windowFocusCallback));
        callbacks.add(glfwSetWindowIconifyCallback(IO.WINDOW_HANDLE, IO::windowIconifyCallback));
        callbacks.add(glfwSetWindowMaximizeCallback(IO.WINDOW_HANDLE, IO::windowMaximizeCallback));
        callbacks.add(glfwSetWindowPosCallback(IO.WINDOW_HANDLE, IO::windowPosCallback));
        callbacks.add(glfwSetWindowSizeCallback(IO.WINDOW_HANDLE, IO::windowSizeCallback));
        callbacks.add(glfwSetWindowContentScaleCallback(IO.WINDOW_HANDLE, IO::windowContentScaleCallback));
        callbacks.add(glfwSetFramebufferSizeCallback(IO.WINDOW_HANDLE, IO::windowFramebufferSizeCallback));
        callbacks.add(glfwSetWindowRefreshCallback(IO.WINDOW_HANDLE, IO::windowRefreshCallback));
        callbacks.add(glfwSetDropCallback(IO.WINDOW_HANDLE, IO::windowDropCallback));
    }
    
    private static void updateWindow()
    {
        IO.WINDOW_ON_CLOSE.fired = false;
        if (IO.WINDOW_CLOSE_REQUESTED)
        {
            IO.WINDOW_ON_CLOSE.fired = true;
            
            IO.WINDOW_CLOSE_REQUESTED = false;
        }
        
        IO.WINDOW_ON_FOCUSED.fired = false;
        if (IO.WINDOW_FOCUSED_CHANGE != null)
        {
            IO.WINDOW_FOCUSED          = IO.WINDOW_FOCUSED_CHANGE;
            IO.WINDOW_ON_FOCUSED.fired = true;
            
            IO.WINDOW_FOCUSED_CHANGE = null;
        }
        
        IO.WINDOW_ON_MINIMIZED.fired = false;
        if (IO.WINDOW_MINIMIZED_CHANGE != null)
        {
            IO.WINDOW_MINIMIZED          = IO.WINDOW_MINIMIZED_CHANGE;
            IO.WINDOW_ON_MINIMIZED.fired = true;
            
            IO.WINDOW_MINIMIZED_CHANGE = null;
        }
        
        IO.WINDOW_ON_MAXIMIZED.fired = false;
        if (IO.WINDOW_MAXIMIZED_CHANGE != null)
        {
            IO.WINDOW_MAXIMIZED          = IO.WINDOW_MAXIMIZED_CHANGE;
            IO.WINDOW_ON_MAXIMIZED.fired = true;
            
            IO.WINDOW_MAXIMIZED_CHANGE = null;
        }
        
        IO.WINDOW_ON_POS.fired = false;
        if (IO.WINDOW_POS_CHANGE != null)
        {
            IO.WINDOW_POS.set(IO.WINDOW_POS_CHANGE);
            IO.WINDOW_ON_POS.fired = true;
            
            IO.WINDOW_POS_CHANGE = null;
        }
        
        IO.WINDOW_ON_SIZE.fired = false;
        if (IO.WINDOW_SIZE_CHANGE != null)
        {
            IO.WINDOW_SIZE.set(IO.WINDOW_SIZE_CHANGE);
            IO.WINDOW_ON_SIZE.fired = true;
            
            IO.WINDOW_SIZE_CHANGE = null;
        }
        
        IO.WINDOW_ON_CONTENT_SCALE.fired = false;
        if (IO.WINDOW_CONTENT_SCALE_CHANGE != null)
        {
            IO.WINDOW_CONTENT_SCALE.set(IO.WINDOW_CONTENT_SCALE_CHANGE);
            IO.WINDOW_ON_CONTENT_SCALE.fired = true;
            
            IO.WINDOW_CONTENT_SCALE_CHANGE = null;
        }
        
        IO.WINDOW_ON_FRAMEBUFFER_SIZE.fired = false;
        if (IO.WINDOW_FRAMEBUFFER_SIZE_CHANGE != null)
        {
            IO.WINDOW_FRAMEBUFFER_SIZE.set(IO.WINDOW_FRAMEBUFFER_SIZE_CHANGE);
            IO.WINDOW_ON_FRAMEBUFFER_SIZE.fired = true;
            
            IO.WINDOW_FRAMEBUFFER_SIZE_CHANGE = null;
        }
        
        IO.WINDOW_ON_REFRESH.fired = false;
        if (IO.WINDOW_REFRESH_REQUESTED)
        {
            IO.WINDOW_ON_REFRESH.fired = true;
            
            IO.WINDOW_REFRESH_REQUESTED = false;
        }
        
        IO.WINDOW_ON_DROPPED.fired = false;
        if (IO.WINDOW_DROPPED_CHANGE != null)
        {
            IO.WINDOW_DROPPED.clear();
            while (IO.WINDOW_DROPPED_CHANGE.hasRemaining()) IO.WINDOW_DROPPED.add(MemoryUtil.memUTF8(IO.WINDOW_DROPPED_CHANGE.get()));
            IO.WINDOW_ON_DROPPED.fired = true;
            
            IO.WINDOW_DROPPED_CHANGE = null;
        }
    }
    
    private static void destroyWindow(@NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Destroy Window");
        
        callbacks.add(glfwSetWindowCloseCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowFocusCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowIconifyCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowMaximizeCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowPosCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowSizeCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowContentScaleCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetFramebufferSizeCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetWindowRefreshCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetDropCallback(IO.WINDOW_HANDLE, null));
        
        glfwDestroyWindow(IO.WINDOW_HANDLE);
        IO.WINDOW_HANDLE = MemoryUtil.NULL;
    }
    
    public static @NotNull Event windowOnClose()
    {
        return IO.WINDOW_ON_CLOSE;
    }
    
    public static boolean windowFocused()
    {
        return IO.WINDOW_FOCUSED;
    }
    
    public static @NotNull Event windowOnFocused()
    {
        return IO.WINDOW_ON_FOCUSED;
    }
    
    public static boolean windowMinimized()
    {
        return IO.WINDOW_MINIMIZED;
    }
    
    public static @NotNull Event windowOnMinimized()
    {
        return IO.WINDOW_ON_MINIMIZED;
    }
    
    public static boolean windowMaximized()
    {
        return IO.WINDOW_MAXIMIZED;
    }
    
    public static @NotNull Event windowOnMaximized()
    {
        return IO.WINDOW_ON_MAXIMIZED;
    }
    
    public static @NotNull @UnmodifiableView Vector2ic windowPos()
    {
        return IO.WINDOW_POS;
    }
    
    public static @NotNull Event windowOnPosChange()
    {
        return IO.WINDOW_ON_POS;
    }
    
    public static @NotNull @UnmodifiableView Vector2ic windowSize()
    {
        return IO.WINDOW_SIZE;
    }
    
    public static @NotNull Event windowOnSizeChange()
    {
        return IO.WINDOW_ON_SIZE;
    }
    
    public static @NotNull @UnmodifiableView Vector2dc windowContentScale()
    {
        return IO.WINDOW_CONTENT_SCALE;
    }
    
    public static @NotNull Event windowOnContentScaleChange()
    {
        return IO.WINDOW_ON_CONTENT_SCALE;
    }
    
    public static @NotNull @UnmodifiableView Vector2ic windowFramebufferSize()
    {
        return IO.WINDOW_FRAMEBUFFER_SIZE;
    }
    
    public static @NotNull Event windowOnFramebufferSizeChange()
    {
        return IO.WINDOW_ON_FRAMEBUFFER_SIZE;
    }
    
    public static @NotNull Event windowOnRefresh()
    {
        return IO.WINDOW_ON_REFRESH;
    }
    
    public static @NotNull @UnmodifiableView List<String> windowDropped()
    {
        return Collections.unmodifiableList(IO.WINDOW_DROPPED);
    }
    
    public static @NotNull Event windowOnDropped()
    {
        return IO.WINDOW_ON_DROPPED;
    }
    
    // -------------------- Window Functions -------------------- //
    
    public static void windowMakeCurrent()
    {
        IO.LOGGER.debug("Making Window Context Current in", Thread.currentThread());
        
        glfwMakeContextCurrent(IO.WINDOW_HANDLE);
        org.lwjgl.opengl.GL.createCapabilities();
    }
    
    public static void windowUnmakeCurrent()
    {
        IO.LOGGER.debug("Removing Window Context in", Thread.currentThread());
        
        org.lwjgl.opengl.GL.setCapabilities(null);
        glfwMakeContextCurrent(MemoryUtil.NULL);
    }
    
    public static void windowSwap()
    {
        glfwSwapBuffers(IO.WINDOW_HANDLE);
    }
    
    public static void windowTitle(@NotNull String title)
    {
        IO.LOGGER.trace("Setting Window Title: '%s'", title);
        
        Engine.executor.submit(() -> glfwSetWindowTitle(IO.WINDOW_HANDLE, title));
    }
    
    public static @Nullable String clipboard()
    {
        try
        {
            return Engine.executor.submit(() -> glfwGetClipboardString(IO.WINDOW_HANDLE)).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Unable to get Window Clipboard");
            return null;
        }
    }
    
    public static void clipboard(@NotNull ByteBuffer string)
    {
        IO.LOGGER.trace("Setting Window Clipboard: '%s'", string);
        
        Engine.executor.submit(() -> glfwSetClipboardString(IO.WINDOW_HANDLE, string));
    }
    
    public static void clipboard(@NotNull CharSequence string)
    {
        IO.LOGGER.trace("Setting Window Clipboard: '%s'", string);
        
        Engine.executor.submit(() -> glfwSetClipboardString(IO.WINDOW_HANDLE, string));
    }
    
    // -------------------- Window Callbacks -------------------- //
    
    private static void windowCloseCallback(long handle)
    {
        IO.WINDOW_CLOSE_REQUESTED = true;
    }
    
    private static void windowFocusCallback(long handle, boolean focused)
    {
        IO.WINDOW_FOCUSED_CHANGE = focused;
    }
    
    private static void windowIconifyCallback(long handle, boolean iconified)
    {
        IO.WINDOW_MINIMIZED_CHANGE = iconified;
    }
    
    private static void windowMaximizeCallback(long handle, boolean maximized)
    {
        IO.WINDOW_MAXIMIZED_CHANGE = maximized;
    }
    
    private static void windowPosCallback(long handle, int x, int y)
    {
        IO.WINDOW_POS_CHANGE = new Vector2i(x, y);
    }
    
    private static void windowSizeCallback(long handle, int width, int height)
    {
        IO.WINDOW_SIZE_CHANGE = new Vector2i(width, height);
    }
    
    private static void windowContentScaleCallback(long handle, float xScale, float yScale)
    {
        IO.WINDOW_CONTENT_SCALE_CHANGE = new Vector2d(xScale, yScale);
    }
    
    private static void windowFramebufferSizeCallback(long handle, int width, int height)
    {
        IO.WINDOW_FRAMEBUFFER_SIZE_CHANGE = new Vector2i(width, height);
    }
    
    private static void windowRefreshCallback(long handle)
    {
        IO.WINDOW_REFRESH_REQUESTED = true;
    }
    
    private static void windowDropCallback(long handle, int count, long names)
    {
        IO.WINDOW_DROPPED_CHANGE = MemoryUtil.memPointerBuffer(names, count);
    }
    
    // -------------------- Mouse State -------------------- //
    
    static           boolean MOUSE_ENTERED        = false;
    static @Nullable Boolean MOUSE_ENTERED_CHANGE = null;
    static final     Event   MOUSE_ON_ENTERED     = new Event();
    
    static final     Vector2d  MOUSE_POS        = new Vector2d();
    static @Nullable Vector2dc MOUSE_POS_CHANGE = null;
    static final     Event     MOUSE_ON_POS     = new Event();
    
    static final Vector2d MOUSE_POS_DELTA = new Vector2d();
    
    static final     Vector2d  MOUSE_SCROLL        = new Vector2d();
    static @Nullable Vector2dc MOUSE_SCROLL_CHANGE = null;
    static final     Event     MOUSE_ON_SCROLL     = new Event();
    
    static final Map<Integer, Button>     MOUSE_BUTTON_MAP    = new HashMap<>();
    static final Map<Button, ButtonInput> MOUSE_BUTTON_STATES = new EnumMap<>(Button.class);
    
    static final EnumSet<Button> MOUSE_BUTTON_DOWN    = EnumSet.noneOf(Button.class);
    static final Event           MOUSE_ON_BUTTON_DOWN = new Event();
    
    static final EnumSet<Button> MOUSE_BUTTON_UP    = EnumSet.noneOf(Button.class);
    static final Event           MOUSE_ON_BUTTON_UP = new Event();
    
    static final EnumSet<Button> MOUSE_BUTTON_REPEATED    = EnumSet.noneOf(Button.class);
    static final Event           MOUSE_ON_BUTTON_REPEATED = new Event();
    
    static final EnumSet<Button> MOUSE_BUTTON_HELD    = EnumSet.noneOf(Button.class);
    static final Event           MOUSE_ON_BUTTON_HELD = new Event();
    
    static final EnumSet<Button> MOUSE_BUTTON_DRAGGED    = EnumSet.noneOf(Button.class);
    static final Event           MOUSE_ON_BUTTON_DRAGGED = new Event();
    
    private static void setupMouse(@NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Setup Mouse");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);
            
            glfwGetCursorPos(IO.WINDOW_HANDLE, x, y);
            IO.MOUSE_POS.set(x.get(0), y.get(0));
        }
        
        for (Button button : Button.values())
        {
            IO.MOUSE_BUTTON_MAP.put(button.ref, button);
            IO.MOUSE_BUTTON_STATES.put(button, new ButtonInput());
        }
        
        mouseShow();
        mouseRawInput(true);
        mouseSticky(false);
        
        callbacks.add(glfwSetCursorEnterCallback(IO.WINDOW_HANDLE, IO::mouseEnteredCallback));
        callbacks.add(glfwSetCursorPosCallback(IO.WINDOW_HANDLE, IO::mousePosCallback));
        callbacks.add(glfwSetScrollCallback(IO.WINDOW_HANDLE, IO::mouseScrollCallback));
        callbacks.add(glfwSetMouseButtonCallback(IO.WINDOW_HANDLE, IO::mouseButtonCallback));
    }
    
    private static void updateMouse(long time)
    {
        IO.MOUSE_ON_ENTERED.fired = false;
        if (IO.MOUSE_ENTERED_CHANGE != null)
        {
            IO.MOUSE_ENTERED          = IO.MOUSE_ENTERED_CHANGE;
            IO.MOUSE_ON_ENTERED.fired = true;
            
            if (IO.MOUSE_ENTERED) IO.MOUSE_POS_CHANGE = new Vector2d(IO.MOUSE_POS.x, IO.MOUSE_POS.y);
            
            IO.MOUSE_ENTERED_CHANGE = null;
        }
        
        IO.MOUSE_ON_POS.fired = false;
        IO.MOUSE_POS_DELTA.set(0);
        if (IO.MOUSE_POS_CHANGE != null)
        {
            IO.MOUSE_POS_DELTA.set(IO.MOUSE_POS_CHANGE.x(), IO.MOUSE_POS_CHANGE.y()).sub(IO.MOUSE_POS);
            IO.MOUSE_POS.set(IO.MOUSE_POS_CHANGE.x(), IO.MOUSE_POS_CHANGE.y());
            IO.MOUSE_ON_POS.fired = true;
            
            IO.MOUSE_POS_CHANGE = null;
        }
        
        IO.MOUSE_ON_SCROLL.fired = false;
        IO.MOUSE_SCROLL.set(0);
        if (IO.MOUSE_SCROLL_CHANGE != null)
        {
            System.out.println("Scroll");
            IO.MOUSE_SCROLL.set(IO.MOUSE_SCROLL_CHANGE.x(), IO.MOUSE_SCROLL_CHANGE.y());
            IO.MOUSE_ON_SCROLL.fired = true;
            
            IO.MOUSE_SCROLL_CHANGE = null;
        }
        
        IO.MOUSE_BUTTON_DOWN.clear();
        IO.MOUSE_BUTTON_UP.clear();
        IO.MOUSE_BUTTON_REPEATED.clear();
        IO.MOUSE_BUTTON_HELD.clear();
        IO.MOUSE_BUTTON_DRAGGED.clear();
        for (Button button : IO.MOUSE_BUTTON_STATES.keySet())
        {
            ButtonInput input = IO.MOUSE_BUTTON_STATES.get(button);
            
            input.state       = input.stateChange;
            input.stateChange = -1;
            switch (input.state)
            {
                case GLFW_PRESS ->
                {
                    int tolerance = 2;
                    
                    boolean inc = Math.abs(IO.MOUSE_POS.x - input.downPos.x) < tolerance &&
                                  Math.abs(IO.MOUSE_POS.y - input.downPos.y) < tolerance &&
                                  time - input.downTime < IO._DOUBLE_PRESS_DELAY;
                    
                    input.held      = true;
                    input.heldTime  = time + IO._HOLD_FREQUENCY;
                    input.downTime  = time;
                    input.downCount = inc ? input.downCount + 1 : 1;
                    input.downPos.set(IO.MOUSE_POS);
                    
                    IO.MOUSE_BUTTON_DOWN.add(button);
                }
                case GLFW_RELEASE ->
                {
                    input.held     = false;
                    input.heldTime = Long.MAX_VALUE;
                    
                    IO.MOUSE_BUTTON_UP.add(button);
                }
                case GLFW_REPEAT -> IO.MOUSE_BUTTON_REPEATED.add(button);
            }
            input.dragging = false;
            if (input.held)
            {
                if (time - input.heldTime >= IO._HOLD_FREQUENCY)
                {
                    IO.MOUSE_BUTTON_HELD.add(button);
                    input.heldTime += IO._HOLD_FREQUENCY;
                }
                if (IO.MOUSE_POS_DELTA.x != 0 || IO.MOUSE_POS_DELTA.y != 0)
                {
                    input.dragging = true;
                    IO.MOUSE_BUTTON_DRAGGED.add(button);
                }
            }
        }
        IO.MOUSE_ON_BUTTON_DOWN.fired     = !IO.MOUSE_BUTTON_DOWN.isEmpty();
        IO.MOUSE_ON_BUTTON_UP.fired       = !IO.MOUSE_BUTTON_UP.isEmpty();
        IO.MOUSE_ON_BUTTON_REPEATED.fired = !IO.MOUSE_BUTTON_REPEATED.isEmpty();
        IO.MOUSE_ON_BUTTON_HELD.fired     = !IO.MOUSE_BUTTON_HELD.isEmpty();
        IO.MOUSE_ON_BUTTON_DRAGGED.fired  = !IO.MOUSE_BUTTON_DRAGGED.isEmpty();
    }
    
    private static void destroyMouse(@NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Destroy Mouse");
        
        callbacks.add(glfwSetCursorEnterCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetCursorPosCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetScrollCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetMouseButtonCallback(IO.WINDOW_HANDLE, null));
        
        IO.MOUSE_BUTTON_STATES.clear();
    }
    
    public static boolean mouseEntered()
    {
        return IO.MOUSE_ENTERED;
    }
    
    public static @NotNull Event mouseOnEntered()
    {
        return IO.MOUSE_ON_ENTERED;
    }
    
    public static @NotNull @UnmodifiableView Vector2dc mousePos()
    {
        return IO.MOUSE_POS;
    }
    
    public static @NotNull @UnmodifiableView Vector2dc mousePosDelta()
    {
        return IO.MOUSE_POS_DELTA;
    }
    
    public static @NotNull Event mouseOnPosChange()
    {
        return IO.MOUSE_ON_POS;
    }
    
    public static @NotNull @UnmodifiableView Vector2dc mouseScroll()
    {
        return IO.MOUSE_SCROLL;
    }
    
    public static @NotNull Event mouseOnScrollChange()
    {
        return IO.MOUSE_ON_SCROLL;
    }
    
    public static boolean mouseButtonDown(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).state == GLFW_PRESS;
    }
    
    public static @NotNull @UnmodifiableView Set<Button> mouseButtonDown()
    {
        return Collections.unmodifiableSet(IO.MOUSE_BUTTON_DOWN);
    }
    
    public static int mouseButtonDownCount(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).downCount;
    }
    
    public static @NotNull Event mouseOnButtonDown()
    {
        return IO.MOUSE_ON_BUTTON_DOWN;
    }
    
    public static boolean mouseButtonUp(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).state == GLFW_RELEASE;
    }
    
    public static @NotNull @UnmodifiableView Set<Button> mouseButtonUp()
    {
        return Collections.unmodifiableSet(IO.MOUSE_BUTTON_UP);
    }
    
    public static @NotNull Event mouseOnButtonUp()
    {
        return IO.MOUSE_ON_BUTTON_UP;
    }
    
    public static boolean mouseButtonRepeated(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).state == GLFW_REPEAT;
    }
    
    public static @NotNull @UnmodifiableView Set<Button> mouseButtonRepeated()
    {
        return Collections.unmodifiableSet(IO.MOUSE_BUTTON_REPEATED);
    }
    
    public static @NotNull Event mouseOnButtonRepeated()
    {
        return IO.MOUSE_ON_BUTTON_REPEATED;
    }
    
    public static boolean mouseButtonHeld(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).held;
    }
    
    public static @NotNull @UnmodifiableView Set<Button> mouseButtonHeld()
    {
        return Collections.unmodifiableSet(IO.MOUSE_BUTTON_HELD);
    }
    
    public static @NotNull Event mouseOnButtonHeld()
    {
        return IO.MOUSE_ON_BUTTON_HELD;
    }
    
    public static boolean mouseButtonDragged(@NotNull Button button)
    {
        return IO.MOUSE_BUTTON_STATES.get(button).dragging;
    }
    
    public static @NotNull @UnmodifiableView Set<Button> mouseButtonDragged()
    {
        return Collections.unmodifiableSet(IO.MOUSE_BUTTON_DRAGGED);
    }
    
    public static @NotNull Event mouseOnButtonDragged()
    {
        return IO.MOUSE_ON_BUTTON_DRAGGED;
    }
    
    // -------------------- Mouse Functions -------------------- //
    
    public static boolean mouseIsShown()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR)).get();
            return result == GLFW_CURSOR_NORMAL;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'mouseShow'");
            return false;
        }
    }
    
    public static void mouseShow()
    {
        IO.LOGGER.trace("Setting property 'mouseShow'");
        
        Engine.executor.submit(() -> {
            if (glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR) == GLFW_CURSOR_DISABLED)
            {
                IO.MOUSE_POS.set(IO.WINDOW_SIZE).mul(0.5);
            }
            glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        });
    }
    
    public static boolean mouseIsHidden()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR)).get();
            return result == GLFW_CURSOR_HIDDEN;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'mouseHide'");
            return false;
        }
    }
    
    public static void mouseHide()
    {
        IO.LOGGER.trace("Setting property 'mouseHide'");
        
        Engine.executor.submit(() -> glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR, GLFW_CURSOR_HIDDEN));
    }
    
    public static boolean mouseIsCaptured()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR)).get();
            return result == GLFW_CURSOR_DISABLED;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'mouseCapture'");
            return false;
        }
    }
    
    public static void mouseCapture()
    {
        IO.LOGGER.trace("Setting property 'mouseCapture'");
        
        Engine.executor.submit(() -> {
            IO.MOUSE_POS.set(IO.WINDOW_SIZE).mul(0.5);
            glfwSetCursorPos(IO.WINDOW_HANDLE, IO.MOUSE_POS.x, IO.MOUSE_POS.y);
            glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        });
    }
    
    public static boolean mouseIsRawInput()
    {
        try
        {
            int result = Engine.executor.submit(() -> {
                if (!glfwRawMouseMotionSupported())
                {
                    IO.LOGGER.warning("Raw Mouse Input is not support on", Platform.get());
                    return GLFW_FALSE;
                }
                return glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_RAW_MOUSE_MOTION);
            }).get();
            return result == GLFW_TRUE;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'mouseRawInput'");
            return false;
        }
    }
    
    public static void mouseRawInput(boolean rawInput)
    {
        IO.LOGGER.trace("Setting property 'mouseRawInput':", rawInput);
        
        Engine.executor.submit(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                IO.LOGGER.warning("Raw Mouse Input is not support on", Platform.get());
                return;
            }
            glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_RAW_MOUSE_MOTION, rawInput ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    public static boolean mouseIsSticky()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_STICKY_MOUSE_BUTTONS)).get();
            return result == GLFW_TRUE;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'mouseSticky'");
            return false;
        }
    }
    
    public static void mouseSticky(boolean sticky)
    {
        IO.LOGGER.trace("Setting property 'mouseSticky':", sticky);
        
        Engine.executor.submit(() -> glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_STICKY_MOUSE_BUTTONS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    // -------------------- Mouse Callbacks -------------------- //
    
    private static void mouseEnteredCallback(long handle, boolean inWindow)
    {
        IO.MOUSE_ENTERED_CHANGE = inWindow;
    }
    
    private static void mousePosCallback(long handle, double x, double y)
    {
        IO.MOUSE_POS_CHANGE = new Vector2d(x, y);
    }
    
    private static void mouseScrollCallback(long handle, double dx, double dy)
    {
        IO.MOUSE_SCROLL_CHANGE = new Vector2d(dx, dy);
    }
    
    private static void mouseButtonCallback(long handle, int button, int action, int mods)
    {
        IO.MOUSE_BUTTON_STATES.get(IO.MOUSE_BUTTON_MAP.get(button)).stateChange = action;
        
        IO.MODIFIER_ACTIVE = mods;
    }
    
    // -------------------- Keyboard State -------------------- //
    
    static final StringBuffer   KEYBOARD_TYPED         = new StringBuffer();
    static final Queue<Integer> KEYBOARD_TYPED_CHANGES = new ConcurrentLinkedQueue<>();
    static final Event          KEYBOARD_ON_TYPED      = new Event();
    
    static final Map<Integer, Key> KEYBOARD_KEY_MAP    = new HashMap<>();
    static final Map<Key, Input>   KEYBOARD_KEY_STATES = new EnumMap<>(Key.class);
    
    static final EnumSet<@NotNull Key> KEYBOARD_KEY_DOWN    = EnumSet.noneOf(Key.class);
    static final Event                 KEYBOARD_ON_KEY_DOWN = new Event();
    
    static final EnumSet<@NotNull Key> KEYBOARD_KEY_UP    = EnumSet.noneOf(Key.class);
    static final Event                 KEYBOARD_ON_KEY_UP = new Event();
    
    static final EnumSet<@NotNull Key> KEYBOARD_KEY_REPEATED    = EnumSet.noneOf(Key.class);
    static final Event                 KEYBOARD_ON_KEY_REPEATED = new Event();
    
    static final EnumSet<@NotNull Key> KEYBOARD_KEY_HELD    = EnumSet.noneOf(Key.class);
    static final Event                 KEYBOARD_ON_KEY_HELD = new Event();
    
    private static void setupKeyboard(@NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Setup Keyboard");
        
        for (Key key : Key.values())
        {
            IO.KEYBOARD_KEY_MAP.put(key.ref, key);
            IO.KEYBOARD_KEY_STATES.put(key, new Input());
        }
        
        keyboardSticky(false);
        
        callbacks.add(glfwSetCharCallback(IO.WINDOW_HANDLE, IO::keyboardCharCallback));
        callbacks.add(glfwSetKeyCallback(IO.WINDOW_HANDLE, IO::keyboardKeyCallback));
    }
    
    private static void updateKeyboard(long time)
    {
        IO.KEYBOARD_ON_TYPED.fired = false;
        IO.KEYBOARD_TYPED.setLength(0);
        
        Integer keyTyped;
        while ((keyTyped = IO.KEYBOARD_TYPED_CHANGES.poll()) != null)
        {
            IO.KEYBOARD_TYPED.appendCodePoint(keyTyped);
            IO.KEYBOARD_ON_TYPED.fired = true;
        }
        
        IO.KEYBOARD_KEY_DOWN.clear();
        IO.KEYBOARD_KEY_UP.clear();
        IO.KEYBOARD_KEY_REPEATED.clear();
        IO.KEYBOARD_KEY_HELD.clear();
        for (Key key : IO.KEYBOARD_KEY_STATES.keySet())
        {
            Input input = IO.KEYBOARD_KEY_STATES.get(key);
            
            input.state       = input.stateChange;
            input.stateChange = -1;
            switch (input.state)
            {
                case GLFW_PRESS ->
                {
                    boolean inc = time - input.downTime < IO._DOUBLE_PRESS_DELAY;
                    
                    input.held      = true;
                    input.heldTime  = time + IO._HOLD_FREQUENCY;
                    input.downTime  = time;
                    input.downCount = inc ? input.downCount + 1 : 1;
                    
                    IO.KEYBOARD_KEY_DOWN.add(key);
                }
                case GLFW_RELEASE ->
                {
                    input.held     = false;
                    input.heldTime = Long.MAX_VALUE;
                    
                    IO.KEYBOARD_KEY_UP.add(key);
                }
                case GLFW_REPEAT -> IO.KEYBOARD_KEY_REPEATED.add(key);
            }
            if (input.held && time - input.heldTime >= IO._HOLD_FREQUENCY)
            {
                input.heldTime += IO._HOLD_FREQUENCY;
                IO.KEYBOARD_KEY_HELD.add(key);
            }
        }
        IO.KEYBOARD_ON_KEY_DOWN.fired     = !IO.KEYBOARD_KEY_DOWN.isEmpty();
        IO.KEYBOARD_ON_KEY_UP.fired       = !IO.KEYBOARD_KEY_UP.isEmpty();
        IO.KEYBOARD_ON_KEY_REPEATED.fired = !IO.KEYBOARD_KEY_REPEATED.isEmpty();
        IO.KEYBOARD_ON_KEY_HELD.fired     = !IO.KEYBOARD_KEY_HELD.isEmpty();
    }
    
    private static void destroyKeyboard(@NotNull List<Callback> callbacks)
    {
        IO.LOGGER.trace("Destroy Modifier");
        
        callbacks.add(glfwSetCharCallback(IO.WINDOW_HANDLE, null));
        callbacks.add(glfwSetKeyCallback(IO.WINDOW_HANDLE, null));
        
        IO.KEYBOARD_KEY_STATES.clear();
    }
    
    public static @NotNull String keyboardTyped()
    {
        return IO.KEYBOARD_TYPED.toString();
    }
    
    public static @NotNull Event keyboardOnTyped()
    {
        return IO.KEYBOARD_ON_TYPED;
    }
    
    public static boolean keyboardKeyDown(@NotNull Key key)
    {
        return IO.KEYBOARD_KEY_STATES.get(key).state == GLFW_PRESS;
    }
    
    public static @NotNull @UnmodifiableView Set<@NotNull Key> keyboardKeyDown()
    {
        return Collections.unmodifiableSet(IO.KEYBOARD_KEY_DOWN);
    }
    
    public static int keyboardKeyDownCount(@NotNull Key key)
    {
        return IO.KEYBOARD_KEY_STATES.get(key).downCount;
    }
    
    public static @NotNull Event keyboardOnKeyDown()
    {
        return IO.KEYBOARD_ON_KEY_DOWN;
    }
    
    public static boolean keyboardKeyUp(@NotNull Key key)
    {
        return IO.KEYBOARD_KEY_STATES.get(key).state == GLFW_RELEASE;
    }
    
    public static @NotNull @UnmodifiableView Set<@NotNull Key> keyboardKeyUp()
    {
        return Collections.unmodifiableSet(IO.KEYBOARD_KEY_UP);
    }
    
    public static @NotNull Event keyboardOnKeyUp()
    {
        return IO.KEYBOARD_ON_KEY_UP;
    }
    
    public static boolean keyboardKeyRepeated(@NotNull Key key)
    {
        return IO.KEYBOARD_KEY_STATES.get(key).state == GLFW_REPEAT;
    }
    
    public static @NotNull @UnmodifiableView Set<@NotNull Key> keyboardKeyRepeated()
    {
        return Collections.unmodifiableSet(IO.KEYBOARD_KEY_REPEATED);
    }
    
    public static @NotNull Event keyboardOnKeyRepeated()
    {
        return IO.KEYBOARD_ON_KEY_REPEATED;
    }
    
    public static boolean keyboardKeyHeld(@NotNull Key key)
    {
        return IO.KEYBOARD_KEY_STATES.get(key).held;
    }
    
    public static @NotNull @UnmodifiableView Set<@NotNull Key> keyboardKeyHeld()
    {
        return Collections.unmodifiableSet(IO.KEYBOARD_KEY_HELD);
    }
    
    public static @NotNull Event keyboardOnKeyHeld()
    {
        return IO.KEYBOARD_ON_KEY_HELD;
    }
    
    // -------------------- Keyboard Functions -------------------- //
    
    public static boolean keyboardIsSticky()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_STICKY_KEYS)).get();
            return result == GLFW_TRUE;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'keyboardSticky'");
            return false;
        }
    }
    
    public static void keyboardSticky(boolean sticky)
    {
        IO.LOGGER.trace("Setting property 'keyboardSticky':", sticky);
        
        Engine.executor.submit(() -> glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_STICKY_KEYS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    // -------------------- Keyboard Callbacks -------------------- //
    
    private static void keyboardCharCallback(long handle, int codePoint)
    {
        IO.KEYBOARD_TYPED_CHANGES.offer(codePoint);
    }
    
    private static void keyboardKeyCallback(long handle, int key, int scancode, int action, int mods)
    {
        IO.KEYBOARD_KEY_STATES.get(IO.KEYBOARD_KEY_MAP.get(key)).stateChange = action;
        
        IO.MODIFIER_ACTIVE = mods;
    }
    
    // -------------------- Modifier State -------------------- //
    
    static int MODIFIER_ACTIVE;
    
    private static void setupModifier()
    {
        IO.LOGGER.trace("Setup Modifier");
        
        modifierLockMods(false);
    }
    
    public static boolean modifierAny(@NotNull Modifier first, @NotNull Modifier @NotNull ... others)
    {
        int query = first.ref;
        for (Modifier mod : others) query |= mod.ref;
        return (IO.MODIFIER_ACTIVE & query) > 0;
    }
    
    public static boolean modifierAll(@NotNull Modifier first, @NotNull Modifier @NotNull ... others)
    {
        int query = first.ref;
        for (Modifier mod : others) query |= mod.ref;
        return (IO.MODIFIER_ACTIVE & query) == query;
    }
    
    public static boolean modifierOnly(@NotNull Modifier first, @NotNull Modifier @NotNull ... others)
    {
        int query = first.ref;
        for (Modifier mod : others) query |= mod.ref;
        return IO.MODIFIER_ACTIVE == query;
    }
    
    // -------------------- Modifier Functions -------------------- //
    
    public static boolean modifierLockMods()
    {
        try
        {
            int result = Engine.executor.submit(() -> glfwGetInputMode(IO.WINDOW_HANDLE, GLFW_LOCK_KEY_MODS)).get();
            return result == GLFW_TRUE;
        }
        catch (InterruptedException | ExecutionException e)
        {
            IO.LOGGER.warning("Could not get property 'modifierLockMods'");
            return false;
        }
    }
    
    public static void modifierLockMods(boolean sticky)
    {
        IO.LOGGER.trace("Setting property 'modifierLockMods':", sticky);
        
        Engine.executor.submit(() -> glfwSetInputMode(IO.WINDOW_HANDLE, GLFW_LOCK_KEY_MODS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    // -------------------- Modifier Callbacks -------------------- //
    
    // -------------------- Sub-Classes -------------------- //
    
    static class Input
    {
        int state       = -1;
        int stateChange = -1;
        
        boolean held     = false;
        long    heldTime = Long.MAX_VALUE;
        
        long downTime  = 0;
        int  downCount = 0;
    }
    
    static final class ButtonInput extends Input
    {
        boolean dragging = false;
        final Vector2d downPos = new Vector2d();
    }
    
    public static final class Event
    {
        private boolean fired    = false;
        private boolean consumed = false;
        
        private Event() {}
        
        public boolean fired()
        {
            return this.fired;
        }
        
        public void consume()
        {
            this.consumed = true;
        }
        
        public boolean consumed()
        {
            return this.consumed;
        }
    }
}
