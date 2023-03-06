#version 440 core
layout (location = 0) in vec4 aPos;
layout (location = 2) in vec4 aColor0;
layout (location = 3) in vec4 aColor1;

struct VSOut
{
    vec2 Pos;
    vec2 Size;
    vec4 ColorInner;
    vec4 ColorOuter;
};

out VSOut vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Pos = aPos.xy;
    vs_out.Size = aPos.zw;
    vs_out.ColorInner = aColor0;
    vs_out.ColorOuter = aColor1;
}
