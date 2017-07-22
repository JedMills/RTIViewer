package openGLWindow;

import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import toolWindow.RTIViewer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 12-Jun-17.
 */
public class RTIWindowRGB extends RTIWindow {

    /** The PTM object that this window will display */
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


    /**
     * Creates a new RTIWindowLRGB and sets the {@link RTIWindowLRGB#ptmObject} to the given object;
     *
     * @see {@link RTIWindow#RTIWindow(RTIObject)}
     *
     * @param ptmObject     the PTMObjectRGB to show in this window
     */
    public RTIWindowRGB(PTMObjectRGB ptmObject) {
        super(ptmObject);

        ptmObjectRGB = ptmObject;
    }




    /**
     * Creates one shader program for all the fragment shaders in the shaders package by calling createShader
     * for each .glsl file.
     *
     * @see shaders.rgbShaders
     * @see RTIWindow#createShaders()
     *
     * @throws Exception if there is an error when reading the vertex/fragment shader files
     */
    protected void createShaders() throws Exception{
        createShader(RTIViewer.ShaderProgram.DEFAULT, "/shaders/defaultVertexShader.glsl",
                "/shaders/rgbShaders/defaultFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "/shaders/defaultVertexShader.glsl",
                "/shaders/rgbShaders/normalsFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "/shaders/defaultVertexShader.glsl",
                "/shaders/rgbShaders/diffuseGainFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "/shaders/defaultVertexShader.glsl",
                "/shaders/rgbShaders/specEnhanceFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "/shaders/defaultVertexShader.glsl",
                "/shaders/rgbShaders/imgUnhsharpMaskFragmentShader.glsl");
    }




    /**
     * Binds the references for the data arrays specific to PTMObjectsRGBs to the given OpenGl program.
     *
     * @see RTIWindow#bindSpecificShaderTextures(int)
     *
     * @param programID the OpenGL reference for the program to bind the data textures to
     */
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
     * @see RTIWindow#bindShaderVals()
     */
    protected void bindShaderVals(){
        //set the standard uniforms for rendering
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        //the references for the two sets of 3 coefficients for the red, green and blue pixel data
        glUniform1i(rVals1Ref, 0);
        glUniform1i(rVals2Ref, 1);
        glUniform1i(gVals1Ref, 2);
        glUniform1i(gVals2Ref, 3);
        glUniform1i(bVals1Ref, 4);
        glUniform1i(bVals2Ref, 5);
        glUniform1i(normalsRef, 6);

        //actually set the textures
        setShaderTexture(0, ptmObjectRGB.getRedVals1());
        setShaderTexture(1, ptmObjectRGB.getRedVals2());
        setShaderTexture(2, ptmObjectRGB.getGreenVals1());
        setShaderTexture(3, ptmObjectRGB.getGreenVals2());
        setShaderTexture(4, ptmObjectRGB.getBlueVals1());
        setShaderTexture(5, ptmObjectRGB.getBlueVals2());
        setNormalsTexture(6, rtiObject.getNormals());
    }
}
