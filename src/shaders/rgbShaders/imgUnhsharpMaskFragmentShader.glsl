#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float imgUnMaskGain;

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
    return vec2(coords.x * imageWidth, coords.y * imageHeight);
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

vec3 calcYUV(float r, float g, float b){
    vec3 yuv = vec3(0, 0, 0);

	yuv.x = r * 0.299 + g * 0.587 + b * 0.144;
	yuv.y = r * -0.14713 + g * -0.28886 + b * 0.436;
	yuv.z = r * 0.615 + g * -0.51499 + b * -0.10001;

	return yuv;
}


float getLumFromCoord(ivec2 ptmCoords){
    ivec4 rCoeffs1 = texelFetch(rVals1, ptmCoords, 0);
    ivec4 rCoeffs2 = texelFetch(rVals2, ptmCoords, 0);
    ivec4 gCoeffs1 = texelFetch(gVals1, ptmCoords, 0);
    ivec4 gCoeffs2 = texelFetch(gVals2, ptmCoords, 0);
    ivec4 bCoeffs1 = texelFetch(bVals1, ptmCoords, 0);
    ivec4 bCoeffs2 = texelFetch(bVals2, ptmCoords, 0);

    float red = applyPTM(rCoeffs1.x, rCoeffs1.y, rCoeffs1.z, rCoeffs2.x, rCoeffs2.y, rCoeffs2.z);
    float green = applyPTM(gCoeffs1.x, gCoeffs1.y, gCoeffs1.z, gCoeffs2.x, gCoeffs2.y, gCoeffs2.z);
    float blue = applyPTM(bCoeffs1.x, bCoeffs1.y, bCoeffs1.z, bCoeffs2.x, bCoeffs2.y, bCoeffs2.z);

    float lum = red * 0.299 + green * 0.587 + blue * 0.144;

    return lum;
}


float calcEnhancedLuminance(float luminance, ivec2 ptmCoords){
    int distance = 2;
    float tempLum;

    for(int xOffset = -distance; xOffset <= distance; xOffset++){
        for(int yOffset = -distance; yOffset <= distance; yOffset++){
            tempLum += getLumFromCoord(ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset));
        }
    }

    tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
    tempLum = luminance + imgUnMaskGain * (luminance - tempLum);

    return tempLum;
}

vec3 getRGB(float lum, float u, float v){
    vec3 rgb = vec3(0, 0, 0);

    rgb.x = lum + v * 1.13983;
    rgb.y = lum + u * -0.39465 + v * -0.5806;
    rgb.z = lum + u * 2.03211;

    return rgb;
}


void main() {
    vec2 coords = convertCoords(texCoordV);

    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));


    ivec4 rCoeffs1 = texelFetch(rVals1, ptmCoords, 0);
    ivec4 rCoeffs2 = texelFetch(rVals2, ptmCoords, 0);
    ivec4 gCoeffs1 = texelFetch(gVals1, ptmCoords, 0);
    ivec4 gCoeffs2 = texelFetch(gVals2, ptmCoords, 0);
    ivec4 bCoeffs1 = texelFetch(bVals1, ptmCoords, 0);
    ivec4 bCoeffs2 = texelFetch(bVals2, ptmCoords, 0);

    float red = applyPTM(rCoeffs1.x, rCoeffs1.y, rCoeffs1.z, rCoeffs2.x, rCoeffs2.y, rCoeffs2.z);
    float green = applyPTM(gCoeffs1.x, gCoeffs1.y, gCoeffs1.z, gCoeffs2.x, gCoeffs2.y, gCoeffs2.z);
    float blue = applyPTM(bCoeffs1.x, bCoeffs1.y, bCoeffs1.z, bCoeffs2.x, bCoeffs2.y, bCoeffs2.z);

    vec3 yuv = calcYUV(red, green, blue);
    float enhancedLum = calcEnhancedLuminance(yuv.x, ivec2(ptmCoords));
    vec3 rgb = getRGB(enhancedLum, yuv.y, yuv.z);

    colorOut = vec4(rgb, 1);
}
