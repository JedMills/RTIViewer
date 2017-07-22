#version 330
//VERTEX_SHADER

//the scale of the image to be rendered, 1 = normal size, 2 = double size etc..
uniform float imageScale;

//the x offset from the center of the rendered quad
uniform float viewportX;

//they offset from the center of the rendered quad
uniform float viewportY;

in vec4 position;
in vec2 texCoord;

//passed to frag shaders to find the right postion in the textures
out vec2 texCoordV;

void main() {
    gl_Position = position;

    //move the position by the pan so that all fragment shaders are offset by pan
    texCoordV = vec2((gl_Position.x + viewportX) / imageScale,
                     (gl_Position.y + viewportY) / imageScale);
}

