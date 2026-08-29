#version 150

uniform float Time;
uniform float TunnelSpeed;
uniform float TunnelBrightness;
uniform float TunnelDensity;
uniform float TunnelFov;
uniform vec4 SpriteBounds;
uniform float ViewYaw;
uniform float ViewPitch;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

mat2 rot(float a) {
    float c = cos(a), s = sin(a);
    return mat2(c, s, -s, c);
}

const float pi = acos(-1.0);
const float pi2 = pi * 2.0;

vec2 pmod(vec2 p, float r) {
    float a = atan(p.x, p.y) + pi / r;
    float n = pi2 / r;
    a = floor(a / n) * n;
    return p * rot(-a);
}

float box(vec3 p, vec3 b) {
    vec3 d = abs(p) - b;
    return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0));
}

float ifsBox(vec3 p, float time) {
    for (int i = 0; i < 5; i++) {
        p = abs(p) - 1.0;
        p.xy *= rot(time * 0.3);
        p.xz *= rot(time * 0.1);
    }
    p.xz *= rot(time);
    return box(p, vec3(0.4, 0.8, 0.3));
}

float map(vec3 p, float time) {
    vec3 p1 = p;
    p1.x = mod(p1.x - 5.0, 10.0) - 5.0;
    p1.y = mod(p1.y - 5.0, 10.0) - 5.0;
    p1.z = mod(p1.z, 16.0) - 8.0;
    p1.xy = pmod(p1.xy, 5.0);
    return ifsBox(p1, time);
}

void main() {
    vec2 uv = (texCoord0 - SpriteBounds.xy) / max(SpriteBounds.zw, vec2(0.0001));
    vec2 p = (uv * 2.0 - 1.0) * TunnelFov;
    float time = Time * TunnelSpeed;

    vec3 cPos = vec3(0.0, 0.0, -3.0 * time);
    float cy = cos(ViewYaw);
    float sy = sin(ViewYaw);
    float cp = cos(ViewPitch);
    float sp = sin(ViewPitch);
    vec3 cDir = normalize(vec3(sy * cp, sp, -cy * cp));
    vec3 cUp = vec3(0.0, 1.0, 0.0);
    vec3 cSide = normalize(cross(cDir, cUp));
    cUp = normalize(cross(cSide, cDir));

    vec3 ray = normalize(cSide * p.x + cUp * p.y + cDir);

    float acc = 0.0;
    float acc2 = 0.0;
    float t = 0.0;
    for (int i = 0; i < 99; i++) {
        vec3 pos = cPos + ray * t;
        float dist = map(pos, time);
        dist = max(abs(dist), 0.02);
        float a = exp(-dist * 3.0 * TunnelDensity);
        if (mod(length(pos) + 24.0 * time, 30.0) < 3.0) {
            a *= 2.0;
            acc2 += a;
        }
        acc += a;
        t += dist * 0.5;
    }

    vec3 col = vec3(
            acc * 0.01,
            acc * 0.011 + acc2 * 0.002,
            acc * 0.012 + acc2 * 0.005) * TunnelBrightness;
    fragColor = vec4(col * vertexColor.rgb, 1.0);
}
