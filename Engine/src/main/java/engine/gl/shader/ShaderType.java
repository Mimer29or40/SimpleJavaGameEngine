package engine.gl.shader;

import org.lwjgl.opengl.GL44;

public enum ShaderType
{
    VERTEX(GL44.GL_VERTEX_SHADER),
    GEOMETRY(GL44.GL_GEOMETRY_SHADER),
    FRAGMENT(GL44.GL_FRAGMENT_SHADER),
    COMPUTE(GL44.GL_COMPUTE_SHADER),
    TESS_CONTROL(GL44.GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION(GL44.GL_TESS_EVALUATION_SHADER),
    ;
    
    public final int ref;
    
    ShaderType(int ref)
    {
        this.ref = ref;
    }
}
