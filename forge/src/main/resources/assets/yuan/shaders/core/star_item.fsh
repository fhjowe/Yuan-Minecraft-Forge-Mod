#version 150

uniform float Time;
uniform float StarDensity;
uniform float StarBrightness;
uniform float StarSize;
uniform float StarColorMode;
uniform vec4 StarBackground;
uniform vec4 SpriteBounds;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    vec2 uv = (texCoord0 - SpriteBounds.xy) / max(SpriteBounds.zw, vec2(0.0001));
    float t = Time;
    vec2 cell = uv * (StarDensity * 5.0);
    vec2 id = floor(cell);
    vec2 f = fract(cell);
    float h = hash(id);
    float star = h > 0.72 ? 1.0 : 0.0;
    float tw = 0.7 + 0.3 * sin(t * 2.0 + h * 6.2831);
    float d = length(f - 0.5);
    float dot = 1.0 - smoothstep(0.0, 0.18 / max(StarSize, 0.01), d);
    vec3 starCol = StarColorMode > 0.5
            ? vec3(
                0.4 + 0.6 * hash(id + vec2(7.0, 3.0)),
                0.4 + 0.6 * hash(id + vec2(13.0, 5.0)),
                0.6 + 0.4 * hash(id + vec2(29.0, 11.0)))
            : vec3(1.0);
    vec3 col = StarBackground.rgb + starCol * dot * tw * StarBrightness * star * 1.6;
    fragColor = vec4(col * vertexColor.rgb, 1.0);
}
