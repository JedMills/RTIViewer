#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

//the image unsharp masking gainparameter that the user can change with the slider
uniform float imgUnMaskGain;

//textures containing the first 3 and last 3 luminance coeffs, and the rgb coeffs
uniform isampler2D lumCoeffs1;
uniform isampler2D lumCoeffs2;
uniform isampler2D rgbCoeffs;

//tecture containing the normal vector for each pixel
uniform sampler2D normals;

//coordinate on textures with the pan from the vertex shader
in vec2 texCoordV;

//colour to write to the pixel this shader is being executed for
out vec4 colorOut;


//convert openGL coords with (0, 0) at the center to coords with (0, 0) in the top left
vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}

//scale the coords which are 0.0 - 1.0 to 0 - imageHeight and 0 - imageWidth
vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}

//the standard PTM equation, see the user guide for a link the the PTM paper
float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i =  255;}
    i = i / 255;

    return i;
}


//convert rgb colourspace to yuv
vec3 calcYUV(float r, float g, float b){
    vec3 yuv = vec3(0, 0, 0);

	yuv.x = r * 0.299 + g * 0.587 + b * 0.144;
	yuv.y = r * -0.14713 + g * -0.28886 + b * 0.436;
	yuv.z = r * 0.615 + g * -0.51499 + b * -0.10001;

	return yuv;
}

//getting the y from yuv (see above) for a speciifed coord
float getLumFromCoord(ivec2 ptmCoords){
    //get the coeffs for this coord
    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals = texelFetch(rgbCoeffs, ptmCoords, 0);

    //find the  rgb values
    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;
    float r = rgbVals.x * lum;
    float g = rgbVals.y * lum;
    float b = rgbVals.z * lum;

    //find the y of yuv for this value
    float returnLum = r * 0.299 + g * 0.587 + b * 0.144;

    return returnLum;
}



//Calculates the enhanced luminace for the pixel with x and y position by averaging the luminance of the pixels
//in a block of 4x4 around it, then applying the enhancement using the gain param
float calcEnhancedLuminance(float luminance, ivec2 ptmCoords){
    int distance = 2;
    float tempLum;

    //average the luminance from around the center pixel
    for(int xOffset = -distance; xOffset <= distance; xOffset++){
        for(int yOffset = -distance; yOffset <= distance; yOffset++){
            tempLum += getLumFromCoord(ivec2(ptmCoords.x + xOffset, ptmCoords.y + yOffset));
        }
    }

    //apply the enhancement, this bit comes from the original RTIViewer
    tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
    tempLum = luminance + imgUnMaskGain * (luminance - tempLum);

    return tempLum;
}



//the opposite of get yuv, converting bac kto rgb colour space
vec3 getRGB(float lum, float u, float v){
    vec3 rgb = vec3(0, 0, 0);

    rgb.x = lum + v * 1.13983;
    rgb.y = lum + u * -0.39465 + v * -0.5806;
    rgb.z = lum + u * 2.03211;

    return rgb;
}


void main() {
    //convert coords so top left is (0, 0)
    vec2 coords = convertCoords(texCoordV);

    //map coords from 0.0 - 1.0 to real coords in texture
    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    //aget the lum and rgb coeffs for this pixel
    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals =  texelFetch(rgbCoeffs, ptmCoords, 0);

    //calculate the rgb values from the luminance
    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;
    float r = rgbVals.x * lum;
    float g = rgbVals.y * lum;
    float b = rgbVals.z * lum;

    //convert to yuv colourspace
    vec3 yuv = calcYUV(r, g, b);

    //get the enahnced lum, and convert back to rgb with this enhancement
    float enhancedLum = calcEnhancedLuminance(yuv.x, ivec2(ptmCoords));
    vec3 rgb = getRGB(enhancedLum, yuv.y, yuv.z);

    //send the colour to be written to the screen, the 1 is the a of rgba (the transparency)
    colorOut = vec4(rgb, 1);
}
