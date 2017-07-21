package imageCreation;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import ptmCreation.PTMObjectLRGB;
import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import ptmCreation.RTIObjectHSH;
import toolWindow.RTIViewer;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class provides static methods to save a snapshot of an {@link RTIObject} with the current rendering parameters
 * of the viewer. It also contains methods used in multiple subclasses for calculation of certain RTIFilters, and
 * methods to save greyscale images.
 *
 * @see RTIObject
 * @see ImageCreatorHSH
 * @see ImageCreatorPTM_LRGB
 * @see ImageCreatorPTM_RGB
 */
public class ImageCreator {


    /**
     * Saves an image to disk using the passed rendering parameters, on a new thread. Uses the subclasses for the
     * relevant {@link RTIObject} type to process the actual images, and then writes this output image.
     *
     * @param rtiObject         object to save a snapshot of
     * @param lightX            x position of the light int the snapshot
     * @param lightY            y position of the light int the snapshot
     * @param shaderProgram     current image filter type to save the snapshot of
     * @param red               whether the red channel should be saved
     * @param green             whether the green channel should be saved
     * @param blue              whether the blue channel should be saved
     * @param format            the format of the file to write 'jpg' or 'png'
     * @param destination       path tof the image file to write
     * @param shaderParams      parameters of the specific shader= program used
     * @param isGreyscale       whether the image should be converted to greyscale or not
     */
    public static void saveImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                    boolean red, boolean green, boolean blue, String format, File destination,
                                    float[] shaderParams, boolean isGreyscale){

        //write this sucker on a new thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                WritableImage createdImage;

                //use the relevant image creator for the RTIObject type to create the image
                if(rtiObject instanceof PTMObjectRGB){
                    createdImage = ImageCreatorPTM_RGB.createImage(rtiObject, lightX, lightY,
                                                                    shaderProgram, red, green, blue, shaderParams);
                }else if(rtiObject instanceof PTMObjectLRGB){
                    createdImage = ImageCreatorPTM_LRGB.createImage(rtiObject, lightX, lightY,
                            shaderProgram, red, green, blue, shaderParams);
                }else if(rtiObject instanceof RTIObjectHSH){
                    createdImage = ImageCreatorHSH.createImage(rtiObject, lightX, lightY,
                                                                shaderProgram, red, green, blue, shaderParams);
                }else{
                    return;
                }

                //try and write it to the disk
                try{
                    saveImage(createdImage, format, destination, isGreyscale);
                }catch (IOException e){
                    e.printStackTrace();

                }
            }
        });

        //start that thread!
        thread.start();
    }




    /**
     * Writes the passed WritableImage to the disk, to the destination File. Will convert the image to greyscale
     * before writing if isGreyscale is true.
     *
     * @param writableImage     image to write to disk
     * @param format            format to write in: 'jpg' or 'png'
     * @param destination       path of the image file to write
     * @param isGreyScale       whether the image should be greyscale or not
     * @throws IOException      if there is an error writing to disk
     */
    private static void saveImage(WritableImage writableImage, String format,
                                  File destination, boolean isGreyScale) throws IOException{
        //the image needs to be turned into a BufferedImage so we can use ImageIO to easily write it to disk
        BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);


        //there is a bug in ImageIO where saving a jpeg to file gives weird colours, because the API thinks
        //that the BufferedImage is in MYK instead of RGB, so this is the workaround
        BufferedImage newImg = new BufferedImage(image.getWidth(),
                                            image.getHeight(), BufferedImage.TYPE_INT_RGB);

        int rgb;
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                rgb = image.getRGB(x, y);

                //this is a convenient place to convert the image to greyscale, os let's do it here
                if(isGreyScale){rgb = convertToGreyscale(rgb);}

                newImg.setRGB(x, y, rgb);
            }
        }
        //woooo we can finally write it to the disk
        ImageIO.write(newImg, format.toUpperCase(), destination);
    }


    /**
     * Converts the passed colour to greyscale.
     *
     * @param rgb       the int representation of the rgb to greyscale
     * @return          the rgb colour, greyscaled
     */
    private static int convertToGreyscale(int rgb){
        //some major bit-shifting is about to happen
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);

        int grayLevel = (r + g + b) / 3;
        int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;

        return gray;
    }


    /**
     * Creates a normals map of the RTIObject. The z component of the normals are shown as blue (255 = z pointing
     * out of the screen, 0 away), the y component shown as green (255 = up, 0 = down), and the x component as
     * red (355 = right, 0 = left).
     *
     * @param rtiObject     object to create the normals image of
     * @param red           whether the red channel should be saved
     * @param green         whether the green channel should be saved
     * @param blue          whether the blue channel should be saved
     * @return              the normals visualisation as a WritableImage
     */
    public static WritableImage createNormalsImage(RTIObject rtiObject, boolean red, boolean green, boolean blue){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        //loop through all the pixels in the RTIObject, and
        int position, r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                //convert the 2D position to a location in the 1D arrays of coefficients, (* 3 as it's a
                //flattened array of x,y,z vectors)
                position = ((y * rtiObject.getWidth()) + x) * 3;

                //x = red
                if(red){
                    r = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position));
                }else{r = 0;}

                //y = green
                if(green) {
                    g = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position + 1));
                }else{g = 0;}

                //z = blue
                if(blue){
                    b = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position + 2));
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, javafx.scene.paint.Color.rgb(r, g, b));
            }
        }

        return writableImage;
    }



    /**
     * Used in HSH images to calculate the luminance of pixel. The specific numbers for this come from the
     * original CHI viewer. The reverse of {@link ImageCreator#getRGB(float, float, float)}.
     *
     * @see ImageCreatorHSH
     *
     * @param r     red component of the pixel
     * @param g     green component of the pixel
     * @param b     blue component of the pixel
     * @return      the rgb converted to yuv
     */
    public static float[] calcYUV(float r, float g, float b){
        float y = r * 0.299f + g * 0.587f + b * 0.144f;
        float u = r * -0.14713f + g * -0.28886f + b * 0.436f;
        float v = r * 0.615f + g * -0.51499f + b * -0.10001f;

        return new float[]{y, u, v};
    }




    /**
     * Used in HSH to calculate the RGB value of pixels after the YUV has been calculated. the specific numbers
     * of this come from the original CHI viewer. The reverse of {@link ImageCreator#calcYUV(float, float, float)}.
     *
     * @param lum   luminance of the pixel
     * @param u     u value of the pixel
     * @param v     v valueof the pixel
     * @return      yuv converted to rgb
     */
    public static float[] getRGB(float lum, float u, float v){
        float r = lum + v * 1.13983f;
        float g = lum + u * -0.39465f + v * -0.5806f;
        float b = lum + u * 2.03211f;

        return new float[]{r, g, b};
    }
}
