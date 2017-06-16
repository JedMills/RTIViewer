package ptmCreation;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import utils.Utils;

import javax.rmi.CORBA.Util;
import java.nio.IntBuffer;

/**
 * Created by Jed on 12-Jun-17.
 */
public class PTMObjectLRGB extends PTMObject {

    private IntBuffer lumCoeffs1;
    private IntBuffer lumCoeffs2;

    private IntBuffer rgbCoeffs;



    public PTMObjectLRGB(String fileName, int width, int height, IntBuffer[] texelData) {
        super(fileName, width, height);

        lumCoeffs1 = texelData[0];
        lumCoeffs2 = texelData[1];
        rgbCoeffs = texelData[2];

        calculateNormals();
        createPreviewImage();
    }

    @Override
    protected void calculateNormals() {
        normals = BufferUtils.createFloatBuffer(width * height * 3);

        Utils.Vector3f temp;
        int[] tempCoeffs = new int[6];
        for(int i = 0; i < width * height; i++){
            tempCoeffs[0] = lumCoeffs1.get(i * 3);
            tempCoeffs[1] = lumCoeffs1.get((i * 3) + 1);
            tempCoeffs[2] = lumCoeffs1.get((i * 3) + 2);
            tempCoeffs[3] = lumCoeffs2.get(i * 3);
            tempCoeffs[4] = lumCoeffs2.get((i * 3) + 1);
            tempCoeffs[5] = lumCoeffs2.get((i * 3) + 2);

            temp = calculateNormal(tempCoeffs);

            normals.put((i * 3), temp.x);
            normals.put((i * 3) + 1, temp.y);
            normals.put((i * 3) + 2, temp.z);
        }
    }

    @Override
    protected void createPreviewImage() {
        previewImage = new WritableImage(width, height);

        int position;
        float lum, red, green, blue;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                position = ((y * width) + x) * 3;

                lum = Utils.calcIntensity(lumCoeffs1, lumCoeffs2, position, 0, 0) / 255.0f;
                red = rgbCoeffs.get(position) * lum / 255.0f;
                green = rgbCoeffs.get(position + 1) * lum / 255.0f;
                blue = rgbCoeffs.get(position + 2) * lum / 255.0f;

                previewImage.getPixelWriter().setColor(x, y, Color.color(red, green, blue));
            }
        }
    }

    public IntBuffer getLumCoeffs1() {
        return lumCoeffs1;
    }

    public IntBuffer getLumCoeffs2() {
        return lumCoeffs2;
    }

    public IntBuffer getRgbCoeffs() {
        return rgbCoeffs;
    }
}
