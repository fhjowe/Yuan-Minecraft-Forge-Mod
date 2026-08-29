#version 150

#define MAX_STEPS 80.0
#define MAX_DIST  8.0
#define SURF_EPS  0.003

uniform vec4 ColorModulator;
uniform float time;
uniform vec2 screenSize;

in vec4 vertexColor;
in vec3 vertexPos;
in vec3 vertexNormal;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(23.43, 41.12))) * 12345.678);
}

float n2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.2;
    for(int i = 0; i < 5; i++) {
        v += a * n2(p.xy);
        p = p * 0.5 + 0.5;
        a *= 0.6;
    }
    return v;
}

float mapSlime(vec3 p)
{
    p.xy += fbm(p * 25.0 + time * 1.2) * 0.2;
    float s = length(p) - 1.75;
    s += fbm(p * 12.0 - time) * 1.45;

    return s;
}

vec3 getNormal(vec3 p) {
    float d = mapSlime(p);
    vec2 e = vec2(0.001, 0.0);
    return normalize(vec3(
                     mapSlime(p + e.xyy) - d,
                     mapSlime(p + e.yxy) - d,
                     mapSlime(p + e.yyx) - d));
}

vec3 iridescentColor(float angle) {
    float r = sin(angle * 30.0 + 0.0) * 0.5 + 0.5;
    float g = sin(angle * 30.0 + 2.0) * 0.5 + 0.5;
    float b = sin(angle * 30.0 + 4.0) * 0.5 + 0.5;
    return vec3(r, g, b);
}

float raymarch(vec3 ro, vec3 rd, out vec3 p) {
    float t = 0.0;
    for(int i = 0; i < int(MAX_STEPS); i++) {
        p = ro + rd * t;
        float d = mapSlime(p);
        if(d < SURF_EPS) return t;
        if(t > MAX_DIST) break;
        t += d * 0.8;
    }
    return 999.0;
}

void main()
{
    vec2 fragCoord = gl_FragCoord.xy;
    vec2 uv = (fragCoord - 0.5 * screenSize) / screenSize.y * 1.2;

    vec3 ro = vec3(0.0, 0.0, 2.3);
    vec3 rd = normalize(vec3(uv, -1.5));

    vec3 p;
    float t = raymarch(ro, rd, p);

    vec3 col = vec3(0.0);

    if(t < 900.0) {
        vec3 n = getNormal(p);

        vec3 inside = vec3(0.792, 0.804, 0.808) * fbm(p * 6.0 + time);
        inside *= 1.2;

        float angle = dot(n, -rd);
        vec3 iri = iridescentColor(angle) * pow(1.0 - angle, 2.0);

        col = inside * 0.5 + iri * 0.8;

        float fresnel = pow(1.0 - angle, 3.0);
        col += fresnel * iri * 1.2;
    }

    vec3 bg = vec3(0.05, 0.07, 0.12);
    col = mix(bg, col, 0.95);

    col *= vertexColor.rgb * ColorModulator.rgb;

    fragColor = vec4(col, vertexColor.a * ColorModulator.a);
}
