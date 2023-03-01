package LearnOpenGL;

import org.joml.Vector2d;
import org.joml.Vector3d;

public final class MeshVertex
{
    public final Vector3d Position  = new Vector3d();
    public final Vector3d Normal    = new Vector3d();
    public final Vector2d TexCoords = new Vector2d();
    public final Vector3d Tangent   = new Vector3d();
    public final Vector3d Bitangent = new Vector3d();
    
    public final int[]    BoneIDs = new int[4];
    public final double[] Weights = new double[4];
}
