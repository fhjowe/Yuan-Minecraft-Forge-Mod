#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;
in vec2 UV0;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;

out vec4 vertexColor;
out vec3 vertexPos;
out vec3 vertexNormal;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexColor = Color;
    vertexPos = pos;
    vertexNormal = Normal;
}
