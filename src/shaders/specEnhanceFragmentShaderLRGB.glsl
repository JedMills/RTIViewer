#version 330

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

uniform float diffConst;
uniform float specConst;
uniform float specExConst;

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




void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    ivec4 lumVals1 = texelFetch(lumCoeffs1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 rgbVals = texelFetch(rgbCoeffs, ivec2(ptmCoords.x, ptmCoords.y), 0);
    vec4 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0);

    vec3 hVector = vec3(0.0, 0.0, 1.0);
    hVector.x += lightX;
    hVector.y += lightY;
    hVector = hVector * 0.5;
    hVector = normalize(hVector);

    float nDotH = dot(hVector, normal.xyz);

    if(nDotH < 0.0){nDotH = 0.0;}
    else if(nDotH > 1.0){nDotH = 1.0;}
    nDotH = pow(nDotH, specExConst);
    nDotH *= specConst * 255;

    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;

    vec3 colour = vec3( (rgbVals.x * diffConst + nDotH) * lum,
                        (rgbVals.y * diffConst + nDotH) * lum,
                        (rgbVals.z * diffConst + nDotH) * lum);

    colorOut = vec4(colour, 1);
}
