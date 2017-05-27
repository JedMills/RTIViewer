#version 420 core
#extension GL_EXT_gpu_shader4 : enable

uniform float lightX;
uniform float lightY;
uniform int imageHeight;
uniform int imageWidth;

uniform isampler2D rVals1;
uniform isampler2D rVals2;

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
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i =  255;}
    i = i / 255;

    return i;
}


/*
vec3 calcIntensity(float pos){
    //float r = applyPTM(rCoeffs1[int(pos)], rCoeffs2[int(pos)]);
    //float g = applyPTM(gCoeffs1[int(pos)], gCoeffs2[int(pos)]);
    //float b = applyPTM(bCoeffs1[int(pos)], bCoeffs2[int(pos)]);

    //float r = applyPTM(rCoeffs1[int(pos)].x,  rCoeffs1[int(pos)].y,rCoeffs1[int(pos)].z, rCoeffs2[int(pos)].x,  rCoeffs2[int(pos)].y,rCoeffs2[int(pos)].z);
    //float g = applyPTM(gCoeffs1[int(pos)].x,  gCoeffs1[int(pos)].y,gCoeffs1[int(pos)].z, gCoeffs2[int(pos)].x,  gCoeffs2[int(pos)].y,gCoeffs2[int(pos)].z);
    //float b = applyPTM(bCoeffs1[int(pos)].x,  bCoeffs1[int(pos)].y,bCoeffs1[int(pos)].z, bCoeffs2[int(pos)].x,  bCoeffs2[int(pos)].y,bCoeffs2[int(pos)].z);

    //return vec3(r, g, b);

    //return vec3(0,0,0);
    return vec3(texture2D(rVals1, pos).x,texture2D(rVals1, pos).y,texture2D(rVals1, pos).z);
}
*/


void main() {

    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    //float position = getArrayPos(ptmCoords);

    ivec4 redCoeffs1 = texelFetch(rVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 redCoeffs2 = texelFetch(rVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);

    float red = applyPTM(redCoeffs1.x, redCoeffs1.y, redCoeffs1.z, redCoeffs2.x, redCoeffs2.y, redCoeffs2.z);
    colorOut = vec4(red, 0, 0, 1);

    /*
    if(redCoeffs1.x == -1 && redCoeffs1.y == -2 && redCoeffs1.z == 3
        && redCoeffs2.x == 100000 && redCoeffs2.y == 2 && redCoeffs2.z == -3){
        colorOut = vec4(0, 1, 0, 1);
    }else if(redCoeffs1.x == 1){
        colorOut = vec4(0, 0, 1, 1);
    }else if(redCoeffs1.x == 0){
        colorOut = vec4(0, 1, 1, 1);
    }else{
       colorOut = vec4(1, 1, 1, 1);
    }
    */

    /*
    if(redCoeffs1.x == 0 && redCoeffs1.y == 0 && redCoeffs1.z == 0
        && redCoeffs2.x == 0 && redCoeffs2.y == 0 && redCoeffs2.z == 0){
        colorOut = vec4(0, 0, 1, 1);
    }else{
        colorOut = vec4(0, 1, 0, 1);
    }
    */

}

