package engine.renderer;

import engine.color.Color;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.GLType;
import engine.gl.Winding;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;
import org.lwjgl.opengl.GL44;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LineRenderer
{
    protected FloatBuffer posBuffer;
    protected ByteBuffer  colBuffer;
    protected VertexArray vertexArray;
    
    protected Program program;
    
    protected final Matrix4d view = new Matrix4d();
    
    public       double thickness = 10.0;
    public final Color  color     = new Color(Color.WHITE);
    
    public LineRenderer()
    {
        int quadCount = 8192;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
        
        this.posBuffer = MemoryUtil.memAllocFloat(vertexCount * 3); // 3 floats per position
        this.colBuffer = MemoryUtil.memAlloc(vertexCount * 4);      // 4 bytes per color
        
        VertexAttribute pos = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute col = new VertexAttribute(GLType.BYTE, 4, false);
        
        this.vertexArray = VertexArray.builder()
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.posBuffer.clear(), pos)
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.colBuffer.clear(), col)
                                      .build();
        
        String vertCode = """
                          #version 440
                          in vec3 aPos;
                          in vec4 aCol;
                          out vec4 vCol;
                          uniform mat4 view;
                          void main()
                          {
                              gl_Position = view * vec4(aPos, 1.0);
                              vCol = aCol;
                          }
                          """;
        
        String geomCode = """
                          #version 440
                          
                          layout(lines_adjacency) in;
                          layout(triangle_strip, max_vertices = 7) out;
                          
                          in vec4 vCol[];
                          
                          out vec4 fCol;
                          
                          uniform ivec2 viewport;
                          uniform float thickness;
                          
                          vec3 toScreenSpace(vec4 v)
                          {
                              return vec3(v.xy / v.w * viewport, (v.z - 0.001) / v.w);
                          }
                          
                          void main(void)
                          {
                              vec3 p[4];
                              p[0] = toScreenSpace(gl_in[0].gl_Position);
                              p[1] = toScreenSpace(gl_in[1].gl_Position);
                              p[2] = toScreenSpace(gl_in[2].gl_Position);
                              p[3] = toScreenSpace(gl_in[3].gl_Position);
                              
                              // Perform Naive Culling
                              vec2 area = viewport * 4;
                              if (p[1].x < -area.x || p[1].x > area.x) return;
                              if (p[1].y < -area.y || p[1].y > area.y) return;
                              if (p[2].x < -area.x || p[2].x > area.x) return;
                              if (p[2].y < -area.y || p[2].y > area.y) return;
                              
                              // Determines the normals for the first two line segments
                              vec2 v0 = p[1].xy - p[0].xy;
                              vec2 v1 = p[2].xy - p[1].xy;
                              vec2 v2 = p[3].xy - p[2].xy;
                              
                              if (length(v0) < 0.000001) v0 = v1;
                              if (length(v2) < 0.000001) v2 = v1;
                              
                              vec2 v0u = normalize(v0);
                              vec2 v1u = normalize(v1);
                              vec2 v2u = normalize(v2);
                              
                              vec2 n0u = vec2(-v0u.y, v0u.x);
                              vec2 n1u = vec2(-v1u.y, v1u.x);
                              vec2 n2u = vec2(-v2u.y, v2u.x);
                              
                              vec2 n0 = thickness * n0u;
                              vec2 n1 = thickness * n1u;
                              vec2 n2 = thickness * n2u;
                              
                              vec2 t1 = normalize(v0u + v1u);
                              vec2 t2 = normalize(v1u + v2u);
                              
                              vec2 m1 = vec2(-t1.y, t1.x);
                              vec2 m2 = vec2(-t2.y, t2.x);
                              m1 *= min(thickness / dot(m1, n1u), min(length(v0), length(v1)));
                              m2 *= min(thickness / dot(m2, n2u), min(length(v1), length(v2)));
                              
                              // Determines location of bevel
                              vec2 bevelP11, bevelP12, bevelP21, miterP1, miterP2;
                              if (dot(v0u, n1u) > 0) {
                                  bevelP11 = p[1].xy + n0;
                                  bevelP12 = p[1].xy + n1;
                                  miterP1 = p[1].xy - m1;
                              }
                              else {
                                  bevelP11 = p[1].xy - n0;
                                  bevelP12 = p[1].xy - n1;
                                  miterP1 = p[1].xy + m1;
                              }
                              if (dot(v1u, n2u) > 0) {
                                  bevelP21 = p[2].xy + n1;
                                  miterP2 = p[2].xy - m2;
                              }
                              else {
                                  bevelP21 = p[2].xy - n1;
                                  miterP2 = p[2].xy + m2;
                              }
                              // Generates Bevel at Joint
                              gl_Position = vec4(bevelP11 / viewport, p[1].z, 1.0);
                              fCol = vCol[1];
                              EmitVertex();
                              
                              gl_Position = vec4(bevelP12 / viewport, p[1].z, 1.0);
                              fCol = vCol[1];
                              EmitVertex();
                              
                              // This need to be the bottom join point
                              gl_Position = vec4(miterP1 / viewport, p[1].z, 1.0);
                              fCol = vCol[1];
                              EmitVertex();
                              
                              EndPrimitive();
                              
                              // Generates Line Strip
                              gl_Position = vec4(bevelP12 / viewport, p[1].z, 1.0);
                              fCol = vCol[1];
                              EmitVertex();
                              
                              gl_Position = vec4(miterP1 / viewport, p[1].z, 1.0);
                              fCol = vCol[1];
                              EmitVertex();
                              
                              if (dot(v0u, n1u) > 0 ^^ dot(v1u, n2u) > 0) {
                                  gl_Position = vec4(miterP2 / viewport, p[2].z, 1.0);
                                  fCol = vCol[2];
                                  EmitVertex();
                                  
                                  gl_Position = vec4(bevelP21 / viewport, p[2].z, 1.0);
                                  fCol = vCol[2];
                                  EmitVertex();
                              }
                              else {
                                  gl_Position = vec4(bevelP21 / viewport, p[2].z, 1.0);
                                  fCol = vCol[2];
                                  EmitVertex();
                                  
                                  gl_Position = vec4(miterP2 / viewport, p[2].z, 1.0);
                                  fCol = vCol[2];
                                  EmitVertex();
                              }
                              
                              EndPrimitive();
                          }
                          """;
        String fragCode = """
                          #version 440
                          in vec4 vCol;
                          out vec4 FragColor;
                          void main()
                          {
                              FragColor = vec4(1.0);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader geomShader = new Shader(ShaderType.GEOMETRY, geomCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        this.program = new Program(vertShader, fragShader);
        //this.program = new Program(vertShader, geomShader, fragShader);
        
        vertShader.delete();
        geomShader.delete();
        fragShader.delete();
    }
    
    public void delete()
    {
        MemoryUtil.memFree(this.posBuffer);
        MemoryUtil.memFree(this.colBuffer);
        this.vertexArray.delete();
        this.program.delete();
    }
    
    public void drawLines(double @NotNull ... points)
    {
        for (int p1 = 0, n = points.length >> 1; p1 < n; p1++)
        {
            int p0 = (p1 - 1 + n) % n;
            int p2 = (p1 + 1 + n) % n;
            int p3 = (p1 + 2 + n) % n;
            
            //this.posBuffer.put((float) points[(2 * p0)]);
            //this.posBuffer.put((float) points[(2 * p0) + 1]);
            //this.posBuffer.put(0F);
            //this.colBuffer.put((byte) color.r());
            //this.colBuffer.put((byte) color.g());
            //this.colBuffer.put((byte) color.b());
            //this.colBuffer.put((byte) color.a());
            
            this.posBuffer.put((float) points[(2 * p1)]);
            this.posBuffer.put((float) points[(2 * p1) + 1]);
            this.posBuffer.put(0F);
            this.colBuffer.put((byte) color.r());
            this.colBuffer.put((byte) color.g());
            this.colBuffer.put((byte) color.b());
            this.colBuffer.put((byte) color.a());
            
            this.posBuffer.put((float) points[(2 * p2)]);
            this.posBuffer.put((float) points[(2 * p2) + 1]);
            this.posBuffer.put(0F);
            this.colBuffer.put((byte) color.r());
            this.colBuffer.put((byte) color.g());
            this.colBuffer.put((byte) color.b());
            this.colBuffer.put((byte) color.a());
            
            //this.posBuffer.put((float) points[(2 * p3)]);
            //this.posBuffer.put((float) points[(2 * p3) + 1]);
            //this.posBuffer.put(0F);
            //this.colBuffer.put((byte) color.r());
            //this.colBuffer.put((byte) color.g());
            //this.colBuffer.put((byte) color.b());
            //this.colBuffer.put((byte) color.a());
        }
        
        Framebuffer fb = Framebuffer.get();
    
        Program.bind(this.program);
        // TODO - Make view matrix mutable
        Program.uniformMatrix4("view", false, this.view.setOrtho(0, fb.width(), fb.height(), 0, -1, 1));
        Program.uniformFloat2("viewport", fb.width(), fb.height());
        Program.uniformFloat("thickness", this.thickness);
    
        GL44.glLineWidth(10);
        
        VertexArray.bind(this.vertexArray);
        this.vertexArray.buffer(0).set(0, this.posBuffer.flip());
        this.vertexArray.buffer(1).set(0, this.colBuffer.flip());
        this.vertexArray.drawElements(DrawMode.LINE_STRIP, 0, points.length);
        
        this.posBuffer.clear();
        this.colBuffer.clear();
    }
}
