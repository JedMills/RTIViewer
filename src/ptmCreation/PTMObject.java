package ptmCreation;

import org.lwjgl.BufferUtils;
import utils.Utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by jed on 16/05/17.
 */
public class PTMObject {

    /**Path of the file*/
    private String fileName;

    /**Width of the image*/
    private int width;

    /**Height of the image*/
    private int height;

    /**Contains the coefficients for PTM polynomial in the red channel*/
    private IntBuffer redVals1;
    private IntBuffer redVals2;

    /**Contains the coefficients for PTM polynomial in the green channel*/
    private IntBuffer greenVals1;
    private IntBuffer greenVals2;

    /**Contains the coefficients for PTM polynomial in the blue channel*/
    private IntBuffer blueVals1;
    private IntBuffer blueVals2;

    /**Contains the surface normals calculated from the PTM file*/
    private FloatBuffer normals;



    /**
     * Creates a new ptmCreation.PTMObject. Sets the passed parameters as relevant attributes.
     * TexelData needs to be a 3D int array:
     *      - first dimension  : length = 3, for red, green then blue colour channels
     *      - second dimension : length = width * height, array for image texels
     *      - third dimension  : length = 6, for the 5 PTM coefficients for each texel
     *
     * @param fileName      path to the .ptm file this object was created using
     * @param width         width of image
     * @param height        height of image
     * @param texelData     3D array of texel data
     */
    public PTMObject(String fileName, int width, int height, IntBuffer[] texelData) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;

        redVals1 = texelData[0];
        redVals2 = texelData[1];
        greenVals1 = texelData[2];
        greenVals2 = texelData[3];
        blueVals1 = texelData[4];
        blueVals2 = texelData[5];

        calculateNormals();
    }


    /**
     * Calculates a normal vector from the 6 PTM coefficients.
     *
     * @param coeffs    the siz PTM coefficients, a0-a5
     * @return          the normalised vector3f
     */
    private Utils.Vector3f calculateNormal(int[] coeffs){
        float zeroTol = Utils.ZEROTOL;
        float[] a = new float[6];
        //convert the coeffs (basically a byte array) to floats
        for(int i = 0; i < 6; i++){
            a[i] = (float) (coeffs[i] /256.0);
        }

        Utils.Vector3f lengths = new Utils.Vector3f(0f, 0f, 0f);
        if(Math.abs(4 * a[1] * a[0] - a[2] * a[2]) < zeroTol){
            lengths.x = 0.0f;
            lengths.y = 0.0f;
        }else{
            if(Math.abs(a[2]) < zeroTol){
                lengths.x = (float) (-a[3] / (2.0 * a[0]));
                lengths.y = (float) (-a[4] / (2.0 * a[1]));
            }else{
                lengths.x = (float) ((a[2]*a[4] - 2.0*a[1]*a[3])/(4.0*a[0]*a[1] - a[2]*a[2]));
                lengths.y = (float) ((a[2]*a[3] - 2.0*a[0]*a[4])/(4.0*a[0]*a[1] - a[2]*a[2]));
            }
        }

        if(Math.abs(a[0]) < zeroTol && Math.abs(a[1]) < zeroTol && Math.abs(a[2]) < zeroTol &&
                Math.abs(a[3]) < zeroTol && Math.abs(a[4]) < zeroTol){
            lengths.x = 0.0f;
            lengths.y = 0.0f;
            lengths.z = 1.0f;
        }else{
            float length2d = (lengths.x * lengths.x) + (lengths.y * lengths.y);

            int maxFound;
            if((4 * a[0] * a[1] - a[2] * a[2]) > zeroTol && a[0] < -zeroTol){
                maxFound = 1;
            }else{
                maxFound = 0;
            }

            if(length2d > 1 - zeroTol || maxFound == 0){
                int stat = Utils.computeMaximumOnCircle(a, lengths);
                if(stat == -1){
                    length2d = (float) Math.sqrt(length2d);
                    if(length2d > zeroTol){
                        lengths.x /= length2d;
                        lengths.y /= length2d;
                    }
                }
            }
            float disc = (float) (1.0 - (lengths.x * lengths.x) - (lengths.y * lengths.y));
            if(disc < 0.0){
                lengths.z = 0.0f;
            }else{
                lengths.z = (float) Math.sqrt(disc);
            }
        }
        return lengths.normalise();
    }


    /**
     * Calculates the normal for each pixel as an average of the normals for
     * the red, green and blue colour channels at each pixel.
     */
    private void calculateNormals(){
        normals = BufferUtils.createFloatBuffer(width * height * 3);
        IntBuffer[] channels = new IntBuffer[]{redVals1, redVals2, greenVals1, greenVals2, blueVals1, blueVals2};


        Utils.Vector3f temp;
        int[] tempCoeffs = new int[6];
        for(int i = 0; i < width * height; i++){
            for(int j = 0 ; j < 3; j ++) {
                tempCoeffs[0] = channels[j * 2].get((i * 3));
                tempCoeffs[1] = channels[j * 2].get((i * 3) + 1);
                tempCoeffs[2] = channels[j * 2].get((i * 3) + 2);
                tempCoeffs[3] = channels[(j * 2) + 1].get((i * 3));
                tempCoeffs[4] = channels[(j * 2) + 1].get((i * 3) + 1);
                tempCoeffs[5] = channels[(j * 2) + 1].get((i * 3) + 2);
                temp = calculateNormal(tempCoeffs);

                normals.put((i * 3), normals.get((i * 3)) + temp.x);
                normals.put((i * 3) + 1, normals.get((i * 3) + 1) + temp.y);
                normals.put((i * 3) + 2, normals.get((i * 3) + 2) + temp.z);
            }

            normals.put((i * 3), normals.get(i * 3) / 3);
            normals.put((i * 3) + 1, normals.get((i * 3) + 1) / 3);
            normals.put((i * 3) + 2, normals.get((i * 3) + 2) / 3);


            temp = new Utils.Vector3f(normals.get(i * 3), normals.get(i* 3 + 1), normals.get(i * 3 + 2));
            temp.normalise();

            normals.put(i * 3, temp.getX());
            normals.put(i * 3 + 1, temp.getY());
            normals.put(i * 3 + 2, temp.getZ());

        }

    }


    public String getFileName() {
        return fileName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public IntBuffer getRedVals1() {
        return redVals1;
    }

    public IntBuffer getRedVals2() {
        return redVals2;
    }

    public IntBuffer getGreenVals1() {
        return greenVals1;
    }

    public IntBuffer getGreenVals2() {
        return greenVals2;
    }

    public IntBuffer getBlueVals1() {
        return blueVals1;
    }

    public IntBuffer getBlueVals2() {
        return blueVals2;
    }

    public FloatBuffer getNormals() {
        return normals;
    }
}
