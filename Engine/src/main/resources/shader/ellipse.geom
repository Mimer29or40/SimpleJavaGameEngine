#version 440 core
out vec4 FragColor;

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

struct VertexData
{
    vec2 Pos;
    vec2 Coord;
};

in VertexData vs_out[];

out VertexData gs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

uniform vec2 size;

void main()
{
    vec2 _size = size * 0.5;

    // Generates Line Strip
    gl_Position = view * vec4(vs_out[0].Pos + vec2(-_size.x, -_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(-1, -1);
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+_size.x, -_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(+1, -1);
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(-_size.x, +_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(-1, +1);
    EmitVertex();

    gl_Position = view * vec4(vs_out[0].Pos + vec2(+_size.x, +_size.y), 0.0, 1.0);
    gs_out.Coord = vec2(+1, +1);
    EmitVertex();

    EndPrimitive();
}
