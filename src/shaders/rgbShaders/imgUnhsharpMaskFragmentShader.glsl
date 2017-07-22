#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

//the image gain argthat the use can change wit hthe slider
uniform float imgUnMaskGain;

//textures contining the first 3 and last 3 coeffs for the red channel
uniform isampler2D rVals1;
uniform isampler2D rVals2;

//textures contining the first 3 and last 3 coeffs for the green channel
uniform isampler2D gVals1;
uniform isampler2D gVals2;

//textures contining the first 3 and last 3 coeffs for the blue channel
uniform isampler2D bVals1;
uniform isampler2D bVals2;

//texture continign the normal for each pixel
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
    return vec2(coords.x * imageWidth, coords.y * imageHeight);
}



//the standard PTM equation, see the user guide for a link to the PTM paper
float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i = 255;}
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
    ivec3 rCoeffs1 = texelFetch(rVals1, ptmCoords, 0).xyz;
    ivec3 rCoeffs2 = texelFetch(rVals2, ptmCoords, 0).xyz;
    ivec3 gCoeffs1 = texelFetch(gVals1, ptmCoords, 0).xyz;
    ivec3 gCoeffs2 = texelFetch(gVals2, ptmCoords, 0).xyz;
    ivec3 bCoeffs1 = texelFetch(bVals1, ptmCoords, 0).xyz;
    ivec3 bCoeffs2 = texelFetch(bVals2, ptmCoords, 0).xyz;

    //find the  rgb values
    float red = applyPTM(rCoeffs1.x, rCoeffs1.y, rCoeffs1.z, rCoeffs2.x, rCoeffs2.y, rCoeffs2.z);
    float green = applyPTM(gCoeffs1.x, gCoeffs1.y, gCoeffs1.z, gCoeffs2.x, gCoeffs2.y, gCoeffs2.z);
    float blue = applyPTM(bCoeffs1.x, bCoeffs1.y, bCoeffs1.z, bCoeffs2.x, bCoeffs2.y, bCoeffs2.z);

    //find the y of yuv for this value
    float lum = red * 0.299 + green * 0.587 + blue * 0.144;

    return lum;
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

    //get all the coeffs for the pixel this shader is being executed for
    ivec4 rCoeffs1 = texelFetch(rVals1, ptmCoords, 0);
    ivec4 rCoeffs2 = texelFetch(rVals2, ptmCoords, 0);
    ivec4 gCoeffs1 = texelFetch(gVals1, ptmCoords, 0);
    ivec4 gCoeffs2 = texelFetch(gVals2, ptmCoords, 0);
    ivec4 bCoeffs1 = texelFetch(bVals1, ptmCoords, 0);
    ivec4 bCoeffs2 = texelFetch(bVals2, ptmCoords, 0);

    //aply the standard PTM equationto find the rgb values for this pixel
    float red = applyPTM(rCoeffs1.x, rCoeffs1.y, rCoeffs1.z, rCoeffs2.x, rCoeffs2.y, rCoeffs2.z);
    float green = applyPTM(gCoeffs1.x, gCoeffs1.y, gCoeffs1.z, gCoeffs2.x, gCoeffs2.y, gCoeffs2.z);
    float blue = applyPTM(bCoeffs1.x, bCoeffs1.y, bCoeffs1.z, bCoeffs2.x, bCoeffs2.y, bCoeffs2.z);

    //convert to yuv colourspace
    vec3 yuv = calcYUV(red, green, blue);

    //get the enahnced lum, and convert back to rgb with this enhancement
    float enhancedLum = calcEnhancedLuminance(yuv.x, ivec2(ptmCoords));
    vec3 rgb = getRGB(enhancedLum, yuv.y, yuv.z);

    //send the colour to be written to the screen, the 1 is the a of rgba (the transparency)
    colorOut = vec4(rgb, 1);

}
