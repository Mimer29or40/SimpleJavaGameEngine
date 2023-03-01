package engine.gl;

import engine.color.ColorFormat;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureDepth;
import engine.gl.texture.TextureDepthStencil;
import engine.gl.texture.TextureStencil;
import engine.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static engine.IO.windowFramebufferSize;
import static org.lwjgl.opengl.GL44.*;

public class Framebuffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    private static Framebuffer bound = Framebuffer.NULL;
    
    public static @NotNull Framebuffer get()
    {
        return Framebuffer.bound;
    }
    
    public static void bind(@NotNull Framebuffer framebuffer)
    {
        Framebuffer.LOGGER.trace("Binding:", Framebuffer.bound = framebuffer);
        
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.id());
        glViewport(0, 0, framebuffer.width(), framebuffer.height());
    }
    
    public static void bindRead(@NotNull Framebuffer framebuffer)
    {
        Framebuffer.LOGGER.trace("Binding Read:", framebuffer);
        
        glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer.id());
    }
    
    public static void bindDraw(@NotNull Framebuffer framebuffer)
    {
        Framebuffer.LOGGER.trace("Binding Draw:", framebuffer);
        
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer.id());
    }
    
    public static void blit(@NotNull Framebuffer read, @NotNull Framebuffer draw, @NotNull EnumSet<ScreenBuffer> buffers, boolean nearest)
    {
        Framebuffer.LOGGER.trace("Bliting from %s to %s", read, draw);
        
        assert buffers.size() > 0;
        int buffer = 0;
        for (ScreenBuffer b : buffers) buffer |= b.ref;
        
        bindRead(read);
        bindDraw(draw);
        glBlitFramebuffer(0, 0, read.width, read.height, 0, 0, draw.width, draw.height, buffer, nearest ? GL_NEAREST : GL_LINEAR);
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    private int width, height;
    
    protected final List<Texture2D> colors;
    
    protected TextureDepth        depth;
    protected TextureStencil      stencil;
    protected TextureDepthStencil depthStencil;
    
    private Framebuffer()
    {
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
        
        this.colors = Arrays.asList(new Texture2D[32]);
        Collections.fill(this.colors, Texture2D.NULL);
        
        this.depth        = TextureDepth.NULL;
        this.stencil      = TextureStencil.NULL;
        this.depthStencil = TextureDepthStencil.NULL;
    }
    
    private Framebuffer(
            int width, int height, @NotNull Texture2D[] colors, @NotNull TextureDepth depth, @NotNull TextureStencil stencil, @NotNull TextureDepthStencil depthStencil)
    {
        this.id = glGenFramebuffers();
        
        this.width  = width;
        this.height = height;
        
        bind(this);
        
        this.colors = Arrays.asList(colors);
        
        int i = 0;
        for (Texture2D color : this.colors)
        {
            if (color != Texture2D.NULL)
            {
                Framebuffer.LOGGER.trace("Attaching Color[%s]=%s to %s", i, color, this);
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i++, color.type, color.id(), 0);
            }
        }
        
        if (depth != TextureDepth.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching Depth=%s to %s", depth, this);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depth.type, depth.id(), 0);
            this.depth = depth;
        }
        
        if (stencil != TextureStencil.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching Stencil=%s to %s", stencil, this);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, stencil.type, stencil.id(), 0);
            this.stencil = stencil;
        }
        
        if (depthStencil != TextureDepthStencil.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching DepthStencil=%s to %s", depthStencil, this);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.type, depthStencil.id(), 0);
            this.depthStencil = depthStencil;
        }
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            String message = switch (status)
                    {
                        case GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
                        case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                        case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                        case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
                        case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
                        case GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
                        case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
                        case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
                        default -> "" + status;
                    };
            throw new IllegalStateException(String.format("%s: Framebuffer Error: %s", this, message));
        }
        
        Framebuffer.LOGGER.debug("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Framebuffer other)) return false;
        return this.id == other.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", width=" + this.width + ", height=" + this.height + '}';
    }
    
    // -------------------- Properties -------------------- //
    
    public int id()
    {
        return this.id;
    }
    
    public int width()
    {
        return this.width;
    }
    
    public int height()
    {
        return this.height;
    }
    
    public @NotNull Texture2D color(int index)
    {
        return this.colors.get(index);
    }
    
    public @NotNull TextureDepth depth()
    {
        return this.depth;
    }
    
    public @NotNull TextureStencil stencil()
    {
        return this.stencil;
    }
    
    public @NotNull TextureDepthStencil depthStencil()
    {
        return this.depthStencil;
    }
    
    // -------------------- Functions -------------------- //
    
    public void delete()
    {
        Framebuffer.LOGGER.debug("Deleting", this);
        
        glDeleteFramebuffers(this.id);
        
        for (Texture2D color : this.colors) if (color != Texture2D.NULL) color.delete();
        Collections.fill(this.colors, Texture2D.NULL);
        
        if (this.depth != null) this.depth.delete();
        this.depth = TextureDepth.NULL;
        
        if (this.stencil != null) this.stencil.delete();
        this.stencil = TextureStencil.NULL;
        
        if (this.depthStencil != null) this.depthStencil.delete();
        this.depthStencil = TextureDepthStencil.NULL;
        
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
    }
    
    public static final Framebuffer NULL = new Framebuffer()
    {
        @Override
        public @NotNull String toString()
        {
            return "Framebuffer.NULL";
        }
        
        @Override
        public int width()
        {
            return windowFramebufferSize().x();
        }
        
        @Override
        public int height()
        {
            return windowFramebufferSize().y();
        }
        
        @Override
        public void delete()
        {
            Framebuffer.LOGGER.warning("Cannot call %s.delete", this);
        }
    };
    
    // -------------------- Builder -------------------- //
    
    private static final Builder BUILDER = new Builder();
    
    public static Builder builder(int width, int height, int samples)
    {
        return Framebuffer.BUILDER.reset(width, height, samples);
    }
    
    public static Builder builder(int width, int height)
    {
        return Framebuffer.BUILDER.reset(width, height, 0);
    }
    
    public static final class Builder
    {
        private int width, height;
        private int samples;
        
        private       int         colorIndex;
        private final Texture2D[] color = new Texture2D[32];
        
        private TextureDepth        depth;
        private TextureStencil      stencil;
        private TextureDepthStencil depthStencil;
        
        private Builder reset(int width, int height, int samples)
        {
            this.width   = width;
            this.height  = height;
            this.samples = samples;
            
            this.colorIndex = 0;
            Arrays.fill(this.color, Texture2D.NULL);
            
            this.depth        = TextureDepth.NULL;
            this.stencil      = TextureStencil.NULL;
            this.depthStencil = TextureDepthStencil.NULL;
            
            return this;
        }
        
        public @NotNull Framebuffer build()
        {
            return new Framebuffer(this.width, this.height, this.color, this.depth, this.stencil, this.depthStencil);
        }
        
        public @NotNull Builder color(@NotNull Texture2D texture)
        {
            this.color[this.colorIndex++] = texture;
            
            return this;
        }
        
        public @NotNull Builder color(@NotNull ColorFormat format)
        {
            return color(new Texture2D(format, this.width, this.height, this.samples));
        }
        
        public @NotNull Builder color()
        {
            return color(new Texture2D(ColorFormat.DEFAULT, this.width, this.height, this.samples));
        }
        
        public @NotNull Builder depth(@NotNull TextureDepth texture)
        {
            this.depth = texture;
            
            return this;
        }
        
        public @NotNull Builder depth()
        {
            return depth(new TextureDepth(this.width, this.height, this.samples));
        }
        
        public @NotNull Builder stencil(@NotNull TextureStencil texture)
        {
            this.stencil = texture;
            
            return this;
        }
        
        public @NotNull Builder stencil()
        {
            return stencil(new TextureStencil(this.width, this.height, this.samples));
        }
        
        public @NotNull Builder depthStencil(@NotNull TextureDepthStencil texture)
        {
            this.depthStencil = texture;
            
            return this;
        }
        
        public @NotNull Builder depthStencil()
        {
            return depthStencil(new TextureDepthStencil(this.width, this.height, this.samples));
        }
    }
}
