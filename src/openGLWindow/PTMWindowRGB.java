package openGLWindow;

import ptmCreation.PTMObjectRGB;
import toolWindow.RTIViewer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 12-Jun-17.
 */
public class PTMWindowRGB extends PTMWindow {

    private PTMObjectRGB ptmObjectRGB;

    /**OpenGL reference for the GLSL isampler2D texture "rVals1", used for passing rVals1 attr to shaders */
    private int rVals1Ref;

    /**OpenGL reference for the GLSL isampler2D texture "rVals2", used for passing rVals2 attr to shaders */
    private int rVals2Ref;

    /**OpenGL reference for the GLSL isampler2D texture "gVals1", used for passing gVals1 attr to shaders */
    private int gVals1Ref;

    /**OpenGL reference for the GLSL isampler2D texture "gVals2", used for passing gVals2 attr to shaders */
    private int gVals2Ref;

    /**OpenGL reference for the GLSL isampler2D texture "bVals1", used for passing bVals1 attr to shaders */
    private int bVals1Ref;

    /**OpenGL reference for the GLSL isampler2D texture "gVals2", used for passing gVals2 attr to shaders */
    private int bVals2Ref;



    public PTMWindowRGB(PTMObjectRGB ptmObject) {
        super(ptmObject);

        ptmObjectRGB = ptmObject;
    }


    /**
     * Creates one shader program for all the fragment shaders in the shaders package by calling createShader
     * for each .glsl file.
     *
     * @throws Exception if there is an error when reading the vertex/fragment shader files
     */
    protected void createShaders() throws Exception{
        createShader(RTIViewer.ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/defaultFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/normalsFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/diffuseGainFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/specEnhanceFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.NORM_UNSHARP_MASK, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/normUnhsharpMaskFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/imgUnhsharpMaskFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.COEFF_UN_MASK,"src/shaders/defaultVertexShader.glsl",
                "src/shaders/rgbShaders/coeffUnhsharpMaskFragmentShader.glsl");
    }


    @Override
    protected void bindSpecificShaderTextures(int programID) {
        rVals1Ref = glGetUniformLocation(programID, "rVals1");
        rVals2Ref = glGetUniformLocation(programID, "rVals2");

        gVals1Ref = glGetUniformLocation(programID, "gVals1");
        gVals2Ref = glGetUniformLocation(programID, "gVals2");

        bVals1Ref = glGetUniformLocation(programID, "bVals1");
        bVals2Ref = glGetUniformLocation(programID, "bVals2");

        normalsRef = glGetUniformLocation(programID, "normals");
    }

    /**
     * Assigns the values of all the shader references that we got in bindShaderReferences. Textures for  each
     * shader do not change during the program, so they only need to be set on initialisation, so there is a boolean
     * to set them or not.
     *
     */
    protected void bindShaderVals(){
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1i(rVals1Ref, 0);
        glUniform1i(rVals2Ref, 1);
        glUniform1i(gVals1Ref, 2);
        glUniform1i(gVals2Ref, 3);
        glUniform1i(bVals1Ref, 4);
        glUniform1i(bVals2Ref, 5);
        glUniform1i(normalsRef, 6);

        setShaderTexture(0, ptmObjectRGB.getRedVals1());
        setShaderTexture(1, ptmObjectRGB.getRedVals2());
        setShaderTexture(2, ptmObjectRGB.getGreenVals1());
        setShaderTexture(3, ptmObjectRGB.getGreenVals2());
        setShaderTexture(4, ptmObjectRGB.getBlueVals1());
        setShaderTexture(5, ptmObjectRGB.getBlueVals2());
        setNormalsTexture(6, ptmObject.getNormals());
    }
}
