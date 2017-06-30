#version 330

#define PI 3.1415926535

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform isampler2D dataTexture;

uniform sampler2D redCoeffs1;
uniform sampler2D redCoeffs2;
uniform sampler2D redCoeffs3;

uniform sampler2D greenCoeffs1;
uniform sampler2D greenCoeffs2;
uniform sampler2D greenCoeffs3;

uniform sampler2D blueCoeffs1;
uniform sampler2D blueCoeffs2;
uniform sampler2D blueCoeffs3;

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
    float red = ((normal.x + 1.0) / 2.0);
    float green = ((normal.y + 1.0) / 2.0);
    float blue = ((normal.z + 1.0) / 2.0);

    return vec3(red, green, blue);
}


void main() {
    vec2 coords = convertCoords(texCoordV);

    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    vec3 normal = texelFetch(normals, ptmCoords, 0).xyz;

    vec3 color = convertNormalToColour(vec3(normal.x, normal.y, normal.z));

    colorOut = vec4(color, 1.0);

}
