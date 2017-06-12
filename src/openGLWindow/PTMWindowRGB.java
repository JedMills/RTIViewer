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
                "src/shaders/defaultFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/normalsFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/diffuseGainFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/specEnhanceFragmentShader.glsl");

    }



    /**
     * Gets the integer OpenGL references from the shader program specified by the shaderID and sets them to the
     * relevant attributes in this class. The textures (rVals1, rVals2 ... etc.) only need to be set first time
     * the program is compiled as they do not changed, so there is an option to set them or not.
     *
     * @param programID         the program for which we want to set the references for
     * @param setTextures       whether we want to set references for textures or not
     */
    protected void bindShaderReferences(int programID, boolean setTextures){
        //get the integer OpenGL reference  from the shader program using its string value
        shaderWidth = glGetUniformLocation(programID, "imageWidth");
        shaderHeight = glGetUniformLocation(programID, "imageHeight");
        imageScaleRef = glGetUniformLocation(programID, "imageScale");
        shaderViewportX = glGetUniformLocation(programID, "viewportX");
        shaderViewportY = glGetUniformLocation(programID, "viewportY");

        shaderLightX = glGetUniformLocation(programID, "lightX");
        shaderLightY = glGetUniformLocation(programID, "lightY");

        //textures only need to be bound first time round
        if(setTextures) {
            rVals1Ref = glGetUniformLocation(programID, "rVals1");
            rVals2Ref = glGetUniformLocation(programID, "rVals2");

            gVals1Ref = glGetUniformLocation(programID, "gVals1");
            gVals2Ref = glGetUniformLocation(programID, "gVals2");

            bVals1Ref = glGetUniformLocation(programID, "bVals1");
            bVals2Ref = glGetUniformLocation(programID, "bVals2");

            normalsRef = glGetUniformLocation(programID, "normals");
        }

        diffGainRef = glGetUniformLocation(programID, "diffGain");
        diffConstRef = glGetUniformLocation(programID, "diffConst");
        specConstRef = glGetUniformLocation(programID, "specConst");
        specExConstRef = glGetUniformLocation(programID, "specExConst");
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
