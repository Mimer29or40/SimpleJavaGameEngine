#version 330

in vec3 POSITION;
in vec3 TEXCOORD;
in vec4 COLOR;

out vec3 fragTexCoord;
out vec4 fragColor;

uniform mat4 MATRIX_MVP;

void main()
{
    gl_Position = MATRIX_MVP * vec4(POSITION, 1.0);
    fragTexCoord = TEXCOORD;
    fragColor = COLOR;
}
