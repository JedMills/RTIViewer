#version 130

/*
uniform float lightX;
uniform float lightY;
uniform int imageHeight;
uniform int imageWidth;
uniform vec3[100] rCoeffs1;
uniform vec3[100] rCoeffs2;
uniform vec3[100] gCoeffs1;
uniform vec3[100] gCoeffs2;
uniform vec3[100] bCoeffs1;
uniform vec3[100] bCoeffs2;
*/

in vec2 texCoordV;
out vec4 colorOut;

void main() {
    //gl_FragColor = vec4(gl_PointCoord.x, gl_PointCoord.x, gl_PointCoord.x, 1);
    //colorOut = vec4(texCoordV.x, 0, 0, 0);
    colorOut = vec4(gl_PointCoord.x, 0, 0, 1);
}