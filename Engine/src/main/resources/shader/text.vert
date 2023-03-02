#version 440 core
layout (location = 0) in vec3 aPos;
layout (location = 3) in vec2 aTexCoord;

struct VertexData
{
    vec2 TexCoord;
};

out VertexData vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.TexCoord = aTexCoord;
    gl_Position = view * vec4(aPos, 1.0);
}
