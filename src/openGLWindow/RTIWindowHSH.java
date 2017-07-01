package openGLWindow;

import org.lwjgl.BufferUtils;
import ptmCreation.RTIObjectHSH;
import toolWindow.RTIViewer;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 18-Jun-17.
 */
public class RTIWindowHSH extends RTIWindow {

    private RTIObjectHSH RTIObjectHSH;
    private int basisTerms;

    private IntBuffer dataTexture;
    private int dataTextureRef;

    private int redCoeffs1Ref;
    private int redCoeffs2Ref;
    private int redCoeffs3Ref;

    private int greenCoeffs1Ref;
    private int greenCoeffs2Ref;
    private int greenCoeffs3Ref;

    private int blueCoeffs1Ref;
    private int blueCoeffs2Ref;
    private int blueCoeffs3Ref;

    public RTIWindowHSH(RTIObjectHSH ptmObject) {
        super(ptmObject);
        RTIObjectHSH = ptmObject;
        basisTerms = ptmObject.getBasisTerms();
        dataTexture = BufferUtils.createIntBuffer(3);
        dataTexture.put(0, basisTerms);
    }

    @Override
    protected void createShaders() throws Exception {
        createShader(RTIViewer.ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/defaultFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/normalsFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/specEnhanceFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/imgUnsharpMaskFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/diffuseGainFragmentShaderHSH.glsl");


    }

    @Override
    protected void bindSpecificShaderTextures(int programID) {
        dataTextureRef = glGetUniformLocation(programID, "dataTexture");
        normalsRef = glGetUniformLocation(programID, "normals");

        redCoeffs1Ref = glGetUniformLocation(programID, "redCoeffs1");
        greenCoeffs1Ref = glGetUniformLocation(programID, "greenCoeffs1");
        blueCoeffs1Ref = glGetUniformLocation(programID, "blueCoeffs1");

        redCoeffs2Ref = glGetUniformLocation(programID, "redCoeffs2");
        greenCoeffs2Ref = glGetUniformLocation(programID, "greenCoeffs2");
        blueCoeffs2Ref = glGetUniformLocation(programID, "blueCoeffs2");

        redCoeffs3Ref = glGetUniformLocation(programID, "redCoeffs3");
        greenCoeffs3Ref = glGetUniformLocation(programID, "greenCoeffs3");
        blueCoeffs3Ref = glGetUniformLocation(programID, "blueCoeffs3");
    }

    @Override
    protected void bindShaderVals() {
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1i(dataTextureRef, 0);
        setShaderTexture(0, dataTexture, 1, 1);

        glUniform1i(normalsRef, 1);
        setNormalsTexture(1, RTIObjectHSH.getNormals());

        glUniform1i(redCoeffs1Ref, 2);
        glUniform1i(greenCoeffs1Ref, 3);
        glUniform1i(blueCoeffs1Ref, 4);

        setNormalsTexture(2, RTIObjectHSH.getRedVals1());
        setNormalsTexture(3, RTIObjectHSH.getGreenVals1());
        setNormalsTexture(4, RTIObjectHSH.getBlueVals1());

        glUniform1i(redCoeffs2Ref, 5);
        glUniform1i(greenCoeffs2Ref, 6);
        glUniform1i(blueCoeffs2Ref, 7);

        if(basisTerms > 3){
            setNormalsTexture(5, RTIObjectHSH.getRedVals2());
            setNormalsTexture(6, RTIObjectHSH.getGreenVals2());
            setNormalsTexture(7, RTIObjectHSH.getBlueVals2());
        }

        glUniform1i(redCoeffs3Ref, 8);
        glUniform1i(greenCoeffs3Ref, 9);
        glUniform1i(blueCoeffs3Ref, 10);

        if(basisTerms > 6){
            setNormalsTexture(8, RTIObjectHSH.getRedVals3());
            setNormalsTexture(9, RTIObjectHSH.getGreenVals3());
            setNormalsTexture(10, RTIObjectHSH.getBlueVals3());
        }
    }
}
