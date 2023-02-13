package engine.gl;

import engine.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL44;

import java.util.Objects;

import static engine.IO.windowFramebufferSize;

public class Framebuffer
{
    private static final Logger LOGGER = Logger.getLogger();
    
    // -------------------- Static -------------------- //
    
    public static void bind(@NotNull Framebuffer framebuffer)
    {
        Framebuffer.LOGGER.trace("Binding:", framebuffer);
        
        GL44.glBindFramebuffer(GL44.GL_FRAMEBUFFER, framebuffer.id());
        GL44.glViewport(0, 0, framebuffer.width(), framebuffer.height());
    }
    
    // -------------------- Instance -------------------- //
    
    protected int id;
    
    private int width, height;
    
    //protected final List<Texture2D> _colorAttachments; // TODO
    //public final    List<Texture2D> colorAttachments; // TODO
    
    //protected Texture2D depthAttachments; // TODO
    //protected Texture2D stencilAttachments; // TODO
    //protected Texture2D depthStencilAttachments; // TODO
    
    private Framebuffer()
    {
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
        
        //this._colorAttachments = new ArrayList<>(32); // TODO
        //this.colorAttachments  = Collections.unmodifiableList(this._colorAttachments); // TODO
        //Collections.fill(this._colorAttachments, Texture2D.NULL); // TODO
        
        //this.depthAttachments        = Texture2D.NULL; // TODO
        //this.stencilAttachments      = Texture2D.NULL; // TODO
        //this.depthStencilAttachments = Texture2D.NULL; // TODO
    }
    
    //private Framebuffer(int width, int height, @NotNull List<Texture2D> colors, @NotNull Texture2D depth, @NotNull Texture2D stencil, @NotNull Texture2D depthStencil)  // TODO
    //{
    //    this.id = GL44.glGenFramebuffers();
    //
    //    this.width  = width;
    //    this.height = height;
    //
    //    bind(this);
    //
    //    this._colorAttachments = new ArrayList<>(colors);
    //    this.colorAttachments  = Collections.unmodifiableList(this._colorAttachments);
    //
    //    int i = 0;
    //    for (Texture2D color : this.colorAttachments)
    //    {
    //        if (color != Texture2D.NULL)
    //        {
    //            Framebuffer.LOGGER.trace("Attaching Color[%s]=%s to %s", i, color, this);
    //            GL44.glFramebufferTexture2D(GL44.GL_FRAMEBUFFER, GL44.GL_COLOR_ATTACHMENT0 + i++, color.type, color.id(), 0);
    //        }
    //    }
    //
    //    if (depth != Texture2D.NULL)
    //    {
    //        Framebuffer.LOGGER.trace("Attaching Depth=%s to %s", depth, this);
    //        GL44.glFramebufferTexture2D(GL44.GL_FRAMEBUFFER, GL44.GL_DEPTH_ATTACHMENT, depth.type, depth.id(), 0);
    //        this.depthAttachments = depth;
    //    }
    //
    //    if (stencil != Texture2D.NULL)
    //    {
    //        Framebuffer.LOGGER.trace("Attaching Stencil=%s to %s", stencil, this);
    //        GL44.glFramebufferTexture2D(GL44.GL_FRAMEBUFFER, GL44.GL_STENCIL_ATTACHMENT, stencil.type, stencil.id(), 0);
    //        this.stencilAttachments = stencil;
    //    }
    //
    //    if (depthStencil != Texture2D.NULL)
    //    {
    //        Framebuffer.LOGGER.trace("Attaching DepthStencil=%s to %s", depthStencil, this);
    //        GL44.glFramebufferTexture2D(GL44.GL_FRAMEBUFFER, GL44.GL_DEPTH_STENCIL_ATTACHMENT, depthStencil.type, depthStencil.id(), 0);
    //        this.depthStencilAttachments = depthStencil;
    //    }
    //
    //    int status = GL44.glCheckFramebufferStatus(GL44.GL_FRAMEBUFFER);
    //
    //    if (status != GL44.GL_FRAMEBUFFER_COMPLETE)
    //    {
    //        String message = switch (status)
    //                {
    //                    case GL44.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
    //                    case GL44.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
    //                    case GL44.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
    //                    default -> "" + status;
    //                };
    //        throw new IllegalStateException(String.format("%s: Framebuffer Error: %s", this, message));
    //    }
    //
    //    Framebuffer.LOGGER.debug("Created", this);
    //}
    
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
        
        GL44.glDeleteFramebuffers(this.id);
        
        //this._colorAttachments.forEach(Texture2D::delete); // TODO
        //this._colorAttachments.clear(); // TODO
        
        //if (this.depthAttachments != null) this.depthAttachments.delete(); // TODO
        //this.depthAttachments = null; // TODO
        
        //if (this.stencilAttachments != null) this.stencilAttachments.delete(); // TODO
        //this.stencilAttachments = null; // TODO
        
        //if (this.depthStencilAttachments != null) this.depthStencilAttachments.delete(); // TODO
        //this.depthStencilAttachments = null; // TODO
        
        this.id = 0;
        
        this.width  = 0;
        this.height = 0;
    }
    
    
    // -------------------- Builder -------------------- //
    
    //private static final Builder BUILDER = new Builder();
    
    //public static Builder builder(int width, int height)
    //{
    //    return Framebuffer.BUILDER.reset(width, height);
    //}
    
    //public static final class Builder // TODO
    //{
    //    private int width, height;
    //
    //    private       int             colorIndex;
    //    private final List<Texture2D> color = new ArrayList<>(32);
    //
    //    private Texture2D depth;
    //    private Texture2D stencil;
    //    private Texture2D depthStencil;
    //
    //    private Builder reset(int width, int height)
    //    {
    //        this.width  = width;
    //        this.height = height;
    //
    //        this.colorIndex = 0;
    //        Collections.fill(this.color, Texture2D.NULL);
    //
    //        this.depth        = Texture2D.NULL;
    //        this.stencil      = Texture2D.NULL;
    //        this.depthStencil = Texture2D.NULL;
    //
    //        return this;
    //    }
    //
    //    public @NotNull Framebuffer build()
    //    {
    //        return new Framebuffer(this.width, this.height, this.color, this.depth, this.stencil, this.depthStencil);
    //    }
    //
    //    public @NotNull Builder color(@NotNull Texture2D texture)
    //    {
    //        this.color.set(this.colorIndex++, texture);
    //
    //        return this;
    //    }
    //
    //    public @NotNull Builder color(@NotNull ColorFormat format, int width, int height)
    //    {
    //        return color(new Texture2D(format, width, height));
    //    }
    //
    //    public @NotNull Builder color(int width, int height)
    //    {
    //        return color(new Texture2D(ColorFormat.DEFAULT, width, height));
    //    }
    //
    //    public @NotNull Builder depth(@NotNull Texture2D texture)
    //    {
    //        this.depth = texture;
    //
    //        return this;
    //    }
    //
    //    public @NotNull Builder depth(int width, int height)
    //    {
    //        return depth(new TextureDepth(width, height));
    //    }
    //
    //    public @NotNull Builder depth()
    //    {
    //        return depth(new TextureDepth(this.width, this.height));
    //    }
    //
    //    public @NotNull Builder stencil(@NotNull Texture2D texture)
    //    {
    //        this.stencil = texture;
    //
    //        return this;
    //    }
    //
    //    public @NotNull Builder stencil(int width, int height)
    //    {
    //        return stencil(new TextureStencil(width, height));
    //    }
    //
    //    public @NotNull Builder stencil()
    //    {
    //        return stencil(new TextureStencil(this.width, this.height));
    //    }
    //
    //    public @NotNull Builder depthStencil(@NotNull Texture2D texture)
    //    {
    //        this.depthStencil = texture;
    //
    //        return this;
    //    }
    //
    //    public @NotNull Builder depthStencil(int width, int height)
    //    {
    //        return depthStencil(new TextureDepthStencil(width, height));
    //    }
    //
    //    public @NotNull Builder depthStencil()
    //    {
    //        return depthStencil(new TextureDepthStencil(this.width, this.height));
    //    }
    //}
    
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
}
