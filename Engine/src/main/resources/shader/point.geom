#version 440 core
out vec4 FragColor;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct VSOut
{
    float Size;
    vec4 Color;
};

struct GSOut
{
    vec2 Coord;
    vec4 Color;
};

in VSOut vs_out[];

out GSOut gs_out;

uniform ivec2 viewport;

vec4 toScreenSpace(vec4 clip)
{
    vec4 screen = vec4(clip.xyz / clip.w, clip.w);
    screen.xy = (screen.xy * 0.5 + 0.5) * viewport;
    return screen;
}

vec4 toClipSpace(vec4 screen)
{
    vec4 clip = vec4(screen);
    clip.xy = (clip.xy / viewport) * 2.0 - 1.0;
    return vec4(clip.xyz * clip.w, clip.w);
}

void main()
{
    float _size = vs_out[0].Size * 0.5;

    vec4 p[1];
    p[0] = toScreenSpace(gl_in[0].gl_Position);

    // Generates Line Strip
    gl_Position = toClipSpace(p[0] + vec4(-_size, -_size, 0, 0));
    gs_out.Coord = vec2(-1, -1);
    gs_out.Color = vs_out[0].Color;
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(+_size, -_size, 0, 0));
    gs_out.Coord = vec2(+1, -1);
    gs_out.Color = vs_out[0].Color;
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(-_size, +_size, 0, 0));
    gs_out.Coord = vec2(-1, +1);
    gs_out.Color = vs_out[0].Color;
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(+_size, +_size, 0, 0));
    gs_out.Coord = vec2(+1, +1);
    gs_out.Color = vs_out[0].Color;
    EmitVertex();

    EndPrimitive();
}
