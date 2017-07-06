package openGLWindow;

import ptmCreation.PTMObjectLRGB;
import toolWindow.RTIViewer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 12-Jun-17.
 */
public class RTIWindowLRGB extends RTIWindow {

    private PTMObjectLRGB ptmObject;

    private int lumCoeffs1Ref;

    private int lumCoeffs2Ref;

    private int rgbCoeffsRef;

    public RTIWindowLRGB(PTMObjectLRGB ptmObject) {
        super(ptmObject);

        this.ptmObject = ptmObject;
    }


    @Override
    protected void bindSpecificShaderTextures(int programID) {
        lumCoeffs1Ref = glGetUniformLocation(programID, "lumCoeffs1");
        lumCoeffs2Ref = glGetUniformLocation(programID, "lumCoeffs2");
        rgbCoeffsRef = glGetUniformLocation(programID, "rgbCoeffs");
        normalsRef = glGetUniformLocation(programID, "normals");
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
        createShader(RTIViewer.ShaderProgram.DEFAULT, "/shaders/defaultVertexShader.glsl",
                "/shaders/lrgbShaders/defaultFragmentShaderLRGB.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "/shaders/defaultVertexShader.glsl",
                "/shaders/lrgbShaders/normalsFragmentShaderLRGB.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "/shaders/defaultVertexShader.glsl",
                "/shaders/lrgbShaders/diffuseGainFragmentShaderLRGB.glsl");


        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "/shaders/defaultVertexShader.glsl",
                "/shaders/lrgbShaders/specEnhanceFragmentShaderLRGB.glsl");


        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "/shaders/defaultVertexShader.glsl",
                "/shaders/lrgbShaders/imgUnsharpMaskFragmentShaderLRGB.glsl");

    }
}
