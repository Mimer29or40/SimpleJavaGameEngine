#version 440 core
out vec4 FragColor;

layout(lines_adjacency) in;
layout(triangle_strip, max_vertices = 7) out;

struct VSOut
{
    float Thickness;
    vec4 Color;
};

struct GSOut
{
    vec4 Color;
};

in VSOut vs_out[];

out GSOut gs_out;

uniform ivec2 viewport;

vec4 toScreenSpace(vec4 clip)
{
    vec4 screen = vec4(clip.xyz / clip.w, clip.w);
    screen.xy = (screen.xy * 0.5 + 0.5) * viewport;
    return screen;
}

vec4 toClipSpace(vec4 screen)
{
    vec4 clip = vec4(screen);
    clip.xy = (clip.xy / viewport) * 2.0 - 1.0;
    return vec4(clip.xyz * clip.w, clip.w);
}

void main()
{
    vec4 p[4];
    p[0] = toScreenSpace(gl_in[0].gl_Position);
    p[1] = toScreenSpace(gl_in[1].gl_Position);
    p[2] = toScreenSpace(gl_in[2].gl_Position);
    p[3] = toScreenSpace(gl_in[3].gl_Position);

    vec2 v0 = p[1].xy - p[0].xy;
    vec2 v1 = p[2].xy - p[1].xy;
    vec2 v2 = p[3].xy - p[2].xy;

    vec2 v0u;
    vec2 v1u = normalize(v1);
    vec2 v2u;

    vec2 n0;
    vec2 n11 = vec2(-v1u.y, v1u.x) * vs_out[1].Thickness * 0.5;
    vec2 n12 = vec2(-v1u.y, v1u.x) * vs_out[2].Thickness * 0.5;
    vec2 n2;

    // Line Start
    bool buttStart = abs(v0.x) < 0.00001 && abs(v0.y) < 0.00001;
    if (buttStart)
    {
        v0u = v1u;
        n0  = n11;
    }
    else
    {
        v0u = normalize(v0);
        n0 = vec2(-v0u.y, v0u.x) * vs_out[1].Thickness * 0.5;
    }

    // Line End
    bool buttEnd = abs(v2.x) < 0.00001 && abs(v2.y) < 0.00001;
    if (buttEnd)
    {
        v2u = v1u;
        n2  = n11;
    }
    else
    {
        v2u = normalize(v2);
        n2 = vec2(-v2u.y, v2u.x) * vs_out[2].Thickness * 0.5;
    }

    // Butt Start
    vec2 o0 = vec2(0.0);
    vec2 o1 = p[1].xy + n11;
    vec2 o2 = p[1].xy - n11;

    // Generates Bevel at Joint
    bool drawBevel = !(buttStart || abs(dot(v0u, v1u)) > 0.99999);
    if (drawBevel)
    {
        if (dot(n0, v1) > 0)
        {
            o0 = p[1].xy - n0;

            float temp = ((n0.x - n11.x) * v0.y - (n0.y - n11.y) * v0.x) / (v1.x * v0.y - v1.y * v0.x);
            if ((temp * v1.x) * (temp * v1.x) + (temp * v1.y) * (temp * v1.y) < v0.x * v0.x + v0.y * v0.y)
            {
                o1 += temp * v1;
            }
        }
        else
        {
            o0 = p[1].xy + n0;

            float temp = ((n11.x - n0.x) * v0.y - (n11.y - n0.y) * v0.x) / (v1.x * v0.y - v1.y * v0.x);
            if ((temp * v1.x) * (temp * v1.x) + (temp * v1.y) * (temp * v1.y) < v0.x * v0.x + v0.y * v0.y)
            {
                o2 += temp * v1;
            }
        }
    }

    // Butt End
    vec2 o3 = p[2].xy + n12;
    vec2 o4 = p[2].xy - n12;

    // Generates Bevel at Joint
    if (!buttEnd && abs(dot(v1u, v2u)) <= 0.99999)
    {
        if (dot(n12, v2) > 0)
        {
            float temp = ((n2.x - n12.x) * v2.y - (n2.y - n12.y) * v2.x) / (v1.x * v2.y - v1.y * v2.x);
            if ((temp * v1.x) * (temp * v1.x) + (temp * v1.y) * (temp * v1.y) < v2.x * v2.x + v2.y * v2.y)
            {
                o3 += temp * v1;
            }
        }
        else
        {
            float temp = ((n12.x - n2.x) * v2.y - (n12.y - n2.y) * v2.x) / (v1.x * v2.y - v1.y * v2.x);
            if ((temp * v1.x) * (temp * v1.x) + (temp * v1.y) * (temp * v1.y) < v2.x * v2.x + v2.y * v2.y)
            {
                o4 += temp * v1;
            }
        }
    }

    if (dot(o3 - o1, v1) <= 0.0)
    {
        o1 = p[1].xy + n11;
        o3 = p[2].xy + n12;
    }

    if (dot(o4 - o2, v1) <= 0.0)
    {
        o2 = p[1].xy - n11;
        o4 = p[2].xy - n12;
    }

    if (drawBevel)
    {
        gl_Position = toClipSpace(vec4(o0, p[1].zw));
        gs_out.Color = vs_out[1].Color;
        EmitVertex();

        gl_Position = toClipSpace(vec4(o1, p[1].zw));
        gs_out.Color = vs_out[1].Color;
        EmitVertex();

        gl_Position = toClipSpace(vec4(o2, p[1].zw));
        gs_out.Color = vs_out[1].Color;
        EmitVertex();

        EndPrimitive();
    }

    // Generates Line Strip
    gl_Position = toClipSpace(vec4(o1, p[1].zw));
    gs_out.Color = vs_out[1].Color;
    EmitVertex();

    gl_Position = toClipSpace(vec4(o2, p[1].zw));
    gs_out.Color = vs_out[1].Color;
    EmitVertex();

    gl_Position = toClipSpace(vec4(o3, p[2].zw));
    gs_out.Color = vs_out[2].Color;
    EmitVertex();

    gl_Position = toClipSpace(vec4(o4, p[2].zw));
    gs_out.Color = vs_out[2].Color;
    EmitVertex();

    EndPrimitive();
}
