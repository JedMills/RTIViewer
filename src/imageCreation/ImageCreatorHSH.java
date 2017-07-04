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
 * Created by Jed on 04-Jul-17.
 */
public class ImageCreatorHSH {

    private static final float NORM_ENHANCE_GAIN = 1.0f;
    private static final float NORM_ENHANCE_ENV = 1.5f;


    public static WritableImage createImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                            boolean red, boolean green, boolean blue, float[] shaderParams){

        RTIObjectHSH rtiObjectHSH = (RTIObjectHSH) rtiObject;

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


    private static WritableImage createDefaultImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                        boolean red, boolean blue, boolean green){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        int position;
        float[] rgb;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                rgb = getRGB(rtiObject, position, hWeights);

                clampRGB(rgb, red, green, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return writableImage;
    }


    private static WritableImage createNormalEnhanceImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                    boolean red, boolean blue, boolean green, float[] shaderParams){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        double lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        int position;
        float[] rgb;
        Utils.Vector3f normal, smoothedNormal, enhancedNormal;
        float enhancement;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                rgb = getRGB(rtiObject, position, hWeights);

                normal = new Utils.Vector3f(rtiObject.getNormals().get(position),
                                            rtiObject.getNormals().get(position + 1),
                                            rtiObject.getNormals().get(position + 2));

                smoothedNormal = getSmoothedNormal(rtiObject, x, y);
                enhancedNormal = getEnhancedNormal(normal, smoothedNormal);
                enhancement = getEnhancement(enhancedNormal, lightX, lightY, lightZ, shaderParams[0]);


                rgb[0] *= enhancement;
                rgb[1] *= enhancement;
                rgb[2] *= enhancement;

                clampRGB(rgb, red, green, blue);

                writableImage.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return writableImage;
    }



    private static WritableImage createSpecEnhanceImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                                        boolean red, boolean blue, boolean green, float[] shaderParams){
        WritableImage image = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());


        double lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        Utils.Vector3f hVector = new Utils.Vector3f(lightX, lightY, 1.0f);
        hVector.multiply(0.5f);
        hVector = hVector.normalise();

        int position;
        float[] rgb;
        Utils.Vector3f normal;
        float nDotH, temp, lum;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                rgb = getRGB(rtiObject, position, hWeights);

                normal = new Utils.Vector3f(rtiObject.getNormals().get(position),
                        rtiObject.getNormals().get(position + 1),
                        rtiObject.getNormals().get(position + 2));

                nDotH = hVector.dot(normal);
                if(nDotH < 0.0){nDotH = 0.0f;}
                else if(nDotH > 1.0){nDotH = 1.0f;}
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);

                temp = (rgb[0] + rgb[1] + rgb[2]) / 3.0f;
                lum = (float)(temp * shaderParams[1] * 4.0 * nDotH);

                rgb[0] = rgb[0] * shaderParams[0] + lum;
                rgb[1] = rgb[1] * shaderParams[0] + lum;
                rgb[2] = rgb[2] * shaderParams[0] + lum;

                clampRGB(rgb, red, green, blue);

                image.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return image;
    }


    private static WritableImage createImgUnMaskImage(RTIObjectHSH rtiObject, float lightX, float lightY,
                                             boolean red, boolean blue, boolean green, float[] shaderParams){

        WritableImage image = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        double[] hWeights = calcAnglesAndHWeights(rtiObject.getBasisTerms(), lightX, lightY);

        int position;
        float[] rgb, yuv;
        float enhancedLum;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                rgb = getRGB(rtiObject, position, hWeights);
                yuv = ImageCreator.calcYUV(rgb[0], rgb[1], rgb[2]);
                enhancedLum = calcEnhancedLum(rtiObject, yuv[0], x, y, shaderParams[0], lightX, lightY, hWeights);

                rgb = ImageCreator.getRGB(enhancedLum, yuv[1], yuv[2]);

                clampRGB(rgb, red, green, blue);

                image.getPixelWriter().setColor(x, y, Color.color(rgb[0], rgb[1], rgb[2]));
            }
        }
        return image;
    }



    private static float calcEnhancedLum(RTIObjectHSH rtiObject, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY, double[] hWeights){
        int distance = 2;
        float tempLum = 0;

        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(rtiObject, x + xOffset, y + yOffset, lightX, lightY, hWeights);
            }
        }

        tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
        tempLum = lum + imgUnMaskGain * (lum - tempLum);

        return tempLum;
    }


    private static float getLumFromCoord(RTIObjectHSH rtiObject, int x, int y, float lightX, float lightY, double[] hWeights){
        int position = ((y * rtiObject.getWidth()) + x) * 3;

        float[] rgb;
        try {
            rgb = getRGB(rtiObject, position, hWeights);
        }catch(IndexOutOfBoundsException e){
            rgb = new float[]{0, 0, 0};
        }

        return (float) (rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.144);

    }



    private static void clampRGB(float[] rgb, boolean red, boolean green, boolean blue){
        if(!red || rgb[0] < 0.0){rgb[0] = 0.0f;}
        else if(rgb[0] > 1.0f){rgb[0] = 1.0f;}

        if(!green || rgb[1] < 0.0){rgb[1] = 0.0f;}
        else if(rgb[1] > 1.0f){rgb[1] = 1.0f;}

        if(!blue || rgb[2] < 0.0){rgb[2] = 0.0f;}
        else if(rgb[2] > 1.0f){rgb[2] = 1.0f;}
    }


    private static double[] calcAnglesAndHWeights(int basisTerms, float lightX, float lightY){
        double lightZ = sqrt(1 - (lightX * lightX) - (lightY * lightY));

        double phi = atan2(lightY, lightX);
        if(phi < 0){phi = 2 * PI + phi;}

        double theta = min(acos(lightZ), PI /2 - 0.04);

        return createHWeights(theta, phi, basisTerms);
    }




    private static float getEnhancement(Utils.Vector3f enhancedNormal, double lightX,
                                                                double lightY, double lightZ, float diffGain){
        double nDotL = enhancedNormal.x * lightX + enhancedNormal.y * lightY + enhancedNormal.z * lightZ;

        if(nDotL < 0.0){nDotL = 0.0;}
        else if(nDotL > 1.0){nDotL = 1.0;}

        return (float)((diffGain) * nDotL + NORM_ENHANCE_ENV) / ((diffGain) + NORM_ENHANCE_ENV);
    }



    private static Utils.Vector3f getEnhancedNormal(Utils.Vector3f normal, Utils.Vector3f smoothNormal){
        Utils.Vector3f enhanced = normal.add((normal.minus(smoothNormal)).multiply(10 * NORM_ENHANCE_GAIN));
        return enhanced.normalise();
    }


    private static Utils.Vector3f getSmoothedNormal(RTIObjectHSH rtiObject, int x, int y){
        int dist = 5;

        Utils.Vector3f smoothedNormal = new Utils.Vector3f(0.0f, 0.0f, 0.0f);

        int position;
        for(int xOffset = -dist; xOffset <= dist; xOffset++){
            for(int yOffset = -dist; yOffset <= dist; yOffset++){
                position = (((y + yOffset) * rtiObject.getWidth()) + (x + xOffset)) * 3;

                try {
                    smoothedNormal.x += 5 * rtiObject.getNormals().get(position);
                    smoothedNormal.y += 5 * rtiObject.getNormals().get(position + 1);
                    smoothedNormal.z += 5 * rtiObject.getNormals().get(position + 2);
                }catch (IndexOutOfBoundsException e){}
            }
        }

        smoothedNormal.x /= (2 * dist + 1) * (2 * dist + 1);
        smoothedNormal.y /= (2 * dist + 1) * (2 * dist + 1);
        smoothedNormal.z /= (2 * dist + 1) * (2 * dist + 1);

        return smoothedNormal.normalise();
    }




    private static float[] getRGB(RTIObjectHSH rtiObject, int position, double[] hWeights){
        float[] redVals = new float[9];
        float[] greenVals = new float[9];
        float[] blueVals = new float[9];

        getTexelData(redVals, rtiObject.getRedVals1(), rtiObject.getRedVals2(),
                rtiObject.getRedVals3(), rtiObject.getBasisTerms(), position);

        getTexelData(greenVals, rtiObject.getGreenVals1(), rtiObject.getGreenVals2(),
                rtiObject.getGreenVals3(), rtiObject.getBasisTerms(), position);

        getTexelData(blueVals, rtiObject.getBlueVals1(), rtiObject.getBlueVals2(),
                rtiObject.getBlueVals3(), rtiObject.getBasisTerms(), position);

        return  createColours(redVals, greenVals, blueVals, rtiObject.getBasisTerms(), hWeights);
    }


    private static float[] createColours(float[] redVals, float[] greenVals, float[] blueVals,
                                         int basisTerms, double[] hWeights){
        float[] rgb = new float[]{0, 0, 0};
        float[][] colourVals = new float[][]{redVals, greenVals, blueVals};

        for(int i = 0; i < 3; i++) {
            for (int k = 0; k < basisTerms; k++) {
                rgb[i] += colourVals[i][k] * hWeights[k];
            }
        }
        return rgb;
    }





    private static void getTexelData(float[] out, FloatBuffer buffer0, FloatBuffer buffer1,
                                        FloatBuffer buffer2, int basisTerms, int position){
        out[0] = buffer0.get(position);
        out[1] = buffer0.get(position + 1);
        out[2] = buffer0.get(position + 2);

        if(basisTerms > 3){
            out[3] = buffer1.get(position);
            out[4] = buffer1.get(position + 1);
            out[5] = buffer1.get(position + 2);
        }

        if(basisTerms > 6){
            out[6] = buffer2.get(position);
            out[7] = buffer2.get(position + 1);
            out[8] = buffer2.get(position + 2);
        }
    }





    private static double[] createHWeights(double theta, double phi, int basisTerms){
        double[] hWeights = new double[16];


        double cosPhi = cos(phi);
        double cosTheta = cos(theta);
        double cosTheta2 = cosTheta * cosTheta;

        hWeights[0] = 1/sqrt(2*PI);
        hWeights[1] = sqrt(6/PI)      *  (cosPhi*sqrt(cosTheta-cosTheta2));
        hWeights[2] = sqrt(3/(2*PI))  *  (-1.0 + 2.0*cosTheta);
        hWeights[3] = sqrt(6/PI)      *  (sqrt(cosTheta - cosTheta2)*sin(phi));
        if (basisTerms > 4)
        {

            hWeights[4] = sqrt(30/PI)     *  (cos(2.0*phi)*(-cosTheta + cosTheta2));
            hWeights[5] = sqrt(30/PI)     *  (cosPhi*(-1.0 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2));
            hWeights[6] = sqrt(5/(2*PI))  *  (1 - 6.0*cosTheta + 6.0*cosTheta2);
            hWeights[7] = sqrt(30/PI)     *  ((-1 + 2.0*cosTheta)*sqrt(cosTheta - cosTheta2)*sin(phi));
            hWeights[8] = sqrt(30/PI)     *  ((-cosTheta + cosTheta2)*sin(2.0*phi));
        }
        if (basisTerms > 9)
        {

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
