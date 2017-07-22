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
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}

//converts the normal vector to a colour, blue = z, green = y, red = x
vec3 convertNormalToColour(vec3 normal){
    float red = ((normal.x + 1.0) / 2.0);
    float green = ((normal.y + 1.0) / 2.0);
    float blue = ((normal.z + 1.0) / 2.0);

    return vec3(red, green, blue);
}




void main() {
    //convert coords so top left is (0, 0)
    vec2 coords = convertCoords(texCoordV);

    //map coords from 0.0 - 1.0 to real coords in texture
    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    //convert the normal to a colour
    vec4 normal = texelFetch(normals, ptmCoords, 0);

    //send the colour to be written to the screen, the 1 is the a of rgba (the transparency)
    colorOut = vec4(convertNormalToColour(vec3(normal.x, normal.y, normal.z)), 1);
}
