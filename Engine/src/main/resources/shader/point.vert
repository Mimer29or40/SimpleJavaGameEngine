#version 440 core
layout (location = 0) in vec4 aPos;
layout (location = 2) in vec4 aColor;

struct VSOut
{
    float Size;
    vec4 Color;
};

out VSOut vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Size = aPos.z;
    vs_out.Color = aColor;
    gl_Position = view * vec4(aPos.xy, 0.0, 1.0);
}
