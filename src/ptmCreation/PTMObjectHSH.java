package ptmCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import java.nio.FloatBuffer;

import static java.lang.Math.*;

/**
 * Created by Jed on 17-Jun-17.
 */
public class PTMObjectHSH extends PTMObject {

    private int numColourChannels;
    private int basisTerms;
    private int basisType;

    private FloatBuffer redVals1;
    private FloatBuffer redVals2;
    private FloatBuffer redVals3;

    private FloatBuffer greenVals1;
    private FloatBuffer greenVals2;
    private FloatBuffer greenVals3;

    private FloatBuffer blueVals1;
    private FloatBuffer blueVals2;
    private FloatBuffer blueVals3;

    public PTMObjectHSH(String fileName, int width, int height, int numColourChannels,
                        int basisTerms, int basisType, FloatBuffer[] texelData) {
        super(fileName, width, height);

        this.numColourChannels = numColourChannels;
        this.basisTerms = basisTerms;
        this.basisType = basisType;


        redVals1 = texelData[0];
        redVals2 = texelData[1];
        redVals3 = texelData[2];

        greenVals1 = texelData[3];
        greenVals2 = texelData[4];
        greenVals3 = texelData[5];

        blueVals1 = texelData[6];
        blueVals2 = texelData[7];
        blueVals3 = texelData[8];

        calculateNormals();
        createPreviewImage();
    }

    @Override
    protected void calculateNormals() {
        float[] hWeights1 = getHSH((float)PI / 4, (float)PI / 6, basisTerms);
        float[] hWeights2 = getHSH((float)PI / 4, 5 * (float)PI / 6, basisTerms);
        float[] hWeights3 = getHSH((float)PI / 4, 3 * (float)PI / 2, basisTerms);

        float[][] lInverse = new float[3][3];
        lInverse[0][0] = 0.816498f;
        lInverse[0][1] = -0.816498f;
        lInverse[0][2] = 0.0f;

        lInverse[1][0] = 0.471407f;
        lInverse[1][1] = 0.47140847f;
        lInverse[1][2] = -0.942815416f;

        lInverse[2][0] = 0.47140227f;
        lInverse[2][1] = 0.4714038113f;
        lInverse[2][2] = 0.471407041f;

        normals = BufferUtils.createFloatBuffer(width * height * 3);

        int offset;
        Utils.Vector3f temp = new Utils.Vector3f(0, 0, 0);
        Utils.Vector3f normal;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                offset = y * width + x;
                temp.setX(0.0f);
                temp.setY(0.0f);
                temp.setZ(0.0f);

                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += redVals1.get(offset * 3 + k) * hWeights1[k];}
                    else if(k < 6){temp.x += redVals2.get(offset * 3 + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += redVals3.get(offset * 3 + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += redVals1.get(offset * 3 + k) * hWeights2[k];}
                    else if(k < 6){temp.y += redVals2.get(offset * 3 + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += redVals3.get(offset * 3 + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += redVals1.get(offset * 3 + k) * hWeights3[k];}
                    else if(k < 6){temp.z += redVals2.get(offset * 3 + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += redVals3.get(offset * 3 + k - 6) * hWeights3[k];}
                }
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += greenVals1.get(offset * 3 + k) * hWeights1[k];}
                    else if(k < 6){temp.x += greenVals2.get(offset * 3 + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += greenVals3.get(offset * 3 + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += greenVals1.get(offset * 3 + k) * hWeights2[k];}
                    else if(k < 6){temp.y += greenVals2.get(offset * 3 + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += greenVals3.get(offset * 3 + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += greenVals1.get(offset * 3 + k) * hWeights3[k];}
                    else if(k < 6){temp.z += greenVals2.get(offset * 3 + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += greenVals3.get(offset * 3 + k - 6) * hWeights3[k];}
                }
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += blueVals1.get(offset * 3 + k) * hWeights1[k];}
                    else if(k < 6){temp.x += blueVals2.get(offset * 3 + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += blueVals3.get(offset * 3 + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += blueVals1.get(offset * 3 + k) * hWeights2[k];}
                    else if(k < 6){temp.y += blueVals2.get(offset * 3 + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += blueVals3.get(offset * 3 + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += blueVals1.get(offset * 3 + k) * hWeights3[k];}
                    else if(k < 6){temp.z += blueVals2.get(offset * 3 + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += blueVals3.get(offset * 3 + k - 6) * hWeights3[k];}
                }

                temp.multiply(0.33333333f);
                normal = Utils.mat3x3_mul_vec3(lInverse, temp);
                normal = normal.normalise();

                normals.put(offset, normal.getX());
                normals.put(offset + 1, normal.getY());
                normals.put(offset + 2, normal.getZ());
            }
        }
    }

    @Override
    protected void createPreviewImage() {
        previewImage = new WritableImage(width, height);

        float phi = 0.0f;
        float theta = (float) acos(1.0);

        float[] hWeights = getHSH(theta, phi, basisTerms);

        int offset;
        float r, g, b;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                offset = (y * width + x) * 3;

                r = 0.0f;
                g = 0.0f;
                b = 0.0f;
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){r += redVals1.get(offset + k) * hWeights[k];}
                    else if(k < 6){r += redVals2.get(offset + k - 3) * hWeights[k];}
                    else if(k < 9){r += redVals3.get(offset + k - 6) * hWeights[k];}

                    if(k < 3){g += greenVals1.get(offset + k) * hWeights[k];}
                    else if(k < 6){g += greenVals2.get(offset + k - 3) * hWeights[k];}
                    else if(k < 9){g += greenVals3.get(offset + k - 6) * hWeights[k];}

                    if(k < 3){b += blueVals1.get(offset + k) * hWeights[k];}
                    else if(k < 6){b += blueVals2.get(offset + k - 3) * hWeights[k];}
                    else if(k < 9){b += blueVals3.get(offset + k - 6) * hWeights[k];}
                }

                r /= 255.0f;
                g /= 255.0f;
                b /= 255.0f;

                if(r > 1.0){r = 1.0f;}
                else if(r < 0){r = 0;}

                if(g > 1.0){g = 1.0f;}
                else if(g < 0){g = 0;}

                if(b > 1.0){b = 1.0f;}
                else if(b < 0){b = 0;}

                previewImage.getPixelWriter().setColor(x, y, Color.color(r, g, b) );
            }
        }
    }

    public WritableImage createNormalsMap(){
        WritableImage writableImage = new WritableImage(width, height);
        int offset;
        float r, g, b;
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                offset = (y * width + x) * 3;


            }
        }

        return writableImage;
    }


    private float[] getHSH(float theta, float phi, int order){
        double[] hweights = new double[16];
        double cosPhi = cos(phi);
        double cosTheta = cos(theta);
        double cosTheta2 = cosTheta * cosTheta;
        hweights[0] = 1/sqrt(2*PI);
        hweights[1] = sqrt(6/PI)      *  (cosPhi*sqrt(cosTheta-cosTheta2));
        hweights[2] = sqrt(3/(2*PI))  *  (-1. + 2.*cosTheta);
        hweights[3] = sqrt(6/PI)      *  (sqrt(cosTheta - cosTheta2)*sin(phi));
        if (order > 2)
        {
            hweights[4] = sqrt(30/PI)     *  (cos(2.*phi)*(-cosTheta + cosTheta2));
            hweights[5] = sqrt(30/PI)     *  (cosPhi*(-1. + 2.*cosTheta)*sqrt(cosTheta - cosTheta2));
            hweights[6] = sqrt(5/(2*PI))  *  (1 - 6.*cosTheta + 6.*cosTheta2);
            hweights[7] = sqrt(30/PI)     *  ((-1 + 2.*cosTheta)*sqrt(cosTheta - cosTheta2)*sin(phi));
            hweights[8] = sqrt(30/PI)     *  ((-cosTheta + cosTheta2)*sin(2.*phi));
        }
        if (order > 3)
        {
            hweights[9]  = 2*sqrt(35/PI)	*	(cos(3.0*phi)*pow((cosTheta - cosTheta2), 1.5f));
            hweights[10] = sqrt(210/PI)	*	(cos(2.0*phi)*(-1 + 2*cosTheta)*(-cosTheta + cosTheta2));
            hweights[11] = 2*sqrt(21/PI)  *	(cos(phi)*sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2));
            hweights[12] = sqrt(7/(2*PI)) *	(-1 + 12*cosTheta - 30*cosTheta2 + 20*cosTheta2*cosTheta);
            hweights[13] = 2*sqrt(21/PI)  *	(sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2)*sin(phi));
            hweights[14] = sqrt(210/PI)  *	(-1 + 2*cosTheta)*(-cosTheta + cosTheta2)*sin(2*phi);
            hweights[15] = 2*sqrt(35/PI)  *	pow((cosTheta - cosTheta2), 1.5f)*sin(3*phi);
        }

        float[] hWeightsOut = new float[16];
        for(int i = 0; i < 16; i++){
            hWeightsOut[i] = (float) hweights[i];
        }
        return hWeightsOut;
    }


    public int getBasisTerms() {
        return basisTerms;
    }


    public FloatBuffer getRedVals1() {
        return redVals1;
    }

    public FloatBuffer getRedVals2() {
        return redVals2;
    }

    public FloatBuffer getRedVals3() {
        return redVals3;
    }

    public FloatBuffer getGreenVals1() {
        return greenVals1;
    }

    public FloatBuffer getGreenVals2() {
        return greenVals2;
    }

    public FloatBuffer getGreenVals3() {
        return greenVals3;
    }

    public FloatBuffer getBlueVals1() {
        return blueVals1;
    }

    public FloatBuffer getBlueVals2() {
        return blueVals2;
    }

    public FloatBuffer getBlueVals3() {
        return blueVals3;
    }
}
