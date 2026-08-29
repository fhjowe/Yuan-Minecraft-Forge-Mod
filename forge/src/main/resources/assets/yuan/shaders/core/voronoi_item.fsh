#version 150

// Voronoi pattern ported from @paper-design/shaders (Apache-2.0).
// Original algorithm: https://www.shadertoy.com/view/ldl3W8

uniform float Time;
uniform float VoronoiColorCount;
uniform float VoronoiStepsPerColor;
uniform vec4 VoronoiColor0;
uniform vec4 VoronoiColor1;
uniform vec4 VoronoiColor2;
uniform vec4 VoronoiColor3;
uniform vec4 VoronoiColor4;
uniform vec4 VoronoiColorGlow;
uniform vec4 VoronoiColorGap;
uniform float VoronoiDistortion;
uniform float VoronoiGap;
uniform float VoronoiGlow;
uniform float VoronoiScale;
uniform float VoronoiFov;
uniform float VoronoiSpeed;
uniform float VoronoiRotation;
uniform float VoronoiOffsetX;
uniform float VoronoiOffsetY;
uniform vec4 SpriteBounds;
uniform float ViewYaw;
uniform float ViewPitch;
uniform sampler2D u_noiseTexture;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#define TWO_PI 6.28318530718

vec2 randomGB(vec2 p) {
    vec2 uv = floor(p) / 100.0 + 0.5;
    return texture(u_noiseTexture, fract(uv)).gb;
}

vec4 voronoi(vec2 x, float t) {
    vec2 ip = floor(x);
    vec2 fp = fract(x);

    vec2 mg, mr;
    float md = 8.0;
    float rand = 0.0;

    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 g = vec2(float(i), float(j));
            vec2 o = randomGB(ip + g);
            float raw_hash = o.x;
            o = 0.5 + VoronoiDistortion * sin(t + TWO_PI * o);
            vec2 r = g + o - fp;
            float d = dot(r, r);

            if (d < md) {
                md = d;
                mr = r;
                mg = g;
                rand = raw_hash;
            }
        }
    }

    md = 8.0;
    for (int j = -2; j <= 2; j++) {
        for (int i = -2; i <= 2; i++) {
            vec2 g = mg + vec2(float(i), float(j));
            vec2 o = randomGB(ip + g);
            o = 0.5 + VoronoiDistortion * sin(t + TWO_PI * o);
            vec2 r = g + o - fp;
            if (dot(mr - r, mr - r) > 0.00001) {
                md = min(md, dot(0.5 * (mr + r), normalize(r - mr)));
            }
        }
    }

    return vec4(md, mr, rand);
}

void main() {
    vec2 uv = (texCoord0 - SpriteBounds.xy) / max(SpriteBounds.zw, vec2(0.0001));
    vec2 p = uv - 0.5 + vec2(VoronoiOffsetX, VoronoiOffsetY) * 0.35;
    float totalRot = VoronoiRotation + ViewYaw;
    if (abs(totalRot) > 0.0001) {
        float cr = cos(totalRot), sr = sin(totalRot);
        p = mat2(cr, -sr, sr, cr) * p;
    }
    p += ViewPitch * 0.35;
    p *= VoronoiScale * max(VoronoiFov, 1.0) * 1.25;

    float t = Time * VoronoiSpeed;

    vec4 voronoiRes = voronoi(p, t);

    float shape = clamp(voronoiRes.w, 0.0, 1.0);
    float mixer = shape * max(VoronoiColorCount - 1.0, 1.0);
    float steps = max(1.0, VoronoiStepsPerColor);

    vec4 gradient = VoronoiColor0;
    gradient.rgb *= gradient.a;
    for (int i = 1; i < 5; i++) {
        if (float(i) >= VoronoiColorCount) break;
        float localT = clamp(mixer - float(i - 1), 0.0, 1.0);
        localT = round(localT * steps) / steps;
        vec4 c = i == 1 ? VoronoiColor1 : (i == 2 ? VoronoiColor2 : (i == 3 ? VoronoiColor3 : VoronoiColor4));
        c.rgb *= c.a;
        gradient = mix(gradient, c, localT);
    }

    if ((mixer < 0.0) || (mixer > (VoronoiColorCount - 1.0))) {
        float localT = mixer + 1.0;
        if (mixer > (VoronoiColorCount - 1.0)) {
            localT = mixer - (VoronoiColorCount - 1.0);
        }
        localT = round(localT * steps) / steps;
        vec4 cFst = VoronoiColor0;
        cFst.rgb *= cFst.a;
        vec4 cLast = VoronoiColor0;
        if (VoronoiColorCount >= 2.0) cLast = VoronoiColor1;
        if (VoronoiColorCount >= 3.0) cLast = VoronoiColor2;
        if (VoronoiColorCount >= 4.0) cLast = VoronoiColor3;
        if (VoronoiColorCount >= 5.0) cLast = VoronoiColor4;
        cLast.rgb *= cLast.a;
        gradient = mix(cLast, cFst, localT);
    }

    vec3 cellColor = gradient.rgb;
    float cellOpacity = gradient.a;

    float glows = length(voronoiRes.yz * VoronoiGlow);
    glows = pow(glows, 1.5);

    vec3 color = mix(cellColor, VoronoiColorGlow.rgb * VoronoiColorGlow.a, VoronoiColorGlow.a * glows);
    float opacity = cellOpacity + VoronoiColorGlow.a * glows;

    float edge = voronoiRes.x;
    float smoothEdge = 0.02 / (2.0 * max(VoronoiScale, 0.01)) * (1.0 + 0.5 * VoronoiGap);
    edge = smoothstep(VoronoiGap - smoothEdge, VoronoiGap + smoothEdge, edge);

    color = mix(VoronoiColorGap.rgb * VoronoiColorGap.a, color, edge);
    opacity = mix(VoronoiColorGap.a, opacity, edge);

    fragColor = vec4(color * vertexColor.rgb, opacity);
}
