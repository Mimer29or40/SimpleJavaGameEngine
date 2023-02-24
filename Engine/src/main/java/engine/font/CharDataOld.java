package engine.font;

public record CharDataOld(char character, int glyph,
                          int advanceWidthUnscaled, int leftSideBearingUnscaled,
                          int x0Unscaled, int y0Unscaled, int x1Unscaled, int y1Unscaled,
                          double u0, double v0, double u1, double v1)
{
}
