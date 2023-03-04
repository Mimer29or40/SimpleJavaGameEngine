#version 440 core

struct VertexData
{
    vec3 Pos;
    vec3 Tan;
    vec3 Bitan;
    vec2 Coord;
};

in VertexData gs_out;

out vec4 FragColor;

uniform vec4 colorInner;
uniform vec4 colorOuter;

void main()
{
    float centerDist = dot(gs_out.Coord, gs_out.Coord);
    if (centerDist > 1.0) discard;
    FragColor = vec4(gs_out.Coord, 1.0, 1.0);
    FragColor = (1.0 - centerDist) * colorInner + centerDist * colorOuter;
}
