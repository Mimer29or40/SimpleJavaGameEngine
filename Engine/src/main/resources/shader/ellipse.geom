#version 440 core
out vec4 FragColor;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct VSOut
{
    vec2 Pos;
    vec2 Size;
    vec4 ColorInner;
    vec4 ColorOuter;
};

struct GSOut
{
    vec2 Coord;
    vec4 ColorInner;
    vec4 ColorOuter;
};

in VSOut vs_out[];

out GSOut gs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vec2 _size = vs_out[0].Size * 0.5;

    // Generates Line Strip
    gl_Position = view * vec4(vs_out[0].Pos + vec2(-_size.x, -_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(-1, -1);
    gs_out.ColorInner = vs_out[0].ColorInner;
    gs_out.ColorOuter = vs_out[0].ColorOuter;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+_size.x, -_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(+1, -1);
    gs_out.ColorInner = vs_out[0].ColorInner;
    gs_out.ColorOuter = vs_out[0].ColorOuter;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(-_size.x, +_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(-1, +1);
    gs_out.ColorInner = vs_out[0].ColorInner;
    gs_out.ColorOuter = vs_out[0].ColorOuter;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+_size.x, +_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(+1, +1);
    gs_out.ColorInner = vs_out[0].ColorInner;
    gs_out.ColorOuter = vs_out[0].ColorOuter;
    EmitVertex();

    EndPrimitive();
}
