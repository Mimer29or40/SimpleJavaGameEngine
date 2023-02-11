package engine.gl;

import org.jetbrains.annotations.NotNull;

public record BlendMode(@NotNull BlendEqn blendEqn, @NotNull BlendFunc srcFunc, @NotNull BlendFunc dstFunc)
{
    public static final BlendMode NONE           = new BlendMode(BlendEqn.ADD, BlendFunc.ZERO, BlendFunc.ONE);
    public static final BlendMode ALPHA          = new BlendMode(BlendEqn.ADD, BlendFunc.SRC_ALPHA, BlendFunc.ONE_MINUS_SRC_ALPHA);
    public static final BlendMode ADDITIVE       = new BlendMode(BlendEqn.ADD, BlendFunc.SRC_ALPHA, BlendFunc.ONE);
    public static final BlendMode MULTIPLICATIVE = new BlendMode(BlendEqn.ADD, BlendFunc.DST_COLOR, BlendFunc.ONE_MINUS_SRC_ALPHA);
    public static final BlendMode STENCIL        = new BlendMode(BlendEqn.ADD, BlendFunc.ZERO, BlendFunc.SRC_ALPHA);
    public static final BlendMode ADD_COLORS     = new BlendMode(BlendEqn.ADD, BlendFunc.ONE, BlendFunc.ONE);
    public static final BlendMode SUB_COLORS     = new BlendMode(BlendEqn.SUBTRACT, BlendFunc.ONE, BlendFunc.ONE);
    public static final BlendMode ILLUMINATE     = new BlendMode(BlendEqn.ADD, BlendFunc.ONE_MINUS_SRC_ALPHA, BlendFunc.SRC_ALPHA);
    
    public static final BlendMode DEFAULT = ALPHA;
}
