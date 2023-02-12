#version 330

in vec3 fragTexCoord;
in vec4 fragColor;

out vec4 finalColor;

uniform sampler2D texture0;

void main()
{
    // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-1/
    // TODO - http://www.reedbeta.com/blog/quadrilateral-interpolation-part-2/
    vec4 texelColor = textureProj(texture0, fragTexCoord);
    finalColor = texelColor * fragColor;
}
