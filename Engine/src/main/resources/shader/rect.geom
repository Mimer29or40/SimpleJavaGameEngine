#version 440 core
out vec4 FragColor;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct VSOut
{
    vec2 Pos;
    vec2 Size;
    vec4 ColorTL;
    vec4 ColorTR;
    vec4 ColorBL;
    vec4 ColorBR;
};

struct GSOut
{
    vec2 Coord;
    vec4 ColorTL;
    vec4 ColorTR;
    vec4 ColorBL;
    vec4 ColorBR;
};

in VSOut vs_out[];

out GSOut gs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vec2 size = vs_out[0].Size * 0.5;

    gl_Position = view * vec4(vs_out[0].Pos + vec2(-size.x, -size.y), 0.0, 1.0);
    gs_out.Coord = vec2(0, 0);
    gs_out.ColorTL = vs_out[0].ColorTL;
    gs_out.ColorTR = vs_out[0].ColorTR;
    gs_out.ColorBL = vs_out[0].ColorBL;
    gs_out.ColorBR = vs_out[0].ColorBR;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+size.x, -size.y), 0.0, 1.0);
    gs_out.Coord = vec2(1, 0);
    gs_out.ColorTL = vs_out[0].ColorTL;
    gs_out.ColorTR = vs_out[0].ColorTR;
    gs_out.ColorBL = vs_out[0].ColorBL;
    gs_out.ColorBR = vs_out[0].ColorBR;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(-size.x, +size.y), 0.0, 1.0);
    gs_out.Coord = vec2(0, 1);
    gs_out.ColorTL = vs_out[0].ColorTL;
    gs_out.ColorTR = vs_out[0].ColorTR;
    gs_out.ColorBL = vs_out[0].ColorBL;
    gs_out.ColorBR = vs_out[0].ColorBR;
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+size.x, +size.y), 0.0, 1.0);
    gs_out.Coord = vec2(1, 1);
    gs_out.ColorTL = vs_out[0].ColorTL;
    gs_out.ColorTR = vs_out[0].ColorTR;
    gs_out.ColorBL = vs_out[0].ColorBL;
    gs_out.ColorBR = vs_out[0].ColorBR;
    EmitVertex();

    EndPrimitive();
}
