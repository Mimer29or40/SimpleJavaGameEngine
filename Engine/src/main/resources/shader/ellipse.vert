#version 440 core
layout (location = 0) in vec2 aPos;

struct VertexData
{
    vec2 Pos;
    vec2 Coord;
};

out VertexData vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Pos = aPos;
}
