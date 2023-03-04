package engine.graphics3;

import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Runtime;

import java.lang.Math;

public class Camera extends Transformation
{
    // -------------------- Projection -------------------- //
    
    protected ProjectionMode projectionMode = ProjectionMode.PERSPECTIVE;
    
    protected double fov       = Math.toRadians(60);
    protected double aspect    = 1.0;
    protected double nearPlane = 0.1;
    protected double farPlane  = 1000.0;
    
    protected       boolean  updateProjection = true;
    protected final Matrix4d projection       = new Matrix4d();
    
    // -------------------- View -------------------- //
    
    protected ViewMode viewMode = ViewMode.THIRD;
    
    protected double orbit = 3.0;
    
    protected       boolean  updateView = true;
    protected final Matrix4d view       = new Matrix4d();
    
    // -------------------- ProjectionView -------------------- //
    
    protected       boolean  updateProjectionView = true;
    protected final Matrix4d projectionView       = new Matrix4d();
    
    // -------------------- Derived -------------------- //
    
    private final Vector3d eye = new Vector3d();
    
    // -------------------- Projection -------------------- //
    
    public @NotNull ProjectionMode projectionMode()
    {
        return this.projectionMode;
    }
    
    public @NotNull Camera projectionMode(@NotNull ProjectionMode mode)
    {
        if (this.projectionMode != mode)
        {
            this.projectionMode   = mode;
            this.updateProjection = true;
        }
        return this;
    }
    
    public double fov()
    {
        return this.fov;
    }
    
    public @NotNull Camera fov(double fov)
    {
        if (!Runtime.equals(this.fov, fov, Transformation.DELTA))
        {
            this.fov              = fov;
            this.updateProjection = true;
        }
        return this;
    }
    
    public double aspect()
    {
        return this.aspect;
    }
    
    public @NotNull Camera aspect(double aspect)
    {
        if (!Runtime.equals(this.aspect, aspect, Transformation.DELTA))
        {
            this.aspect           = aspect;
            this.updateProjection = true;
        }
        return this;
    }
    
    public double nearPlane()
    {
        return this.nearPlane;
    }
    
    public @NotNull Camera nearPlane(double nearPlane)
    {
        if (!Runtime.equals(this.nearPlane, nearPlane, Transformation.DELTA))
        {
            this.nearPlane        = nearPlane;
            this.updateProjection = true;
        }
        return this;
    }
    
    public double farPlane()
    {
        return this.farPlane;
    }
    
    public @NotNull Camera farPlane(double farPlane)
    {
        if (!Runtime.equals(this.farPlane, farPlane, Transformation.DELTA))
        {
            this.farPlane         = farPlane;
            this.updateProjection = true;
        }
        return this;
    }
    
    protected void updateProjection()
    {
        ProjectionMode projectionMode = projectionMode();
        
        double fov       = fov();
        double aspect    = aspect();
        double nearPlane = nearPlane();
        double farPlane  = farPlane();
        
        switch (projectionMode)
        {
            case PERSPECTIVE -> this.projection.setPerspective(fov, aspect, nearPlane, farPlane);
            case ORTHOGRAPHIC -> this.projection.setOrtho(-aspect, aspect, -1, 1, nearPlane, farPlane);
        }
    
        this.updateProjection = false;
        this.updateProjectionView = true;
    }
    
    public @NotNull Matrix4dc projection()
    {
        if (this.updateProjection) updateProjection();
        return this.projection;
    }
    
    // -------------------- View -------------------- //
    
    public @NotNull ViewMode viewMode()
    {
        return this.viewMode;
    }
    
    public @NotNull Camera viewMode(@NotNull ViewMode mode)
    {
        if (this.viewMode != mode)
        {
            this.viewMode   = mode;
            this.updateView = true;
        }
        return this;
    }
    
    public double orbit()
    {
        return this.orbit;
    }
    
    public @NotNull Camera orbit(double orbit)
    {
        if (!Runtime.equals(this.orbit, orbit, Transformation.DELTA))
        {
            this.orbit      = orbit;
            this.updateView = true;
        }
        return this;
    }
    
    @Override
    protected void updateTransformation()
    {
        super.updateTransformation();
        
        this.updateView = true;
    }
    
    protected void updateView()
    {
        ViewMode viewMode = viewMode();
        
        double    orbit          = orbit();
        Matrix4dc transformation = transformation();
    
        this.view.identity();
        if (viewMode == ViewMode.THIRD) this.view.translate(0, 0, -orbit);
        this.view.mul(transformation);
        
        this.updateView           = false;
        this.updateProjectionView = true;
    }
    
    public @NotNull Matrix4dc view()
    {
        if (this.updateView) updateView();
        return this.view;
    }
    
    // -------------------- ProjectionView -------------------- //
    
    protected void updateProjectionView()
    {
        Matrix4dc projection = projection();
        Matrix4dc view       = view();
    
        this.projectionView.set(projection).mul(view);
        
        this.updateProjectionView = false;
    }
    
    public @NotNull Matrix4dc projectionView()
    {
        if (this.updateProjectionView) updateProjectionView();
        return this.projectionView;
    }
    
    // -------------------- Derived -------------------- //
    
    public @NotNull Vector3dc eye()
    {
        return switch (viewMode())
                {
                    case FIRST -> this.eye.set(position());
                    case THIRD -> this.eye.set(zAxis()).mul(orbit()).add(position());
                };
    }
}
