#version 130

in vec4 position;
in vec2 texCoord;
out vec2 texCoordV;

void main() {
    //gl_Position = ftransform();
    gl_Position = position;
    texCoordV = texCoord;
}

