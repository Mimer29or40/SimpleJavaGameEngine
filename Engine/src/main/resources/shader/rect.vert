#version 440 core
layout (location = 0) in vec4 aPos;
layout (location = 2) in vec4 aColor0;
layout (location = 3) in vec4 aColor1;
layout (location = 4) in vec4 aColor2;
layout (location = 5) in vec4 aColor3;

struct VSOut
{
    vec2 Pos;
    vec2 Size;
    vec4 ColorTL;
    vec4 ColorTR;
    vec4 ColorBL;
    vec4 ColorBR;
};

out VSOut vs_out;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    vs_out.Pos = aPos.xy;
    vs_out.Size = aPos.zw;
    vs_out.ColorTL = aColor0;
    vs_out.ColorTR = aColor1;
    vs_out.ColorBL = aColor2;
    vs_out.ColorBR = aColor3;
}
