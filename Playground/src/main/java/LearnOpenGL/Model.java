package LearnOpenGL;

import engine.Image;
import engine.gl.texture.Texture2D;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

public class Model
{
    public final Path directory;
    
    // model data
    public final List<MeshTexture> textures_loaded = new ArrayList<>(); // stores all the textures loaded so far, optimization to make sure textures aren't loaded more than once.
    public final List<Mesh>        meshes          = new ArrayList<>();
    public       boolean           gammaCorrection = false;
    
    // constructor, expects a filepath to a 3D model.
    public Model(@NotNull Path path)
    {
        // retrieve the directory path of the filepath
        directory = path.getParent();
        
        // read file via ASSIMP
        AIScene scene = aiImportFile(path.toString(), aiProcess_Triangulate | aiProcess_GenSmoothNormals | aiProcess_FlipUVs | aiProcess_CalcTangentSpace);
        // check for errors
        if (scene == null || (scene.mFlags() & AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null) // if is Not Zero
        {
            System.err.printf("ERROR::ASSIMP:: %s%n", aiGetErrorString());
            return;
        }
        
        AINode root = scene.mRootNode();
        // process ASSIMP's root node recursively
        if (root != null) processNode(root, scene);
    }
    
    // draws the model, and thus all its meshes
    public void Draw()
    {
        for (Mesh mesh : this.meshes) mesh.draw();
    }
    
    // processes a node in a recursive fashion. Processes each individual mesh located at the node and repeats this process on its children nodes (if any).
    private void processNode(@NotNull AINode node, @NotNull AIScene scene)
    {
        PointerBuffer sceneMeshes = scene.mMeshes();
        if (sceneMeshes != null)
        {
            // process each mesh located at the current node
            IntBuffer meshes = node.mMeshes();
            if (meshes != null)
            {
                while (meshes.hasRemaining())
                {
                    // the node object only contains indices to index the actual objects in the scene.
                    // the scene contains all the data, node is just to keep stuff organized (like relations between nodes).
                    
                    AIMesh mesh = AIMesh.create(sceneMeshes.get(meshes.get()));
                    this.meshes.add(processMesh(mesh, scene));
                }
            }
        }
        
        // after we've processed all the meshes (if any) we then recursively process each of the children nodes
        PointerBuffer children = node.mChildren();
        if (children != null)
        {
            while (children.hasRemaining())
            {
                processNode(AINode.create(children.get()), scene);
            }
        }
    }
    
    private @NotNull Mesh processMesh(@NotNull AIMesh mesh, @NotNull AIScene scene)
    {
        // data to fill
        List<MeshVertex> vertices = new ArrayList<>();
    
        AIVector3D.Buffer positions     = mesh.mVertices();
        AIVector3D.Buffer normals       = mesh.mNormals();
        AIVector3D.Buffer textureCoords = mesh.mTextureCoords(0);
        AIVector3D.Buffer tangents      = mesh.mTangents();
        AIVector3D.Buffer bitangents    = mesh.mBitangents();
        
        // walk through each of the mesh's vertices
        for (int i = 0, n = mesh.mNumVertices(); i < n; i++)
        {
            MeshVertex vertex = new MeshVertex();
            
            AIVector3D position = positions.get(i);
            vertex.Position.x = position.x();
            vertex.Position.y = position.y();
            vertex.Position.z = position.z();
            
            // normals
            if (normals != null)
            {
                AIVector3D normal = normals.get(i);
                vertex.Normal.x = normal.x();
                vertex.Normal.y = normal.y();
                vertex.Normal.z = normal.z();
            }
            
            // texture coordinates
            if (textureCoords != null) // does the mesh contain texture coordinates?
            {
                // a vertex can contain up to 8 different texture coordinates. We thus make the assumption that we won't
                // use models where a vertex can have multiple texture coordinates so we always take the first set (0).
                AIVector3D textureCoord = textureCoords.get(i);
                vertex.TexCoords.x = textureCoord.x();
                vertex.TexCoords.y = textureCoord.y();
            }
            
            // tangents
            if (tangents != null)
            {
                AIVector3D tangent = tangents.get(i);
                vertex.Tangent.x = tangent.x();
                vertex.Tangent.y = tangent.y();
                vertex.Tangent.z = tangent.z();
            }
            
            // bitangents
            if (bitangents != null)
            {
                AIVector3D bitangent = bitangents.get(i);
                vertex.Bitangent.x = bitangent.x();
                vertex.Bitangent.y = bitangent.y();
                vertex.Bitangent.z = bitangent.z();
            }
            
            vertices.add(vertex);
        }
        
        List<Integer> indices = new ArrayList<>();
        
        // now wak through each of the mesh's faces (a face is a mesh its triangle) and retrieve the corresponding vertex indices.
        AIFace.Buffer faces = mesh.mFaces();
        while (faces.hasRemaining())
        {
            AIFace face = faces.get();
            // retrieve all indices of the face and store them in the indices vector
            IntBuffer faceIndices = face.mIndices();
            while (faceIndices.hasRemaining()) indices.add(faceIndices.get());
        }
        
        List<MeshTexture> textures = new ArrayList<>();
        
        PointerBuffer     materials = Objects.requireNonNull(scene.mMaterials());
        // process materials
        AIMaterial material = AIMaterial.create(materials.get(mesh.mMaterialIndex()));
        // we assume a convention for sampler names in the shaders. Each diffuse texture should be named
        // as 'texture_diffuseN' where N is a sequential number ranging from 1 to MAX_SAMPLER_NUMBER.
        // Same applies to other texture as the following list summarizes:
        // diffuse: texture_diffuseN
        // specular: texture_specularN
        // normal: texture_normalN
        
        // 1. diffuse maps
        textures.addAll(loadMaterialTextures(material, aiTextureType_DIFFUSE, "texture_diffuse"));
        // 2. specular maps
        textures.addAll(loadMaterialTextures(material, aiTextureType_SPECULAR, "texture_specular"));
        // 3. normal maps
        textures.addAll(loadMaterialTextures(material, aiTextureType_HEIGHT, "texture_normal"));
        // 4. height maps
        textures.addAll(loadMaterialTextures(material, aiTextureType_AMBIENT, "texture_height"));
        
        // return a mesh object created from the extracted mesh data
        return new Mesh(vertices, indices, textures);
    }
    
    // checks all material textures of a given type and loads the textures if they're not loaded yet.
    // the required info is returned as a Texture struct.
    private @NotNull List<MeshTexture> loadMaterialTextures(@NotNull AIMaterial mat, int type, @NotNull String typeName)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            List<MeshTexture> textures = new ArrayList<>();
            for (int i = 0, n = aiGetMaterialTextureCount(mat, type); i < n; i++)
            {
                AIString aiString = AIString.malloc(stack);
                aiGetMaterialTexture(mat, type, i, aiString, (IntBuffer) null, null, null, null, null, null);
                String str = aiString.dataString();
                // check if texture was loaded before and if so, continue to next iteration: skip loading a new texture
                boolean skip = false;
                for (MeshTexture meshTexture : textures_loaded)
                {
                    if (meshTexture.path.equals(str))
                    {
                        textures.add(meshTexture);
                        skip = true; // a texture with the same filepath has already been loaded, continue to next one. (optimization)
                        break;
                    }
                }
                if (!skip)
                {   // if texture hasn't been loaded already, load it
                    MeshTexture texture = new MeshTexture();
                    
                    Image image = new Image(this.directory.resolve(str));
                    
                    texture.texture = new Texture2D(image);
                    texture.texture.genMipmaps();
                    texture.type    = typeName;
                    texture.path    = str;
                    textures.add(texture);
                    textures_loaded.add(texture);  // store it as texture loaded for entire model, to ensure we won't unnecessary load duplicate textures.
                    
                    image.delete();
                }
            }
            return textures;
        }
    }
}
