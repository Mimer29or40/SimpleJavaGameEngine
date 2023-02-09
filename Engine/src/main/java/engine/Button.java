package engine;

import static org.lwjgl.glfw.GLFW.*;

public enum Button
{
    UNKNOWN(-1),
    
    ONE(GLFW_MOUSE_BUTTON_1),
    TWO(GLFW_MOUSE_BUTTON_2),
    THREE(GLFW_MOUSE_BUTTON_3),
    FOUR(GLFW_MOUSE_BUTTON_4),
    FIVE(GLFW_MOUSE_BUTTON_5),
    SIX(GLFW_MOUSE_BUTTON_6),
    SEVEN(GLFW_MOUSE_BUTTON_7),
    EIGHT(GLFW_MOUSE_BUTTON_8),
    ;
    
    public static final Button LEFT   = Button.ONE;
    public static final Button RIGHT  = Button.TWO;
    public static final Button MIDDLE = Button.THREE;
    
    final int ref;
    
    Button(int ref)
    {
        this.ref = ref;
    }
}
