#version 440 core
out vec4 FragColor;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct VertexData
{
    vec2 Coord;
};

out VertexData gs_out;

uniform ivec2 viewport;
uniform float size;

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
    float _size = size * 0.5;

    vec4 p[1];
    p[0] = toScreenSpace(gl_in[0].gl_Position);

    // Generates Line Strip
    gl_Position = toClipSpace(p[0] + vec4(-_size, -_size, 0, 0));
    gs_out.Coord = vec2(-1, -1);
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(+_size, -_size, 0, 0));
    gs_out.Coord = vec2(+1, -1);
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(-_size, +_size, 0, 0));
    gs_out.Coord = vec2(-1, +1);
    EmitVertex();

    gl_Position = toClipSpace(p[0] + vec4(+_size, +_size, 0, 0));
    gs_out.Coord = vec2(+1, +1);
    EmitVertex();

    EndPrimitive();
}
