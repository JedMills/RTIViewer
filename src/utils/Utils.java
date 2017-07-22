package utils;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.Math.sin;

/**
 * Contains all the utility functions that are used throughout the viewer.
 *
 * Created by jed on 16/05/17.
 */
public class Utils {

    public static final float ZEROTOL = 0.00001f;

    /**
     * Returns true if value is in the array, false if not.
     *
     * @param value     value to find
     * @param values    array tp find it in
     * @return          whether value is in the array
     */
    public static boolean checkIn(String value, String[] values){
        for(String s : values){
            if(value.equals(s)){return true;}
        }
        return false;
    }



    public static boolean checkIn(String value, List<String> values){
        for(String s : values){
            if(value.equals(s)){return true;}
        }
        return false;
    }




    /**
     * Returns the intensity calculated from the PTM polynomial:
     * I = (a0 * Lx^2) + (a1 * ly^2) + (a2 * Lx * Ly) + (a3 * Lx) + (a4 * Ly)+ a5
     * Thresholds value between 0 and 255.
     *
     * @param coeffs    array of the six PTM polynomial coefficients a0-a5
     * @param light     light vector
     * @return
     */
    public static int calcIntensity(int[] coeffs, Vector2f light){
        //i = (a0 * Lu^2) + (a1 * Lv^2) + (a2 * Lu * Lv) + (a3 * Lu) + (a4 * Lv) + a5
        double intensity =  (coeffs[0] * light.getX() * light.getX()) +
                (coeffs[1] * light.getY() * light.getY()) +
                (coeffs[2] * light.getX() * light.getY()) +
                (coeffs[3] * light.getX()) +
                (coeffs[4] * light.getY()) + coeffs[5];

        //threshold these to an unsigned byte for RGB
        if(intensity > 255){intensity = 255;}
        else if(intensity < 0){intensity = 0;}

        return (int) intensity;
    }

    public static int calcIntensity(int[] coeffs, float x, float y){
        //i = (a0 * Lu^2) + (a1 * Lv^2) + (a2 * Lu * Lv) + (a3 * Lu) + (a4 * Lv) + a5
        double intensity =  (coeffs[0] * x * x) +
                (coeffs[1] * y * y) +
                (coeffs[2] * x * y) +
                (coeffs[3] * x) +
                (coeffs[4] * y) + coeffs[5];

        //threshold these to an unsigned byte for RGB
        if(intensity > 255){intensity = 255;}
        else if(intensity < 0){intensity = 0;}

        return (int) intensity;
    }




    public static int calcIntensity(IntBuffer coeffs1, IntBuffer coeffs2, int position, float lightX, float lightY){
        //i = (a0 * Lu^2) + (a1 * Lv^2) + (a2 * Lu * Lv) + (a3 * Lu) + (a4 * Lv) + a5
        double intensity =  (coeffs1.get(position) * lightX * lightX) +
                (coeffs1.get(position + 1) * lightY * lightY) +
                (coeffs1.get(position + 2) * lightX * lightY) +
                (coeffs2.get(position) * lightX) +
                (coeffs2.get(position + 1) * lightY) + coeffs2.get(position + 2);

        //threshold these to an unsigned byte for RGB
        if(intensity > 255){intensity = 255;}
        else if(intensity < 0){intensity = 0;}

        return (int) intensity;
    }




    /**
     * All the 3D vectors needed in this program are from here!
     *
     * Created by jed on 16/05/17.
     */
    public static class Vector3f{
        /**The x component*/
        public float x;
        /**The y component*/
        public float y;
        /**The z component*/
        public float z;

        /**
         *
         * @param x     for the x component of the vector
         * @param y     for the y component of the vector
         * @param z     for the z component of the vector
         */
        public Vector3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * @return x component of the vector
         */
        public float getX() {
            return x;
        }

        /**
         * @param x set the x component of the vector
         */
        public void setX(float x) {
            this.x = x;
        }

        /**
         * @return y component of the vector
         */
        public float getY() {
            return y;
        }

        /**
         * @param y set the y component of the vector
         */
        public void setY(float y) {
            this.y = y;
        }

        /**
         * @return z component of the vector
         */
        public float getZ() {
            return z;
        }

        /**
         * @param z set the z component of the vector
         */
        public void setZ(float z) {
            this.z = z;
        }

        /**
         * @return  a new vector with normalised lengths
         */
        public Vector3f normalise(){
            Vector3f v = new Vector3f(0f, 0f, 0f);
            float length = (float) Math.sqrt(x*x + y*y + z*z);
            if(length != 0){
                v.x = x / length;
                v.y = y / length;
                v.z = z / length;
            }
            return v;
        }

        public float get(int i){
            if(i == 0){return x;}
            else if(i == 1){return y;}
            else if(i == 2){return z;}

            return 0;
        }

        public float dot(Vector3f v){
            return (x * v.x) + (y * v.y) + (z * v.z);
        }

        public Vector3f multiply(float a){
            return new Vector3f(this.x * a, this.y * a, this.z * a);
        }

        public float length(){return (float) Math.pow(x*x + y*y + z*z, 0.5);}

        public Vector3f add(Vector3f vec){
            return new Vector3f(x + vec.x, y + vec.y, z + vec.z);
        }

        public Vector3f minus(Vector3f vec){
            return new Vector3f(x - vec.x, y - vec.y, z - vec.z);
        }
    }


    public static class Vector2f{

        public float x;

        public float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public Vector2f normalise(){
            Vector2f v = new Vector2f(0.0f, 0.0f);
            float length = (float) Math.sqrt(x*x + y*y);
            if(length != 0){
                v.x = x / length;
                v.y = y / length;
            }
            return v;
        }

        public float length(){
            return (float) Math.sqrt(x*x + y*y);
        }
    }



    public static int computeMaximumOnCircle(float[] a, Vector3f lengths){
        float db0, db1, db2, db3, db4;
        float[] roots = new float[4];
        float u, v, maxval, maxu = - 1, maxv = -1, inc, arg, polyval;
        int index, nroots;

        index = -1;
        nroots = -1;

        db0 = a[2] - a[3];
        db1 = 4 * a[1] - 2 * a[4] - 4 * a[0];
        db2 = -6 * a[2];
        db3 = -4 * a[1] - 2 * a[4] + 4 * a[0];
        db4 = a[2] + a[3];

        if(Math.abs(db0) < ZEROTOL && Math.abs(db1) < ZEROTOL &&
                Math.abs(db2) < ZEROTOL && Math.abs(db3) < ZEROTOL){
            lengths.x = 0.0f;
            lengths.y = 1.0f;
            return 1;
        }

        if(db0 != 0){
            float[] c = new float[]{db4, db3, db2, db1, db0};
            nroots = solveQuartic(c, roots);
        }else if(db1 != 0){
            float[] c = new float[]{db4, db3, db2, db1};
            nroots = solveCubic(c, roots);
        }else{
            float[] c = new float[]{db4, db3, db2};
            nroots = solveQuadratic(c, roots);
        }

        if(nroots <= 0){
            return 0;
        }

        switch(nroots){
            case 1:
                index = 0;
                break;
            default:
                float[] vals = new float[nroots];
                index = 0;
                for(int i = 0; i < nroots; i ++){
                    u = 2 * roots[i] / (1 + roots[i] * roots[i]);
                    v = (1 - roots[i] * roots[i]) / (1 + roots[i] * roots[i]);
                    vals[i] = a[0] * u * u + a[1] * v * v + a[2] * u * v + a[3] * u + a[4] * v + a[5];
                    if(vals[i] > vals[index]){
                        index = 1;
                    }
                }
                vals = null;
        }

        lengths.x = 2 * roots[index] / (1 + roots[index] * roots[index]);
        lengths.y = (1 - roots[index] * roots[index])/ (1 + roots[index] * roots[index]);

        maxval = -1000;
        for(int k = 0; k <= 20; k++){
            inc = (float) (1 / 9.0) / 20 * k;
            arg = (float) (Math.PI * (26.0 / 18.0 + inc));
            u = (float) Math.cos(arg);
            v = (float) Math.sin(arg);
            polyval = a[0] * u * u + a[1] * v * v + a[2] * u * v + a[3] * u	+ a[4] * v + a[5];
            if (maxval < polyval) {
                maxval = polyval;
                maxu = u;
                maxv = v;
            }
        }

        u = 2 * roots[index] / (1 + roots[index] * roots[index]);
        v = (1 - roots[index] * roots[index]) / (1 + roots[index] * roots[index]);
        float val1 = a[0] * u * u + a[1] * v * v + a[2] * u * v + a[3] * u + a[4] * v + a[5];
        if (maxval > val1) {
            lengths.x = maxu;
            lengths.y = maxv;
        }
        return 1;
    }


    public static int solveQuartic(float[] c, float[] solutions){
        float[] coeffs = new float[4];
        float z, u, v, sub;
        float A, B, C, D;
        float sq_A, p, q, r;
        int i, num;

        /* normal form: x^4 + Ax^3 + Bx^2 + Cx + D = 0 */
        A = c[3] / c[4];
        B = c[2] / c[4];
        C = c[1] / c[4];
        D = c[0] / c[4];

        /* substitute x = y - A/4 to eliminate cubic term: x^4 + px^2 + qx + r = 0 */
        sq_A = A * A;
        p = (float) -3.0 / 8 * sq_A + B;
        q = (float) (1.0 / 8 * sq_A * A - 1.0 / 2 * A * B + C);
        r = (float) (-3.0 / 256 * sq_A * sq_A + 1.0 / 16 * sq_A * B - 1.0 / 4 * A * C + D);

        if(isZero(r)){
            /* no absolute term: y(y^3 + py + q) = 0 */
            coeffs[0] = q;
            coeffs[1] = p;
            coeffs[2] = 0;
            coeffs[3] = 1;

            num = solveCubic(coeffs, solutions);
            solutions[num++] = 0;
        }else{
            /* solve the resolvent cubic ... */
            coeffs[0] = (float) (1.0 / 2 * r * p - 1.0 / 8 * q * q);
            coeffs[1] = -r;
            coeffs[2] = (float) -1.0 / 2 * p;
            coeffs[3] = 1;
            solveCubic(coeffs, solutions);

            /* ... and take the one real solution ... */
            z = solutions[0];

            /* ... to build two quadratic equations */
            u = z * z - r;
            v = 2 * z - p;
            if(isZero(u)){u = 0;}
            else if(u > 0){u = (float) Math.sqrt(u);}
            else{return 0;}

            if(isZero(v)){v = 0;}
            else if(v > 0){v = (float) Math.sqrt(v);}
            else{return 0;}

            coeffs[0] = z - u;
            coeffs[1] = q < 0 ? -v : v;
            coeffs[2] = 1;
            num = solveQuadratic(coeffs, solutions);
            coeffs[0] = z + u;
            coeffs[1] = q < 0 ? v : -v;
            coeffs[2] = 1;
            num += solveQuadratic(coeffs, solutions, num);
        }

        /* resubstitute */
        sub = (float) 1.0 / 4 * A;
        for(i = 0; i < num; i++){
            solutions[i] -= sub;
        }
        return num;
    }



    public static int solveCubic(float[] c, float[] solutions){
        int i, num;
        float sub;
        float A, B, C;
        float sq_A, p, q;
        float cb_p, D;

        /* normal form: x^3 + Ax^2 + Bx + C = 0 */
        A = c[2] / c[3];
        B = c[1] / c[3];
        C = c[0] / c[3];

        /* substitute x = y - A/3 to eliminate quadratic term: x^3 +px + q = 0 */
        sq_A = A * A;
        p = (float) (1.0 / 3 * (-1.0 / 3 * sq_A + B));
        q = (float) (1.0 / 2 * (2.0 / 27 * A * sq_A - 1.0 / 3 * A * B + C));

        /* use Cardano's formula */
        cb_p = p * p * p;
        D = q * q + cb_p;

        if(isZero(D)){  /* one triple solution */
            if(isZero(q)){
                solutions[0] = 0;
                num = 1;
            }else{      /* one single and one float solution */
                float u = cubeRoot(-q);
                solutions[0] = 2 * u;
                solutions[1] = -u;
                num = 2;
            }
        }else if(D < 0){    /* Casus irreducibilis: three real solutions */
            float phi = (float) (1.0 / 3 * Math.acos(-q / Math.sqrt(-cb_p)));
            float t = (float) (2 * Math.sqrt(-p));

            solutions[0] = (float) (t * Math.cos(phi));
            solutions[1] = (float) (-t * Math.cos(phi + Math.PI / 3));
            solutions[2] = (float) (-t * Math.cos(phi - Math.PI / 3));
            num = 3;
        }else{          /* one real solution */
            float sqrt_D = (float) Math.sqrt(D);
            float u = cubeRoot(sqrt_D - q);
            float v = -cubeRoot(sqrt_D + q);

            solutions[0] = u + v;
            num = 1;
        }

        /* resubstitute */
        sub = (float) 1.0 / 3 * A;
        for (i = 0; i < num; ++i) {
            solutions[i] -= sub;
        }
        return num;
    }


    public static int solveQuadratic(float[] c, float[]solutions){
        return solveQuadratic(c, solutions, 0);
    }

    public static int solveQuadratic(float[] c, float[] solutions, int n){
        float p, q, D;

	    /* normal form: x^2 + px + q = 0 */
        p = c[1] / (2 * c[2]);
        q = c[0] / c[2];
        D = p * p - q;

        if(isZero(D)){
            solutions[0 + n] = -p;
            return 1;
        }else if(D < 0){
            return 0;
        }else if(D > 0){
            float sqrt_D = (float) Math.sqrt(D);
            solutions[0 + n] = sqrt_D - p;
            solutions[1 + n] = -sqrt_D - p;
            return 2;
        }
        return -1;
    }


    public static boolean isZero(float x){
        float limit = 1e-9f;
        return x > -limit && x < limit;
    }


    public static float cubeRoot(float x){
        if (x > 0) {
            return (float) Math.pow(x, 1.0f / 3.0f);
        }else if (x < 0) {
            return (float) -Math.pow(-x, 1.0f / 3.0f);
        }
        return 0;
    }

    public static int[] convertNormalToColour(Vector3f normal){

        int red = convertNormalCoordToColour(normal.x);
        int green = convertNormalCoordToColour(normal.y);
        int blue = convertNormalCoordToColour(normal.z);

        return new int[]{red, green, blue};
    }


    public static int convertNormalCoordToColour(float coord){
        int colour = (int) Math.floor(((coord + 1) / 2) * 255.0);

        if(colour > 255){colour = 255;}
        else if(colour < 0){colour = 0;}

        return colour;
    }

    public static int toByte(float value){
        if(value < 0.0){return 0;}
        else if(value > 255.0){return 255;}
        else{return (int) value;}
    }



    public static Vector3f mat3x3_mul_vec3(float[][] mat, Vector3f vec){
        Vector3f returnVec = new Vector3f(0, 0, 0);

        returnVec.x = (mat[0][0] * vec.x) + (mat[0][1] * vec.y) + (mat[0][2] * vec.z);
        returnVec.y = (mat[1][0] * vec.x) + (mat[1][1] * vec.y) + (mat[1][2] * vec.z);
        returnVec.z = (mat[2][0] * vec.x) + (mat[2][1] * vec.y) + (mat[2][2] * vec.z);

        return returnVec;
    }



    public static int[] intsFromStrings(String[] strings, int num) throws NumberFormatException{
        int[] returnItems = new int[num];
        for(int i = 0; i < num; i ++){
            returnItems[i] = Integer.parseInt(strings[i]);
        }
        return returnItems;
    }


    public static int[] flatten(int[][] array){
        int sideLength = array[0].length;
        for(int i = 0; i < array.length; i++){
            if(array[i].length != sideLength){
                throw new RuntimeException("flatten method must take in a non-ragged array");
            }
        }
        int[] returnArray = new int[array.length * array[0].length];

        int position = 0;
        for(int[] i : array){
            for(int j : i){
                returnArray[position++] = j;
            }
        }

        return returnArray;
    }


    public static int[] sliceArray(int[] array, int start, int end){
        int length = end - start;
        int[] slice = new int[length];

        for(int i = 0; i < length; i++){
            slice[i] = array[start + i];
        }
        return slice;
    }


    public static int indexOf(int[] array, int x, int size){
        int index = -1;
        for(int i = 0; i < size; i++){
            if(array[i] == x){
                index = i;
            }
        }
        return index;
    }

    public static int[] combine(int[] ref, int[] plane, int size){
        int[] returnArray = new int[size];
        for(int i = 0; i < size; i++){
            returnArray[i] = ref[i] + plane[i] - 128;
            if(returnArray[i] < 0){
                returnArray[i] += 256;
            }
        }
        return returnArray;
    }

    public static int[] invert(int[] source, int size){
        int[] result = new int[size];
        for(int i = 0; i < size; i++){
            result[i] = 255 - source[i];
        }
        return result;
    }


    public static void correctCoeff(int[] c, byte[] info, int sizeInfo, int w, int h){
        for(int i = 0; i < sizeInfo; i++){
            int p3 = info[i];
            int p2 = info[i+1];
            int p1 = info[i+2];
            int p0 = info[i+3];
            int v = info[i+4];
            int idx = p3<<24 | p2<<16 | p1<<8 | p0;
            int w2 = idx % w;
            int h3 = idx / w;
            int h2 = h - h3 - 1;
            int idx2 = h2*w + w2;
            c[idx2] = v;
        }
    }


    public static void flip(BufferedImage image){
        for (int i=0;i<image.getWidth();i++)
            for (int j=0;j<image.getHeight()/2;j++)
            {
                int tmp = image.getRGB(i, j);
                image.setRGB(i, j, image.getRGB(i, image.getHeight()-j-1));
                image.setRGB(i, image.getHeight()-j-1, tmp);
            }
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if ( Files.isReadable(path) ) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while ( fc.read(buffer) != -1 ) ;
            }
        } else {
            try (
                    InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while ( true ) {
                    int bytes = rbc.read(buffer);
                    if ( bytes == -1 )
                        break;
                    if ( buffer.remaining() == 0 )
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }


    public static float applyDiffGain(IntBuffer coeffs1, IntBuffer coeffs2, int position, FloatBuffer normals, float lightX, float lightY, float gain){
        //calculate the modified PTM polynomial coefficients
        float a0 = gain * coeffs1.get(position);
        float a1 = gain * coeffs1.get(position + 1);
        float a2 = gain * coeffs1.get(position + 2);
        float a3t =  ((coeffs1.get(position)<<1)*normals.get(position) + coeffs1.get(position + 2)*normals.get(position + 1));
        float a3 = (1.0f - gain) * a3t + coeffs2.get(position);
        float a4t = ((coeffs1.get(position + 1)<<1)*normals.get(position + 1) + coeffs1.get(position + 2)*normals.get(position));
        float a4 = (1.0f - gain) * a4t + coeffs2.get(position + 1);
        float a5 = (1.0f - gain) * (coeffs1.get(position)*normals.get(position)*normals.get(position) + coeffs1.get(position + 1)*normals.get(position + 1)*normals.get(position + 1)
                + coeffs1.get(position + 2)*normals.get(position)*normals.get(position + 1)) + (coeffs2.get(position) - a3) * normals.get(position)
                + (coeffs2.get(position + 1) - a4) * normals.get(position + 1) + coeffs2.get(position + 2);

        //modified PTM polynomial
        return a0*lightX*lightX + a1*lightY*lightY + a2*lightX*lightY + a3*lightX + a4*lightY + a5;
    }


    /**
     * Calculates the hemispherical weighting for the given incident light vector angles. Will only calculate the first
     * n terms up to basisTerms, and will leave the rest as zero. The maths for this function comes from the original
     * RTIViewer.
     *
     * @param theta         angle round circle of incident light vector
     * @param phi           azimuthal angle of incident light vector
     * @param basisTerms    number of HSH terms used for the RTIObject
     * @return              the hWeights for this light angle
     */
    public static double[] createHWeights(double theta, double phi, int basisTerms){
        double[] hWeights = new double[16];

        double cosPhi = cos(phi);
        double cosTheta = cos(theta);
        double cosTheta2 = cosTheta * cosTheta;

        hWeights[0] = 1/sqrt(2*PI);
        hWeights[1] = sqrt(6/PI)      *  (cosPhi*sqrt(cosTheta-cosTheta2));
        hWeights[2] = sqrt(3/(2*PI))  *  (-1.0 + 2.0*cosTheta);
        hWeights[3] = sqrt(6/PI)      *  (sqrt(cosTheta - cosTheta2)*sin(phi));

        if (basisTerms > 4) {
            hWeights[4] = sqrt(30/PI)     *  (cos(2.0*phi)*(-cosTheta + cosTheta2));
            hWeights[5] = sqrt(30/PI)     *  (cosPhi*(-1.0 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2));
            hWeights[6] = sqrt(5/(2*PI))  *  (1 - 6.0*cosTheta + 6.0*cosTheta2);
            hWeights[7] = sqrt(30/PI)     *  ((-1 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2)*sin(phi));
            hWeights[8] = sqrt(30/PI)     *  ((-cosTheta + cosTheta2)*sin(2.0*phi));
        }

        if (basisTerms > 9) {
            hWeights[9] = 2*sqrt(35/PI)	*	(cos(3.0*phi)*pow((cosTheta - cosTheta2), 1.5f));
            hWeights[10] = sqrt(210/PI)	*	(cos(2.0*phi)*(-1 + 2*cosTheta)*(-cosTheta + cosTheta2));
            hWeights[11] = 2*sqrt(21/PI)  *	(cos(phi)*sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2));
            hWeights[12] = sqrt(7/(2*PI)) *	(-1 + 12*cosTheta - 30*cosTheta2 + 20*cosTheta2*cosTheta);
            hWeights[13] = 2*sqrt(21/PI)  *	(sqrt(cosTheta - cosTheta2)*(1 - 5*cosTheta + 5*cosTheta2)*sin(phi));
            hWeights[14] = sqrt(210/PI)   *	(-1 + 2*cosTheta)*(-cosTheta + cosTheta2)*sin(2*phi);
            hWeights[15] = 2*sqrt(35/PI)  *	pow((cosTheta - cosTheta2), 1.5f)*sin(3*phi);
        }

        return hWeights;
    }
}
