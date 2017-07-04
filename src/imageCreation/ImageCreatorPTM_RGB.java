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
 * Created by Jed on 03-Jul-17.
 */
public class ImageCreatorPTM_RGB {


    public static WritableImage createImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                            boolean red, boolean green, boolean blue, float[] shaderParams){

        PTMObjectRGB ptmObjectRGB = (PTMObjectRGB) rtiObject;

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


    private static WritableImage createDefaultImage(PTMObjectRGB rtiObject, float lightX, float lightY,
                                                    boolean red, boolean green, boolean blue){

        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        int position;
        float r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++){
            for(int y = 0; y < rtiObject.getHeight(); y++){
                position = ((y * rtiObject.getWidth()) + x) * 3;

                if(red) {
                    r = Utils.calcIntensity(rtiObject.getRedVals1(), rtiObject.getRedVals2(), position, lightX, lightY) / 255.0f;
                }else{r = 0;}

                if(green){
                    g = Utils.calcIntensity(rtiObject.getGreenVals1(), rtiObject.getGreenVals2(), position, lightX, lightY) / 255.0f;
                }else{g = 0;}

                if(blue) {
                    b = Utils.calcIntensity(rtiObject.getBlueVals1(), rtiObject.getBlueVals2(), position, lightX, lightY) / 255.0f;
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }
        return writableImage;
    }


    private static WritableImage createDiffGainImage(PTMObjectRGB rtiObject, float lightX, float lightY,
                                                     boolean red, boolean green, boolean blue, float[] shaderParams){

        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        int position, r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                if(red) {
                    r = Utils.toByte(applyDiffGain(rtiObject.getRedVals1(), rtiObject.getRedVals2(), position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{r = 0;}

                if(green){
                    g = Utils.toByte(applyDiffGain(rtiObject.getGreenVals1(), rtiObject.getGreenVals2(), position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{g = 0;}

                if(blue) {
                    b = Utils.toByte(applyDiffGain(rtiObject.getBlueVals1(), rtiObject.getBlueVals2(), position, rtiObject.getNormals(), lightX, lightY, shaderParams[0]));
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, Color.rgb(r, g, b));
            }
        }

        return writableImage;
    }



    private static WritableImage createSpecEnhanceImage(PTMObjectRGB ptmObjectRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams){

        WritableImage writableImage = new WritableImage(ptmObjectRGB.getWidth(), ptmObjectRGB.getHeight());

        Utils.Vector3f hVector = new Utils.Vector3f(lightX / 2.0f, lightY / 2.0f, 0.5f);
        hVector = hVector.normalise();

        FloatBuffer normals = ptmObjectRGB.getNormals();


        int position, r, g, b;
        for(int x = 0; x < ptmObjectRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectRGB.getHeight(); y++) {
                position = ((y * ptmObjectRGB.getWidth()) + x) * 3;


                float nDotH  =  hVector.x * normals.get(position)       +
                                hVector.y * normals.get(position + 1)   +
                                hVector.z * normals.get(position + 2);

                if(nDotH < 0.0){nDotH = 0.0f;}
                else if(nDotH > 1.0){nDotH = 1.0f;}
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);


                r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(), position, lightX, lightY);
                g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(), position, lightX, lightY);
                b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(), position, lightX, lightY);

                float temp = (r + g + b) / 3;
                temp = temp * shaderParams[1] * 2 * nDotH;

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


    private static WritableImage createImgUnMaskImage(PTMObjectRGB ptmObjectRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams) {

        WritableImage writableImage = new WritableImage(ptmObjectRGB.getWidth(), ptmObjectRGB.getHeight());

        int position;
        float r, g, b;
        for(int x = 0; x < ptmObjectRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectRGB.getHeight(); y++) {
                position = ((y * ptmObjectRGB.getWidth()) + x) * 3;

                r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(),
                        position, lightX, lightY) / 255.0f;
                g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(),
                        position, lightX, lightY) / 255.0f;
                b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(),
                        position, lightX, lightY) / 255.0f;

                float[] yuv = calcYUV(r, g, b);
                float enhancedLum = calcEnhancedLum(ptmObjectRGB, yuv[0], x, y, shaderParams[0], lightX, lightY);
                float[] rgb = getRGB(enhancedLum, yuv[1], yuv[2]);

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



    private static float calcEnhancedLum(PTMObjectRGB ptmObjectRGB, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY){
        int distance = 2;
        float tempLum = 0;

        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(ptmObjectRGB, x + xOffset, y + yOffset, lightX, lightY);
            }
        }

        tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
        tempLum = lum + imgUnMaskGain * (lum - tempLum);

        return tempLum;
    }


    private static float getLumFromCoord(PTMObjectRGB ptmObjectRGB, int x, int y, float lightX, float lightY){
        int position = ((y * ptmObjectRGB.getWidth()) + x) * 3;

        float r = 0;
        float g = 0;
        float b = 0;
        try {
            r = Utils.calcIntensity(ptmObjectRGB.getRedVals1(), ptmObjectRGB.getRedVals2(), position, lightX, lightY) / 255.0f;
            g = Utils.calcIntensity(ptmObjectRGB.getGreenVals1(), ptmObjectRGB.getGreenVals2(), position, lightX, lightY) / 255.0f;
            b = Utils.calcIntensity(ptmObjectRGB.getBlueVals1(), ptmObjectRGB.getBlueVals2(), position, lightX, lightY) / 255.0f;
        }catch(IndexOutOfBoundsException e){
            r = 0;
            g = 0;
            b = 0;
        }

        return (float) (r * 0.299 + g * 0.587 + b * 0.144);

    }


}
