#version 440 core

struct VertexData
{
    vec4 Color;
};

in VertexData gs_out;

out vec4 FragColor;

void main()
{
    FragColor = gs_out.Color;
}
