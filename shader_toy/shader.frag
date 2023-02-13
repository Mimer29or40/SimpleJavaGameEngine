#version 440 core

uniform float TIME;
uniform vec2 WINDOW_FRAMEBUFFER_SIZE;

in vec4 gl_FragCoord;

out vec4 FragColor;

void main()
{
    vec2 uv = gl_FragCoord.xy / WINDOW_FRAMEBUFFER_SIZE;

    FragColor = vec4(uv, sin(TIME) * 0.5 + 0.5, 1.0f);
}
