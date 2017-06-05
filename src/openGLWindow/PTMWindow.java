package openGLWindow;

import org.lwjgl.BufferUtils;
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
 * Created by Jed on 03-Jun-17.
 */
public class PTMWindow implements Runnable{

    private PTMObject ptmObject;
    private int imageWidth;
    private int imageHeight;
    private IntBuffer rVals1, rVals2, gVals1, gVals2, bVals1, bVals2;
    private long window;

    private int defaultProgram, normalsProgram, diffGainProgram, specEnhanceProgram;

    private int rVals1Ref, rVals2Ref;
    private int gVals1Ref, gVals2Ref;
    private int bVals1Ref, bVals2Ref;
    private int normalsRef, imageScaleRef;

    private int diffGainRef;
    private int specConstRef, diffConstRef, specExConstRef;

    private int shaderWidth, shaderHeight;

    private int shaderLightX, shaderLightY;

    private RTIViewer.ShaderProgram currentProgram = RTIViewer.ShaderProgram.DEFAULT;

    private IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
    private IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
    private int viewportX, viewportY;
    private float imageScale = 1.0f;

    public PTMWindow(PTMObject ptmObject) throws Exception{
        this.ptmObject = ptmObject;

        rVals1 = ptmObject.getRedVals1();
        rVals2 = ptmObject.getRedVals2();
        gVals1 = ptmObject.getGreenVals1();
        gVals2 = ptmObject.getGreenVals2();
        bVals1 = ptmObject.getBlueVals1();
        bVals2 = ptmObject.getBlueVals2();

        imageWidth = ptmObject.getWidth();
        imageHeight = ptmObject.getHeight();

    }

    private void setupGLFW(){
        glfwInit();
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()){
            throw  new IllegalStateException("Unable to initialise GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow((int)(imageWidth * 0.5), (int)(imageHeight * 0.5), ptmObject.getFileName(), NULL, NULL);


        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);
            }
        });


        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                //scroll up positive, scroll down negative
                imageScale += 0.1 * yoffset;
                if(imageScale > 10){imageScale = 10f;}
                else if(imageScale < 1){imageScale = 1f;}
            }
        });


        glfwWindowHint(GLFW_REFRESH_RATE, 60);

        try(MemoryStack stack = stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(window,
                    (vidMode.width() - pWidth.get(0))/2,
                    (vidMode.height() - pHeight.get(0))/2);
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);
    }

    private void createShaders() throws Exception{
        createShader(RTIViewer.ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/defaultFragmentShader.glsl");

        createShader(RTIViewer.ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/normalsFragmentShader.glsl");


        createShader(RTIViewer.ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/diffuseGainFragmentShader.glsl");
        

        createShader(RTIViewer.ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/specEnhanceFragmentShader.glsl");

    }


    private void createShader(RTIViewer.ShaderProgram type, String vertShaderFile, String fragShaderFile) throws Exception{
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
        }

        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        String vertSource = ShaderUtils.readFromFile(vertShaderFile);
        String fragSource = ShaderUtils.readFromFile(fragShaderFile);

        GL20.glShaderSource(vertShader, vertSource);
        GL20.glShaderSource(fragShader, fragSource);
        GL20.glCompileShader(vertShader);
        GL20.glCompileShader(fragShader);

        if(GL20.glGetShaderi(fragShader, GL_COMPILE_STATUS) == GL_FALSE){
            throw new Exception("Couldn't compile frag shader " + GL20.glGetShaderInfoLog(fragShader));
        }
        if(GL20.glGetShaderi(vertShader, GL_COMPILE_STATUS) == GL_FALSE){
            throw new Exception("Couldn't compile vert shader " + GL20.glGetShaderInfoLog(vertShader));
        }

        GL20.glAttachShader(currentProgram, vertShader);
        GL20.glAttachShader(currentProgram, fragShader);
        GL20.glLinkProgram(currentProgram);


        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);

        GL20.glUseProgram(currentProgram);
        bindShaderReferences(currentProgram);
        bindShaderVals();

        GL20.glValidateProgram(currentProgram);

        if(GL20.glGetProgrami(currentProgram, GL_VALIDATE_STATUS) == GL_FALSE){
            System.err.println(type.toString());
            throw new Exception("Couldn't validate shader program: "  + GL20.glGetProgramInfoLog(currentProgram));

        }
        GL20.glUseProgram(0);
    }

    private void bindShaderReferences(int programID){
        shaderWidth = glGetUniformLocation(programID, "imageWidth");
        shaderHeight = glGetUniformLocation(programID, "imageHeight");
        imageScaleRef = glGetUniformLocation(programID, "imageScale");

        shaderLightX = glGetUniformLocation(programID, "lightX");
        shaderLightY = glGetUniformLocation(programID, "lightY");

        rVals1Ref = glGetUniformLocation(programID, "rVals1");
        rVals2Ref = glGetUniformLocation(programID, "rVals2");

        gVals1Ref = glGetUniformLocation(programID, "gVals1");
        gVals2Ref = glGetUniformLocation(programID, "gVals2");

        bVals1Ref = glGetUniformLocation(programID, "bVals1");
        bVals2Ref = glGetUniformLocation(programID, "bVals2");

        normalsRef = glGetUniformLocation(programID, "normals");

        diffGainRef = glGetUniformLocation(programID, "diffGain");
        diffConstRef = glGetUniformLocation(programID, "diffConst");
        specConstRef = glGetUniformLocation(programID, "specConst");
        specExConstRef = glGetUniformLocation(programID, "specExConst");
    }

    private void bindShaderVals(){
        glUniform1i(shaderWidth, imageWidth);
        glUniform1i(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);

        glUniform1i(rVals1Ref, 0);
        glUniform1i(rVals2Ref, 1);
        glUniform1i(gVals1Ref, 2);
        glUniform1i(gVals2Ref, 3);
        glUniform1i(bVals1Ref, 4);
        glUniform1i(bVals2Ref, 5);
        glUniform1i(normalsRef, 6);

        setShaderTexture(0, rVals1);
        setShaderTexture(1, rVals2);
        setShaderTexture(2, gVals1);
        setShaderTexture(3, gVals2);
        setShaderTexture(4, bVals1);
        setShaderTexture(5, bVals2);
        setNormalsTexture(6, ptmObject.getNormals());
    }

    private void setShaderTexture(int textureNum, IntBuffer coeffArray){
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32I, imageWidth, imageHeight,
                0, GL_RGB_INTEGER, GL_INT, coeffArray);
        glBindTexture(GL_TEXTURE_2D, textureRef);

    }

    private void setNormalsTexture(int textureNum, FloatBuffer normals){
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, imageWidth, imageHeight,
                0, GL_RGB, GL_FLOAT, normals);
        glBindTexture(GL_TEXTURE_2D, textureRef);
    }

    @Override
    public void run(){
        setupGLFW();
        try{
            createShaders();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        glfwShowWindow(window);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        while (!glfwWindowShouldClose(window)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            setShaderParams();

            glBegin(GL_QUADS);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();

        }

        cleanUp();
    }

    private void setShaderParams(){
        int program = 0;
        if(currentProgram.equals(RTIViewer.ShaderProgram.DEFAULT)) {
            program = defaultProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            program = normalsProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            program = diffGainProgram;
        }else if(currentProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            program = specEnhanceProgram;
        }
        GL20.glUseProgram(program);
        bindShaderReferences(program);

        glUniform1f(shaderLightX, RTIViewer.globalLightPos.getX());
        glUniform1f(shaderLightY, RTIViewer.globalLightPos.getY());
        glUniform1f(imageScaleRef, imageScale);

        glUniform1f(diffGainRef, (float) (RTIViewer.globalDiffGainVal / 10.0));

        glUniform1f(diffConstRef, (float) (RTIViewer.globalDiffColourVal / 10.0));
        glUniform1f(specConstRef, (float) (RTIViewer.globalSpecularityVal / 10.0));
        glUniform1f(specExConstRef, (float) (RTIViewer.globalHighlightSizeVal / 10.0));
    }


    private void cleanUp(){
        GL20.glDeleteProgram(defaultProgram);
        GL20.glDeleteProgram(normalsProgram);
        GL20.glDeleteProgram(diffGainProgram);
        GL20.glDeleteProgram(specEnhanceProgram);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }


    public void setCurrentProgram(RTIViewer.ShaderProgram currentProgram) {
        this.currentProgram = currentProgram;
    }


}
