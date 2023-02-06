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
    
    }
    
    @Override
    protected void update(int frame, double time, double deltaTime)
    {
        if (windowOnClose()) System.out.println("windowOnClose()");
        if (windowOnFocused()) System.out.println("windowOnFocused()=" + windowFocused());
        if (windowOnMinimized()) System.out.println("windowOnMinimized()=" + windowMinimized());
        if (windowOnMaximized()) System.out.println("windowOnMaximized()=" + windowMaximized());
        if (windowOnPosChange()) System.out.println("windowOnPosChange()=" + windowPos());
        if (windowOnSizeChange()) System.out.println("windowOnSizeChange()=" + windowSize());
        if (windowOnContentScaleChange()) System.out.println("windowOnContentScaleChange()=" + windowContentScale());
        if (windowOnFramebufferSizeChange()) System.out.println("windowOnFramebufferSizeChange()=" + windowFramebufferSize());
        if (windowOnRefresh()) System.out.println("windowOnRefresh()");
        if (windowOnDropped()) System.out.println("windowOnDropped()=" + windowDropped());
        
        if (mouseOnEntered()) System.out.println("mouseOnEntered()" + mouseEntered());
        if (mouseOnPosChange()) System.out.println("mouseOnPosChange()=" + mousePos() + mousePosDelta());
        if (mouseOnScrollChange()) System.out.println("mouseOnScrollChange()=" + mouseScroll());
        if (mouseOnButtonDown()) System.out.println("mouseOnButtonDown()=" + mouseButtonDown());
        if (mouseOnButtonUp()) System.out.println("mouseOnButtonUp()=" + mouseButtonUp());
        if (mouseOnButtonRepeated()) System.out.println("mouseOnButtonRepeated()=" + mouseButtonRepeated());
        if (mouseOnButtonHeld()) System.out.println("mouseOnButtonHeld()=" + mouseButtonHeld());
        if (mouseOnButtonDragged()) System.out.println("mouseOnButtonDragged()=" + mouseButtonDragged());
        
        if (keyboardOnTyped()) System.out.println("keyboardOnTyped()=" + keyboardTyped());
        if (keyboardOnKeyDown()) System.out.println("keyboardOnKeyDown()=" + keyboardKeyDown());
        if (keyboardOnKeyUp()) System.out.println("keyboardOnKeyUp()=" + keyboardKeyUp());
        if (keyboardOnKeyRepeated()) System.out.println("keyboardOnKeyRepeated()=" + keyboardKeyRepeated());
        if (keyboardOnKeyHeld()) System.out.println("keyboardOnKeyHeld()=" + keyboardKeyHeld());
        
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
