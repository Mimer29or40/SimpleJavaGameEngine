package LearnOpenGL;

import engine.Key;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import static engine.IO.*;

public class Camera
{
    // Default camera values
    private static final double YAW         = -90.0;
    private static final double PITCH       = 0.0;
    private static final double SPEED       = 2.5;
    private static final double SENSITIVITY = 0.1;
    private static final double ZOOM        = 45.0;
    
    // camera Attributes
    public final Vector3d Position = new Vector3d(0, 0, 0);
    
    public final Vector3d Front = new Vector3d(0, 0, -1);
    public final Vector3d Up    = new Vector3d();
    public final Vector3d Right = new Vector3d();
    
    public final Vector3d WorldUp = new Vector3d(0, 1, 0);
    
    private final Matrix4d projection = new Matrix4d();
    private final Matrix4d view       = new Matrix4d();
    
    // euler Angles
    public double Yaw   = YAW;
    public double Pitch = PITCH;
    
    // camera options
    public double MovementSpeed    = SPEED;
    public double MouseSensitivity = SENSITIVITY;
    public double Zoom             = ZOOM;
    
    // constructor with vectors
    public Camera(@Nullable Vector3dc position, @Nullable Vector3dc up, @Nullable Double yaw, @Nullable Double pitch)
    {
        if (position != null) Position.set(position);
        if (up != null) WorldUp.set(up);
        if (yaw != null) Yaw = yaw;
        if (pitch != null) Pitch = pitch;
        updateCameraVectors();
    }
    
    public Matrix4dc GetProjectionMatrix()
    {
        return projection.setPerspective(Math.toRadians(Zoom), (double) windowFramebufferSize().x() / windowFramebufferSize().y(), 0.1, 1000.0);
    }
    
    // returns the view matrix calculated using Euler Angles and the LookAt Matrix
    public Matrix4dc GetViewMatrix()
    {
        return view.setLookAt(Position, Position.add(Front, new Vector3d()), Up);
    }
    
    public void update(double time, double deltaTime, boolean constrainPitch)
    {
        if (mouseOnPosChange().fired())
        {
            double xoffset = mousePosDelta().x();
            double yoffset = -mousePosDelta().y();
            
            xoffset *= MouseSensitivity;
            yoffset *= MouseSensitivity;
            
            Yaw += xoffset;
            Pitch += yoffset;
            
            // make sure that when pitch is out of bounds, screen doesn't get flipped
            if (constrainPitch)
            {
                if (Pitch > 89.0f) Pitch = 89.0f;
                if (Pitch < -89.0f) Pitch = -89.0f;
            }
            
            // update Front, Right and Up Vectors using the updated Euler angles
            updateCameraVectors();
        }
        
        if (mouseOnScrollChange().fired())
        {
            Zoom -= (float) mouseScroll().y();
            if (Zoom < 1.0f) Zoom = 1.0f;
            if (Zoom > 45.0f) Zoom = 45.0f;
        }
        
        Vector3d temp = new Vector3d();
        
        double velocity = MovementSpeed * deltaTime;
        if (keyboardKeyHeld(Key.L_SHIFT)) velocity *= 5;
        if (keyboardKeyHeld(Key.W)) Position.add(temp.set(Front).mul(velocity));
        if (keyboardKeyHeld(Key.S)) Position.sub(temp.set(Front).mul(velocity));
        if (keyboardKeyHeld(Key.A)) Position.sub(temp.set(Right).mul(velocity));
        if (keyboardKeyHeld(Key.D)) Position.add(temp.set(Right).mul(velocity));
        if (keyboardKeyHeld(Key.SPACE)) Position.add(temp.set(Up).mul(velocity));
        if (keyboardKeyHeld(Key.L_CONTROL)) Position.sub(temp.set(Up).mul(velocity));
    }
    
    // calculates the front vector from the Camera's (updated) Euler Angles
    public void updateCameraVectors()
    {
        // calculate the new Front vector
        Front.x = Math.cos(Math.toRadians(Yaw)) * Math.cos(Math.toRadians(Pitch));
        Front.y = Math.sin(Math.toRadians(Pitch));
        Front.z = Math.sin(Math.toRadians(Yaw)) * Math.cos(Math.toRadians(Pitch));
        Front.normalize();
        
        // also re-calculate the Right and Up vector
        Front.cross(WorldUp, Right).normalize();  // normalize the vectors, because their length gets closer to 0 the more you look up or down which results in slower movement.
        Right.cross(Front, Up).normalize();
    }
}
