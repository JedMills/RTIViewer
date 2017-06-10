#version 330

uniform float imageScale;
uniform float viewportX;
uniform float viewportY;

in vec4 position;
in vec2 texCoord;
out vec2 texCoordV;

void main() {
    gl_Position = position;
    texCoordV = vec2((gl_Position.x + viewportX) / imageScale,
                     (gl_Position.y + viewportY) / imageScale);
}

