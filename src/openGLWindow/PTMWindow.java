package openGLWindow;

import javafx.scene.image.Image;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import ptmCreation.PTMObject;
import toolWindow.RTIViewer;
import utils.ShaderUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL30.GL_RGB32F;
import static org.lwjgl.opengl.GL30.GL_RGB32I;
import static org.lwjgl.opengl.GL30.GL_RGB_INTEGER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * <p>
 * This class represents a window containing the RTI image that has been loaded by the user. The image in the
 * window is generated using OpenGL via the lightweight java game library (LWJGL). The RTI image can be
 * filtered and enhanced in several ways by changing the fragment shader that the textured quad on the window uses.
 * </p>
 *
 * <p>
 * Each instance holds its own shader references, and is responsible for updating them as per the values in the
 * PTMViewer window. Each instance also cleans up its own OpenGL shader orograms when it the run method finishes.
 * </p>
 *
 * Created by Jed on 03-Jun-17.
 */
public abstract class PTMWindow implements Runnable{

    /**The ptm image that this window will display*/
    public PTMObject ptmObject;

    /**Width of the ptmObject attribute that this window displays*/
    protected float imageWidth;

    /**Height of the ptmObject attribute that this window displays*/
    protected float imageHeight;

    /**OpenGL reference for the window created*/
    protected long window;

    /**OpenGL reference for the default fragment shader */
    protected int defaultProgram;

    /**OpenGL reference for the normals visualisation fragment shader */
    protected int normalsProgram;

    /**OpenGL reference for the diffuse gain fragment shader*/
    protected int diffGainProgram;

    /**OpenGL reference for the specular enhancement fragment shader*/
    protected int specEnhanceProgram;

    /**OpenGL reference for the normals unsharp masking fragment shader*/
    protected int normUnsharpMaskProgram;

    /**OpenGL reference for the image unsharp masking fragment shader*/
    protected int imgUnsharpMaskProgram;

    /**OpenGL reference for the coefficient unsharp masking fragment shader*/
    protected int coeffUnsharpMaskProgram;

    /**OpenGL reference for the GLSL uniform "viewportX" found in the fragment shader */
    protected float viewportX = 0.0f;

    /**OpenGL reference for the GLSL uniform "viewportY" found in the fragment shader */
    protected float viewportY = 0.0f;

    /**OpenGL reference for the GLSL sampler2D texture "normals", used for passing normals attr to shaders*/
    protected int normalsRef;

    /**OpenGL reference for the GLSL uniform "imageScale", used for passing zoom level to shaders */
    protected int imageScaleRef;

    /**OpenGL reference for the GLSL uniform "diffGain" in diffuseGainFragmentShader.glsl*/
    protected int diffGainRef;

    /**OpenGL reference for the GLSL uniform "specConst" in specEnhanceFragmentShader.glsl*/
    protected int specConstRef;

    /**OpenGL reference for the GLSL uniform "diffConst" in specEnhanceFragmentShader.glsl*/
    protected int diffConstRef;

    /**OpenGL reference for the GLSL uniform "specExConst" in specEnhanceFragmentShader.glsl*/
    protected int specExConstRef;

    /**OpenGL reference for the GLSL uniform "normUnMaskGain" in normUnsharpMaskFragmentShader.glsl*/
    protected int normUnMaskGainRef;

    /**OpenGL reference for the GLSL uniform "normUnMaskEnv" in normUnsharpMaskFragmentShader.glsl*/
    protected int normUnMaskEnvRef;

    /**OpenGL reference for the GLSL uniform "imgUnMaskGain" in the image unsharp mask fragment shaders*/
    protected int imgUnMaskGainRef;

    /**OpenGL reference for the GLSL uniform "coeffUnMaskGain" in the image unsharp mask fragment shaders*/
    protected int coeffUnMaskGainRef;

    /**OpenGL reference for the GLSL uniform "imageWidth" found in shaders*/
    protected int shaderWidth;

    /**OpenGL reference for the GLSL uniform "imageHeight" found in shaders*/
    protected int shaderHeight;

    /**OpenGL reference for the GLSL uniform "viewportX" found in the vertex shader*/
    protected int shaderViewportX;

    /**OpenGL reference for the GLSL uniform "viewportY" found in the vertex shader*/
    protected int shaderViewportY;

    /**OpenGL reference for the GLSL uniform "lightX" found in the fragment shaders */
    protected int shaderLightX;

    /**OpenGL reference for the GLSL uniform "lightY" found in the fragment shaders */
    protected int shaderLightY;

    /**Current shader program this window is set to, is set by the RTIViewer program */
    protected RTIViewer.ShaderProgram currentProgram = RTIViewer.ShaderProgram.DEFAULT;

    /**The current zoom level of the viewed image, 1.0 = no zoom */
    protected float imageScale = 1.0f;

    /**Holds the width of the viewing window, which gets updated each frame by glfw*/
    protected int[] windowWidth = new int[1];

    /**Holds the height of the viewing window, which gets updated each frame by glfw*/
    protected int[] windowHeight = new int[1];

    /**Current non-normalised x position in the viewing window of the cursor, updated each frame by glfw*/
    protected double[] mouseXPos = new double[1];

    /**Current non-normalised y position in the viewing window of the cursor, updated each frame by glfw*/
    protected double[] mouseYPos = new double[1];

    /**Non-normalised x position of the cursor last frame, updated each frame by glfw*/
    protected double[] lastXPos = new double[1];

    /**Non-normalised y position of the cursor last frame, updated each frame by glfw*/
    protected double[] lastYPos = new double[1];

    /**X offset for the glViewport(...) each frame, used to center image in a non-ratio window size*/
    protected int xOffset = 0;

    /**Y offset for the glViewport(...) each frame, used to center image in a non-ratio window size*/
    protected int yOffset = 0;

    /**Width of the image as displayed on the window, updated each frame*/
    protected int reducedWidth;

    /**Height of the image as displayed on the window, updated each frame*/
    protected int reducedHeight;


    /**
     * Creates a new PTMWindow, setting the passed PTMObject as this window's PTMObject, which it will
     * display using the parameters in the RTIViewer window.
     *
     * @param ptmObject
     */
    public PTMWindow(PTMObject ptmObject){
        this.ptmObject = ptmObject;

        imageWidth = ptmObject.getWidth();
        imageHeight = ptmObject.getHeight();
    }



    /**
     * Initialises GLFW so OpenGL can be used to display the image in this window. Creates a new window, which
     * will be the UI for this PTMWindow, sets callbacks to deal with zooming using the scroll wheel and panning
     * with the mouse, and places the window to be in the middle of the screen. Does not actually call the window
     * to be displayed.
     */
    private void setupGLFW(){
        //initialise glfw for the calls, and throw a new exception to exit if unsuccessful
        glfwInit();
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()){throw  new IllegalStateException("Unable to initialise GLFW");}

        //don't make the window visible yet, make it resizable and display at 60Hz
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_REFRESH_RATE, 60);

        //create a new window, of size half image width by half image height, with file location as the title
        window = glfwCreateWindow((int)(imageWidth * 0.5),
                                  (int)(imageHeight * 0.5),
                                   ptmObject.getFileName(), NULL, NULL);

        //allows the user to zoom in and out with the scroll wheel
        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                updateImageScale(yoffset);
            }
        });

        //remove itself from the RTIViewer's list of windows upon close
        glfwSetWindowCloseCallback(window, new GLFWWindowCloseCallbackI() {
            @Override
            public void invoke(long window) {
                RTIViewer.removeWindow(PTMWindow.this);
                glfwSetWindowShouldClose(window, true);
            }
        });


        glfwSetWindowFocusCallback(window, new GLFWWindowFocusCallbackI() {
            @Override
            public void invoke(long window, boolean focused) {
                if(focused){
                    RTIViewer.setFocusedWindow(PTMWindow.this);
                }
            }
        });

        //translates the window so it appears in the center of the screen
        try(MemoryStack stack = stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(window,
                    (vidMode.width() - pWidth.get(0))/2,
                    (vidMode.height() - pHeight.get(0))/2);
        }

        //make the new window the selected one when it appears
        glfwMakeContextCurrent(window);

        //create OpenGL capabilities and try to sync with monitor
        GL.createCapabilities();
        glfwSwapInterval(1);
    }

    private void updateImageScale(double yoffset){
        float oldScale = imageScale;

        //scroll up positive, scroll down negative
        imageScale += 0.1 * yoffset;
        if(imageScale > 10){imageScale = 10f;}
        else if(imageScale < 1){imageScale = 1;}

        //translate the image a little bit to keep center of screen
        viewportX *= imageScale / oldScale;
        viewportY *= imageScale / oldScale;

        //check the viewport to make sure image is still completely in viewport
        checkViewport();
    }

    protected abstract void createShaders() throws Exception;



    /**
     * Creates a new OpenGL shader program from the vertex and fragment shader file locations passed. Checks the
     * validity of both shaders and the validity of the compiled program, and throws an Exception if they are
     * invalid.
     *
     * @param type              which viewing filter the program should be represented by
     * @param vertShaderFile    location of the vertex GLSL shader for this program
     * @param fragShaderFile    location of the fragment GLSL shader for this program
     * @throws Exception        if there is an error parsing the shaders or compiling the shader program
     */
    protected void createShader(RTIViewer.ShaderProgram type, String vertShaderFile,
                                                                    String fragShaderFile) throws Exception{
        /* Each filter type in the RTIViewer has its own shader program so we need to create
           this program and set the current program to it so we can assign it vertex and
           fragment shaders */
        int currentProgram = 0;
        if(type.equals(RTIViewer.ShaderProgram.DEFAULT)){
            defaultProgram = GL20.glCreateProgram();
            currentProgram = defaultProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.NORMALS)){
            normalsProgram = GL20.glCreateProgram();
            currentProgram = normalsProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            diffGainProgram = GL20.glCreateProgram();
            currentProgram = diffGainProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            specEnhanceProgram = GL20.glCreateProgram();
            currentProgram = specEnhanceProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.NORM_UNSHARP_MASK)){
            normUnsharpMaskProgram = GL20.glCreateProgram();
            currentProgram = normUnsharpMaskProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            imgUnsharpMaskProgram = GL20.glCreateProgram();
            currentProgram = imgUnsharpMaskProgram;
        }else if(type.equals(RTIViewer.ShaderProgram.COEFF_UN_MASK)){
            coeffUnsharpMaskProgram = GL20.glCreateProgram();
            currentProgram = coeffUnsharpMaskProgram;
        }

        //create references for the vertex and fragment shaders, which are compiled in a bit
        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        //parse the source files
        String vertSource = ShaderUtils.readFromFile(vertShaderFile);
        String fragSource = ShaderUtils.readFromFile(fragShaderFile);

        //assign and compile the shaders for this program
        GL20.glShaderSource(vertShader, vertSource);
        GL20.glShaderSource(fragShader, fragSource);
        GL20.glCompileShader(vertShader);
        GL20.glCompileShader(fragShader);

        //check they were compiled and assigned successfully
        if(GL20.glGetShaderi(fragShader, GL_COMPILE_STATUS) == GL_FALSE){
            throw new Exception("Couldn't compile frag shader " + GL20.glGetShaderInfoLog(fragShader));
        }
        if(GL20.glGetShaderi(vertShader, GL_COMPILE_STATUS) == GL_FALSE){
            throw new Exception("Couldn't compile vert shader " + GL20.glGetShaderInfoLog(vertShader));
        }

        //attach the shaders to the current program, link them and set the current program as this newly
        //created OpenGL program
        GL20.glAttachShader(currentProgram, vertShader);
        GL20.glAttachShader(currentProgram, fragShader);
        GL20.glLinkProgram(currentProgram);
        GL20.glUseProgram(currentProgram);

        //bind the references in this class to the uniforms/textures in the shaders, and actually set the values
        bindShaderReferences(currentProgram, true);
        bindShaderVals();

        //validate the program and throw and error if it wasn't made successfully
        GL20.glValidateProgram(currentProgram);
        if(GL20.glGetProgrami(currentProgram, GL_VALIDATE_STATUS) == GL_FALSE){
            System.err.println(type.toString());
            throw new Exception("Couldn't validate shader program: "  + GL20.glGetProgramInfoLog(currentProgram));

        }
    }


    /**
     * Gets the integer OpenGL references from the shader program specified by the shaderID and sets them to the
     * relevant attributes in this class. The textures (rVals1, rVals2 ... etc.) only need to be set first time
     * the program is compiled as they do not changed, so there is an option to set them or not.
     *
     * @param programID         the program for which we want to set the references for
     * @param setTextures       whether we want to set references for textures or not
     */
    private void bindShaderReferences(int programID, boolean setTextures){
        //get the integer OpenGL reference  from the shader program using its string value
        shaderWidth = glGetUniformLocation(programID, "imageWidth");
        shaderHeight = glGetUniformLocation(programID, "imageHeight");
        imageScaleRef = glGetUniformLocation(programID, "imageScale");
        shaderViewportX = glGetUniformLocation(programID, "viewportX");
        shaderViewportY = glGetUniformLocation(programID, "viewportY");

        shaderLightX = glGetUniformLocation(programID, "lightX");
        shaderLightY = glGetUniformLocation(programID, "lightY");

        diffGainRef = glGetUniformLocation(programID, "diffGain");
        diffConstRef = glGetUniformLocation(programID, "diffConst");
        specConstRef = glGetUniformLocation(programID, "specConst");
        specExConstRef = glGetUniformLocation(programID, "specExConst");

        normUnMaskGainRef = glGetUniformLocation(programID, "normUnMaskGain");
        normUnMaskEnvRef = glGetUniformLocation(programID, "normUnMaskEnv");

        imgUnMaskGainRef = glGetUniformLocation(programID, "imgUnMaskGain");

        coeffUnMaskGainRef = glGetUniformLocation(programID, "coeffUnMaskGain");

        if(setTextures){bindSpecificShaderTextures(programID);}
    }



    protected abstract void bindSpecificShaderTextures(int programID);

    protected abstract void bindShaderVals();



    /**
     * Creates a new OpenGL texture for the shader programs to use with the id as the number passed, using
     * the flattened set of three ptm coefficients (a0-a2 or a3-a5).
     *
     * @param textureNum        number of the texture to assign
     * @param coeffArray        flattened set of 3 ptm coeffs (a0-a2 or a3-a5) toset the texture as
     */
    protected void setShaderTexture(int textureNum, IntBuffer coeffArray){
        //make the active texture the one passed, create this texture and bind it
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);

        //GL_NEAREST gives best interpolated image quality
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //actually create and bind the texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32I, (int)imageWidth, (int)imageHeight,
                0, GL_RGB_INTEGER, GL_INT, coeffArray);
        glBindTexture(GL_TEXTURE_2D, textureRef);

    }



    /**
     * Creates a new OpenGL texture for the shader programs to use that contains the values for the normal
     * vector of each texel.
     *
     * @param textureNum        number of the texture to set as the normals texture
     * @param normals           flattened array of xyz vectors to set as this texture
     */
    protected void setNormalsTexture(int textureNum, FloatBuffer normals){
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, (int)imageWidth, (int)imageHeight,
                0, GL_RGB, GL_FLOAT, normals);
        glBindTexture(GL_TEXTURE_2D, textureRef);
    }



    /**
     * Sets up GLFW, creates all the shaders and displays the gl window. While the windowShouldClose glfw attribute
     * is false for this window, the window will set the parameters for the shaders based on the values on the
     * components of the RTIViewer, draw a quad, and use the appropriate shader to draw the image for the ptm image
     * using the relevant filter. The window also accounts for resizing and zooming.
     */
    @Override
    public void run(){
        //set everything up ready to show the RTI file in the window
        setupGLFW();
        try{
            createShaders();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        currentProgram = RTIViewer.currentProgram;
        RTIViewer.updateViewportPos(this, viewportX, viewportY, imageScale);

        //display the window, and set OpenGL to the default viewing mode
        glfwShowWindow(window);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        //update the image on the window while the window is still be open
        while (!glfwWindowShouldClose(window)){
            //resets OpenGl colour buffers so they don't all just immediately overflow and everything crashes
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //set the values in the shaders used to draw image as the values from the RTIViewer window
            setShaderParams();

            //draw the quad that will be textured as the RTI image
            glBegin(GL_QUADS);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            //back to the default program
            GL20.glUseProgram(0);

            //update the window size buffers, set the viewport depending on the window size
            glfwGetWindowSize(window, windowWidth, windowHeight);
            setViewport();

            //check for panning with the mouse
            grabMouseState();

            //swap the double-buffer for the window
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        //the window needs to close, so clean up shaders etc.
        cleanUp();
    }



    /**
     * Sets the uniform values of the current program to those in the RTIViewer window, and those defined in this
     * window such as the viewportX and viewportY.
     */
    private void setShaderParams(){
        //set the current program to the one chosen in the RTIViewer tool window
        int program = 0;
        if(currentProgram.equals(RTIViewer.ShaderProgram.DEFAULT)) {
            program = defaultProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            program = normalsProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            program = diffGainProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            program = specEnhanceProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.NORM_UNSHARP_MASK)){
            program = normUnsharpMaskProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            program = imgUnsharpMaskProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.COEFF_UN_MASK)){
            program = coeffUnsharpMaskProgram;
        }


        //use this program and rebind all the references otherwise OpenGL seems to forget about them
        GL20.glUseProgram(program);
        bindShaderReferences(program, false);

        //set all the things that change
        glUniform1f(shaderLightX, RTIViewer.globalLightPos.getX());
        glUniform1f(shaderLightY, RTIViewer.globalLightPos.getY());
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1f(diffGainRef, normaliseShaderParam(RTIViewer.globalDiffGainVal, 1.0f, 10.0f));

        glUniform1f(diffConstRef, normaliseShaderParam(RTIViewer.globalDiffColourVal, 0.0f, 1.0f));
        glUniform1f(specConstRef, normaliseShaderParam(RTIViewer.globalSpecularityVal, 0.0f, 1.0f));
        glUniform1f(specExConstRef, normaliseShaderParam(RTIViewer.globalHighlightSizeVal, 1.0f, 150.0f));

        glUniform1f(normUnMaskGainRef, normaliseShaderParam(RTIViewer.globalNormUnMaskGain, 0.0f, 40.0f));
        glUniform1f(normUnMaskEnvRef, normaliseShaderParam(RTIViewer.globalNormUnMaskEnv, 0.0f, 0.4f));

        glUniform1f(imgUnMaskGainRef, normaliseShaderParam(RTIViewer.globalImgUnMaskGain, 0.01f, 4.0f));

        glUniform1f(coeffUnMaskGainRef, normaliseShaderParam(RTIViewer.globalCoeffUnMaskGain, 0.01f, 6.0f));
    }


    private float normaliseShaderParam(double value, float min, float max){
        return (float) (min + value * (max - min) / 100.0);
    }


    /**
     * Delete all the shader programs and destroy the window.
     */
    private void cleanUp(){
        GL20.glDeleteProgram(defaultProgram);
        GL20.glDeleteProgram(normalsProgram);
        GL20.glDeleteProgram(diffGainProgram);
        GL20.glDeleteProgram(specEnhanceProgram);
        GL20.glDeleteProgram(normUnsharpMaskProgram);
        GL20.glDeleteProgram(imgUnsharpMaskProgram);
        GL20.glDeleteProgram(coeffUnsharpMaskProgram);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }



    /**
     * Limits the viewport to the bounds of the image at the current zoom level so the use can't pan outside the
     * image.
     */
    private void checkViewport(){
        //the maximum and minimum x's and y's are simply the zoom - 1 as the GL space for the viewport is normalised
        //to 0 - 1
        float minX = -(imageScale - 1.0f);
        float maxX = (imageScale - 1.0f);
        float minY = -(imageScale - 1.0f);
        float maxY = (imageScale - 1.0f);

        //limit the viewport to the edges of the zoomed-in image
        if(viewportX < minX){viewportX = minX;}
        if(viewportX > maxX){viewportX = maxX;}
        if(viewportY < minY){viewportY = minY;}
        if(viewportY > maxY){viewportY = maxY;}

        RTIViewer.updateViewportPos(this, viewportX, viewportY, imageScale);
    }



    /**
     * Sets the location of the RTI image in the window. Centers the image in the frame, with empty space on either
     * side fo the image if the window is not of the same aspect ratio as the RTI image.
     */
    private void setViewport(){
        if(windowWidth[0] > windowHeight[0]){
            //there will be space on either side of the image, so translate in the x and limit the width
            //of the image to keep the image's aspect ratio
            reducedWidth = (int)((imageWidth / imageHeight) * windowHeight[0]);
            xOffset = (windowWidth[0] - reducedWidth) / 2;
            yOffset = 0;
            reducedHeight = windowHeight[0];
        }else{
            //there will be space on the top and bottom of the image, so translate in the y and limit the height
            //of the image to keep the image's aspect ratio
            reducedHeight = (int)((imageHeight / imageWidth) * windowWidth[0]);
            yOffset = (windowHeight[0] - reducedHeight) / 2;
            xOffset = 0;
            reducedWidth = windowWidth[0];
        }
        glViewport(xOffset, yOffset, reducedWidth, reducedHeight);
    }



    /**
     * Gets the x,y location of the mouse on the window. Checks if the left mouse button is held down. if it is,
     * will find the difference between its current position and last position using lastXPos and lastYPos, and will
     * pan the RTI image depending on the change in the mouse position (the mouse has been dragged).
     */
    private void grabMouseState(){
        //get whether the left mouse button is held down, and the cursor position
        int i = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1);
        glfwGetCursorPos(window, mouseXPos, mouseYPos);

        //if the left mouse button is held down, the image is being dragged so pan it
        if(i == GLFW_TRUE){
            //distance mouse has been dragged
            double deltaX = lastXPos[0] - mouseXPos[0];
            double deltaY = lastYPos[0] - mouseYPos[0];

            //translate the viewport by this distance
            if(Math.abs(deltaX) > 0.01 && Math.abs(deltaY) > 0.01) {
                viewportX += 2 * deltaX / ((Math.pow(imageScale, 0.2)) * windowWidth[0]);
                viewportY -= 2 * deltaY / ((Math.pow(imageScale, 0.2)) * windowHeight[0]);
                /*
                double r = Math.pow((deltaX * deltaX) + (deltaY * deltaY), 0.5) / (imageScale * windowHeight[0]);
                double theta = Math.atan2(deltaY, deltaX);
                viewportX += r * Math.cos(theta);
                viewportY += r * Math.sin(theta);
                */
            }
            //make sure the user does't pan outside the image
            checkViewport();
        }
        //update the last position of the cursor
        lastXPos[0] = mouseXPos[0];
        lastYPos[0] = mouseYPos[0];
    }



    /**
     * Sets the currentProgram attribute
     *
     * @param currentProgram        the filter program for this window's RTI image
     */
    public void setCurrentProgram(RTIViewer.ShaderProgram currentProgram) {
        this.currentProgram = currentProgram;
    }



    /**
     * Sets the glfw attribute windowShouldClose
     *
     * @param shouldClose       whether this window should close
     */
    public void setShouldClose(boolean shouldClose){
        glfwSetWindowShouldClose(window, shouldClose);
    }


    public Float getViewportX() {
        return viewportX;
    }


    public Float getViewportY() {
        return viewportY;
    }

    public float getImageScale() {
        return imageScale;
    }

    public void updateViewportFromPreview(float x, float y, float previewScale){
        viewportX = x * previewScale * 2;
        viewportY = y * previewScale * 2;
    }

    public void updateViewportFromPreview(float previewScale){
        float oldScale = imageScale;

        imageScale = previewScale;

        //translate the image a little bit to keep center of screen
        viewportX *= imageScale / oldScale;
        viewportY *= imageScale / oldScale;

        //check the viewport to make sure image is still completely in viewport
        checkViewport();
    }
}
