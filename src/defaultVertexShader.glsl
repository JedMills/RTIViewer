#version 420 core

in vec4 position;
in vec2 texCoord;
out vec2 texCoordV;

void main() {
    gl_Position = position;
    texCoordV = gl_Position.xy;
}

