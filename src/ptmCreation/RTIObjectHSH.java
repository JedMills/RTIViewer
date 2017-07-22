package ptmCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import java.nio.FloatBuffer;

import static java.lang.Math.*;

/**
 * This class represents a HSH (Hemispherical Harmonics)object that is loaded from a .rti file on the disk.
 * HSH objects have a varying number of HSH coefficients per pixel, depending on the order of the HSH object. The
 * colour for each pixel of an HSH object is calculated by determining HSH 'hWeights' depending on the angle of
 * light. There is a link to the original HSH paper in the user guide for this app.
 *
 * @see RTIObject
 *
 * @author Jed Mills
 */
public class RTIObjectHSH extends RTIObject {

    /** Number of HSH terms that this HSH objetc uses to simulate colour, num terms terms = order squared*/
    private int basisTerms;

    /** Flattened array containing up to the first 3 HSH coefficients per pixel for the red channel*/
    private FloatBuffer redVals1;

    /** Flattened array containing up to the second 3 HSH coefficients per pixel for the red channel*/
    private FloatBuffer redVals2;

    /** Flattened array containing up to the third 3 HSH coefficients per pixel for the red channel*/
    private FloatBuffer redVals3;

    /** Flattened array containing up to the first 3 HSH coefficients per pixel for the green channel*/
    private FloatBuffer greenVals1;

    /** Flattened array containing up to the second 3 HSH coefficients per pixel for the green channel*/
    private FloatBuffer greenVals2;

    /** Flattened array containing up to the third 3 HSH coefficients per pixel for the green channel*/
    private FloatBuffer greenVals3;

    /** Flattened array containing up to the first 3 HSH coefficients per pixel for the blue channel*/
    private FloatBuffer blueVals1;

    /** Flattened array containing up to the second 3 HSH coefficients per pixel for the blue channel*/
    private FloatBuffer blueVals2;

    /** Flattened array containing up to the third 3 HSH coefficients per pixel for the blue channel*/
    private FloatBuffer blueVals3;



    /**
     * Create a new HSH object width given width and height from the given texel data. If an HSH object has
     * <= 3 coefficients per pixel, the FloatBuffers in the textData attribute marked with a * or ** below can
     * be of length 3, with 3 zeros in, as these textures won't be used by the OpenGl shaders to render, but still
     * need to be bound, even is they have one element in. If the HSH object ahs <= 6 terms, FloatBuffers marked with
     * ** can be of length 3 with 3 zeros.
     *
     * The texel data should be in the order of:
     * <ol>
     *     <li>an IntBuffer containing the first 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     *
     *     <li>an IntBuffer containing the first 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     *
     *     <li>an IntBuffer containing the first 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the second 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3. *</li>
     *
     *     <li>an IntBuffer containing the third 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3. **</li>
     * </ol>
     *
     * @param fileName          name of the the file containing the PTM object
     * @param width             width of the PTM
     * @param height            height of the PTM
     * @param texelData         see above for the details of this argument
     */
    public RTIObjectHSH(String fileName, int width, int height, int numColourChannels,
                        int basisTerms, int basisType, FloatBuffer[] texelData) {
        super(fileName, width, height);

        this.basisTerms = basisTerms;

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




    /**
     * Calculates the normals vector for each texel using the HSH coefficients per colour pixel. The maths for this
     * method comes from the original RTI viewer, which in turn comes from the original HSH paper, which there is
     * a link to in the user guide for this app.
     *
     * @see RTIObject#calculateNormals()
     * @see RTIObject#calculateNormal(int[])
     */
    @Override
    protected void calculateNormals() {
        //calcuating three different sets of hWeigths for lights at different azimuths
        double[] hWeights1 = Utils.createHWeights((float)PI / 4, (float)PI / 6, basisTerms);
        double[] hWeights2 = Utils.createHWeights((float)PI / 4, 5 * (float)PI / 6, basisTerms);
        double[] hWeights3 = Utils.createHWeights((float)PI / 4, 3 * (float)PI / 2, basisTerms);

        //this inverse matrix comes from the original HSH paper
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
                //offset in the 1D arrays from the 2D position
                offset = (y * width + x) * 3;
                temp.setX(0.0f);
                temp.setY(0.0f);
                temp.setZ(0.0f);

                //apply this multiplication of certain hWeights to the coefficients for red green and blue.
                //don't really know how this works to be honest
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += redVals1.get(offset + k) * hWeights1[k];}
                    else if(k < 6){temp.x += redVals2.get(offset + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += redVals3.get(offset + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += redVals1.get(offset + k) * hWeights2[k];}
                    else if(k < 6){temp.y += redVals2.get(offset + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += redVals3.get(offset + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += redVals1.get(offset + k) * hWeights3[k];}
                    else if(k < 6){temp.z += redVals2.get(offset + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += redVals3.get(offset + k - 6) * hWeights3[k];}
                }
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += greenVals1.get(offset + k) * hWeights1[k];}
                    else if(k < 6){temp.x += greenVals2.get(offset + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += greenVals3.get(offset + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += greenVals1.get(offset + k) * hWeights2[k];}
                    else if(k < 6){temp.y += greenVals2.get(offset + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += greenVals3.get(offset + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += greenVals1.get(offset + k) * hWeights3[k];}
                    else if(k < 6){temp.z += greenVals2.get(offset + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += greenVals3.get(offset + k - 6) * hWeights3[k];}
                }
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){temp.x += blueVals1.get(offset + k) * hWeights1[k];}
                    else if(k < 6){temp.x += blueVals2.get(offset + k - 3) * hWeights1[k];}
                    else if(k < 9){temp.x += blueVals3.get(offset + k - 6) * hWeights1[k];}

                    if(k < 3){temp.y += blueVals1.get(offset + k) * hWeights2[k];}
                    else if(k < 6){temp.y += blueVals2.get(offset + k - 3) * hWeights2[k];}
                    else if(k < 9){temp.y += blueVals3.get(offset + k - 6) * hWeights2[k];}

                    if(k < 3){temp.z += blueVals1.get(offset + k) * hWeights3[k];}
                    else if(k < 6){temp.z += blueVals2.get(offset + k - 3) * hWeights3[k];}
                    else if(k < 9){temp.z += blueVals3.get(offset + k - 6) * hWeights3[k];}
                }

                //average and normalise it
                temp.multiply(0.33333333f);
                normal = Utils.mat3x3_mul_vec3(lInverse, temp);
                normal = normal.normalise();

                //store it in the normals array
                normals.put(offset, normal.getX());
                normals.put(offset + 1, normal.getY());
                normals.put(offset + 2, normal.getZ());
            }
        }
    }




    /**
     * Creates the preview image used for showing in the preview tab, using default rendering and light at position
     * (0, 0), and stores it in the {@link RTIObject#previewImage} attribute. Uses the process described in the
     * documentation for this class to calculate the colour of each pixel
     *
     * @see RTIObject#previewImage
     * @see RTIObject#createPreviewImage()
     */
    @Override
    protected void createPreviewImage() {
        //create the image to write the pixels to
        previewImage = new WritableImage(width, height);

        //the light position for the prevew image is (0, 0, 1)
        float lightX = 0.0f;
        float lightY = 0.0f;
        float lightZ = 1.0f;

        //turn this position into spherical coordinates
        float phi = (float)Math.atan2(lightY, lightX);
        float theta = (float) min(acos(lightZ), PI / 2 - 0.04);

        //calculate the mysterious hWeights for this light angle
        double[] hWeights = Utils.createHWeights(theta, phi, basisTerms);

        int offset;
        float r, g, b;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                //position in 1D arrays from the 2D pixel location
                offset = (y * width + x) * 3;

                r = 0.0f;
                g = 0.0f;
                b = 0.0f;

                //use the hWeights to calculate the reg, green and blue values for each pixel
                //the maths for this comes from the original RTIViewer, and I'm not too sure how it workds
                for(int k = 0; k < basisTerms; k++){
                    if(k < 3){r += redVals1.get(offset + k) * (hWeights[k]);}
                    else if(k < 6){r += redVals2.get(offset + k - 3) * (hWeights[k]);}
                    else if(k < 9){r += redVals3.get(offset + k - 6) * (hWeights[k]);}

                    if(k < 3){g += greenVals1.get(offset + k) * hWeights[k];}
                    else if(k < 6){g += greenVals2.get(offset + k - 3) * hWeights[k];}
                    else if(k < 9){g += greenVals3.get(offset + k - 6) * hWeights[k];}

                    if(k < 3){b += blueVals1.get(offset + k) * hWeights[k];}
                    else if(k < 6){b += blueVals2.get(offset + k - 3) * hWeights[k];}
                    else if(k < 9){b += blueVals3.get(offset + k - 6) * hWeights[k];}
                }

                //clamp the values between 0.0 and 1.0
                if(r > 1.0){r = 1.0f;}
                else if(r < 0){r = 0;}

                if(g > 1.0){g = 1.0f;}
                else if(g < 0){g = 0;}

                if(b > 1.0){b = 1.0f;}
                else if(b < 0){b = 0;}

                //store them in the image
                previewImage.getPixelWriter().setColor(x, y, Color.color(r, g, b) );
            }
        }
    }


    /**
     * @return  {@link RTIObjectHSH#basisTerms}
     */
    public int getBasisTerms() {
        return basisTerms;
    }


    /**
     * @return {@link RTIObjectHSH#redVals1}
     */
    public FloatBuffer getRedVals1() {
        return redVals1;
    }


    /**
     * @return {@link RTIObjectHSH#redVals2}
     */
    public FloatBuffer getRedVals2() {
        return redVals2;
    }


    /**
     * @return {@link RTIObjectHSH#redVals3}
     */
    public FloatBuffer getRedVals3() {
        return redVals3;
    }


    /**
     * @return {@link RTIObjectHSH#greenVals1}
     */
    public FloatBuffer getGreenVals1() {
        return greenVals1;
    }


    /**
     * @return {@link RTIObjectHSH#greenVals2}
     */
    public FloatBuffer getGreenVals2() {
        return greenVals2;
    }


    /**
     * @return {@link RTIObjectHSH#greenVals3}
     */
    public FloatBuffer getGreenVals3() {
        return greenVals3;
    }


    /**
     * @return {@link RTIObjectHSH#blueVals1}
     */
    public FloatBuffer getBlueVals1() {
        return blueVals1;
    }


    /**
     * @return {@link RTIObjectHSH#blueVals2}
     */
    public FloatBuffer getBlueVals2() {
        return blueVals2;
    }


    /**
     * @return {@link RTIObjectHSH#blueVals3}
     */
    public FloatBuffer getBlueVals3() {
        return blueVals3;
    }
}
