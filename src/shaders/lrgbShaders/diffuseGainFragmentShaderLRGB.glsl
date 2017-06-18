#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float diffGain;

uniform isampler2D lumCoeffs1;
uniform isampler2D lumCoeffs2;
uniform isampler2D rgbCoeffs;
uniform sampler2D normals;


in vec2 texCoordV;
out vec4 colorOut;


float minGain = 1.0;
float maxGain = 10.0;

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


float calcModifiedGain(){
    return minGain + (diffGain * ((maxGain - minGain) / 100));
}




float applyDiffuseGain(ivec4 coeffs1, ivec4 coeffs2, vec4 normal, float modGain){
    float a0 = modGain * coeffs1.x;
    float a1 = modGain * coeffs1.y;
    float a2 = modGain * coeffs1.z;
    float a3t = ((coeffs1.x << 1) * normal.x) + (coeffs1.z * normal.y);
    float a3 = ((1.0 - modGain) * a3t) + coeffs2.x;
    float a4t = ((coeffs1.y << 1) * normal.y) + (coeffs1.z * normal.x);
    float a4 = ((1.0 - modGain) * a4t) + coeffs2.y;
    float a5 = (1.0 - modGain) * (coeffs1.x*normal.x*normal.x + coeffs1.y*normal.y*normal.y +
                               coeffs1.z*normal.x*normal.y)
               + (coeffs2.x - a3) * normal.x
               + (coeffs2.y - a4) * normal.y
               + coeffs2.z;

    return applyPTM(a0, a1, a2, a3, a4, a5);
}



void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    ivec4 lumVals1 = texelFetch(lumCoeffs1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 rgbVals = texelFetch(rgbCoeffs, ivec2(ptmCoords.x, ptmCoords.y), 0);
    vec4 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0);

    float modGain = calcModifiedGain();
    float lum = applyDiffuseGain(lumVals1, lumVals2, normal, diffGain) / 256.0;

    colorOut = vec4(rgbVals.x * lum, rgbVals.y * lum, rgbVals.z * lum, 1);
}