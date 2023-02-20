package engine.renderer;

import engine.Image;
import engine.color.Color;
import engine.color.Colorc;
import engine.font.CharData;
import engine.font.Font;
import engine.font.TextAlign;
import engine.gl.Framebuffer;
import engine.gl.GL;
import engine.gl.GLType;
import engine.gl.Winding;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.shader.Shader;
import engine.gl.shader.ShaderType;
import engine.gl.texture.Texture;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import engine.util.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class TextRenderer
{
    protected FloatBuffer posBuffer;
    protected FloatBuffer texBuffer;
    protected VertexArray vertexArray;
    protected int         quadCount;
    
    protected Program program;
    
    protected final Matrix4d view = new Matrix4d();
    
    protected Font font;
    
    protected double    size  = 24.0;
    protected Color     color = new Color(Color.WHITE);
    protected TextAlign align = TextAlign.TOP_LEFT;
    
    public TextRenderer()
    {
        int quadCount = 8192;
        
        int vertexCount = quadCount * 4; // 4 vertices per quads
        
        this.posBuffer = MemoryUtil.memAllocFloat(vertexCount * 3); // 3 floats per position
        this.texBuffer = MemoryUtil.memAllocFloat(vertexCount * 2); // 2 floats per texcoord
        
        IntBuffer indices = MemoryUtil.memCallocInt(quadCount * 6); // 6 indices per quad
        for (int i = 0; i < quadCount; ++i)
        {
            indices.put(4 * i);
            indices.put(4 * i + 1);
            indices.put(4 * i + 2);
            indices.put(4 * i);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
        }
        
        VertexAttribute pos = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute tex = new VertexAttribute(GLType.FLOAT, 2, false);
        
        this.vertexArray = VertexArray.builder()
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.posBuffer, pos)
                                      .buffer(BufferUsage.DYNAMIC_DRAW, this.texBuffer, tex)
                                      .indexBuffer(BufferUsage.STATIC_DRAW, indices.clear())
                                      .build();
        
        MemoryUtil.memFree(indices);
        
        String vertCode = """
                          #version 440
                          in vec3 aPos;
                          in vec2 aTex;
                          out vec2 tex;
                          uniform mat4 view;
                          void main()
                          {
                              gl_Position = view * vec4(aPos, 1.0);
                              tex = aTex;
                          }
                          """;
        String fragCode = """
                          #version 440
                          in vec2 tex;
                          out vec4 FragColor;
                          uniform sampler2D fontTexture;
                          void main()
                          {
                              FragColor = texture(fontTexture, tex);
                              FragColor = vec4(1.0);
                          }
                          """;
        
        Shader vertShader = new Shader(ShaderType.VERTEX, vertCode);
        Shader fragShader = new Shader(ShaderType.FRAGMENT, fragCode);
        
        this.program = new Program(vertShader, fragShader);
    
        vertShader.delete();
        fragShader.delete();
        
        this.font = new Font(IOUtil.getPath("font/PressStart2P/PressStart2P.ttf"), true, false, false);
    }
    
    public void delete()
    {
        MemoryUtil.memFree(this.posBuffer);
        MemoryUtil.memFree(this.texBuffer);
        this.vertexArray.delete();
        this.program.delete();
    
        this.font.delete();
    }
    
    public double textSize()
    {
        return this.size;
    }
    
    public void textSize(double size)
    {
        this.size = size;
    }
    
    public @NotNull Colorc textColor()
    {
        return this.color;
    }
    
    public void textColor(@NotNull Colorc color)
    {
        this.color.set(color);
    }
    
    public @NotNull TextAlign textAlign()
    {
        return this.align;
    }
    
    public void textAlign(@NotNull TextAlign align)
    {
        this.align = align;
    }
    
    public double textWidth(@NotNull String text, int maxWidth)
    {
        return this.font.textWidth(text, this.size, maxWidth);
    }
    
    public double textWidth(@NotNull String text)
    {
        return this.font.textWidth(text, this.size);
    }
    
    public double textHeight(@NotNull String text, int maxWidth)
    {
        return this.font.textHeight(text, this.size, maxWidth);
    }
    
    public double textHeight(@NotNull String text)
    {
        return this.font.textHeight(text, this.size);
    }
    
    protected void drawLine(@NotNull String line, double x, double y)
    {
        double scale = this.font.scale(this.size);
        
        CharData prevChar = null, currChar;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            char character = line.charAt(i);
            
            currChar = this.font.charData.get(character);
            
            x += this.font.kerningAdvanceUnscaled(prevChar, currChar) * scale;
            
            double x0 = x + currChar.x0Unscaled() * scale;
            double y0 = y + currChar.y0Unscaled() * scale;
            double x1 = x + currChar.x1Unscaled() * scale;
            double y1 = y + currChar.y1Unscaled() * scale;
            
            this.posBuffer.put((float) x0).put((float) y0).put(0F);
            this.texBuffer.put((float) currChar.u0()).put((float) currChar.v0());
            
            this.posBuffer.put((float) x0).put((float) y1).put(0F);
            this.texBuffer.put((float) currChar.u0()).put((float) currChar.v1());
            
            this.posBuffer.put((float) x1).put((float) y1).put(0F);
            this.texBuffer.put((float) currChar.u1()).put((float) currChar.v1());
            
            this.posBuffer.put((float) x1).put((float) y0).put(0F);
            this.texBuffer.put((float) currChar.u1()).put((float) currChar.v0());
            
            this.quadCount++;
            
            x += currChar.advanceWidthUnscaled() * scale;
            
            prevChar = currChar;
        }
        // Correct increment formula would be: depthInc = (zFar - zNear)/pow(2, bits)
        //this.currentDepth -= 0.00005; // TODO
    
        GL.winding(Winding.CCW);
        
        Framebuffer fb = Framebuffer.get();
        
        Texture.bind(this.font.texture, 0);
        
        Program.bind(this.program);
        Program.uniformInt("fontTexture", 0);
        Program.uniformMatrix4("view", false, this.view.setOrtho(0, fb.width(), fb.height(), 0, -1, 1));
        
        VertexArray.bind(this.vertexArray);
        this.vertexArray.buffer(0).set(0, this.posBuffer.flip());
        this.vertexArray.buffer(1).set(0, this.texBuffer.flip());
        this.vertexArray.drawElements(DrawMode.TRIANGLES, 0, this.quadCount * 6);
        
        this.posBuffer.clear();
        this.texBuffer.clear();
    
        this.quadCount = 0;
        
        //this.currentDepth = 0.99995; // TODO
    }
    
    public void drawText(@NotNull String text, double x, double y, double width, double height)
    {
        List<String> lines = this.font.splitText(text, this.size, width);
        
        double actualHeight = this.font.textHeight(text, this.size);
        
        int hPos = this.align.getH(), vPos = this.align.getV();
        
        double yOffset = vPos == -1 ? 0 : vPos == 0 ? 0.5 * (height - actualHeight) : height - actualHeight;
        for (String line : lines)
        {
            double lineWidth  = this.font.textWidth(line, this.size);
            double lineHeight = this.font.textHeight(line, this.size);
            
            double xOffset = hPos == -1 ? 0 : hPos == 0 ? 0.5 * (width - lineWidth) : width - lineWidth;
            
            drawLine(line, x + xOffset, y + yOffset);
            
            yOffset += lineHeight;
        }
    }
    
    public void drawText(@NotNull String text, double x, double y)
    {
        drawText(text, x, y, 0, 0);
    }
}
