package engine;

import static org.lwjgl.glfw.GLFW.*;

public enum Modifier
{
    NONE(0),
    
    SHIFT(GLFW_MOD_SHIFT),
    CONTROL(GLFW_MOD_CONTROL),
    ALT(GLFW_MOD_ALT),
    SUPER(GLFW_MOD_SUPER),
    CAPS_LOCK(GLFW_MOD_CAPS_LOCK),
    NUM_LOCK(GLFW_MOD_NUM_LOCK),
    ;
    
    final int ref;
    
    Modifier(int ref)
    {
        this.ref = ref;
    }
}
