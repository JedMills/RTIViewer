#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float imgUnMaskGain;

uniform isampler2D lumCoeffs1;
uniform isampler2D lumCoeffs2;
uniform isampler2D rgbCoeffs;
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
    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals = texelFetch(rgbCoeffs, ptmCoords, 0);

    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;
    float r = rgbVals.x * lum;
    float g = rgbVals.y * lum;
    float b = rgbVals.z * lum;

    float returnLum = r * 0.299 + g * 0.587 + b * 0.144;

    return returnLum;
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

    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals = texelFetch(rgbCoeffs, ptmCoords, 0);

    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;
    float r = rgbVals.x * lum;
    float g = rgbVals.y * lum;
    float b = rgbVals.z * lum;

    vec3 yuv = calcYUV(r, g, b);
    float enhancedLum = calcEnhancedLuminance(yuv.x, ivec2(ptmCoords));
    vec3 rgb = getRGB(enhancedLum, yuv.y, yuv.z);

    colorOut = vec4(rgb, 1);
}
