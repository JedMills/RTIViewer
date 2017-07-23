package openGLWindow;

import org.lwjgl.BufferUtils;
import ptmCreation.RTIObject;
import ptmCreation.RTIObjectHSH;
import toolWindow.RTIViewer;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * This class represents the {@link RTIWindow} used for displaying {@link RTIObjectHSH}s. It contains the shader
 * program references for the data arrays specific to HSH RTI objects, creates the specific shader programs for
 * HSH objects.
 */
public class RTIWindowHSH extends RTIWindow {

    /** The RTI object this window shows */
    private RTIObjectHSH rtiObjectHSH;

    /** Number fo basis terms for the HSH representation of the RTI object */
    private int basisTerms;

    /** A small (1 x 1) texture made for HSH rendering used to pass the basis terms to OpenGL */
    private IntBuffer dataTexture;

    /** OpenGL reference for the dataTexture */
    private int dataTextureRef;

    /** Reference for texture containing the terms 0 - 2 HSH terms for the red component of each pixel */
    private int redCoeffs1Ref;

    /** Reference for texture containing the terms 3 - 5 HSH terms for the red component of each pixel */
    private int redCoeffs2Ref;

    /** Reference for texture containing the terms 6 - 8 HSH terms for the red component of each pixel */
    private int redCoeffs3Ref;

    /** Reference for texture containing the first 3 HSH terms for the green component of each pixel */
    private int greenCoeffs1Ref;

    /** Reference for texture containing the terms 3 - 5 HSH terms for the green component of each pixel */
    private int greenCoeffs2Ref;

    /** Reference for texture containing the terms 6 - 8 HSH terms for the green component of each pixel */
    private int greenCoeffs3Ref;

    /** Reference for texture containing the first 3 HSH terms for the blue component of each pixel */
    private int blueCoeffs1Ref;

    /** Reference for texture containing the terms 3 - 5 HSH terms for the blue component of each pixel */
    private int blueCoeffs2Ref;

    /** Reference for texture containing the terms 6 - 8 HSH terms for the blue component of each pixel */
    private int blueCoeffs3Ref;


    /**
     * Creates a new RTIWindowHSH to display the passed RTIObjectHSH. Creates the data texture and stores the
     * RTIObjectHSH's basis terms in it so OpenGL can be fed this value.
     *
     * @see RTIWindow#RTIWindow(RTIObject)
     *
     * @param ptmObject     the RTIObjectHSH to show in this window
     */
    public RTIWindowHSH(RTIObjectHSH ptmObject) {
        super(ptmObject);
        rtiObjectHSH = ptmObject;
        basisTerms = ptmObject.getBasisTerms();
        dataTexture = BufferUtils.createIntBuffer(3);
        dataTexture.put(0, basisTerms);
    }


    /**
     * Creates the shaders that this window will use to render the {@link RTIWindowHSH#rtiObjectHSH}.
     *
     * @see shaders.hshShaders
     * @see RTIWindow#createShaders()
     *
     * @throws Exception    if there is an error reading the glsl files or an OpenGL error making the shaders programs
     */
    @Override
    protected void createShaders() throws Exception {
        createShader(RTIViewer.ShaderProgram.DEFAULT, "/shaders/defaultVertexShader.glsl",
                "/shaders/hshShaders/defaultFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "/shaders/defaultVertexShader.glsl",
                "/shaders/hshShaders/normalsFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "/shaders/defaultVertexShader.glsl",
                "/shaders/hshShaders/specEnhanceFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK, "/shaders/defaultVertexShader.glsl",
                "/shaders/hshShaders/imgUnsharpMaskFragmentShaderHSH.glsl");

        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "/shaders/defaultVertexShader.glsl",
                "/shaders/hshShaders/normalsEnhanceFragmentShaderHSH.glsl");


    }



    /**
     * Binds the references for the data arrays specific to RTIObjectsHSHs to the given OpenGl program.
     *
     * @see RTIWindow#bindSpecificShaderTextures(int)
     *
     * @param programID the OpenGL reference for the program to bind the data textures to
     */
    @Override
    protected void bindSpecificShaderTextures(int programID) {
        //1 x 1 texture containing the basis terms for this RTIObject
        dataTextureRef = glGetUniformLocation(programID, "dataTexture");
        normalsRef = glGetUniformLocation(programID, "normals");

        //see the attribute descriptions for what these coeff arrays are
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




    /**
     * This method binds the specific values used for HSH in Java to the OpenGl values so these values can be fed
     * into into OpenGl so OpenGL can render with updated parameters.
     *
     * @see RTIWindow#bindShaderVals()
     * @see RTIObjectHSH
     */
    @Override
    protected void bindShaderVals() {
        //set the standard uniforms for rendering
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1i(dataTextureRef, 0);
        setShaderTexture(0, dataTexture, 1, 1);

        glUniform1i(normalsRef, 1);
        setNormalsTexture(1, rtiObjectHSH.getNormals());

        glUniform1i(redCoeffs1Ref, 2);
        glUniform1i(greenCoeffs1Ref, 3);
        glUniform1i(blueCoeffs1Ref, 4);

        //we're using setNormalsTexture here as it's a quick way to set a texture of floats, which is
        //needed for HSH, as opposed to PTM, which can use ints
        setNormalsTexture(2, rtiObjectHSH.getRedVals1());
        setNormalsTexture(3, rtiObjectHSH.getGreenVals1());
        setNormalsTexture(4, rtiObjectHSH.getBlueVals1());

        glUniform1i(redCoeffs2Ref, 5);
        glUniform1i(greenCoeffs2Ref, 6);
        glUniform1i(blueCoeffs2Ref, 7);

        //these textures only need to be set if the RTIObjectHSH has enough basis terms
        if(basisTerms > 3){
            setNormalsTexture(5, rtiObjectHSH.getRedVals2());
            setNormalsTexture(6, rtiObjectHSH.getGreenVals2());
            setNormalsTexture(7, rtiObjectHSH.getBlueVals2());
        }

        glUniform1i(redCoeffs3Ref, 8);
        glUniform1i(greenCoeffs3Ref, 9);
        glUniform1i(blueCoeffs3Ref, 10);

        if(basisTerms > 6){
            setNormalsTexture(8, rtiObjectHSH.getRedVals3());
            setNormalsTexture(9, rtiObjectHSH.getGreenVals3());
            setNormalsTexture(10, rtiObjectHSH.getBlueVals3());
        }
    }
}
