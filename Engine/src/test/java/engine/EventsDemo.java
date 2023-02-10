package engine;

import static engine.IO.*;

public class EventsDemo extends Engine
{
    protected EventsDemo()
    {
        super(640, 400);
    }
    
    @Override
    protected void setup()
    {
        System.out.println("mouseIsRawInput() = " + mouseIsRawInput());
        System.out.println("mouseIsSticky() = " + mouseIsSticky());
        System.out.println("keyboardIsSticky() = " + keyboardIsSticky());
        System.out.println("modifierLockMods() = " + modifierLockMods());
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (windowOnClose().fired()) System.out.println("windowOnClose()");
        if (windowOnFocused().fired()) System.out.println("windowOnFocused() = " + windowFocused());
        if (windowOnMinimized().fired()) System.out.println("windowOnMinimized() = " + windowMinimized());
        if (windowOnMaximized().fired()) System.out.println("windowOnMaximized() = " + windowMaximized());
        if (windowOnPosChange().fired()) System.out.println("windowOnPosChange() = " + windowPos());
        if (windowOnSizeChange().fired()) System.out.println("windowOnSizeChange() = " + windowSize());
        if (windowOnContentScaleChange().fired()) System.out.println("windowOnContentScaleChange() = " + windowContentScale());
        if (windowOnFramebufferSizeChange().fired()) System.out.println("windowOnFramebufferSizeChange() = " + windowFramebufferSize());
        if (windowOnRefresh().fired()) System.out.println("windowOnRefresh()");
        if (windowOnDropped().fired()) System.out.println("windowOnDropped() = " + windowDropped());
        
        if (mouseOnEntered().fired()) System.out.println("mouseOnEntered()" + mouseEntered());
        if (mouseOnPosChange().fired()) System.out.println("mouseOnPosChange() = " + mousePos() + mousePosDelta());
        if (mouseOnScrollChange().fired()) System.out.println("mouseOnScrollChange() = " + mouseScroll());
        if (mouseOnButtonDown().fired()) System.out.println("mouseOnButtonDown() = " + mouseButtonDown());
        if (mouseOnButtonUp().fired()) System.out.println("mouseOnButtonUp() = " + mouseButtonUp());
        if (mouseOnButtonRepeated().fired()) System.out.println("mouseOnButtonRepeated() = " + mouseButtonRepeated());
        if (mouseOnButtonHeld().fired()) System.out.println("mouseOnButtonHeld() = " + mouseButtonHeld());
        if (mouseOnButtonDragged().fired()) System.out.println("mouseOnButtonDragged() = " + mouseButtonDragged());
        
        if (keyboardOnTyped().fired()) System.out.println("keyboardOnTyped() = " + keyboardTyped());
        if (keyboardOnKeyDown().fired()) System.out.println("keyboardOnKeyDown() = " + keyboardKeyDown());
        if (keyboardOnKeyUp().fired()) System.out.println("keyboardOnKeyUp() = " + keyboardKeyUp());
        if (keyboardOnKeyRepeated().fired()) System.out.println("keyboardOnKeyRepeated() = " + keyboardKeyRepeated());
        if (keyboardOnKeyHeld().fired()) System.out.println("keyboardOnKeyHeld() = " + keyboardKeyHeld());
        
        if (modifierAny(Modifier.CONTROL, Modifier.ALT)) System.out.println("Modifier.CONTROL | Modifier.ALT Active");
        if (modifierAll(Modifier.CONTROL, Modifier.ALT)) System.out.println("Modifier.CONTROL & Modifier.ALT Active");
        
        if (keyboardKeyDown(Key.SPACE) && modifierOnly(Modifier.SHIFT))
        {
            if (mouseIsShown()) {mouseHide();}
            else if (mouseIsHidden()) {mouseCapture();}
            else if (mouseIsCaptured()) {mouseShow();}
        }
        
        if (mouseButtonDown(Button.LEFT)) System.out.println("LEFT " + mouseButtonDownCount(Button.ONE));
        if (mouseButtonUp(Button.MIDDLE)) System.out.println("MIDDLE Up");
        if (mouseButtonRepeated(Button.RIGHT)) System.out.println("RIGHT Repeated");
        if (mouseButtonHeld(Button.FOUR)) System.out.println("FOUR Held");
        if (mouseButtonDragged(Button.FIVE)) System.out.println("FIVE Held");
        
        if (keyboardKeyDown(Key.W)) System.out.println("W Down " + keyboardKeyDownCount(Key.W));
        if (keyboardKeyUp(Key.A)) System.out.println("A Up");
        if (keyboardKeyRepeated(Key.S)) System.out.println("S Repeated");
        if (keyboardKeyHeld(Key.D)) System.out.println("D Held");
    }
    
    @Override
    protected void draw(int frame, double time, double deltaTime)
    {
    
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Engine instance = new EventsDemo();
        start(instance);
    }
}
