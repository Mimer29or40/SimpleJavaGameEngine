package engine.font;

/**
 * Determines how the text is aligned to a point or in a rectangle.
 * <p>
 * <p>
 * {@link TextAlign#TOP_LEFT} <p>
 * -- The point is above and to the left of the text. <p>
 * -- The text is drawn in the top left corner of a rect <p>
 * <p>
 * {@link TextAlign#TOP} <p>
 * -- The point is above and centered on the text. <p>
 * -- The text is drawn in the top and centered in a rect <p>
 * <p>
 * {@link TextAlign#TOP_RIGHT} <p>
 * -- The point is above and to the right of the text. <p>
 * -- The text is drawn in the top right corner of a rect <p>
 * <p>
 * {@link TextAlign#LEFT} <p>
 * -- The point is centered and to the left of the text. <p>
 * -- The text is drawn in the center left side of a rect <p>
 * <p>
 * {@link TextAlign#CENTER} <p>
 * -- The point is centered on the text. <p>
 * -- The text is drawn in the center of a rect <p>
 * <p>
 * {@link TextAlign#RIGHT} <p>
 * -- The point is centered and to the right of the text. <p>
 * -- The text is drawn in the center right side of a rect <p>
 * <p>
 * {@link TextAlign#BOTTOM_LEFT} <p>
 * -- The point is below and to the left of the text. <p>
 * -- The text is drawn in the bottom left corner of a rect <p>
 * <p>
 * {@link TextAlign#BOTTOM} <p>
 * -- The point is below and centered on the text. <p>
 * -- The text is drawn in the bottom center of a rect <p>
 * <p>
 * {@link TextAlign#BOTTOM_RIGHT} <p>
 * -- The point is below and to the right of the text. <p>
 * -- The text is drawn in the bottom right corner of a rect <p>
 * <p>
 */
public enum TextAlign
{
    TOP_LEFT(-1, -1),
    TOP(0, -1),
    TOP_RIGHT(1, -1),
    LEFT(-1, 0),
    CENTER(0, 0),
    RIGHT(1, 0),
    BOTTOM_LEFT(-1, 1),
    BOTTOM(0, 1),
    BOTTOM_RIGHT(1, 1),
    ;
    
    private final int h, v;
    
    TextAlign(int h, int v)
    {
        this.h = h;
        this.v = v;
    }
    
    public int getH()
    {
        return this.h;
    }
    
    public int getV()
    {
        return this.v;
    }
}
