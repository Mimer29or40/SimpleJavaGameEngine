#version 440 core

struct VertexData
{
    vec2 TexCoord;
};

in VertexData vs_out;

out vec4 FragColor;

uniform sampler2D fontTexture;
uniform vec4 fontColor;

void main()
{
    vec4 textureColor = texture(fontTexture, vs_out.TexCoord);
    if (textureColor.r < 0.5) discard;
    FragColor = fontColor;
}