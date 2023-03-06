#version 440 core

struct GSOut
{
    vec2 Coord;
    vec4 Color;
};

in GSOut gs_out;

out vec4 FragColor;

void main()
{
    if (dot(gs_out.Coord, gs_out.Coord) > 1.0) discard;
    FragColor = vec4(gs_out.Coord, 1.0, 1.0);
    FragColor = gs_out.Color;
}
