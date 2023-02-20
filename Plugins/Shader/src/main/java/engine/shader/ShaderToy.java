package engine.shader;

import engine.gl.Framebuffer;
import engine.gl.GLType;
import engine.gl.buffer.BufferArray;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static engine.IO.*;

public class ShaderToy
{
    private static final Logger LOGGER = Logger.getLogger();
    
    public final Path file;
    
    private final AtomicBoolean threadRunning;
    private final AtomicBoolean fileChanged;
    
    private boolean shouldLoadShader;
    
    private VertexArray vertexArray;
    
    private Shader  vertexShader;
    private Shader  fragmentShader;
    private Program program;
    
    public ShaderToy(@NotNull Path file)
    {
        this.file = file.toAbsolutePath();
        
        this.threadRunning = new AtomicBoolean(true);
        this.fileChanged   = new AtomicBoolean(false);
        
        this.shouldLoadShader = true;
    }
    
    @Override
    public String toString()
    {
        return "ShaderToy{" + "file='" + this.file + '\'' + '}';
    }
    
    public void setup()
    {
        ShaderToy.LOGGER.debug("Setup");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer data = stack.floats(-1F, +1F, 0F, 0F, // Top Left
                                            +1F, +1F, 1F, 0F, // Top Right
                                            -1F, -1F, 0F, 1F, // Bottom Left
                                            +1F, +1F, 1F, 0F, // Top Right
                                            +1F, -1F, 1F, 1F, // Bottom Right
                                            -1F, -1F, 0F, 1F  // Bottom Left
                                           );
            
            final BufferArray     buffer    = new BufferArray(BufferUsage.STATIC_DRAW, data);
            final VertexAttribute fragPos   = new VertexAttribute(GLType.FLOAT, 2);
            final VertexAttribute fragCoord = new VertexAttribute(GLType.FLOAT, 2);
            this.vertexArray = VertexArray.builder().buffer(buffer, fragPos, fragCoord).build();
        }
        
        Thread thread = new Thread(this::threadRunner, "ShaderFileWatcher");
        thread.setDaemon(true);
        thread.start();
        
        final String vertexCode = """
                                  #version 440 core
                                  
                                  layout(location = 0) in vec2 pos;
                                  layout(location = 1) in vec2 coord;
                                  
                                  out vec2 FragCoord;
                                  
                                  void main(void)
                                  {
                                      FragCoord = coord;
                                      gl_Position = vec4(pos, 0.0, 1.0);
                                  }
                                  """;
        
        this.vertexShader   = new Shader(ShaderType.VERTEX, vertexCode);
        this.fragmentShader = null;
        this.program        = null;
        
        loadFragmentShader();
    }
    
    public void update(int frame, double time, double deltaTime)
    {
    
    }
    
    public void draw(int frame, double time, double deltaTime)
    {
        if (this.fileChanged.getAndSet(false) || this.shouldLoadShader)
        {
            loadFragmentShader();
        }
        
        if (this.program != null)
        {
            Framebuffer.bind(Framebuffer.NULL);
            Program.bind(this.program);
            
            Program.uniformInt("FRAME", frame);
            Program.uniformFloat("TIME", time);
            Program.uniformFloat("DELTA_TIME", deltaTime);
            
            Program.uniformBool("WINDOW_ON_CLOSE", windowOnClose().fired());
            Program.uniformBool("WINDOW_FOCUSED", windowFocused());
            Program.uniformBool("WINDOW_ON_FOCUSED", windowOnFocused().fired());
            Program.uniformBool("WINDOW_MINIMIZED", windowMinimized());
            Program.uniformBool("WINDOW_ON_MINIMIZED", windowOnMinimized().fired());
            Program.uniformBool("WINDOW_MAXIMIZED", windowMaximized());
            Program.uniformBool("WINDOW_ON_MAXIMIZED", windowOnMaximized().fired());
            Program.uniformInt2("WINDOW_POS", windowPos());
            Program.uniformBool("WINDOW_ON_POS_CHANGE", windowOnPosChange().fired());
            Program.uniformInt2("WINDOW_SIZE", windowSize());
            Program.uniformBool("WINDOW_ON_SIZE_CHANGE", windowOnSizeChange().fired());
            Program.uniformFloat2("WINDOW_CONTENT_SCALE", windowContentScale());
            Program.uniformBool("WINDOW_ON_CONTENT_SCALE_CHANGE", windowOnContentScaleChange().fired());
            Program.uniformInt2("WINDOW_FRAMEBUFFER_SIZE", windowFramebufferSize());
            Program.uniformBool("WINDOW_ON_FRAMEBUFFER_SIZE_CHANGE", windowOnFramebufferSizeChange().fired());
            Program.uniformBool("WINDOW_ON_REFRESH", windowOnRefresh().fired());
            //windowDropped();
            //windowOnDropped();
            
            Program.uniformBool("MOUSE_ENTERED", mouseEntered());
            Program.uniformBool("MOUSE_ON_ENTERED", mouseOnEntered().fired());
            Program.uniformFloat2("MOUSE_POS", mousePos());
            Program.uniformFloat2("MOUSE_POS_DELTA", mousePosDelta());
            Program.uniformBool("MOUSE_ON_POS_CHANGE", mouseOnPosChange().fired());
            Program.uniformFloat2("MOUSE_SCROLL", mouseScroll());
            Program.uniformBool("MOUSE_ON_SCROLL_CHANGE", mouseOnScrollChange().fired());
            //mouseButtonDown();
            //mouseButtonDown();
            //mouseButtonDownCount();
            Program.uniformBool("MOUSE_ON_BUTTON_DOWN", mouseOnButtonDown().fired());
            //mouseButtonUp();
            //mouseButtonUp();
            Program.uniformBool("MOUSE_ON_BUTTON_UP", mouseOnButtonUp().fired());
            //mouseButtonRepeated();
            //mouseButtonRepeated();
            Program.uniformBool("MOUSE_ON_BUTTON_REPEATED", mouseOnButtonRepeated().fired());
            //mouseButtonHeld();
            //mouseButtonHeld();
            Program.uniformBool("MOUSE_ON_BUTTON_HELD", mouseOnButtonHeld().fired());
            //mouseButtonDragged();
            //mouseButtonDragged();
            Program.uniformBool("MOUSE_ON_BUTTON_DRAGGED", mouseOnButtonDragged().fired());
            
            //keyboardTyped();
            Program.uniformBool("KEYBOARD_ON_TYPED", keyboardOnTyped().fired());
            //keyboardKeyDown();
            //keyboardKeyDown();
            //keyboardKeyDownCount();
            Program.uniformBool("KEYBOARD_ON_KEY_DOWN", keyboardOnKeyDown().fired());
            //keyboardKeyUp();
            //keyboardKeyUp();
            Program.uniformBool("KEYBOARD_ON_KEY_UP", keyboardOnKeyUp().fired());
            //keyboardKeyRepeated();
            //keyboardKeyRepeated();
            Program.uniformBool("KEYBOARD_ON_KEY_REPEATED", keyboardOnKeyRepeated().fired());
            //keyboardKeyHeld();
            //keyboardKeyHeld();
            Program.uniformBool("KEYBOARD_ON_KEY_HELD", keyboardOnKeyHeld().fired());
            
            this.vertexArray.draw(DrawMode.TRIANGLES);
            
            // TODO - Draw Frame & Time at bottom of the screen
        }
    }
    
    public void destroy()
    {
        ShaderToy.LOGGER.debug("Destroy");
        
        if (this.vertexShader != null) this.vertexShader.delete();
        this.vertexShader = null;
        
        if (this.fragmentShader != null) this.fragmentShader.delete();
        this.fragmentShader = null;
        
        if (this.program != null) this.program.delete();
        this.program = null;
        
        this.vertexArray.delete();
    }
    
    private void loadFragmentShader()
    {
        // TODO - Think of a way to not through out the last successfully compiles shader
        Shader  newShader  = null;
        Program newProgram = null;
        try
        {
            final String fragmentCode = Files.readString(this.file);
            
            newShader  = new Shader(ShaderType.FRAGMENT, fragmentCode);
            newProgram = new Program(this.vertexShader, newShader);
            
            if (this.fragmentShader != null) this.fragmentShader.delete();
            this.fragmentShader = newShader;
            
            if (this.program != null) this.program.delete();
            this.program = newProgram;
            
            this.shouldLoadShader = false;
        }
        catch (Throwable e)
        {
            ShaderToy.LOGGER.warning("Unable to Load Shader");
            ShaderToy.LOGGER.warning(e);
            
            if (newShader != null) newShader.delete();
            if (newProgram != null) newProgram.delete();
            
            this.shouldLoadShader = true;
        }
    }
    
    private void threadRunner()
    {
        ShaderToy.LOGGER.debug("Starting", this);
        try (WatchService watchService = FileSystems.getDefault().newWatchService())
        {
            this.file.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            final Path fileName = this.file.getFileName();
            while (this.threadRunning.get())
            {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW)
                    {
                        Thread.yield();
                    }
                    else
                    {
                        final Path changed = ((Path) event.context()).getFileName();
                        if (fileName.equals(changed)) this.fileChanged.set(true);
                    }
                }
                if (!wk.reset()) break;
                Thread.yield();
            }
        }
        catch (Throwable e)
        {
            ShaderToy.LOGGER.severe(e);
        }
    }
}
