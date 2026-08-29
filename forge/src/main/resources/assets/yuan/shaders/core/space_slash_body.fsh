#version 150

uniform float Time;
uniform float Progress;
uniform float Seed;
uniform vec4 CoreColor;
uniform vec4 EdgeColor;
uniform vec4 GlowColor;
uniform float CoreShade;
uniform float EdgeBrightness;
uniform float GlowStrength;
uniform float NoiseStrength;
uniform float TipFade;
uniform float SweepSpeed;
uniform float SweepSoftness;
uniform float FadeStart;
uniform float FadeDuration;
uniform float HoldFraction;
uniform float CoreWidth;
uniform float EdgeWidth;
uniform float GlowWidth;
uniform float Starfield;
uniform float StarDensity;
uniform float StarBrightness;
uniform float StarSize;
uniform float StarColorMode;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7)) + Seed * 0.618) * 43758.5453);
}

void main() {
    float t = texCoord0.x;
    float v = texCoord0.y;
    float d = abs(v - 0.5) * 2.0;

    float endFade = smoothstep(0.0, TipFade, t) * smoothstep(1.0, 1.0 - TipFade, t);
    float revealEnd = min(Progress / SweepSpeed, 1.0);
    float sweep = 1.0 - smoothstep(revealEnd - SweepSoftness, revealEnd, t);
    float fadeFrom = max(FadeStart, min(1.0, SweepSpeed + HoldFraction));
    float fadeOut = 1.0 - smoothstep(fadeFrom, fadeFrom + FadeDuration, Progress);

    float core = smoothstep(1.0, CoreWidth, d);
    float edge = smoothstep(EdgeWidth, min(1.0, EdgeWidth + 0.06), d)
            * (1.0 - smoothstep(0.94, 0.98, d));
    float glow = smoothstep(GlowWidth, 0.98, d) * (1.0 - smoothstep(0.99, 1.0, d));

    float noise = hash(vec2(t * 36.0, v * 12.0 + floor(Time * 20.0)));
    float crack = 0.5 + 0.5 * sin(t * 90.0 + v * 28.0 + Seed);

    float coreShade = 0.09 - 0.05 * d * d;
    vec3 coreCol = CoreColor.rgb
            * (coreShade * CoreShade + 0.04 * noise * NoiseStrength + 0.02 * crack * NoiseStrength);
    float starHash = hash(vec2(t * StarDensity * 40.0, v * StarDensity * 40.0));
    float star = starHash > 0.94 ? 1.0 : 0.0;
    float starDot = 1.0 - smoothstep(0.0, 0.08 / max(StarSize, 0.01), abs(fract(starHash * 17.0) - 0.5));
    vec3 starCol = StarColorMode > 0.5
            ? vec3(
                0.4 + 0.6 * hash(vec2(t * 13.0, v * 7.0)),
                0.4 + 0.6 * hash(vec2(t * 7.0, v * 13.0)),
                0.6 + 0.4 * hash(vec2(t * 29.0, v * 5.0)))
            : vec3(1.0);
    starCol = starCol * star * starDot * StarBrightness * Starfield;
    vec3 col = coreCol * core + starCol * core + EdgeColor.rgb * edge * EdgeBrightness
            + GlowColor.rgb * glow * GlowStrength;
    float a = core + edge * 0.75 + glow * 0.22;

    float alpha = vertexColor.a * endFade * sweep * fadeOut;
    fragColor = vec4(col, alpha * clamp(a, 0.0, 1.0));
}
