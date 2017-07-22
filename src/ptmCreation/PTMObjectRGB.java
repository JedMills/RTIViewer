package ptmCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import java.nio.IntBuffer;

/**
* This class represents a PTM object with RGB format that is loaded from a .ptm file on the disk. PTM RGB objects
* have 6 coefficients per pixel, which the PTM equation is used to calculate the colour for each pixel
* of. This is the LRGB PTM format. See the user guide for a link to the original PTM paper.
*
* @see RTIObject
*
* @author Jed Mills
*/
public class PTMObjectRGB extends RTIObject {

    /** Flattened array of of the first 3 red coefficients per pixel for the PTM object */
    private IntBuffer redVals1;

    /** Flattened array of of the last 3 red coefficients per pixel for the PTM object */
    private IntBuffer redVals2;

    /** Flattened array of of the first 3 green coefficients per pixel for the PTM object */
    private IntBuffer greenVals1;

    /** Flattened array of of the last 3 green coefficients per pixel for the PTM object */
    private IntBuffer greenVals2;

    /** Flattened array of of the first 3 blue coefficients per pixel for the PTM object */
    private IntBuffer blueVals1;

    /** Flattened array of of the last 3 blue coefficients per pixel for the PTM object */
    private IntBuffer blueVals2;


    /**
     * Create a new PTM LRGB object width given width and height from the given texel data.
     * The texel data should be in the order of:
     * <ol>
     *     <li>an IntBuffer containing the first 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the last 3 red coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *     <li>an IntBuffer containing the first 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *    <li>an IntBuffer containing the last 3 green coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *    <li>an IntBuffer containing the first 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *
     *    <li>an IntBuffer containing the last 3 blue coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     * </ol>
     *
     * @param fileName          name of the the file containing the PTM object
     * @param width             width of the PTM
     * @param height            height of the PTM
     * @param texelData         see above for the details of this argument
     */
    public PTMObjectRGB(String fileName, int width, int height, IntBuffer[] texelData) {
        super(fileName, width, height);

        redVals1 = texelData[0];
        redVals2 = texelData[1];
        greenVals1 = texelData[2];
        greenVals2 = texelData[3];
        blueVals1 = texelData[4];
        blueVals2 = texelData[5];

        //calculate the normals and store them in the normals attribute
        calculateNormals();
        //create the preview image to show in the preview tab of the viewer
        createPreviewImage();
    }


    /**
     * Calculates the normals vector for each texel using  the 6 coefficients per colour pixel. The maths for this
     * method comes from the original RTI viewer, which in turn comes from the original PTM paper, which there is a
     * link to in the use guide for this app.
     *
     * @see RTIObject#calculateNormals()
     * @see RTIObject#calculateNormal(int[])
     */
    protected void calculateNormals(){
        //create the normals array, the length is *3 as it is  a flattened array of (x,y,z) vectors
        normals = BufferUtils.createFloatBuffer(width * height * 3);
        //put these inhere so we can easily loop over them
        IntBuffer[] channels = new IntBuffer[]{redVals1, redVals2, greenVals1, greenVals2, blueVals1, blueVals2};

        Utils.Vector3f temp;
        int[] tempCoeffs = new int[6];
        for(int i = 0; i < width * height; i++){
            for(int j = 0 ; j < 3; j ++) {
                //get the 6 coefficients per colour for each pixel, which are split across two arrays with 3 each
                tempCoeffs[0] = channels[j * 2].get((i * 3));
                tempCoeffs[1] = channels[j * 2].get((i * 3) + 1);
                tempCoeffs[2] = channels[j * 2].get((i * 3) + 2);
                tempCoeffs[3] = channels[(j * 2) + 1].get((i * 3));
                tempCoeffs[4] = channels[(j * 2) + 1].get((i * 3) + 1);
                tempCoeffs[5] = channels[(j * 2) + 1].get((i * 3) + 2);

                temp = calculateNormal(tempCoeffs);

                //add the normal calculated in the normals attribute
                normals.put((i * 3), normals.get((i * 3)) + temp.x);
                normals.put((i * 3) + 1, normals.get((i * 3) + 1) + temp.y);
                normals.put((i * 3) + 2, normals.get((i * 3) + 2) + temp.z);
            }

            //find the average value for the normal oif the normals calculated from the red, green and blue coeffs
            normals.put((i * 3), normals.get(i * 3) / 3);
            normals.put((i * 3) + 1, normals.get((i * 3) + 1) / 3);
            normals.put((i * 3) + 2, normals.get((i * 3) + 2) / 3);

            //normalise it
            temp = new Utils.Vector3f(normals.get(i * 3), normals.get(i* 3 + 1), normals.get(i * 3 + 2));
            temp.normalise();

            normals.put(i * 3, temp.getX());
            normals.put(i * 3 + 1, temp.getY());
            normals.put(i * 3 + 2, temp.getZ());
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
    protected void createPreviewImage(){
        //create the image to write the pixels to
        previewImage = new WritableImage(width, height);

        int position;
        float red, green, blue;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                //position in 1D array
                position = ((y * width) + x) * 3;

                //calculate the red, green and blue value for each pixel using the PTM equation
                red = Utils.calcIntensity(redVals1, redVals2, position, 0, 0) / 255.0f;
                green = Utils.calcIntensity(greenVals1, greenVals2, position, 0, 0) / 255.0f;
                blue = Utils.calcIntensity(blueVals1, blueVals2, position, 0, 0) / 255.0f;


                previewImage.getPixelWriter().setColor(x, y, Color.color(red, green, blue));
            }
        }
    }


    /**
     * @return {@link PTMObjectRGB#redVals1}
     */
    public IntBuffer getRedVals1() {
        return redVals1;
    }

    /**
     * @return {@link PTMObjectRGB#redVals2}
     */
    public IntBuffer getRedVals2() {
        return redVals2;
    }

    /**
     * @return {@link PTMObjectRGB#greenVals1}
     */
    public IntBuffer getGreenVals1() {
        return greenVals1;
    }

    /**
     * @return {@link PTMObjectRGB#greenVals2}
     */
    public IntBuffer getGreenVals2() {
        return greenVals2;
    }

    /**
     * @return {@link PTMObjectRGB#blueVals1}
     */
    public IntBuffer getBlueVals1() {
        return blueVals1;
    }

    /**
     * @return {@link PTMObjectRGB#blueVals2}
     */
    public IntBuffer getBlueVals2() {
        return blueVals2;
    }
}
