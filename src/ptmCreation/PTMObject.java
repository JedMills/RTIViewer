package ptmCreation;

import utils.Utils;

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
    private int[][] redVals;

    /**Contains the coefficients for PTM polynomial in the green channel*/
    private int[][] greenVals;

    /**Contains the coefficients for PTM polynomial in the blue channel*/
    private int[][] blueVals;

    /**Contains the surface normals calculated from the PTM file*/
    private Utils.Vector3f[] normals;



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
    public PTMObject(String fileName, int width, int height, int[][][] texelData) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;

        redVals = texelData[0];
        greenVals = texelData[1];
        blueVals = texelData[2];

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
        //to easilt loop over the colour channels
        int[][][] channels = new int[][][]{redVals, greenVals, blueVals};

        //initialise the normals array
        normals = new Utils.Vector3f[width * height];
        for(int i = 0; i < normals.length; i++){
            normals[i] = new Utils.Vector3f(0,0,0);
        }

        //calculate each normal
        Utils.Vector3f temp;
        for(int i = 0; i < redVals.length; i++){
            for(int[][] channel : channels){
                //sum the normals for the red, green and blue channels
                temp = calculateNormal(channel[i]);
                normals[i].x += temp.x;
                normals[i].y += temp.y;
                normals[i].z += temp.z;
            }
            //divide by 3 for the average, then normalise
            normals[i].x /= 3.0;
            normals[i].y /= 3.0;
            normals[i].z /= 3.0;
            normals[i] = normals[i].normalise();

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

    public int[][] getRedVals() {
        return redVals;
    }

    public int[][] getGreenVals() {
        return greenVals;
    }

    public int[][] getBlueVals() {
        return blueVals;
    }

    public Utils.Vector3f[] getNormals(){return normals;}
}
