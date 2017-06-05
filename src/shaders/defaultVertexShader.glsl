#version 420 core

uniform float imageScale;

in vec4 position;
in vec2 texCoord;
out vec2 texCoordV;

void main() {
    gl_Position = position;
    texCoordV = vec2(gl_Position.x / imageScale, gl_Position.y / imageScale);
}

