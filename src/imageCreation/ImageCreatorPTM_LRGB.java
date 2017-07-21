package imageCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import ptmCreation.PTMObjectLRGB;
import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import toolWindow.RTIViewer;
import utils.Utils;

import static imageCreation.ImageCreator.calcYUV;
import static imageCreation.ImageCreator.getRGB;
import static utils.Utils.applyDiffGain;


/**
 * Creates images for {@link PTMObjectLRGB} objects, given the specific rendering rending parameters for the relevant
 * RTI enhancement. Provides static methods that are used in {@link ImageCreator} to calculate the images for any of
 * the enhancements for HSH.
 *
 * @see PTMObjectLRGB
 * @see ImageCreator
 */
public class ImageCreatorPTM_LRGB {


    /**
     * Creates an image of the {@link PTMObjectLRGB} given the specific rendering mode, light position, and rendering
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

        PTMObjectLRGB ptmObjectLRGB = (PTMObjectLRGB) rtiObject;

        //calculate the relevant image for the given rendering mode
        if(shaderProgram.equals(RTIViewer.ShaderProgram.DEFAULT)){
            return createDefaultImage(ptmObjectLRGB, lightX, lightY, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            return createDiffGainImage(ptmObjectLRGB, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            return ImageCreator.createNormalsImage(ptmObjectLRGB, red, green, blue);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            return createSpecEnhanceImage(ptmObjectLRGB, lightX, lightY, red, green, blue, shaderParams);

        }else if(shaderProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            return createImgUnMaskImage(ptmObjectLRGB, lightX, lightY, red, green, blue, shaderParams);

        }else{
            return null;
        }
    }




    /**
     * Creates a WritableImage for the given PTMObjectLRGB using the default rendering mode, given the light
     * x and y positions passed. Will only write red, green and blue channels if their arguments are true.
     *
     * @param ptmObjectLRGB     object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @return                  image with default rendering
     */
    private static WritableImage createDefaultImage(PTMObjectLRGB ptmObjectLRGB, float lightX,
                                                        float lightY, boolean red, boolean green, boolean blue){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                //calculate the luminance by applying the ptm equation to the lum coefficients
                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                                            ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / 255.0f;

                //get the rgb by multiplying the rgb coefficients by the luminace
                r = ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;

                //clamp the values between 0 and 255
                r = lum * r;
                r = clampChannel(r, red);

                g = lum * g;
                g = clampChannel(g, green);

                b = lum * b;
                b = clampChannel(b, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }
        return writableImage;
    }






    /**
     * Creates a WritableImage of the given RTIObject using the diffuse gain.  Will only write red, green and blue
     * channels if their arguments are true. See the RTIViewer user guide for the paper for this enhancement.
     *
     * @param ptmObjectLRGB     object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      parameters of the specific shader= program used
     * @return                  image using normals enhancement
     */
    private static WritableImage createDiffGainImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                     boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                //calculate enhanced luminance by applying the diff gain equation to the lum coeffs
                lum = applyDiffGain(ptmObjectLRGB.getLumCoeffs1(), ptmObjectLRGB.getLumCoeffs2(),
                                    position, ptmObjectLRGB.getNormals(), lightX, lightY, shaderParams[0]) / 255.0f;

                //get the rgb by multiplying the rgb coefficients by the enhanced luminace
                r = ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;

                //clamp the values between 0 and 255
                r = lum * r;
                r = clampChannel(r, red);

                g = lum * g;
                g = clampChannel(g, green);

                b = lum * b;
                b = clampChannel(b, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }
        return writableImage;
    }




    /**
     * Creates a WritableImage of the given {@link PTMObjectLRGB} using the specular enhancement filter, and the
     * diffuse colour, specularity and highlight size given in the shaderParams argument, in that order. See the
     * RTIViewer user guide for the original paper for this enhancement. Will only write red, green and blue channels
     * if their arguments are true.
     *
     * @param ptmObjectLRGB     object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      diffuse colour, specularity, and highlight size vals in that order
     * @return                  image with specular enhancement filter
     */
    private static WritableImage createSpecEnhanceImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        //create a normalised light vector for the incident light
        Utils.Vector3f hVector = new Utils.Vector3f(lightX, lightY, 1.0f);
        hVector.multiply(0.5f);
        hVector = hVector.normalise();

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float lum, r, g, b, nDotH;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                //calculate the luminance by applying the ptm equation to the lum coefficients
                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                        ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / (255.0f * 255.0f);

                //get the rgb coefficients for this pixel
                r = ptmObjectLRGB.getRgbCoeffs().get(position);
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1);
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2);

                //dot product the normal with the light vector, and raise this to the power of the
                //highlight size parameter of specular enhancement
                nDotH = hVector.x * ptmObjectLRGB.getNormals().get(position) +
                        hVector.y * ptmObjectLRGB.getNormals().get(position + 1) +
                        hVector.z * ptmObjectLRGB.getNormals().get(position + 2);

                nDotH = clampChannel(nDotH, true);
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);
                nDotH *= shaderParams[1] * 255.0f;

                //the final colours are a product of the three specular enhancement parameters
                r = ((r * shaderParams[0]) + nDotH) * lum;
                r = clampChannel(r, red);

                g = ((g * shaderParams[0]) + nDotH) * lum;
                g = clampChannel(g, green);

                b = ((b * shaderParams[0]) + nDotH) * lum;
                b = clampChannel(b, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
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
     * @param ptmObjectLRGB     object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param shaderParams      the 'gain' value for this enhancement
     * @return                  image using the image unsharp masking enhancement
     */
    private static WritableImage createImgUnMaskImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                      boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage image = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        //loop through all the pixels, calculate the RGB value of them, and write them to the image
        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectLRGB.getHeight(); y++) {
                //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                //calculate the luminance by applying the ptm equation to the lum coefficients
                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                        ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / (255.0f * 255.0f);

                //get the rgb by multiplying the rgb coefficients by the luminace
                r = ptmObjectLRGB.getRgbCoeffs().get(position) * lum;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) * lum;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) * lum;

                //convert the rgb colour space to yuv to get the luminance
                float[] yuv = calcYUV(r, g, b);

                //calculate the enhanced luminance for this pixel by averaging the luminance of surrounding pixels
                //and applying the image gain, see calcEnhancedLum
                float enhancedLum = calcEnhancedLum(ptmObjectLRGB, yuv[0], x, y, shaderParams[0], lightX, lightY);

                //go back to rgb colour space using the new enhanced luminance
                float[] rgb = getRGB(enhancedLum, yuv[1], yuv[2]);

                //clamp the colours between 0 and 255
                r = clampChannel(rgb[0], red);
                g = clampChannel(rgb[1], green);
                b = clampChannel(rgb[2], blue);

                image.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }


        return image;
    }





    /**
     * Clamps the value between 0 and 1. Returned value will be set to 0 if useChannel is false.
     *
     * @param val           value to clamp
     * @param useChannel    returned value will be 0 if true
     * @return              clamped value
     */
    private static float clampChannel(float val, boolean useChannel){
        if(!useChannel){return 0.0f;}
        if(val > 1.0f){return 1.0f;}
        if(val < 0.0){return 0.0f;}
        return val;
    }





    /**
     * Calculates the enhanced luminace for the pixel with x and y position by averaging the luminance of the pixels
     * in a block of 4x4 around it, then applying the enhancement using the gain param.
     *
     * @param ptmObjectLRGB     RTIObject with the HSH coefficients
     * @param lum               luminance of the center pixel to calculate enhanced luminance for
     * @param x                 x position of the pixel to calculate enhanced luminance for
     * @param y                 y position of the pixel to calculate enhanced luminance for
     * @param imgUnMaskGain     gain value for the ehnhanced luminance
     * @param lightX            x position of the incident light
     * @param lightY            y position of the incident light
     * @return                  the enhanced luminance for the pixel with given x and y
     */
    private static float calcEnhancedLum(PTMObjectLRGB ptmObjectLRGB, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY){
        int distance = 2;
        float tempLum = 0;

        //average the luminance from around the center pixel
        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(ptmObjectLRGB, x + xOffset, y + yOffset, lightX, lightY);
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
     * @param ptmObjectLRGB RTIObject containing the coefficient data
     * @param x             x position of the pixel to get lum from
     * @param y             y position of the pixel to get lum from
     * @param lightX        x postion of the incident light vector
     * @param lightY        y postion of the incident light vector
     * @return              the luminance of the pixel with position (x,y)
     */
    private static float getLumFromCoord(PTMObjectLRGB ptmObjectLRGB, int x, int y, float lightX, float lightY){
        //2D position to the 1D arrays of coeffs, (* 3 as it's a flattened array of x,y,z vectors )
        int position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

        float r = 0;
        float g = 0;
        float b = 0;

        //get the RGB and if it's outside, the size of the RTIObject, return 0
        if(x > ptmObjectLRGB.getWidth() - 1 || y > ptmObjectLRGB.getHeight() - 1 || x < 0 || y < 0) {
            r = 0; g = 0; b = 0;
        }else{
            float lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                    ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / 255.0f;

            r = lum * ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
            g = lum * ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
            b = lum * ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;
        }

        //convert toy YUV colourspace to get the luminance for this pixel
        return (float) (r * 0.299 + g * 0.587 + b * 0.144);

    }
}
