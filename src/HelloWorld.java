import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import ptmCreation.PTMObject;
import ptmCreation.PTMParser;
import utils.ShaderUtils;

import java.nio.*;


import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTTextureInteger.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


/**
 * Created by jed on 23/05/17.
 */
public class HelloWorld {

    private long window;

    private int defaultProgram, normalsProgram, diffGainProgram, specEnhanceProgram;
    private int shaderWidth, shaderHeight;

    private int shaderLightX, shaderLightY;

    private int imageHeight, imageWidth;

    private int rVals1Ref, rVals2Ref;
    private int gVals1Ref, gVals2Ref;
    private int bVals1Ref, bVals2Ref;
    private int normalsRef;

    private int gainRef, minGainRef, maxGainRef;
    private int specConstRef, diffConstRef, specExConstRef;

    private IntBuffer rVals1, rVals2, gVals1, gVals2, bVals1, bVals2;
    private FloatBuffer normals;

    ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    public void run() throws Exception{
        setupPTM();
        init();
        createShaders();
        glfwShowWindow(window);
        loop();
        cleanUp();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    public void init(){
        glfwInit();
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()){
            throw  new IllegalStateException("Unable to initialise GLFW");
        }


        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow((int)(imageWidth * 0.5), (int)(imageHeight * 0.5), "Hello LWJGL!", NULL, NULL);

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true);
            } else if(key == GLFW_KEY_1 && action == GLFW_RELEASE){
                currentProgram = ShaderProgram.DEFAULT;
            } else if(key == GLFW_KEY_2 && action == GLFW_RELEASE){
                currentProgram = ShaderProgram.NORMALS;
            } else if(key == GLFW_KEY_3 && action == GLFW_RELEASE){
                currentProgram = ShaderProgram.DIFF_GAIN;
            } else if(key == GLFW_KEY_4 && action == GLFW_RELEASE){
                currentProgram = ShaderProgram.SPEC_ENHANCE;
            }
        });

        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);
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


    private void setupPTM() throws Exception{
        PTMObject ptmObject = PTMParser.createPtmFromFile("leafTest.ptm");

        imageHeight = ptmObject.getHeight();
        imageWidth = ptmObject.getWidth();

        rVals1 = ptmObject.getRedVals1();
        rVals2 = ptmObject.getRedVals2();
        gVals1 = ptmObject.getGreenVals1();
        gVals2 = ptmObject.getGreenVals2();
        bVals1 = ptmObject.getBlueVals1();
        bVals2 = ptmObject.getBlueVals2();
        normals = ptmObject.getNormals();
    }



    private void createShaders() throws Exception{
        createShader(ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl", "src/shaders/defaultFragmentShader.glsl");
        createShader(ShaderProgram.NORMALS, "src/shaders/defaultVertexShader.glsl", "src/shaders/normalsFragmentShader.glsl");
        createShader(ShaderProgram.DIFF_GAIN, "src/shaders/defaultVertexShader.glsl", "src/shaders/diffuseGainFragmentShader.glsl");
        createShader(ShaderProgram.SPEC_ENHANCE, "src/shaders/defaultVertexShader.glsl", "src/shaders/specEnhanceFragmentShader.glsl");
    }


    private void createShader(ShaderProgram type, String vertShaderFile, String fragShaderFile) throws Exception{
        int currentProgram = 0;
        if(type.equals(ShaderProgram.DEFAULT)){
            defaultProgram = GL20.glCreateProgram();
            currentProgram = defaultProgram;
        }else if(type.equals(ShaderProgram.NORMALS)){
            normalsProgram = GL20.glCreateProgram();
            currentProgram = normalsProgram;
        }else if(type.equals(ShaderProgram.DIFF_GAIN)){
            diffGainProgram = GL20.glCreateProgram();
            currentProgram = diffGainProgram;
        }else if(type.equals(ShaderProgram.SPEC_ENHANCE)){
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
        GL20.glValidateProgram(currentProgram);
        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);

        bindShaderReferences(currentProgram);
        GL20.glUseProgram(currentProgram);
        bindShaderVals();
    }


    private void bindShaderReferences(int programID){
        shaderWidth = glGetUniformLocation(programID, "imageWidth");
        shaderHeight = glGetUniformLocation(programID, "imageHeight");
        shaderLightX = glGetUniformLocation(programID, "lightX");
        shaderLightY = glGetUniformLocation(programID, "lightY");

        rVals1Ref = glGetUniformLocation(programID, "rVals1");
        rVals2Ref = glGetUniformLocation(programID, "rVals2");

        gVals1Ref = glGetUniformLocation(programID, "gVals1");
        gVals2Ref = glGetUniformLocation(programID, "gVals2");

        bVals1Ref = glGetUniformLocation(programID, "bVals1");
        bVals2Ref = glGetUniformLocation(programID, "bVals2");

        normalsRef = glGetUniformLocation(programID, "normals");

        gainRef = glGetUniformLocation(programID, "gain");
        minGainRef = glGetUniformLocation(programID, "minGain");
        maxGainRef = glGetUniformLocation(programID, "maxGain");

        diffConstRef = glGetUniformLocation(programID, "diffConst");
        specConstRef = glGetUniformLocation(programID, "specConst");
        specExConstRef = glGetUniformLocation(programID, "specExConst");
    }



    private void setShaderTexture(int textureNum, IntBuffer coeffArray){
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32I, imageWidth, imageHeight, 0, GL_RGB_INTEGER, GL_INT, coeffArray);
        glBindTexture(GL_TEXTURE_2D, textureRef);

    }

    private void setNormalsTexture(int textureNum, FloatBuffer normals){
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureNum);
        int textureRef = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureRef);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, imageWidth, imageHeight, 0, GL_RGB, GL_FLOAT, normals);
        glBindTexture(GL_TEXTURE_2D, textureRef);
    }


    private void bindShaderVals(){
        glUniform1i(shaderWidth, imageWidth);
        glUniform1i(shaderHeight, imageHeight);

        glUniform1f(gainRef, 4.0f);
        glUniform1f(minGainRef, 1.0f);
        glUniform1f(maxGainRef, 10.0f);

        glUniform1f(diffConstRef, 0.1f);
        glUniform1f(specConstRef, 0.5f);
        glUniform1f(specExConstRef, 10.0f);

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
        setNormalsTexture(6, normals);
    }



    private void loop(){
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();


        float theta = 0.0f;

        while (!glfwWindowShouldClose(window)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            if(currentProgram.equals(ShaderProgram.DEFAULT)) {
                GL20.glUseProgram(defaultProgram);
            }else if(currentProgram.equals(ShaderProgram.NORMALS)){
                GL20.glUseProgram(normalsProgram);
            }else if(currentProgram.equals(ShaderProgram.DIFF_GAIN)){
                GL20.glUseProgram(diffGainProgram);
            }else if(currentProgram.equals(ShaderProgram.SPEC_ENHANCE)){
                GL20.glUseProgram(specEnhanceProgram);
            }


            glUniform1f(shaderLightX, (float)(0.8 * Math.cos(theta)));
            glUniform1f(shaderLightY, (float)(0.8 * Math.sin(theta)));

            glColor3d(1, 0, 0);
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();

            theta += 0.01;
        }
    }

    public static void main(String[] args) throws Exception {
        new HelloWorld().run();
    }

    private void cleanUp(){
        GL20.glDeleteProgram(defaultProgram);
        GL20.glDeleteProgram(normalsProgram);
        GL20.glDeleteProgram(diffGainProgram);
        GL20.glDeleteProgram(specEnhanceProgram);
    }


    private enum ShaderProgram{
        DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE;
    }

}
