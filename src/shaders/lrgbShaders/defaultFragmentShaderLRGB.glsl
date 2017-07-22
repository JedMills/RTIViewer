#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

//textures containing the first 3 and last 3 luminance coeffs, and the rgb coeffs
uniform isampler2D lumCoeffs1;
uniform isampler2D lumCoeffs2;
uniform isampler2D rgbCoeffs;

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



void main(){
    //convert coords so top left is (0, 0)
    vec2 coords = convertCoords(texCoordV);

    //map coords from 0.0 - 1.0 to real coords in texture
    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    //aget the lum and rgb coeffs for this pixel
    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals =  texelFetch(rgbCoeffs, ptmCoords, 0);

    //apply the PTM equationto the lum coeffs to get the luminance for this pixel,
    float lum = applyPTM(lumVals1.x, lumVals1.y, lumVals1.z, lumVals2.x, lumVals2.y, lumVals2.z) / 255.0;

    //the colour to be shown for this pixel on the screen, the 1 is the a of rgba (the transparency, 1 = opaque)
    colorOut = vec4(rgbVals.x * lum, rgbVals.y * lum, rgbVals.z * lum, 1);
}

