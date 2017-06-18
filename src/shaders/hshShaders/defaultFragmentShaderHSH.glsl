#version 330

#define PI 3.1415926535

uniform float lightX;
uniform float lightY;
uniform float imageHeight;
uniform float imageWidth;

/* uniform isampler2D dataTexture; */

/*
uniform sampler2D redCoeffs1;
uniform sampler2D redCoeffs2;
uniform sampler2D redCoeffs3;

uniform sampler2D greenCoeffs1;
uniform sampler2D greenCoeffs2;
uniform sampler2D greenCoeffs3;

uniform sampler2D blueCoeffs1;
uniform sampler2D blueCoeffs2;
uniform sampler2D blueCoeffs3;
*/
/* uniform sampler2D normals; */


in vec2 texCoordV;
out vec4 colorOut;


vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}

/*
mat4x4 getHSH(float theta, float phi, int order){
        mat4x4 hweights = mat4x4(0);

        float cosPhi = cos(phi);
        float cosTheta = cos(theta);
        float cosTheta2 = cosTheta * cosTheta;

        hweights[0][0] = 1/sqrt(2*PI);
        hweights[1][0] = sqrt(6/PI)      *  (cosPhi*sqrt(cosTheta-cosTheta2));
        hweights[2][0] = sqrt(3/(2*PI))  *  (-1. + 2.*cosTheta);
        hweights[3][0] = sqrt(6/PI)      *  (sqrt(cosTheta - cosTheta2)*sin(phi));
        if (order > 2)
        {
            hweights[0][1] = sqrt(30/PI)     *  (cos(2.*phi)*(-cosTheta + cosTheta2));
            hweights[1][1] = sqrt(30/PI)     *  (cosPhi*(-1. + 2.*cosTheta)*sqrt(cosTheta - cosTheta2));
            hweights[2][1] = sqrt(5/(2*PI))  *  (1 - 6.*cosTheta + 6.*cosTheta2);
            hweights[3][1] = sqrt(30/PI)     *  ((-1 + 2.*cosTheta)*sqrt(cosTheta - cosTheta2)*sin(phi));
            hweights[0][2] = sqrt(30/PI)     *  ((-cosTheta + cosTheta2)*sin(2.*phi));
        }
        if (order > 3)
        {
            hweights[1][2]  = 2*sqrt(35/PI)	*	(cos(3.0*phi)*pow((cosTheta - cosTheta2), 1.5f));
            hweights[2][2] = sqrt(210/PI)	*	(cos(2.0*phi)*(-1 + 2*cosTheta)*(-cosTheta + cosTheta2));
            hweights[3][2] = 2*sqrt(21/PI)  *	(cos(phi)*sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2));
            hweights[0][3] = sqrt(7/(2*PI)) *	(-1 + 12*cosTheta - 30*cosTheta2 + 20*cosTheta2*cosTheta);
            hweights[1][3] = 2*sqrt(21/PI)  *	(sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2)*sin(phi));
            hweights[2][3] = sqrt(210/PI)  *	(-1 + 2*cosTheta)*(-cosTheta + cosTheta2)*sin(2*phi);
            hweights[3][3] = 2*sqrt(35/PI)  *	pow((cosTheta - cosTheta2), 1.5f)*sin(3*phi);
        }

        return hweights;
    }
*/


void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    colorOut = vec4(0, 1, 0, 1);
}
