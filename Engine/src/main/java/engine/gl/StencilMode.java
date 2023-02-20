package engine.gl;

import org.jetbrains.annotations.NotNull;

public record StencilMode(@NotNull StencilFunc func, int ref, int mask, @NotNull StencilOp sFail, @NotNull StencilOp dpFail, @NotNull StencilOp dpPass)
{
    public static final StencilMode NONE = new StencilMode(StencilFunc.ALWAYS, 1, 0xFF, StencilOp.KEEP, StencilOp.KEEP, StencilOp.KEEP);
    
    public static final StencilMode DEFAULT = NONE;
}
