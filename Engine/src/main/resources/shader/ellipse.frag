#version 440 core

struct GSOut
{
    vec2 Coord;
    vec4 ColorInner;
    vec4 ColorOuter;
};

in GSOut gs_out;

out vec4 FragColor;

void main()
{
    float centerDist = dot(gs_out.Coord, gs_out.Coord);
    if (centerDist > 1.0) discard;
    FragColor = (1.0 - centerDist) * gs_out.ColorInner + centerDist * gs_out.ColorOuter;
}
