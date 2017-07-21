package imageCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import toolWindow.RTIViewer;
import utils.Utils;

import static imageCreation.ImageCreator.calcYUV;
import static imageCreation.ImageCreator.getRGB;
import static utils.Utils.applyDiffGain;

import java.nio.FloatBuffer;


/**
 * Creates images for {@link PTMObjectRGB} objects, given the specific rendering rending parameters for the relevant
 * RTI enhancement. Provides static methods that are used in {@link ImageCreator} to calculate the images for any of
 * the enhancements for HSH.
 *
 * @see PTMObjectRGB
 * @see ImageCreator
 */
public class ImageCreatorPTM_RGB {


    /**
     * Creates an image of the {@link PTMObjectRGB} given the specific rendering mode, light position, and rendering
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

        PTMObjectRGB ptmObjectRGB = (PTMObjectRGB) rtiObject;

        //calculate the relevant image for the given rendering mode
        if(shaderProgram.equals(RTIViewer.ShaderProgram.DEFAULT)){
            return createDefaultImage(ptmObjectRGB, lightX, lightY, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            return createDiffGainImage(ptmObjectRGB, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            return ImageCreator.createNormalsImage(ptmObjectRGB, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            return createSpecEnhanceImage(ptmObjectRGB, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            return createImgUnMaskImage(ptmObjectRGB, lightX, lightY, red, green, blue, shaderParams);

        }else{
            return null;
        }

    }


    /**
     * Creates a WritableImage for the given PTMObjectLRGB using the default rendering mode, given the light
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
    private static WritableImage createDefaultImage(PTMObjectRGB rtiObject, float lightX, float lightY,
                                                    boolean red, boolean green, boolean blue){

        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++){
            for(int y = 0; y < rtiObject.getHeight(); y++){
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate the rgb values by applying the PTM equation to the coefficients for each channel
                if(red) {
                    r = Utils.calcIntensity(rtiObject.getRedVals1(), rtiObject.getRedVals2(),
                                                                            position, lightX, lightY) / 255.0f;
                }else{r = 0;}

                if(green){
                    g = Utils.calcIntensity(rtiObject.getGreenVals1(), rtiObject.getGreenVals2(),
                                                                            position, lightX, lightY) / 255.0f;
                }else{g = 0;}

                if(blue) {
                    b = Utils.calcIntensity(rtiObject.getBlueVals1(), rtiObject.getBlueVals2(),
                                                                            position, lightX, lightY) / 255.0f;
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }
        return writableImage;
    }




    /**
     * Creates a WritableImage of the given RTIObject using the diffuse gain.  Will only write red, green and blue
     * channels if their arguments are true. See the RTIViewer user guide for the paper for this enhancement.
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
    private static WritableImage createDiffGainImage(PTMObjectRGB rtiObject, float lightX, float lightY,
                                                     boolean red, boolean green, boolean blue, float[] shaderParams){

        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position, r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //calculate the rgb values by applying the diffuse gain equation to the coefficients for each channel
                if(red) {
                    r = Utils.toByte(applyDiffGain(rtiObject.getRedVals1(), rtiObject.getRedVals2(),
                            position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{r = 0;}

                if(green){
                    g = Utils.toByte(applyDiffGain(rtiObject.getGreenVals1(), rtiObject.getGreenVals2(),
                            position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{g = 0;}

                if(blue) {
                    b = Utils.toByte(applyDiffGain(rtiObject.getBlueVals1(), rtiObject.getBlueVals2(),
                            position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.rgb(r, g, b));
            }
        }

        return writableImage;
    }


    /**
     * Creates a WritableImage of the given {@link PTMObjectRGB} using the specular enhancement filter, and the
     * diffuse colour, specularity and highlight size given in the shaderParams argument, in that order. See the
     * RTIViewer user guide for the original paper for this enhancement. Will only write red, green and blue channels
     * if their arguments are true.
     *
     * @param ptmObjectRGB      object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      diffuse colour, specularity, and highlight size vals in that order
     * @return                  image with specular enhancement filter
     */
    private static WritableImage createSpecEnhanceImage(PTMObjectRGB ptmObjectRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams){

        WritableImage writableImage = new WritableImage(ptmObjectRGB.getWidth(), ptmObjectRGB.getHeight());

        //create a normalised light vector for the incident light
        Utils.Vector3f hVector = new Utils.Vector3f(lightX / 2.0f, lightY / 2.0f, 0.5f);
        hVector = hVector.normalise();

        FloatBuffer normals = ptmObjectRGB.getNormals();

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position, r, g, b;
        for(int x = 0; x < ptmObjectRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectRGB.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of r,g,b vectors )
                position = ((y * ptmObjectRGB.getWidth()) + x) * 3;

                //dot product the normal with the light vector, and raise this to the power of the
                //highlight size parameter of specular enhancement
                float nDotH  =  hVector.x * normals.get(position)       +
                                hVector.y * normals.get(position + 1)   +
                                hVector.z * normals.get(position + 2);

                if(nDotH < 0.0){nDotH = 0.0f;}
                else if(nDotH > 1.0){nDotH = 1.0f;}
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);

                //calculate the rgb values by applying the PTM equation to the coefficients for each channel
                r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(),
                                                                            position, lightX, lightY);
                g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(),
                                                                            position, lightX, lightY);
                b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(),
                                                                            position, lightX, lightY);

                //calculate the luminance for this pixel
                float temp = (r + g + b) / 3;
                temp = temp * shaderParams[1] * 2 * nDotH;

                //multiply by the diffuse colour parameter and add the temp val
                if(red){r = Utils.toByte(r * shaderParams[0] + temp);}
                else{r = 0;}

                if(green){g = Utils.toByte(g * shaderParams[0] + temp);}
                else{g = 0;}

                if(blue){b = Utils.toByte(b * shaderParams[0] + temp);}
                else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.rgb(r, g, b));

            }
        }
        return writableImage;
    }





    /**
     * Creates a new WritableImage of this RTIObject using the light position, using the image unsharp masking
     * enhancement. Will only write red, green and blue channels if their arguments are true. The shaderParams
     * for this enhancement just have one float in for the 'gain'. See the RTIViewer user guide for the original
     * paper for this technique.
     *
     * @param ptmObjectRGB      object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      the 'gain' value for this enhancement
     * @return                  image using the image unsharp masking enhancement
     */
    private static WritableImage createImgUnMaskImage(PTMObjectRGB ptmObjectRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams) {

        WritableImage writableImage = new WritableImage(ptmObjectRGB.getWidth(), ptmObjectRGB.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float r, g, b;
        for(int x = 0; x < ptmObjectRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectRGB.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of r,g,b vectors )
                position = ((y * ptmObjectRGB.getWidth()) + x) * 3;

                //calculate the rgb values by applying the PTM equation to the coefficients for each channel
                r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(),
                        position, lightX, lightY) / 255.0f;
                g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(),
                        position, lightX, lightY) / 255.0f;
                b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(),
                        position, lightX, lightY) / 255.0f;

                //convert the rgb colour space to yuv to get the luminance
                float[] yuv = calcYUV(r, g, b);

                //calculate the enhanced luminance for this pixel by averaging the luminance of surrounding pixels
                //and applying the image gain, see calcEnhancedLum
                float enhancedLum = calcEnhancedLum(ptmObjectRGB, yuv[0], x, y, shaderParams[0], lightX, lightY);

                //go back to rgb colour space using the new enhanced luminance
                float[] rgb = getRGB(enhancedLum, yuv[1], yuv[2]);

                //only write the rgb values if their boolean values ar true
                if(red){r = rgb[0];}
                else{r = 0;}

                if(r > 1.0){r = 1.0f;}
                else if(r < 0){r = 0;}

                if(green){g = rgb[1];}
                else{g = 0;}

                if(g > 1.0){g = 1.0f;}
                else if(g < 0){g = 0;}

                if(blue){b = rgb[2];}
                else{b = 0;}

                if(b > 1.0){b = 1.0f;}
                else if(b < 0){b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }

        return writableImage;
    }





    /**
     * Calculates the enhanced luminace for the pixel with x and y position by averaging the luminance of the pixels
     * in a block of 4x4 around it, then applying the enhancement using the gain param.
     *
     * @param ptmObjectRGB      RTIObject with the HSH coefficients
     * @param lum               luminance of the center pixel to calculate enhanced luminance for
     * @param x                 x position of the pixel to calculate enhanced luminance for
     * @param y                 y position of the pixel to calculate enhanced luminance for
     * @param imgUnMaskGain     gain value for the ehnhanced luminance
     * @param lightX            x position of the incident light
     * @param lightY            y position of the incident light
     * @return                  the enhanced luminance for the pixel with given x and y
     */
    private static float calcEnhancedLum(PTMObjectRGB ptmObjectRGB, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY){
        int distance = 2;
        float tempLum = 0;

        //average the luminance from around the center pixel
        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(ptmObjectRGB, x + xOffset, y + yOffset, lightX, lightY);
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
     * @param ptmObjectRGB  RTIObject containing the coefficient data
     * @param x             x position of the pixel to get lum from
     * @param y             y position of the pixel to get lum from
     * @param lightX        x postion of the incident light vector
     * @param lightY        y postion of the incident light vector
     * @return              the luminance of the pixel with position (x,y)
     */
    private static float getLumFromCoord(PTMObjectRGB ptmObjectRGB, int x, int y, float lightX, float lightY){
        //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
        int position = ((y * ptmObjectRGB.getWidth()) + x) * 3;

        float r = 0;
        float g = 0;
        float b = 0;

        //get the RGB and if it's outside, the size of the RTIObject, return 0
        if(x > ptmObjectRGB.getWidth() - 1 || y > ptmObjectRGB.getHeight() - 1 || x < 0 || y < 0) {
            r = 0; g = 0; b = 0;
        }else{
            r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(),
                    position, lightX, lightY) / 255.0f;
            g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(),
                    position, lightX, lightY) / 255.0f;
            b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(),
                    position, lightX, lightY) / 255.0f;
        }

        //convert toy YUV colourspace to get the luminance for this pixel
        return (float) (r * 0.299 + g * 0.587 + b * 0.144);
    }


}
