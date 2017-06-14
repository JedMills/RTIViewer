#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float normUnMaskGain;
uniform float normUnMaskEnv;

uniform isampler2D rVals1;
uniform isampler2D rVals2;
uniform isampler2D gVals1;
uniform isampler2D gVals2;
uniform isampler2D bVals1;
uniform isampler2D bVals2;
uniform sampler2D normals;

in vec2 texCoordV;
out vec4 colorOut;

float diffuse = 5.0;


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


vec3 smoothNormal(vec3 normal, ivec2 ptmCoords){
    int distance = 2;
    vec3 tempNormal = vec3(0,0,0);

    for(int xOffset = -distance; xOffset <= distance; xOffset++){
        for(int yOffset = -distance; yOffset <= distance; yOffset++){
            tempNormal += texelFetch(normals, ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), 0).xyz;
        }
    }

    tempNormal /= ((distance * 2) + 1) * ((distance * 2) + 1);

    return tempNormal;
}



float applyModel(vec3 normal, vec3 smoothedNormal){
    vec3 normalE = normal + (smoothedNormal - normal) * normUnMaskGain;
    float nDotE = abs(normalE.x * lightX) + abs(normalE.y * lightY) + (0.1);
    //float nDotE = normalE.x + normalE.y;

    if(nDotE < 0.0){nDotE = 0.0;}
    else if(nDotE > 1.0){nDotE = 1.0;}

    return (diffuse + (1 / normUnMaskEnv)) / (diffuse * nDotE + (1 / normUnMaskEnv));
}


void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    ivec4 rCoeffs1 = texelFetch(rVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 rCoeffs2 = texelFetch(rVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 gCoeffs1 = texelFetch(gVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 gCoeffs2 = texelFetch(gVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 bCoeffs1 = texelFetch(bVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 bCoeffs2 = texelFetch(bVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    vec3 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0).xyz;


    float r = applyPTM(rCoeffs1.x, rCoeffs1.y, rCoeffs1.z, rCoeffs2.x, rCoeffs2.y, rCoeffs2.z);
    float g = applyPTM(gCoeffs1.x, gCoeffs1.y, gCoeffs1.z, gCoeffs2.x, gCoeffs2.y, gCoeffs2.z);
    float b = applyPTM(bCoeffs1.x, bCoeffs1.y, bCoeffs1.z, bCoeffs2.x, bCoeffs2.y, bCoeffs2.z);

    vec3 smoothedNormal = smoothNormal(normal, ivec2(ptmCoords));
    float diff = applyModel(normal, smoothedNormal);

    colorOut = vec4(r * diff, g * diff, b * diff, 1.0);
}
