#version 420 core

uniform float lightX;
uniform float lightY;
uniform int imageHeight;
uniform int imageWidth;
uniform ivec3[LENGTH] rCoeffs1;
uniform ivec3[LENGTH] rCoeffs2;
uniform ivec3[LENGTH] gCoeffs1;
uniform ivec3[LENGTH] gCoeffs2;
uniform ivec3[LENGTH] bCoeffs1;
uniform ivec3[LENGTH] bCoeffs2;


in vec2 texCoordV;
out vec4 colorOut;


vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(floor(coords.x * imageWidth),
                floor(coords.y * imageHeight));
}


float getArrayPos(vec2 ptmCoords){
    return (ptmCoords.y * imageWidth) + ptmCoords.x;
}

/*
float applyPTM(vec3 coeffs1, vec3 coeffs2){
    return (float(coeffs1.x) * lightX * lightX) + (float(coeffs1.y) * lightY * lightY)
            + (float(coeffs1.z) * lightX * lightY) + (float(coeffs2.x) * lightX)
            + (float(coeffs2.y) * lightY) + float(coeffs2.z);
}
*/

float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    return (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;
}


vec3 calcIntensity(float pos){
    //float r = applyPTM(rCoeffs1[int(pos)], rCoeffs2[int(pos)]);
    //float g = applyPTM(gCoeffs1[int(pos)], gCoeffs2[int(pos)]);
    //float b = applyPTM(bCoeffs1[int(pos)], bCoeffs2[int(pos)]);

    float r = applyPTM(rCoeffs1[int(pos)].x,  rCoeffs1[int(pos)].y,rCoeffs1[int(pos)].z, rCoeffs2[int(pos)].x,  rCoeffs2[int(pos)].y,rCoeffs2[int(pos)].z);
    float g = applyPTM(gCoeffs1[int(pos)].x,  gCoeffs1[int(pos)].y,gCoeffs1[int(pos)].z, gCoeffs2[int(pos)].x,  gCoeffs2[int(pos)].y,gCoeffs2[int(pos)].z);
    float b = applyPTM(bCoeffs1[int(pos)].x,  bCoeffs1[int(pos)].y,bCoeffs1[int(pos)].z, bCoeffs2[int(pos)].x,  bCoeffs2[int(pos)].y,bCoeffs2[int(pos)].z);

    return vec3(r, g, b);
}



void main() {

    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    float position = getArrayPos(ptmCoords);

    vec3 colour;


    if(position / 2256576 < 0.5){
        //colour = calcIntensity(position);
        colour = vec3(0, 1, 0);
    }else{
        colour = vec3(0, 0, 1);
    }

    /*
    if(rCoeffs1[22550] == vec3(0,0,0) && rCoeffs1[22504] == vec3(0,0,0)){
        colour = vec3(0, 1, 0);
    }else{
        colour = vec3(0, 0, 1);
    }
    */


    colorOut = vec4(colour.x, colour.y, colour.z, 1);
}

