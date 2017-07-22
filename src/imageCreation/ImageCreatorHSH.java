package imageCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import ptmCreation.RTIObjectHSH;
import toolWindow.RTIViewer;
import utils.Utils;

import java.nio.FloatBuffer;

import static java.lang.Math.*;

/**
 * Creates images for {@link RTIObjectHSH} objects, given the specific rendering rending parameters for the relevant
 * RTI enhancement. Provides static methods that are used in {@link ImageCreator} to calculate the images for any of
 * the enhancements for HSH.
 *
 * @see RTIObjectHSH
 * @see ImageCreator
 */
public class ImageCreatorHSH {


    /** This rendering param for normals enhancement is fixed in this version of the viewer*/
    private static final float NORM_ENHANCE_GAIN = 1.0f;

    /** This rendering param for normals enhancement is fixed in this version of the viewer*/
    private static final float NORM_ENHANCE_ENV = 1.5f;


    /**
     * Creates an image of the {@link RTIObjectHSH} given the specific rendering mode, light position, and rendering
     * parameters. Will only save the colour channels specified by the red, green, and blue arguments.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param shaderProgram     current image filter type to save the snapshot of
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      parameters of the specific shader= program used
     * @return                  image with the rendering of the shaderProgram arg
     */
    public static WritableImage createImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                            boolean red, boolean green, boolean blue, float[] shaderParams){

        RTIObjectHSH rtiObjectHSH = (RTIObjectHSH) rtiObject;

        //calculate the relevant image for the given rendering mode
        if(shaderProgram.equals(RTIViewer.ShaderProgram.DEFAULT)){
            return createDefaultImage(rtiObjectHSH, lightX, lightY, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            return createNormalEnhanceImage(rtiObjectHSH, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            return ImageCreator.createNormalsImage(rtiObjectHSH, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            return createSpecEnhanceImage(rtiObjectHSH, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            return createImgUnMaskImage(rtiObjectHSH, lightX, lightY, red, green, blue, shaderParams);

        }else{
            return null;
        }
    }




    /**
     * Creates a WritableImage for the given RTIObjectHSH using the default rendering mode, given the light
     * x and y positions passed. Will only write red, green and blue channels if their arguments are true.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @return                  image with default rendering
     */
    private static WritableImage createDefaultImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                        boolean red, boolean blue, boolean green){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //calculate the mysterious 'hWeights' that are used to turn the HSH values into colours
        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float[] rgb;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate rgb for this pixel from the hWeights
                rgb = getRGB(rtiObject, position, hWeights);

                //clamp the RGB values between 0 and 255
                clampRGB(rgb, red, green, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return writableImage;
    }




    /**
     * Creates a WritableImage of the given RTIObject using the normals enhancement filter. The normals gain
     * and environment parameters of this enhancement are fixed in this version fo the viewer to
     * {@link ImageCreatorHSH#NORM_ENHANCE_GAIN} and {@link ImageCreatorHSH#NORM_ENHANCE_ENV} as they don't
     * seem to actually change the image too much. Will only write red, green and blue channels if their
     * arguments are true. See the RTIViewer user guide for the paper for this enhancement.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      parameters of the specific shader= program used
     * @return                  image using normals enhancement
     */
    private static WritableImage createNormalEnhanceImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                    boolean red, boolean blue, boolean green, float[] shaderParams){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //calculate the z value of the light vector from the x and y using pythagoras
        double lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

        //calculate the mysterious 'hWeights' that are used to turn the HSH values into colours
        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        //loop through all the pixels and calculate the colour of them using the normals enhancement algorithm
        int position;
        float[] rgb;
        Utils.Vector3f normal, smoothedNormal, enhancedNormal;
        float enhancement;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate rgb fpr this pixel from the hWeights
                rgb = getRGB(rtiObject, position, hWeights);

                //find the normal vector od this point from the flattened normals array
                normal = new Utils.Vector3f(rtiObject.getNormals().get(position),
                                            rtiObject.getNormals().get(position + 1),
                                            rtiObject.getNormals().get(position + 2));

                //smooth the normal by averaging it with the normals in the surrounding pixels
                smoothedNormal = getSmoothedNormal(rtiObject, x, y);

                //enhance this pixel's normal by pointing it in the opposite direction than the smoothed one
                enhancedNormal = getEnhancedNormal(normal, smoothedNormal);

                //dot product the enhanced normal with the light vector to get the enhanced luminance of this pixel
                enhancement = getEnhancement(enhancedNormal, lightX, lightY, lightZ, shaderParams[0]);

                //multiply this colour by the calculated enhanced luminance
                rgb[0] *= enhancement;
                rgb[1] *= enhancement;
                rgb[2] *= enhancement;

                //clamp the colours between 0 and 255
                clampRGB(rgb, red, green, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return writableImage;
    }




    /**
     * Creates a WritableImage of the given {@link RTIObjectHSH} using the specular enhancement filter, and the
     * diffuse colour, specularity and highlight size given in the shaderParams argument, in that order. See the
     * RTIViewer user guide for the original paper for this enhancement. Will only write red, green and blue channels
     * if their arguments are true.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      diffuse colour, specularity, and highlight size vals in that order
     * @return                  image with specular enhancement filter
     */
    private static WritableImage createSpecEnhanceImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                        boolean red, boolean blue, boolean green, float[] shaderParams){
        WritableImage image = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //calculate the mysterious 'hWeights' that are used to turn the HSH values into colours
        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        //create a normalised light vector for the incident light
        Utils.Vector3f hVector = new Utils.Vector3f(lightX, lightY, 1.0f);
        hVector.multiply(0.5f);
        hVector = hVector.normalise();

        //loop through all the pixels and calculate the colour of them using the normals enhancement algorithm
        int position;
        float[] rgb;
        Utils.Vector3f normal;
        float nDotH, temp, lum;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate rgb for this pixel from the hWeights
                rgb = getRGB(rtiObject, position, hWeights);

                //get the normal vector for this pixel from the flattened array
                normal = new Utils.Vector3f(rtiObject.getNormals().get(position),
                        rtiObject.getNormals().get(position + 1),
                        rtiObject.getNormals().get(position + 2));

                //dot product the normal with the light vector, and raise this to the power of the
                //highlight size parameter of specular enhancement
                nDotH = hVector.dot(normal);
                if(nDotH < 0.0){nDotH = 0.0f;}
                else if(nDotH > 1.0){nDotH = 1.0f;}
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);

                //calculate the luminance for this pixel
                temp = (rgb[0] + rgb[1] + rgb[2]) / 3.0f;
                lum = (float)(temp * shaderParams[1] * 4.0 * nDotH);

                //the final colour is a product of the original colour times by the diffuse colour arg,
                //plus the enhanced luminance from specular enhancement
                rgb[0] = rgb[0] * shaderParams[0] + lum;
                rgb[1] = rgb[1] * shaderParams[0] + lum;
                rgb[2] = rgb[2] * shaderParams[0] + lum;

                //clamp the colours between 0 and 255
                clampRGB(rgb, red, green, blue);

                image.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return image;
    }




    /**
     * Creates a new WritableImage of this RTIObject using the light position, using the image unsharp masking
     * enhancement. Will only write red, green and blue channels if their arguments are true. The shaderParams
     * for this enhancement just have one float in for the 'gain'. See the RTIViewer user guide for the original
     * paper for this technique.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      the 'gain' value for this enhancement
     * @return                  image using the image unsharp masking enhancement
     */
    private static WritableImage createImgUnMaskImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                             boolean red, boolean blue, boolean green, float[] shaderParams){

        WritableImage image = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //calculate the mysterious 'hWeights' that are used to turn the HSH values into colours
        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        //loop through all the pixels and calculate the colour of them using the normals enhancement algorithm
        int position;
        float[] rgb, yuv;
        float enhancedLum;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate rgb for this pixel from the hWeights
                rgb = getRGB(rtiObject, position, hWeights);

                //convert the rgb colour space to yuv to get the luminance
                yuv = ImageCreator.calcYUV(rgb[0], rgb[1], rgb[2]);

                //calculate the enhanced luminance for this pixel by averaging the luminance of surrounding pixels
                //and applying the image gain, see calcEnhancedLum
                enhancedLum = calcEnhancedLum(rtiObject, yuv[0], x, y, shaderParams[0], lightX, lightY, hWeights);

                //go back to rgb colour space using the new enhanced luminance
                rgb = ImageCreator.getRGB(enhancedLum, yuv[1], yuv[2]);

                //clamp the colours between 0 and 255
                clampRGB(rgb, red, green, blue);

                image.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return image;
    }




    /**
     * Calculates the enhanced luminace for the pixel with x and y position by averaging the luminance of the pixels
     * in a block of 4x4 around it, then applying the enhancement using the gain param.
     *
     * @param rtiObject         RTIObject with the HSH coefficients
     * @param lum               luminance of the center pixel to calculate enhanced luminance for
     * @param x                 x position of the pixel to calculate enhanced luminance for
     * @param y                 y position of the pixel to calculate enhanced luminance for
     * @param imgUnMaskGain     gain value for the ehnhanced luminance
     * @param lightX            x position of the incident light
     * @param lightY            y position of the incident light
     * @param hWeights          hWeights calculated from the light position
     * @return                  the enhanced luminance for the pixel with given x and y
     */
    private static float calcEnhancedLum(RTIObjectHSH rtiObject, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY, double[] hWeights){
        int distance = 2;
        float tempLum = 0;

        //average the luminance from around the center pixel
        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(rtiObject, x + xOffset, y + yOffset, hWeights);
            }
        }

        //apply the enhancement, this bit comes from the original RTIViewer
        tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
        tempLum = lum + imgUnMaskGain * (lum - tempLum);

        return tempLum;
    }




    /**
     * Gets the RGB values of the pixel with given (x, y) position, then converts them to luminance using the
     * same maths as in {@link ImageCreator#calcYUV(float, float, float)}. Returns 0 if the (x, y) position is outside
     * the width and height of the RTIObject passed.
     *
     * @param rtiObject     RTIObject containing the coefficient data
     * @param x             x position of the pixel to get lum from
     * @param y             y position of the pixel to get lum from
     * @param hWeights      hWeights calculated from the light position
     * @return              the luminance of the pixel with position (x,y)
     */
    private static float getLumFromCoord(RTIObjectHSH rtiObject, int x, int y, double[] hWeights){
        //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
        int position = ((y * rtiObject.getWidth()) + x) * 3;

        //get the RGB and if it's outside, the size of the RTIObject, return 0
        float[] rgb = new float[3];
        if(x > rtiObject.getWidth() - 1 || y > rtiObject.getHeight() - 1 || x < 0 || y < 0){
            rgb[0] = 0; rgb[1] = 0; rgb[2] = 0;
        }else {
                rgb = getRGB(rtiObject, position, hWeights);
        }

        //convert toy YUV colourspace to get the luminance for this pixel
        return (float) (rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.144);
    }




    /**
     * Clamps the given rgb float arry between 0 and 255. Also sets the given color channels to 0 if their
     * boolean argument is false.
     *
     * @param rgb       the rgb array  thatis to be clamped
     * @param red       rgb[0] (r) will be 0 if this is true
     * @param green     rgb[1] (g) will be 0 is this is true
     * @param blue      rgb[2] (b) will be 0 id this is true
     */
    private static void clampRGB(float[] rgb, boolean red, boolean green, boolean blue){
        if(!red || rgb[0] < 0.0){rgb[0] = 0.0f;}
        else if(rgb[0] > 1.0f){rgb[0] = 1.0f;}

        if(!green || rgb[1] < 0.0){rgb[1] = 0.0f;}
        else if(rgb[1] > 1.0f){rgb[1] = 1.0f;}

        if(!blue || rgb[2] < 0.0){rgb[2] = 0.0f;}
        else if(rgb[2] > 1.0f){rgb[2] = 1.0f;}
    }




    /**
     * Converts the light x, y and z positions passed to spherical coordinates, then uses this theta and phi
     * to calculate the hWeights that are required for calculating the colour of a HSH pixel.
     *
     * @param basisTerms    number of terms in the HSH rti
     * @param lightX        x position of the light in the rendering
     * @param lightY        y position of the light in the rendering
     * @return              the hWeights array
     */
    private static double[] calcAnglesAndHWeights(int basisTerms, float lightX, float lightY){
        //calculate the light z for a normalised vector using pythagoras
        double lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

        //convert the cartesian light vector to spherical coordinates
        double phi = atan2(lightY, lightX);
        if(phi < 0){phi = 2 * PI + phi;}

        double theta = min(acos(lightZ), PI /2 - 0.04);

        //use this to calculate the hWeights
        return Utils.createHWeights(theta, phi, basisTerms);
    }




    /**
     * Calculates the enhanced luminance for a normal vector using the image unsharp masking enhancement,
     * given the light position and gain values passed.
     *
     * @param enhancedNormal    enhanced normal vector for this pixel
     * @param lightX            light x position in this rendering
     * @param lightY            light y position in this rendering
     * @param lightZ            light z position in this rendering
     * @param diffGain          normals enhancement diffuse gain value
     * @return                  the enhanced luminance for this pixel
     */
    private static float getEnhancement(Utils.Vector3f enhancedNormal, double lightX,
                                                                double lightY, double lightZ, float diffGain){
        //dot product the light and normal vector
        double nDotL = enhancedNormal.x * lightX + enhancedNormal.y * lightY + enhancedNormal.z * lightZ;

        //clamp it
        if(nDotL < 0.0){nDotL = 0.0;}
        else if(nDotL > 1.0){nDotL = 1.0;}

        //use the image unsharp masking enhancement
        return (float)((diffGain) * nDotL + NORM_ENHANCE_ENV) / ((diffGain) + NORM_ENHANCE_ENV);
    }




    /**
     * Calculates an 'enhanced' normal for the pixel by taking away the smoothed normal from the actual normal.
     *
     * @param normal            normal vector for the pixel
     * @param smoothNormal      smoothed normal vector for the pixel
     * @return                  the enhanced normal vector for the pixel
     */
    private static Utils.Vector3f getEnhancedNormal(Utils.Vector3f normal, Utils.Vector3f smoothNormal){
        Utils.Vector3f enhanced = normal.add((normal.minus(smoothNormal)).multiply(10 * NORM_ENHANCE_GAIN));
        return enhanced.normalise();
    }




    /**
     * Calculates the smoothed normal around a pixel with given (x,y ) postion by averaging the normals in a
     * block of side length 10 around the pixel.
     *
     * @param rtiObject     the RTIObject containing the texel data
     * @param x             x position of the pixel to get the smoothed normal for
     * @param y             x position of the pixel to get the smoothed normal for
     * @return              smoothed normal for the pixel with (x, y) position
     */
    private static Utils.Vector3f getSmoothedNormal(RTIObjectHSH rtiObject, int x, int y){
        int dist = 5;

        Utils.Vector3f smoothedNormal = new Utils.Vector3f(0.0f, 0.0f, 0.0f);

        //go ina block  of side length 2 * dist around the pixel and total the components of the vectors
        int position;
        for(int xOffset = -dist; xOffset <= dist; xOffset++){
            for(int yOffset = -dist; yOffset <= dist; yOffset++){
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = (((y + yOffset) * rtiObject.getWidth()) + (x + xOffset)) * 3;

                try {
                    //5 seems to be a good number to get good enhancement here
                    smoothedNormal.x += 5 * rtiObject.getNormals().get(position);
                    smoothedNormal.y += 5 * rtiObject.getNormals().get(position + 1);
                    smoothedNormal.z += 5 * rtiObject.getNormals().get(position + 2);
                }catch (IndexOutOfBoundsException e){}
            }
        }

        //divide by number of pixels used to get average
        smoothedNormal.x /= (2 * dist + 1) * (2 * dist + 1);
        smoothedNormal.y /= (2 * dist + 1) * (2 * dist + 1);
        smoothedNormal.z /= (2 * dist + 1) * (2 * dist + 1);

        return smoothedNormal.normalise();
    }




    /**
     * Gets the coefficients from the RTIObjectHSH's coefficient arrays at the given position, and uses
     * {@link ImageCreatorHSH#createColours(float[], float[], float[], int, double[])} to calculate the colour for
     * the pixel.
      *
     * @param rtiObject     object containing the data
     * @param position      positon in the 1D array of the pixel
     * @param hWeights      hWeights for the current light vector
     * @return              rgb values for the pixel at position
     */
    private static float[] getRGB(RTIObjectHSH rtiObject, int position, double[] hWeights){
        float[] redVals = new float[9];
        float[] greenVals = new float[9];
        float[] blueVals = new float[9];

        //get the actual texel data for this position
        getTexelData(redVals, rtiObject.getRedVals1(), rtiObject.getRedVals2(),
                rtiObject.getRedVals3(), rtiObject.getBasisTerms(), position);

        getTexelData(greenVals, rtiObject.getGreenVals1(), rtiObject.getGreenVals2(),
                rtiObject.getGreenVals3(), rtiObject.getBasisTerms(), position);

        getTexelData(blueVals, rtiObject.getBlueVals1(), rtiObject.getBlueVals2(),
                rtiObject.getBlueVals3(), rtiObject.getBasisTerms(), position);

        //now calculate the rgb values from it
        return  createColours(redVals, greenVals, blueVals, rtiObject.getBasisTerms(), hWeights);
    }




    /**
     * Uses the hWeights passes to calculate the rgb values from the HSH texel data given in the passed arrays.
     * Will only apply hWeights up to basisTerms.
     *
     * @param redVals       HSH vals from the red channel for this pixel
     * @param greenVals     HSH vals from the green channel for this pixel
     * @param blueVals      HSH vals data from the blue channel for this pixel
     * @param basisTerms    number of HSH terms used
     * @param hWeights      hWeigghts for the current light vector
     * @return              the rgb intensities calculated from the HSH values and hWeights
     */
    private static float[] createColours(float[] redVals, float[] greenVals, float[] blueVals,
                                         int basisTerms, double[] hWeights){
        float[] rgb = new float[]{0, 0, 0};
        float[][] colourVals = new float[][]{redVals, greenVals, blueVals};

        //apply hWeights up to basisTerms
        for(int i = 0; i < 3; i++) {
            for (int k = 0; k < basisTerms; k++) {
                rgb[i] += colourVals[i][k] * hWeights[k];
            }
        }
        return rgb;
    }





    /**
     * Stores the HSH coefficients for a texel at position in out. The number of non-zero coefficients will be the value
     * of basis terms.
     *
     * @param out           the array to write the values to
     * @param buffer0       the array containing the HSH values 0, 1, and 2
     * @param buffer1       the array containing the HSH values 3, 4, and 5, if present
     * @param buffer2       the array containing the HSH values 6, 7, and 8, if present
     * @param basisTerms    number of hshTerms
     * @param position      position in 1D array of the texel to get the coeffs for
     */
    private static void getTexelData(float[] out, FloatBuffer buffer0, FloatBuffer buffer1,
                                        FloatBuffer buffer2, int basisTerms, int position){
        out[0] = buffer0.get(position);
        out[1] = buffer0.get(position + 1);
        out[2] = buffer0.get(position + 2);

        //if basisTerms <= 3, buffer2 will only be of length 3 so is not used
        if(basisTerms > 3){
            out[3] = buffer1.get(position);
            out[4] = buffer1.get(position + 1);
            out[5] = buffer1.get(position + 2);
        }

        //if basisTerms <= 6, buffer2 will only be of length 3 so is not used
        if(basisTerms > 6){
            out[6] = buffer2.get(position);
            out[7] = buffer2.get(position + 1);
            out[8] = buffer2.get(position + 2);
        }
    }

}
