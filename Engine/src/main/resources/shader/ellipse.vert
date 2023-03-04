#version 440 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aTan;
layout (location = 2) in vec3 aBitan;

struct VertexData
{
    vec3 Pos;
    vec3 Tan;
    vec3 Bitan;
    vec2 Coord;
};

out VertexData vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Pos = aPos;
    vs_out.Tan = aTan;
    vs_out.Bitan = aBitan;
}
