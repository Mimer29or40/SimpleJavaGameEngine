#version 440 core

struct VertexData
{
    vec2 Coord;
};

in VertexData gs_out;

out vec4 FragColor;

uniform vec4 color;

void main()
{
    if (dot(gs_out.Coord, gs_out.Coord) > 1.0) discard;
    FragColor = vec4(gs_out.Coord, 1.0, 1.0);
    FragColor = color;
}
