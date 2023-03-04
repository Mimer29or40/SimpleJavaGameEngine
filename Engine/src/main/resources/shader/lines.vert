#version 440 core
layout (location = 0) in vec2 aPos;
layout (location = 2) in vec4 aColor;

struct VertexData
{
    vec4 Color;
};

out VertexData vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Color = aColor;

    gl_Position = view * vec4(aPos, 1.0, 1.0);
}
