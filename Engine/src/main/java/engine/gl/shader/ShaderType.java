package engine.gl.shader;

import static org.lwjgl.opengl.GL44.*;

public enum ShaderType
{
    VERTEX(GL_VERTEX_SHADER),
    GEOMETRY(GL_GEOMETRY_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER),
    COMPUTE(GL_COMPUTE_SHADER),
    TESS_CONTROL(GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION(GL_TESS_EVALUATION_SHADER),
    ;
    
    public final int ref;
    
    ShaderType(int ref)
    {
        this.ref = ref;
    }
}
