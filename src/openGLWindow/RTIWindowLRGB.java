package openGLWindow;

import ptmCreation.PTMObjectLRGB;
import ptmCreation.RTIObject;
import toolWindow.RTIViewer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * This class represents the {@link RTIWindow} used for displaying {@link PTMObjectLRGB}s. It contains the shader
 * program references for the data arrays specific to LRGB PTM objects, creates the specific shader programs for
 * LRGB PTM objects.
 */
public class RTIWindowLRGB extends RTIWindow {

    /** ThePTM object that this window will display */
    private PTMObjectLRGB ptmObject;

    /** The reference for the texture containing the first 3 luminance LRGB coeffs for this PTM object */
    private int lumCoeffs1Ref;

    /** The reference for the texture containing the last 3 luminance LRGB coeffs for this PTM object */
    private int lumCoeffs2Ref;

    /** The reference for the texture containing the 3 rgb values for this PTM object */
    private int rgbCoeffsRef;


    /**
     * Creates a new RTIWindowLRGB and sets the {@link RTIWindowLRGB#ptmObject} to the given object;
     *
     * @see {@link RTIWindow#RTIWindow(RTIObject)}
     *
     * @param ptmObject     the PTMObjectLRGB to show in this window
     */
    public RTIWindowLRGB(PTMObjectLRGB ptmObject) {
        super(ptmObject);

        this.ptmObject = ptmObject;
    }




    /**
     * Binds the references for the data arrays specific to PTMObjectsLRGBs to the given OpenGl program.
     *
     * @see RTIWindow#bindSpecificShaderTextures(int)
     *
     * @param programID the OpenGL reference for the program to bind the data textures to
     */
    @Override
    protected void bindSpecificShaderTextures(int programID) {
        //ptm objects have 6 luminance coefficients, and 3 rgb coefficient, and of course there's the normals too
        lumCoeffs1Ref = glGetUniformLocation(programID, "lumCoeffs1");
        lumCoeffs2Ref = glGetUniformLocation(programID, "lumCoeffs2");
        rgbCoeffsRef = glGetUniformLocation(programID, "rgbCoeffs");
        normalsRef = glGetUniformLocation(programID, "normals");
    }




    /**
     * This method binds the specific values used for PTM LRGB in Java to the OpenGl values so these values can be fed
     * into into OpenGl so OpenGL can render with updated parameters.
     *
     * @see RTIWindow#bindShaderVals()
     * @see PTMObjectLRGB
     */
    @Override
    protected void bindShaderVals() {
        //set the standard uniforms for rendering
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        //the references for the luminance, rgb and normals textures
        glUniform1i(lumCoeffs1Ref, 0);
        glUniform1i(lumCoeffs2Ref, 1);
        glUniform1i(rgbCoeffsRef, 2);
        glUniform1i(normalsRef, 3);

        //actually set the textures
        setShaderTexture(0, ptmObject.getLumCoeffs1());
        setShaderTexture(1, ptmObject.getLumCoeffs2());
        setShaderTexture(2, ptmObject.getRgbCoeffs());
        setNormalsTexture(3, ptmObject.getNormals());
    }




    /**
     * Creates the shaders that this window will use to render the {@link RTIWindowLRGB#ptmObject}.
     *
     * @see shaders.lrgbShaders
     * @see RTIWindow#createShaders()
     *
     * @throws Exception    if there is an error reading the glsl files or an OpenGL error making the shaders programs
     */
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
