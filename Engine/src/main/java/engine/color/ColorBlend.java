package engine.color;

import org.jetbrains.annotations.NotNull;

public record ColorBlend(@NotNull Func srcFunc, @NotNull Func dstFunc, @NotNull Eqn blendEqn)
{
    public static final ColorBlend NONE           = new ColorBlend(Func.ONE, Func.ZERO, Eqn.ADD);
    public static final ColorBlend ALPHA          = new ColorBlend(Func.SRC_ALPHA, Func.ONE_MINUS_SRC_ALPHA, Eqn.ADD);
    public static final ColorBlend ADDITIVE       = new ColorBlend(Func.SRC_ALPHA, Func.ONE, Eqn.ADD);
    public static final ColorBlend MULTIPLICATIVE = new ColorBlend(Func.DST_COLOR, Func.ONE_MINUS_SRC_ALPHA, Eqn.ADD);
    public static final ColorBlend STENCIL        = new ColorBlend(Func.ZERO, Func.SRC_ALPHA, Eqn.ADD);
    public static final ColorBlend ADD_COLORS     = new ColorBlend(Func.ONE, Func.ONE, Eqn.ADD);
    public static final ColorBlend SUB_COLORS     = new ColorBlend(Func.ONE, Func.ONE, Eqn.SUBTRACT);
    public static final ColorBlend ILLUMINATE     = new ColorBlend(Func.ONE_MINUS_SRC_ALPHA, Func.SRC_ALPHA, Eqn.ADD);
    
    public static final ColorBlend DEFAULT = ALPHA;
    
    public void blend(@NotNull Colorc src, @NotNull Colorc dst, @NotNull Color out)
    {
        int srcR = src.r(), srcG = src.g(), srcB = src.b(), srcA = src.a();
        int dstR = dst.r(), dstG = dst.g(), dstB = dst.b(), dstA = dst.a();
        
        int srcFuncR = this.srcFunc.call(srcR, srcA, dstR, dstA);
        int srcFuncG = this.srcFunc.call(srcG, srcA, dstG, dstA);
        int srcFuncB = this.srcFunc.call(srcB, srcA, dstB, dstA);
        int srcFuncA = this.srcFunc.call(srcA, srcA, dstA, dstA);
        
        int dstFuncR = this.dstFunc.call(srcR, srcA, dstR, dstA);
        int dstFuncG = this.dstFunc.call(srcG, srcA, dstG, dstA);
        int dstFuncB = this.dstFunc.call(srcB, srcA, dstB, dstA);
        int dstFuncA = this.dstFunc.call(srcA, srcA, dstA, dstA);
        
        out.r = this.blendEqn.call(srcFuncR * srcR, dstFuncR * dstR) / 255;
        out.g = this.blendEqn.call(srcFuncG * srcG, dstFuncG * dstG) / 255;
        out.b = this.blendEqn.call(srcFuncB * srcB, dstFuncB * dstB) / 255;
        out.a = this.blendEqn.call(srcFuncA * srcA, dstFuncA * dstA) / 255;
    }
    
    public enum Func
    {
        ZERO,
        ONE,
        SRC_COLOR,
        ONE_MINUS_SRC_COLOR,
        SRC_ALPHA,
        ONE_MINUS_SRC_ALPHA,
        DST_COLOR,
        ONE_MINUS_DST_COLOR,
        DST_ALPHA,
        ONE_MINUS_DST_ALPHA,
        ;
        
        public int call(int srcColor, int srcAlpha, int dstColor, int dstAlpha)
        {
            return switch (this)
                    {
                        case ZERO -> 0;
                        case ONE -> 255;
                        case SRC_COLOR -> srcColor;
                        case ONE_MINUS_SRC_COLOR -> 255 - srcColor;
                        case SRC_ALPHA -> srcAlpha;
                        case ONE_MINUS_SRC_ALPHA -> 25 - srcAlpha;
                        case DST_COLOR -> dstColor;
                        case ONE_MINUS_DST_COLOR -> 255 - dstColor;
                        case DST_ALPHA -> dstAlpha;
                        case ONE_MINUS_DST_ALPHA -> 255 - dstAlpha;
                    };
        }
    }
    
    public enum Eqn
    {
        ADD,
        SUBTRACT,
        REVERSE_SUBTRACT,
        MIN,
        MAX,
        ;
        
        public int call(int src, int dst)
        {
            return switch (this)
                    {
                        case ADD -> src + dst;
                        case SUBTRACT -> src - dst;
                        case REVERSE_SUBTRACT -> dst - src;
                        case MIN -> Math.min(src, dst);
                        case MAX -> Math.max(src, dst);
                    };
        }
    }
}
