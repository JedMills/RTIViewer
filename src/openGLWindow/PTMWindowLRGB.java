package openGLWindow;

import ptmCreation.PTMObject;
import ptmCreation.PTMObjectLRGB;
import toolWindow.RTIViewer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 12-Jun-17.
 */
public class PTMWindowLRGB extends PTMWindow {

    private PTMObjectLRGB ptmObject;

    private int lumCoeffs1Ref;

    private int lumCoeffs2Ref;

    private int rgbCoeffsRef;

    public PTMWindowLRGB(PTMObjectLRGB ptmObject) {
        super(ptmObject);

        this.ptmObject = ptmObject;
    }

    @Override
    protected void bindShaderReferences(int programID, boolean setTextures) {
        shaderWidth = glGetUniformLocation(programID, "imageWidth");
        shaderHeight = glGetUniformLocation(programID, "imageHeight");
        imageScaleRef = glGetUniformLocation(programID, "imageScale");
        shaderViewportX = glGetUniformLocation(programID, "viewportX");
        shaderViewportY = glGetUniformLocation(programID, "viewportY");

        shaderLightX = glGetUniformLocation(programID, "lightX");
        shaderLightY = glGetUniformLocation(programID, "lightY");

        if(setTextures){
            lumCoeffs1Ref = glGetUniformLocation(programID, "lumCoeffs1");
            lumCoeffs2Ref = glGetUniformLocation(programID, "lumCoeffs2");
            rgbCoeffsRef = glGetUniformLocation(programID, "rgbCoeffs");
            normalsRef = glGetUniformLocation(programID, "normals");
        }

        diffGainRef = glGetUniformLocation(programID, "diffGain");
        diffConstRef = glGetUniformLocation(programID, "diffConst");
        specConstRef = glGetUniformLocation(programID, "specConst");
        specExConstRef = glGetUniformLocation(programID, "specExConst");

        normUnMaskGainRef = glGetUniformLocation(programID, "normUnMaskGain");
        normUnMaskEnvRef = glGetUniformLocation(programID, "normUnMaskEnv");

        imgUnMaskGainRef = glGetUniformLocation(programID, "imgUnMaskGain");

        coeffUnMaskGainRef = glGetUniformLocation(programID, "coeffUnMaskGain");
    }

    @Override
    protected void bindShaderVals() {
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1i(lumCoeffs1Ref, 0);
        glUniform1i(lumCoeffs2Ref, 1);
        glUniform1i(rgbCoeffsRef, 2);
        glUniform1i(normalsRef, 3);

        setShaderTexture(0, ptmObject.getLumCoeffs1());
        setShaderTexture(1, ptmObject.getLumCoeffs2());
        setShaderTexture(2, ptmObject.getRgbCoeffs());
        setNormalsTexture(3, ptmObject.getNormals());
    }


    @Override
    protected void createShaders() throws Exception {
        createShader(RTIViewer.ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/lrgbShaders/defaultFragmentShaderLRGB.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/lrgbShaders/normalsFragmentShaderLRGB.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/lrgbShaders/diffuseGainFragmentShaderLRGB.glsl");


        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/lrgbShaders/specEnhanceFragmentShaderLRGB.glsl");


        /*
        createShader(RTIViewer.ShaderProgram.NORM_UNSHARP_MASK, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/normUnsharpMaskFragmentShaderLRGB.glsl");
        */

        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/lrgbShaders/imgUnsharpMaskFragmentShaderLRGB.glsl");

    }
}
