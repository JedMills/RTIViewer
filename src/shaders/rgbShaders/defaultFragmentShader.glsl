#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform isampler2D rVals1;
uniform isampler2D rVals2;
uniform isampler2D gVals1;
uniform isampler2D gVals2;
uniform isampler2D bVals1;
uniform isampler2D bVals2;

in vec2 texCoordV;
out vec4 colorOut;


vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}


float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i =  255;}
    i = i / 255;

    return i;
}


void main() {

    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    ivec4 redCoeffs1 = texelFetch(rVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 redCoeffs2 = texelFetch(rVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 greenCoeffs1 = texelFetch(gVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 greenCoeffs2 = texelFetch(gVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 blueCoeffs1 = texelFetch(bVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 blueCoeffs2 = texelFetch(bVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);

    float red = applyPTM(redCoeffs1.x, redCoeffs1.y, redCoeffs1.z, redCoeffs2.x, redCoeffs2.y, redCoeffs2.z);
    float green = applyPTM(greenCoeffs1.x, greenCoeffs1.y, greenCoeffs1.z, greenCoeffs2.x, greenCoeffs2.y, greenCoeffs2.z);
    float blue = applyPTM(blueCoeffs1.x, blueCoeffs1.y, blueCoeffs1.z, blueCoeffs2.x, blueCoeffs2.y, blueCoeffs2.z);

    colorOut = vec4(red, green, blue, 1);
}

