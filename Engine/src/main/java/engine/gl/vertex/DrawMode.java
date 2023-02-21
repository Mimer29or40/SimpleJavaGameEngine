package engine.gl.vertex;

import static org.lwjgl.opengl.GL44.*;

public enum DrawMode
{
    POINTS(GL_POINTS),
    LINE_STRIP(GL_LINE_STRIP),
    LINE_STRIP_ADJACENCY(GL_LINE_STRIP_ADJACENCY),
    LINE_LOOP(GL_LINE_LOOP),
    LINES(GL_LINES),
    LINES_ADJACENCY(GL_LINES_ADJACENCY),
    TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
    TRIANGLE_STRIP_ADJACENCY(GL_TRIANGLE_STRIP_ADJACENCY),
    TRIANGLE_FAN(GL_TRIANGLE_FAN),
    TRIANGLES(GL_TRIANGLES),
    TRIANGLES_ADJACENCY(GL_TRIANGLES_ADJACENCY),
    QUADS(GL_QUADS),
    PATCHES(GL_PATCHES),
    ;
    
    public static final DrawMode DEFAULT = TRIANGLES;
    
    public final int ref;
    
    DrawMode(int ref)
    {
        this.ref = ref;
    }
}
