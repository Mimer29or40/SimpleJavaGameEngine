package engine.gl;

import engine.color.ColorFormat;
import engine.gl.texture.Texture2D;
import engine.gl.texture.TextureDepth;
import engine.gl.texture.TextureDepthStencil;
import engine.gl.texture.TextureStencil;
import engine.util.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL40;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static engine.IO.windowSize;
import static engine.Renderer.bind;

public class Framebuffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static final Framebuffer NULL = new Null();
    
    // -------------------- Creation -------------------- //
    
    private static final Builder BUILDER = new Builder();
    
    public static Builder builder(int width, int height)
    {
        return Framebuffer.BUILDER.reset(width, height);
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    private int width, height;
    
    protected final List<Texture2D> _colorAttachments;
    public final    List<Texture2D> colorAttachments;
    
    protected Texture2D depthAttachments;
    protected Texture2D stencilAttachments;
    protected Texture2D depthStencilAttachments;
    
    private Framebuffer()
    {
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
        
        this._colorAttachments = new ArrayList<>(32);
        this.colorAttachments  = Collections.unmodifiableList(this._colorAttachments);
        Collections.fill(this._colorAttachments, Texture2D.NULL);
        
        this.depthAttachments        = Texture2D.NULL;
        this.stencilAttachments      = Texture2D.NULL;
        this.depthStencilAttachments = Texture2D.NULL;
    }
    
    private Framebuffer(int width, int height, @NotNull List<Texture2D> colors, @NotNull Texture2D depth, @NotNull Texture2D stencil, @NotNull Texture2D depthStencil)
    {
        this.id = GL40.glGenFramebuffers();
        
        this.width  = width;
        this.height = height;
        
        bind(this);
        
        this._colorAttachments = new ArrayList<>(colors);
        this.colorAttachments  = Collections.unmodifiableList(this._colorAttachments);
        
        int i = 0;
        for (Texture2D color : this.colorAttachments)
        {
            if (color != Texture2D.NULL)
            {
                Framebuffer.LOGGER.trace("Attaching Color[%s]=%s to %s", i, color, this);
                GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_COLOR_ATTACHMENT0 + i++, color.type, color.id(), 0);
            }
        }
        
        if (depth != Texture2D.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching Depth=%s to %s", depth, this);
            GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_DEPTH_ATTACHMENT, depth.type, depth.id(), 0);
            this.depthAttachments = depth;
        }
        
        if (stencil != Texture2D.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching Stencil=%s to %s", stencil, this);
            GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_STENCIL_ATTACHMENT, stencil.type, stencil.id(), 0);
            this.stencilAttachments = stencil;
        }
        
        if (depthStencil != Texture2D.NULL)
        {
            Framebuffer.LOGGER.trace("Attaching DepthStencil=%s to %s", depthStencil, this);
            GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.type, depthStencil.id(), 0);
            this.depthStencilAttachments = depthStencil;
        }
        
        int status = GL40.glCheckFramebufferStatus(GL40.GL_FRAMEBUFFER);
        
        if (status != GL40.GL_FRAMEBUFFER_COMPLETE)
        {
            String message = switch (status)
                    {
                        case GL40.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
                        case GL40.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
                        case GL40.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
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
    
    // -------------------- Functions -------------------- //
    
    /**
     * Delete framebuffer from GPU
     */
    public void delete()
    {
        Framebuffer.LOGGER.debug("Deleting", this);
        
        GL40.glDeleteFramebuffers(this.id);
        
        this._colorAttachments.forEach(Texture2D::delete);
        this._colorAttachments.clear();
        
        if (this.depthAttachments != null) this.depthAttachments.delete();
        this.depthAttachments = null;
        
        if (this.stencilAttachments != null) this.stencilAttachments.delete();
        this.stencilAttachments = null;
        
        if (this.depthStencilAttachments != null) this.depthStencilAttachments.delete();
        this.depthStencilAttachments = null;
        
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
    }
    
    // -------------------- Sub-Classes -------------------- //
    
    public static final class Builder
    {
        private int width, height;
        
        private       int             colorIndex;
        private final List<Texture2D> color = new ArrayList<>(32);
        
        private Texture2D depth;
        private Texture2D stencil;
        private Texture2D depthStencil;
        
        private Builder reset(int width, int height)
        {
            this.width  = width;
            this.height = height;
            
            this.colorIndex = 0;
            Collections.fill(this.color, Texture2D.NULL);
            
            this.depth        = Texture2D.NULL;
            this.stencil      = Texture2D.NULL;
            this.depthStencil = Texture2D.NULL;
            
            return this;
        }
        
        public @NotNull Framebuffer build()
        {
            return new Framebuffer(this.width, this.height, this.color, this.depth, this.stencil, this.depthStencil);
        }
        
        public @NotNull Builder color(@NotNull Texture2D texture)
        {
            this.color.set(this.colorIndex++, texture);
            
            return this;
        }
        
        public @NotNull Builder color(@NotNull ColorFormat format, int width, int height)
        {
            return color(new Texture2D(format, width, height));
        }
        
        public @NotNull Builder color(int width, int height)
        {
            return color(new Texture2D(ColorFormat.DEFAULT, width, height));
        }
        
        public @NotNull Builder depth(@NotNull Texture2D texture)
        {
            this.depth = texture;
            
            return this;
        }
        
        public @NotNull Builder depth(int width, int height)
        {
            return depth(new TextureDepth(width, height));
        }
        
        public @NotNull Builder depth()
        {
            return depth(new TextureDepth(this.width, this.height));
        }
        
        public @NotNull Builder stencil(@NotNull Texture2D texture)
        {
            this.stencil = texture;
            
            return this;
        }
        
        public @NotNull Builder stencil(int width, int height)
        {
            return stencil(new TextureStencil(width, height));
        }
        
        public @NotNull Builder stencil()
        {
            return stencil(new TextureStencil(this.width, this.height));
        }
        
        public @NotNull Builder depthStencil(@NotNull Texture2D texture)
        {
            this.depthStencil = texture;
            
            return this;
        }
        
        public @NotNull Builder depthStencil(int width, int height)
        {
            return depthStencil(new TextureDepthStencil(width, height));
        }
        
        public @NotNull Builder depthStencil()
        {
            return depthStencil(new TextureDepthStencil(this.width, this.height));
        }
    }
    
    private static final class Null extends Framebuffer
    {
        @Contract(pure = true)
        @Override
        public @NotNull String toString()
        {
            return "Framebuffer.NULL";
        }
        
        @Override
        public int width()
        {
            return windowSize().x();
        }
        
        @Override
        public int height()
        {
            return windowSize().y();
        }
        
        @Override
        public void delete()
        {
            Framebuffer.LOGGER.warning("Cannot call %s.delete", this);
        }
    }
}
