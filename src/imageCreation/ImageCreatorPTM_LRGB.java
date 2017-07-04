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
 * Created by Jed on 04-Jul-17.
 */
public class ImageCreatorPTM_LRGB {


    public static WritableImage createImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                            boolean red, boolean green, boolean blue, float[] shaderParams){

        PTMObjectLRGB ptmObjectLRGB = (PTMObjectLRGB) rtiObject;

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


    private static WritableImage createDefaultImage(PTMObjectLRGB ptmObjectLRGB, float lightX,
                                                        float lightY, boolean red, boolean green, boolean blue){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                                            ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / 255.0f;

                r = ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;

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

    private static WritableImage createDiffGainImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                     boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                lum = applyDiffGain(ptmObjectLRGB.getLumCoeffs1(), ptmObjectLRGB.getLumCoeffs2(),
                                    position, ptmObjectLRGB.getNormals(), lightX, lightY, shaderParams[0]) / 255.0f;

                r = ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;

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



    private static WritableImage createSpecEnhanceImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                        boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage writableImage = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        Utils.Vector3f hVector = new Utils.Vector3f(lightX, lightY, 1.0f);
        hVector.multiply(0.5f);
        hVector = hVector.normalise();
        int position;
        float lum, r, g, b, nDotH;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++){
            for(int y = 0; y < ptmObjectLRGB.getHeight(); y++){
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                        ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / (255.0f * 255.0f);

                r = ptmObjectLRGB.getRgbCoeffs().get(position);
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1);
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2);

                nDotH = hVector.x * ptmObjectLRGB.getNormals().get(position) +
                        hVector.y * ptmObjectLRGB.getNormals().get(position + 1) +
                        hVector.z * ptmObjectLRGB.getNormals().get(position + 2);

                nDotH = clampChannel(nDotH, true);
                nDotH = (float) Math.pow(nDotH, shaderParams[2]);
                nDotH *= shaderParams[1] * 255.0f;

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



    private static WritableImage createImgUnMaskImage(PTMObjectLRGB ptmObjectLRGB, float lightX, float lightY,
                                                      boolean red, boolean green, boolean blue, float[] shaderParams){
        WritableImage image = new WritableImage(ptmObjectLRGB.getWidth(), ptmObjectLRGB.getHeight());

        System.out.println("gain: " + shaderParams[0]);

        int position;
        float lum, r, g, b;
        for(int x = 0; x < ptmObjectLRGB.getWidth(); x++) {
            for (int y = 0; y < ptmObjectLRGB.getHeight(); y++) {
                position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

                lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                        ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / (255.0f * 255.0f);

                r = ptmObjectLRGB.getRgbCoeffs().get(position) * lum;
                g = ptmObjectLRGB.getRgbCoeffs().get(position + 1) * lum;
                b = ptmObjectLRGB.getRgbCoeffs().get(position + 2) * lum;

                float[] yuv = calcYUV(r, g, b);
                float enhancedLum = calcEnhancedLum(ptmObjectLRGB, yuv[0], x, y, shaderParams[0], lightX, lightY);
                float[] rgb = getRGB(enhancedLum, yuv[1], yuv[2]);

                r = clampChannel(rgb[0], red);
                g = clampChannel(rgb[1], green);
                b = clampChannel(rgb[2], blue);

                image.getPixelWriter().setColor(x, y, Color.color(r, g, b));
            }
        }


        return image;
    }



    private static float clampChannel(float val, boolean useChannel){
        if(!useChannel){return 0.0f;}
        if(val > 1.0f){return 1.0f;}
        if(val < 0.0){return 0.0f;}
        return val;
    }


    private static float calcEnhancedLum(PTMObjectLRGB ptmObjectLRGB, float lum, int x, int y,
                                         float imgUnMaskGain, float lightX, float lightY){
        int distance = 2;
        float tempLum = 0;

        for(int xOffset = -distance; xOffset <= distance; xOffset++){
            for(int yOffset = -distance; yOffset <= distance; yOffset++){
                tempLum += getLumFromCoord(ptmObjectLRGB, x + xOffset, y + yOffset, lightX, lightY);
            }
        }

        tempLum /= ((distance * 2) + 1) * ((distance * 2) + 1);
        tempLum = lum + imgUnMaskGain * (lum - tempLum);

        return tempLum;
    }


    private static float getLumFromCoord(PTMObjectLRGB ptmObjectLRGB, int x, int y, float lightX, float lightY){
        int position = ((y * ptmObjectLRGB.getWidth()) + x) * 3;

        float r = 0;
        float g = 0;
        float b = 0;
        try {
            float lum = Utils.calcIntensity(ptmObjectLRGB.getLumCoeffs1(),
                    ptmObjectLRGB.getLumCoeffs2(), position, lightX, lightY) / 255.0f;

            r = lum * ptmObjectLRGB.getRgbCoeffs().get(position) / 255.0f;
            g = lum * ptmObjectLRGB.getRgbCoeffs().get(position + 1) / 255.0f;
            b = lum * ptmObjectLRGB.getRgbCoeffs().get(position + 2) / 255.0f;
        }catch(IndexOutOfBoundsException e){
            r = 0;
            g = 0;
            b = 0;
        }

        return (float) (r * 0.299 + g * 0.587 + b * 0.144);

    }
}
