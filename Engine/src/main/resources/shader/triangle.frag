#version 440 core

struct VSOut
{
    vec4 Color;
};

in VSOut vs_out;

out vec4 FragColor;

void main()
{
    FragColor = vs_out.Color;
}
