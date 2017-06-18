package utils;

import java.nio.IntBuffer;

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

    public static int calcIntensity(IntBuffer coeffs1, IntBuffer coeffs2, int position, float x, float y){
        //i = (a0 * Lu^2) + (a1 * Lv^2) + (a2 * Lu * Lv) + (a3 * Lu) + (a4 * Lv) + a5
        double intensity =  (coeffs1.get(position) * x * x) +
                (coeffs1.get(position + 1) * y * y) +
                (coeffs1.get(position + 2) * x * y) +
                (coeffs2.get(position) * x) +
                (coeffs2.get(position + 1) * y) + coeffs2.get(position + 2);

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

        int red = (int) Math.floor(((normal.x + 1) / 2) * 255.0);
        int green = (int) Math.floor(((normal.y + 1) / 2) * 255.0);
        int blue = (int) Math.floor((normal.z + 1) * 128.0);

        if(red > 255){red = 255;}
        else if(red < 0){red = 0;}

        if(green > 255){green = 255;}
        else if(green < 0){green = 0;}

        if(blue > 255){blue = 255;}
        else if(blue < 0){blue = 0;}

        return new int[]{red, green, blue};
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
}
