#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 OutSize;
uniform vec2 InSize;
uniform float Saturation;
uniform float Style;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    vec4 c = texture(DiffuseSampler, texCoord);
    float gray = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = mix(vec3(gray), c.rgb, Saturation);

    if (Style > 0.5 && Style < 1.5) {
        color = vec3(dot(color, vec3(1.0, 0.5, 0.2))) * vec3(0.85, 0.72, 0.5);
    } else if (Style > 1.5) {
        vec2 uv = texCoord - 0.5;
        float d = length(uv);
        color *= 1.0 - smoothstep(0.25, 0.72, d) * 0.75;
    }

    fragColor = vec4(color, 1.0);
}
