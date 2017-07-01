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

uniform float imgUnMaskGain;

in vec2 texCoordV;
out vec4 colorOut;


vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}


mat4x4 getHSH(float theta, float phi, int basisTerms){
    mat4x4 hweights = mat4x4(0);

    float cosPhi = cos(phi);
    float cosTheta = cos(theta);
    float cosTheta2 = cosTheta * cosTheta;

    hweights[0][0] = 1/sqrt(2*PI);
    hweights[1][0] = sqrt(6/PI)      *  (cosPhi*sqrt(cosTheta-cosTheta2));
    hweights[2][0] = sqrt(3/(2*PI))  *  (-1.0 + 2.0*cosTheta);
    hweights[3][0] = sqrt(6/PI)      *  (sqrt(cosTheta - cosTheta2)*sin(phi));

    if (basisTerms > 4){
        hweights[0][1] = sqrt(30/PI)     *  (cos(2.0*phi)*(-cosTheta + cosTheta2));
        hweights[1][1] = sqrt(30/PI)     *  (cosPhi*(-1.0 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2));
        hweights[2][1] = sqrt(5/(2*PI))  *  (1 - 6.0*cosTheta + 6.0*cosTheta2);
        hweights[3][1] = sqrt(30/PI)     *  ((-1 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2)*sin(phi));
        hweights[0][2] = sqrt(30/PI)     *  ((-cosTheta + cosTheta2)*sin(2.0*phi));
    }
    if (basisTerms > 9){
        hweights[1][2] = 2*sqrt(35/PI)	*	(cos(3.0*phi)*pow((cosTheta - cosTheta2), 1.5f));
        hweights[2][2] = sqrt(210/PI)	*	(cos(2.0*phi)*(-1 + 2*cosTheta)*(-cosTheta + cosTheta2));
        hweights[3][2] = 2*sqrt(21/PI)  *	(cos(phi)*sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2));
        hweights[0][3] = sqrt(7/(2*PI)) *	(-1 + 12*cosTheta - 30*cosTheta2 + 20*cosTheta2*cosTheta);
        hweights[1][3] = 2*sqrt(21/PI)  *	(sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2)*sin(phi));
        hweights[2][3] = sqrt(210/PI)   *	(-1 + 2*cosTheta)*(-cosTheta + cosTheta2)*sin(2*phi);
        hweights[3][3] = 2*sqrt(35/PI)  *	pow((cosTheta - cosTheta2), 1.5f)*sin(3*phi);
    }

    return hweights;
}



vec3 calcYUV(float r, float g, float b){
    vec3 yuv = vec3(0, 0, 0);

	yuv.x = r * 0.299 + g * 0.587 + b * 0.144;
	yuv.y = r * -0.14713 + g * -0.28886 + b * 0.436;
	yuv.z = r * 0.615 + g * -0.51499 + b * -0.10001;

	return yuv;
}



vec3 rgbFromHWeights(   vec3 redVals1,      vec3 redVals2,      vec3 redVals3,
                        vec3 greenVals1,    vec3 greenVals2,    vec3 greenVals3,
                        vec3 blueVals1,     vec3 blueVals2,     vec3 blueVals3,
                        int basisTerms,     mat4x4 hWeights){
    float r = 0.0;
    float g = 0.0;
    float b = 0.0;


    for(int k = 0; k < basisTerms; k++){
        if      (k == 0){r += redVals1.x   * hWeights[0][0];}
        else if (k == 1){r += redVals1.y   * hWeights[1][0];}
        else if (k == 2){r += redVals1.z   * hWeights[2][0];}
        else if (k == 3){r += redVals2.x   * hWeights[3][0];}
        else if (k == 4){r += redVals2.y   * hWeights[0][1];}
        else if (k == 5){r += redVals2.z   * hWeights[1][1];}
        else if (k == 6){r += redVals3.x   * hWeights[2][1];}
        else if (k == 7){r += redVals3.y   * hWeights[3][1];}
        else if (k == 8){r += redVals3.z   * hWeights[0][2];}

        if      (k == 0){g += greenVals1.x * hWeights[0][0];}
        else if (k == 1){g += greenVals1.y * hWeights[1][0];}
        else if (k == 2){g += greenVals1.z * hWeights[2][0];}
        else if (k == 3){g += greenVals2.x * hWeights[3][0];}
        else if (k == 4){g += greenVals2.y * hWeights[0][1];}
        else if (k == 5){g += greenVals2.z * hWeights[1][1];}
        else if (k == 6){g += greenVals3.x * hWeights[2][1];}
        else if (k == 7){g += greenVals3.y * hWeights[3][1];}
        else if (k == 8){g += greenVals3.z * hWeights[0][2];}

        if      (k == 0){b += blueVals1.x  * hWeights[0][0];}
        else if (k == 1){b += blueVals1.y  * hWeights[1][0];}
        else if (k == 2){b += blueVals1.z  * hWeights[2][0];}
        else if (k == 3){b += blueVals2.x  * hWeights[3][0];}
        else if (k == 4){b += blueVals2.y  * hWeights[0][1];}
        else if (k == 5){b += blueVals2.z  * hWeights[1][1];}
        else if (k == 6){b += blueVals3.x  * hWeights[2][1];}
        else if (k == 7){b += blueVals3.y  * hWeights[3][1];}
        else if (k == 8){b += blueVals3.z  * hWeights[0][2];}

    }

    return vec3(r, g, b);
}



float getLumFromCoord(ivec2 ptmCoords, mat4x4 hWeights, int basisTerms){

    vec3 redVals1 = texelFetch(redCoeffs1, ptmCoords, 0).xyz;
    vec3 redVals2;
    vec3 redVals3;

    vec3 greenVals1 = texelFetch(greenCoeffs1, ptmCoords, 0).xyz;
    vec3 greenVals2;
    vec3 greenVals3;

    vec3 blueVals1 = texelFetch(blueCoeffs1, ptmCoords, 0).xyz;
    vec3 blueVals2;
    vec3 blueVals3;

    if(basisTerms > 3){
        redVals2 = texelFetch(redCoeffs2, ptmCoords, 0).xyz;
        greenVals2 = texelFetch(greenCoeffs2, ptmCoords, 0).xyz;
        blueVals2 = texelFetch(blueCoeffs2, ptmCoords, 0).xyz;
    }

    if(basisTerms > 6){
         redVals3 = texelFetch(redCoeffs3, ptmCoords, 0).xyz;
         greenVals3 = texelFetch(greenCoeffs3, ptmCoords, 0).xyz;
         blueVals3 = texelFetch(blueCoeffs3, ptmCoords, 0).xyz;
    }


    vec3 rgb = rgbFromHWeights(redVals1, redVals2, redVals3,
                                greenVals1, greenVals2, greenVals3,
                                blueVals1, blueVals2, blueVals3,
                                basisTerms, hWeights);

    float returnLum = rgb.x * 0.299 + rgb.y * 0.587 + rgb.z * 0.144;

    return returnLum;
}


float calcEnhancedLuminance(float luminance, ivec2 ptmCoords, mat4x4 hWeights, int basisTerms){
    int distance = 2;
    float tempLum;

    for(int xOffset = -distance; xOffset <= distance; xOffset++){
        for(int yOffset = -distance; yOffset <= distance; yOffset++){
            tempLum += getLumFromCoord(ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset), hWeights, basisTerms);
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

    float lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

    float phi = atan(lightY, lightX);

    if(phi < 0){
        phi = 2 * PI + phi;
    }

    float theta = min(acos(lightZ), PI /2 - 0.04);

    int basisTerms = texelFetch(dataTexture, ivec2(0, 0), 0).x;

    mat4x4 hWeights = getHSH(theta, phi, basisTerms);

    vec3 redVals1 = texelFetch(redCoeffs1, ptmCoords, 0).xyz;
    vec3 redVals2;
    vec3 redVals3;

    vec3 greenVals1 = texelFetch(greenCoeffs1, ptmCoords, 0).xyz;
    vec3 greenVals2;
    vec3 greenVals3;

    vec3 blueVals1 = texelFetch(blueCoeffs1, ptmCoords, 0).xyz;
    vec3 blueVals2;
    vec3 blueVals3;

    if(basisTerms > 3){
        redVals2 = texelFetch(redCoeffs2, ptmCoords, 0).xyz;
        greenVals2 = texelFetch(greenCoeffs2, ptmCoords, 0).xyz;
        blueVals2 = texelFetch(blueCoeffs2, ptmCoords, 0).xyz;
    }

    if(basisTerms > 6){
         redVals3 = texelFetch(redCoeffs3, ptmCoords, 0).xyz;
         greenVals3 = texelFetch(greenCoeffs3, ptmCoords, 0).xyz;
         blueVals3 = texelFetch(blueCoeffs3, ptmCoords, 0).xyz;
    }


    vec3 rgb = rgbFromHWeights(redVals1, redVals2, redVals3,
                                greenVals1, greenVals2, greenVals3,
                                blueVals1, blueVals2, blueVals3,
                                basisTerms, hWeights);


    vec3 yuv = calcYUV(rgb.x, rgb.y, rgb.z);
    float enhancedLum = calcEnhancedLuminance(yuv.x, ivec2(ptmCoords), hWeights, basisTerms);
    vec3 enhancedRGB = getRGB(enhancedLum, yuv.y, yuv.z);

    colorOut = vec4(enhancedRGB, 1);

}
