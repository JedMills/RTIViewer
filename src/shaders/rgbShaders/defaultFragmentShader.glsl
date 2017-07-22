#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

//textures contining the first 3 and last 3 coeffs for the red channel
uniform isampler2D rVals1;
uniform isampler2D rVals2;

//textures contining the first 3 and last 3 coeffs for the green channel
uniform isampler2D gVals1;
uniform isampler2D gVals2;

//textures contining the first 3 and last 3 coeffs for the blue channel
uniform isampler2D bVals1;
uniform isampler2D bVals2;

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


void main() {
    //convert coords so top left is (0, 0)
    vec2 coords = convertCoords(texCoordV);

    //map coords from 0.0 - 1.0 to real coords in texture
    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    //get all the coeffs for the pixel this shader is being executed for
    ivec3 redCoeffs1 = texelFetch(rVals1, ptmCoords, 0).xyz;
    ivec3 redCoeffs2 = texelFetch(rVals2, ptmCoords, 0).xyz;
    ivec3 greenCoeffs1 = texelFetch(gVals1, ptmCoords, 0).xyz;
    ivec3 greenCoeffs2 = texelFetch(gVals2, ptmCoords, 0).xyz;
    ivec3 blueCoeffs1 = texelFetch(bVals1, ptmCoords, 0).xyz;
    ivec3 blueCoeffs2 = texelFetch(bVals2, ptmCoords, 0).xyz;

    //apply the PTM equation to the red green and blue channels
    float red = applyPTM(redCoeffs1.x, redCoeffs1.y, redCoeffs1.z, redCoeffs2.x, redCoeffs2.y, redCoeffs2.z);
    float green = applyPTM(greenCoeffs1.x, greenCoeffs1.y, greenCoeffs1.z, greenCoeffs2.x, greenCoeffs2.y, greenCoeffs2.z);
    float blue = applyPTM(blueCoeffs1.x, blueCoeffs1.y, blueCoeffs1.z, blueCoeffs2.x, blueCoeffs2.y, blueCoeffs2.z);

    //send the colour to be written to the screen, the 1 is the a of rgba (the transparency)
    colorOut = vec4(red, green, blue, 1);
}

