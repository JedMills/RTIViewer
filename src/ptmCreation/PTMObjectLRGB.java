package ptmCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import java.nio.IntBuffer;

/**
 * This class represents a PTM object with LRGB format that is loaded from a .ptm file on the disk. PTM LRGB objects
 * have 6 luminance coefficients per pixel, which the PTM equation is used to calculate the luminance for each pixel
 * of. It also has 3 RGB coefficients per pixel, which are multiplied by the calculated luminance to find the colour of
 * each pixel. This is the LRGB PTM format. See the user guide for a link to the original PTM paper.
 *
 * @see RTIObject
 *
 * @author Jed Mills
 */
public class PTMObjectLRGB extends RTIObject {

    /** The first 3 luminance coefficients per pixel, flattened into a 1D array*/
    private IntBuffer lumCoeffs1;

    /** The last 3 luminance coefficients per pixel, flattened into a 1D array*/
    private IntBuffer lumCoeffs2;

    /** The rgb coefficients per pixel, flattened into a 1D array*/
    private IntBuffer rgbCoeffs;




    /**
     * Create a new PTM LRGB object width given width and height from the given texel data.
     * The texel data should be in the order of:
     * <ol>
     *     <li>an IntBuffer containing the first 3 luminance coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *     <li>an IntBuffer containing the last 3 luminance coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     *     <li>an IntBuffer containing the 3 rgb coefficients per pixel, flattened, so its length
     *     is width * height * 3</li>
     * </ol>
     *
     * @param fileName          name of the the file containing the PTM object
     * @param width             width of the PTM
     * @param height            height of the PTM
     * @param texelData         see the above for the details of this argument
     */
    public PTMObjectLRGB(String fileName, int width, int height, IntBuffer[] texelData) {
        super(fileName, width, height);

        lumCoeffs1 = texelData[0];
        lumCoeffs2 = texelData[1];
        rgbCoeffs = texelData[2];

        //calculate the normals and store them in the normals attribute
        calculateNormals();
        //create the preview image to show in the preview tab of the viewer
        createPreviewImage();
    }




    /**
     * Calculates the normals vector for each texel using  the 6 luminance coefficients per pixel, as these are
     * the coefficients that are responsive to the light direction on this form of PTM.
     *
     * @see RTIObject#calculateNormals()
     * @see RTIObject#calculateNormal(int[])
     */
    @Override
    protected void calculateNormals() {
        //create the normals array, the length is *3 as it is  a flattened array of (x,y,z) vectors
        normals = BufferUtils.createFloatBuffer(width * height * 3);

        Utils.Vector3f temp;
        int[] tempCoeffs = new int[6];
        for(int i = 0; i < width * height; i++){
            //get the 6 luminance coefficients for each pixel, which are split across two arrays with 3 each
            tempCoeffs[0] = lumCoeffs1.get(i * 3);
            tempCoeffs[1] = lumCoeffs1.get((i * 3) + 1);
            tempCoeffs[2] = lumCoeffs1.get((i * 3) + 2);
            tempCoeffs[3] = lumCoeffs2.get(i * 3);
            tempCoeffs[4] = lumCoeffs2.get((i * 3) + 1);
            tempCoeffs[5] = lumCoeffs2.get((i * 3) + 2);

            temp = calculateNormal(tempCoeffs);

            //store the normal in the normals attribute
            normals.put((i * 3), temp.x);
            normals.put((i * 3) + 1, temp.y);
            normals.put((i * 3) + 2, temp.z);
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

        int position;
        float lum, red, green, blue;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                //position in 1D array
                position = ((y * width) + x) * 3;

                //calculate the luminance for this pixel using the 6 luminance coeffs and the PTM equation
                lum = Utils.calcIntensity(lumCoeffs1, lumCoeffs2, position, 0, 0) / 255.0f;

                //calculate the colour by multiplying the luminance by the colour coeffs.
                //divided by 255 as we want colours in range 0.0 - 1.0
                red = rgbCoeffs.get(position) * lum / 255.0f;
                green = rgbCoeffs.get(position + 1) * lum / 255.0f;
                blue = rgbCoeffs.get(position + 2) * lum / 255.0f;

                previewImage.getPixelWriter().setColor(x, y, Color.color(red, green, blue));
            }
        }
    }


    /**
     * @return  {@link PTMObjectLRGB#lumCoeffs1}
     */
    public IntBuffer getLumCoeffs1() {
        return lumCoeffs1;
    }


    /**
     * @return {@link PTMObjectLRGB#lumCoeffs2}
     */
    public IntBuffer getLumCoeffs2() {
        return lumCoeffs2;
    }


    /**
     * @return {@link PTMObjectLRGB#rgbCoeffs}
     */
    public IntBuffer getRgbCoeffs() {
        return rgbCoeffs;
    }
}
