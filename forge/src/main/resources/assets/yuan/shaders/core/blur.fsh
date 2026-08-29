#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 Direction;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 texelSize = 1.0 / textureSize(DiffuseSampler, 0);
    float sigma = max(Radius / 3.0, 0.001);

    vec4 sum = texture(DiffuseSampler, texCoord);
    float totalWeight = 1.0;

    for (int i = 1; i <= 32; i++) {
        if (float(i) > Radius) break;
        float w = exp(-0.5 * float(i * i) / (sigma * sigma));
        vec2 offset = Direction * texelSize * float(i);
        sum += texture(DiffuseSampler, texCoord + offset) * w;
        sum += texture(DiffuseSampler, texCoord - offset) * w;
        totalWeight += 2.0 * w;
    }

    fragColor = sum / totalWeight;
}
