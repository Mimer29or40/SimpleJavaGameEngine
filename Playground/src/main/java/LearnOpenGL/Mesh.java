package LearnOpenGL;

import engine.gl.GLType;
import engine.gl.buffer.BufferUsage;
import engine.gl.shader.Program;
import engine.gl.texture.Texture;
import engine.gl.texture.Texture2D;
import engine.gl.vertex.DrawMode;
import engine.gl.vertex.VertexArray;
import engine.gl.vertex.VertexAttribute;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Mesh
{
    public final List<MeshVertex>  vertices;
    public final List<Integer>     indices;
    public final List<MeshTexture> textures;
    
    public final VertexArray VAO;
    
    public Mesh(@NotNull List<MeshVertex> vertices, @NotNull List<Integer> indices, @NotNull List<MeshTexture> textures)
    {
        this.vertices = vertices;
        this.indices  = indices;
        this.textures = textures;
    
        VertexAttribute position  = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute normal    = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute texCoords = new VertexAttribute(GLType.FLOAT, 2, false);
        VertexAttribute tangent   = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute bitangent = new VertexAttribute(GLType.FLOAT, 3, false);
        VertexAttribute boneIds   = new VertexAttribute(GLType.INT, 4, false);
        VertexAttribute weights   = new VertexAttribute(GLType.FLOAT, 4, false);
    
        int        bufferSize = position.size() + normal.size() + texCoords.size() + tangent.size() + bitangent.size() + boneIds.size() + weights.size();
        ByteBuffer buffer     = MemoryUtil.memAlloc(bufferSize * vertices.size());
        for (MeshVertex vertex : vertices)
        {
            buffer.putFloat((float) vertex.Position.x).putFloat((float) vertex.Position.y).putFloat((float) vertex.Position.z);
            buffer.putFloat((float) vertex.Normal.x).putFloat((float) vertex.Normal.y).putFloat((float) vertex.Normal.z);
            buffer.putFloat((float) vertex.TexCoords.x).putFloat((float) vertex.TexCoords.y);
            buffer.putFloat((float) vertex.Tangent.x).putFloat((float) vertex.Tangent.y).putFloat((float) vertex.Tangent.z);
            buffer.putFloat((float) vertex.Bitangent.x).putFloat((float) vertex.Bitangent.y).putFloat((float) vertex.Bitangent.z);
            buffer.putInt(vertex.BoneIDs[0]).putInt(vertex.BoneIDs[1]).putInt(vertex.BoneIDs[2]).putInt(vertex.BoneIDs[3]);
            buffer.putFloat((float) vertex.Weights[0]).putFloat((float) vertex.Weights[1]).putFloat((float) vertex.Weights[2]).putFloat((float) vertex.Weights[3]);
        }
    
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.size());
        for (Integer index : indices) indexBuffer.put(index);
    
        this.VAO = VertexArray.builder()
                              .buffer(BufferUsage.STATIC_DRAW, buffer.clear(), position, normal, texCoords, tangent, bitangent, boneIds, weights)
                              .indexBuffer(BufferUsage.STATIC_DRAW, indexBuffer.clear())
                              .build();
        
        MemoryUtil.memFree(buffer);
        MemoryUtil.memFree(indexBuffer);
    }
    
    public void draw()
    {
        // bind appropriate textures
        int diffuseNr  = 1;
        int specularNr = 1;
        int normalNr   = 1;
        int heightNr   = 1;
        for (int i = 0, n = textures.size(); i < n; i++)
        {
            // retrieve texture number (the N in diffuse_textureN)
            String name   = textures.get(i).type;
            int    number = 0;
            switch (name)
            {
                case "texture_diffuse" -> number = diffuseNr++;
                case "texture_specular" -> number = specularNr++;
                case "texture_normal" -> number = normalNr++;
                case "texture_height" -> number = heightNr++;
            }
            
            // now set the sampler to the correct texture unit
            Program.uniformInt(name + number, i);
            //active proper texture unit and finally bind the texture
            Texture.bind(textures.get(i).texture, i);
        }
        
        // draw mesh
        VAO.drawElements(DrawMode.TRIANGLES, indices.size());
        
        // always good practice to set everything back to defaults once configured.
        Texture.bind(Texture2D.NULL, 0);
    }
}
