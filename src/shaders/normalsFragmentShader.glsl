#version 420 core
//#extension GL_EXT_gpu_shader4 : enable

uniform float lightX;
uniform float lightY;
uniform int imageHeight;
uniform int imageWidth;

uniform isampler2D rVals1;
uniform isampler2D rVals2;
uniform isampler2D gVals1;
uniform isampler2D gVals2;
uniform isampler2D bVals1;
uniform isampler2D bVals2;
uniform sampler2D normals;

in vec2 texCoordV;
out vec4 colorOut;


vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}


vec3 convertNormalToColour(vec3 normal){
    float red = ((normal.x + 1.0) / 2.0) * 255.0;
    float green = ((normal.y + 1.0) / 2.0) * 255.0;
    float blue = ((normal.z + 1.0) / 2.0) * 255.0;

    red = red / 255.0;
    green = green / 255.0;
    blue = blue / 255.0;

    return vec3(red, green, blue);
}




void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    vec4 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0);

    colorOut = vec4(convertNormalToColour(vec3(normal.x, normal.y, normal.z)), 1);
}
