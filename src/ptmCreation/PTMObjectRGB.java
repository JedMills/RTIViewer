package ptmCreation;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * Created by Jed on 12-Jun-17.
 */
public class PTMObjectRGB extends PTMObject {

    /**Contains the coefficients for PTM polynomial in the red channel*/
    private IntBuffer redVals1;
    private IntBuffer redVals2;

    /**Contains the coefficients for PTM polynomial in the green channel*/
    private IntBuffer greenVals1;
    private IntBuffer greenVals2;

    /**Contains the coefficients for PTM polynomial in the blue channel*/
    private IntBuffer blueVals1;
    private IntBuffer blueVals2;

    public PTMObjectRGB(String fileName, int width, int height, IntBuffer[] texelData) {
        super(fileName, width, height);

        redVals1 = texelData[0];
        redVals2 = texelData[1];
        greenVals1 = texelData[2];
        greenVals2 = texelData[3];
        blueVals1 = texelData[4];
        blueVals2 = texelData[5];

        calculateNormals();
        createPreviewImage();
    }


    /**
     * Calculates the normal for each pixel as an average of the normals for
     * the red, green and blue colour channels at each pixel.
     */
    protected void calculateNormals(){
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

    private void createPreviewImage(){
        previewImage = new WritableImage(width, height);


        int position;
        float red, green, blue;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                position = ((y * width) + x) * 3;

                red = Utils.calcIntensity(redVals1, redVals2, position, 0, 0) / 255.0f;
                green = Utils.calcIntensity(greenVals1, greenVals2, position, 0, 0) / 255.0f;
                blue = Utils.calcIntensity(blueVals1, blueVals2, position, 0, 0) / 255.0f;


                previewImage.getPixelWriter().setColor(x, y, Color.color(red, green, blue));
            }
        }
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
}
