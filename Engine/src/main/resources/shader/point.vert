#version 440 core
layout (location = 0) in vec2 aPos;

layout(std140, binding = 0) uniform View { mat4 view; };

void main()
{
    gl_Position = view * vec4(aPos, 0.0, 1.0);
}
