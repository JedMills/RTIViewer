#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

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
    vec2 ptmCoords = convertToPTMCoords(coords);

    //convert the normal to a colour
    vec4 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0);

    //send the colour to be written to the screen, the 1 is the a of rgba (the transparency)
    vec3 color = convertNormalToColour(vec3(normal.x, normal.y, normal.z));
    colorOut = vec4(color, 1.0);
}
