#version 440 core

struct GSOut
{
    vec2 Coord;
    vec4 ColorTL;
    vec4 ColorTR;
    vec4 ColorBL;
    vec4 ColorBR;
};

in GSOut gs_out;

out vec4 FragColor;

void main()
{
    float tx = gs_out.Coord.x;
    float ty = gs_out.Coord.y;
    float txInv = 1.0 - tx;
    float tyInv = 1.0 - ty;

    vec4 colorL = tyInv * gs_out.ColorTL + ty * gs_out.ColorBL;
    vec4 colorR = tyInv * gs_out.ColorTR + ty * gs_out.ColorBR;

    FragColor = txInv * colorL + tx * colorR;
}
