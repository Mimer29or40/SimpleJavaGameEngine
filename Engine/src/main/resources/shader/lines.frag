#version 440 core

struct GSOut
{
    vec4 Color;
};

in GSOut gs_out;

out vec4 FragColor;

void main()
{
    FragColor = gs_out.Color;
}
