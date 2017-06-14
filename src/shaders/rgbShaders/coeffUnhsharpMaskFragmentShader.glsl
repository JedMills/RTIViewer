#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float coeffUnMaskGain;

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

mat3x2 calcEnhancedCoeffs(ivec2 ptmCoords, int type, mat3x2 coeffs){
    int distance = 1;
    mat3x2 tempCoeffs = mat3x2(0,0,0,0,0,0);

    for(int xOffset = -distance; xOffset <= distance; xOffset++){
        for(int yOffset = -distance; yOffset <= distance; yOffset++){
            if(xOffset != 0 && yOffset != 0){
                if(type == 0){
                    ivec4 rCoeffs1 = texelFetch(rVals1, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    ivec4 rCoeffs2 = texelFetch(rVals2, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    tempCoeffs[0][0] += rCoeffs1.x;
                    tempCoeffs[1][0] += rCoeffs1.y;
                    tempCoeffs[2][0] += rCoeffs1.z;
                    tempCoeffs[0][1] += rCoeffs1.x;
                    tempCoeffs[1][1] += rCoeffs1.y;
                    tempCoeffs[2][1] += rCoeffs1.z;
                }
                else if(type == 1){
                    ivec4 gCoeffs1 = texelFetch(gVals1, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    ivec4 gCoeffs2 = texelFetch(gVals2, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    tempCoeffs[0][0] += gCoeffs1.x;
                    tempCoeffs[1][0] += gCoeffs1.y;
                    tempCoeffs[2][0] += gCoeffs1.z;
                    tempCoeffs[0][1] += gCoeffs1.x;
                    tempCoeffs[1][1] += gCoeffs1.y;
                    tempCoeffs[2][1] += gCoeffs1.z;
                }
                else if(type == 2){
                    ivec4 bCoeffs1 = texelFetch(bVals1, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    ivec4 bCoeffs2 = texelFetch(bVals2, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0);
                    tempCoeffs[0][0] += bCoeffs1.x;
                    tempCoeffs[1][0] += bCoeffs1.y;
                    tempCoeffs[2][0] += bCoeffs1.z;
                    tempCoeffs[0][1] += bCoeffs1.x;
                    tempCoeffs[1][1] += bCoeffs1.y;
                    tempCoeffs[2][1] += bCoeffs1.z;
                }
            }
        }
    }
    //tempCoeffs /= ((distance * 2) + 1) * ((distance * 2) + 1);
    //tempCoeffs = coeffs + coeffUnMaskGain * (coeffs - tempCoeffs);
    //tempCoeffs = coeffUnMaskGain * (coeffs - tempCoeffs);
    //tempCoeffs =  coeffs + coeffUnMaskGain * (coeffs - tempCoeffs);
    //tempCoeffs = coeffs + coeffUnMaskGain * (coeffs - tempCoeffs);
    mat3x2 returnMat = coeffs + coeffUnMaskGain * (coeffs - tempCoeffs);

    return returnMat;
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

    mat3x2 rEn = calcEnhancedCoeffs(ptmCoords, 0, mat3x2(rCoeffs1.xyz, rCoeffs2.xyz));
    mat3x2 gEn = calcEnhancedCoeffs(ptmCoords, 1, mat3x2(gCoeffs1.xyz, gCoeffs2.xyz));
    mat3x2 bEn = calcEnhancedCoeffs(ptmCoords, 2, mat3x2(bCoeffs1.xyz, bCoeffs2.xyz));

    float red = applyPTM(rEn[0][0], rEn[1][0], rEn[2][0], rEn[0][1], rEn[1][1], rEn[2][1]);
    float green = applyPTM(gEn[0][0], gEn[1][0], gEn[2][0], gEn[0][1], gEn[1][1], gEn[2][1]);
    float blue = applyPTM(bEn[0][0], bEn[1][0], bEn[2][0], bEn[0][1], bEn[1][1], bEn[2][1]);

    //red -= coeffUnMaskGain * 0.05;
    //blue -= coeffUnMaskGain * 0.05;
    //green -= coeffUnMaskGain * 0.05;

    colorOut = vec4(red, green, blue, 1);
}
