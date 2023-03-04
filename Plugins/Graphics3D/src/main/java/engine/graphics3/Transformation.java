package engine.graphics3;

import org.jetbrains.annotations.NotNull;
import org.joml.Runtime;
import org.joml.*;

public class Transformation
{
    public static final double DELTA = 1e-9;
    
    protected final Vector3d    position = new Vector3d();
    protected final Quaterniond rotation = new Quaterniond();
    
    protected       boolean  updateTransformation = true;
    protected final Matrix4d transformation       = new Matrix4d();
    
    // -------------------- Derived -------------------- //
    
    protected final Vector3d xAxis = new Vector3d();
    protected final Vector3d yAxis = new Vector3d();
    protected final Vector3d zAxis = new Vector3d();
    protected final Vector3d right = new Vector3d();
    protected final Vector3d up    = new Vector3d();
    protected final Vector3d front = new Vector3d();
    
    // -------------------- Position -------------------- //
    
    public @NotNull Vector3dc position()
    {
        return this.position;
    }
    
    public @NotNull Transformation position(double x, double y, double z)
    {
        if (!Runtime.equals(this.position.x, x, Transformation.DELTA) ||
            !Runtime.equals(this.position.y, y, Transformation.DELTA) ||
            !Runtime.equals(this.position.z, z, Transformation.DELTA))
        {
            this.position.set(x, y, z);
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation position(@NotNull Vector3dc position)
    {
        return position(position.x(), position.y(), position.z());
    }
    
    public @NotNull Transformation translate(double x, double y, double z)
    {
        boolean xEquals = Runtime.equals(x, 0, Transformation.DELTA);
        boolean yEquals = Runtime.equals(y, 0, Transformation.DELTA);
        boolean zEquals = Runtime.equals(z, 0, Transformation.DELTA);
        if (!xEquals || !yEquals || !zEquals)
        {
            Vector3dc xAxis = right();
            Vector3dc yAxis = up();
            Vector3dc zAxis = front();
            
            if (!xEquals) this.position.add(xAxis.x() * x, xAxis.y() * y, xAxis.z() * z);
            if (!yEquals) this.position.add(yAxis.x() * x, yAxis.y() * y, yAxis.z() * z);
            if (!zEquals) this.position.add(zAxis.x() * x, zAxis.y() * y, zAxis.z() * z);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation translate(@NotNull Vector3dc translation)
    {
        return translate(translation.x(), translation.y(), translation.z());
    }
    
    public @NotNull Transformation translateGlobal(double x, double y, double z)
    {
        if (!Runtime.equals(x, 0, Transformation.DELTA) || !Runtime.equals(y, 0, Transformation.DELTA) || !Runtime.equals(z, 0, Transformation.DELTA))
        {
            this.position.add(x, y, z);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation translateGlobal(@NotNull Vector3dc translation)
    {
        return translateGlobal(translation.x(), translation.y(), translation.z());
    }
    
    // -------------------- Rotation -------------------- //
    
    public @NotNull Quaterniondc rotation()
    {
        return this.rotation;
    }
    
    public @NotNull Transformation rotation(@NotNull Quaterniondc rotation)
    {
        if (this.rotation.equals(rotation, Transformation.DELTA))
        {
            this.rotation.set(rotation);
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation rotate(double x, double y, double z)
    {
        boolean xEquals = Runtime.equals(x, 0, Transformation.DELTA);
        boolean yEquals = Runtime.equals(y, 0, Transformation.DELTA);
        boolean zEquals = Runtime.equals(z, 0, Transformation.DELTA);
        if (!xEquals || !yEquals || !zEquals)
        {
            Vector3dc xAxis = xAxis();
            Vector3dc yAxis = yAxis();
            Vector3dc zAxis = zAxis();
            
            if (!xEquals) this.rotation.rotateAxis(x, xAxis);
            if (!yEquals) this.rotation.rotateAxis(y, yAxis);
            if (!zEquals) this.rotation.rotateAxis(z, zAxis);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation rotate(@NotNull Vector3dc axis)
    {
        return rotate(axis.x(), axis.y(), axis.z());
    }
    
    public @NotNull Transformation rotateAxis(double angle, double x, double y, double z)
    {
        if (!Runtime.equals(angle, 0, Transformation.DELTA) ||
            !Runtime.equals(x, 0, Transformation.DELTA) ||
            !Runtime.equals(y, 0, Transformation.DELTA) ||
            !Runtime.equals(z, 0, Transformation.DELTA))
        {
            this.rotation.rotateAxis(angle, x, y, z);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation rotateAxis(double angle, @NotNull Vector3dc axis)
    {
        return rotateAxis(angle, axis.x(), axis.y(), axis.z());
    }
    
    public @NotNull Transformation rotateGlobal(double x, double y, double z)
    {
        boolean xEquals = Runtime.equals(x, 0, Transformation.DELTA);
        boolean yEquals = Runtime.equals(y, 0, Transformation.DELTA);
        boolean zEquals = Runtime.equals(z, 0, Transformation.DELTA);
        if (!xEquals || !yEquals || !zEquals)
        {
            if (!xEquals) this.rotation.rotateX(x);
            if (!yEquals) this.rotation.rotateY(y);
            if (!zEquals) this.rotation.rotateZ(z);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation rotateGlobal(@NotNull Vector3dc axis)
    {
        return rotateGlobal(axis.x(), axis.y(), axis.z());
    }
    
    public @NotNull Transformation lookAlong(double x, double y, double z, double upX, double upY, double upZ)
    {
        if (!Runtime.equals(x, 0, Transformation.DELTA) ||
            !Runtime.equals(y, 0, Transformation.DELTA) ||
            !Runtime.equals(z, 0, Transformation.DELTA) ||
            !Runtime.equals(upX, 0, Transformation.DELTA) ||
            !Runtime.equals(upY, 0, Transformation.DELTA) ||
            !Runtime.equals(upZ, 0, Transformation.DELTA))
        {
            this.rotation.identity().lookAlong(x, y, z, upX, upY, upZ);
            
            this.updateTransformation = true;
        }
        return this;
    }
    
    public @NotNull Transformation lookAlong(@NotNull Vector3dc dir, double upX, double upY, double upZ)
    {
        return lookAlong(dir.x(), dir.y(), dir.z(), upX, upY, upZ);
    }
    
    public @NotNull Transformation lookAlong(double x, double y, double z, @NotNull Vector3dc up)
    {
        return lookAlong(x, y, z, up.x(), up.y(), up.z());
    }
    
    public @NotNull Transformation lookAlong(@NotNull Vector3dc dir, @NotNull Vector3dc up)
    {
        return lookAlong(dir.x(), dir.y(), dir.z(), up.x(), up.y(), up.z());
    }
    
    public @NotNull Transformation lookAlong(double x, double y, double z)
    {
        return lookAlong(x, y, z, up());
    }
    
    public @NotNull Transformation lookAlong(@NotNull Vector3dc dir)
    {
        return lookAlong(dir.x(), dir.y(), dir.z(), up());
    }
    
    public @NotNull Transformation lookAt(double x, double y, double z, double upX, double upY, double upZ)
    {
        Vector3dc position = position();
        x -= position.x();
        y -= position.y();
        z -= position.z();
        return lookAlong(x, y, z, upX, upY, upZ);
    }
    
    public @NotNull Transformation lookAt(@NotNull Vector3dc pos, double upX, double upY, double upZ)
    {
        return lookAt(pos.x(), pos.y(), pos.z(), upX, upY, upZ);
    }
    
    public @NotNull Transformation lookAt(double x, double y, double z, @NotNull Vector3dc up)
    {
        return lookAt(x, y, z, up.x(), up.y(), up.z());
    }
    
    public @NotNull Transformation lookAt(@NotNull Vector3dc pos, @NotNull Vector3dc up)
    {
        return lookAt(pos.x(), pos.y(), pos.z(), up.x(), up.y(), up.z());
    }
    
    public @NotNull Transformation lookAt(double x, double y, double z)
    {
        return lookAt(x, y, z, up());
    }
    
    public @NotNull Transformation lookAt(@NotNull Vector3dc pos)
    {
        return lookAt(pos.x(), pos.y(), pos.z(), up());
    }
    
    // -------------------- Transformations -------------------- //
    
    protected void updateTransformation()
    {
        Vector3dc position = position();
        double x = -position.x();
        double y = -position.y();
        double z = -position.z();
        
        this.transformation.identity().rotate(rotation()).translate(x, y, z);
        this.updateTransformation = false;
    }
    
    public @NotNull Matrix4dc transformation()
    {
        if (this.updateTransformation) updateTransformation();
        return this.transformation;
    }
    
    // -------------------- Derived -------------------- //
    
    public @NotNull Vector3dc xAxis()
    {
        return rotation().positiveX(this.xAxis);
    }
    
    public @NotNull Vector3dc yAxis()
    {
        return rotation().positiveY(this.yAxis);
    }
    
    public @NotNull Vector3dc zAxis()
    {
        return rotation().positiveZ(this.zAxis);
    }
    
    public @NotNull Vector3dc right()
    {
        return this.right.set(xAxis());
    }
    
    public @NotNull Vector3dc up()
    {
        return this.up.set(yAxis());
    }
    
    public @NotNull Vector3dc front()
    {
        return this.front.set(zAxis()).negate();
    }
}
