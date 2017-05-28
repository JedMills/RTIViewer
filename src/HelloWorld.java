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

    private int shaderProgram, vertexShader, fragmentShader;
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
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true);
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
        PTMObject ptmObject = PTMParser.createPtmFromFile("boneTest.ptm");

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



    private void createShaders(){
        shaderProgram = GL20.glCreateProgram();
        vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        String vertexShaderSource = ShaderUtils.readFromFile("src/shaders/defaultVertexShader.glsl");
        String fragmentShaderSource = ShaderUtils.readFromFile("src/shaders/specEnhanceFragmentShader.glsl");

        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(vertexShader);
        GL20.glCompileShader(fragmentShader);

        if(GL20.glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE){
            System.err.println("Couldn't compile frag shader " + GL20.glGetShaderInfoLog(fragmentShader));
        }
        if(GL20.glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE){
            System.err.println("Couldn't compile vert shader " + GL20.glGetShaderInfoLog(vertexShader));
        }
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);

        GL20.glLinkProgram(shaderProgram);
        GL20.glValidateProgram(shaderProgram);
        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);

        bindShaderReferences();
        GL20.glUseProgram(shaderProgram);
        bindShaderVals();
    }


    private void bindShaderReferences(){
        shaderWidth = glGetUniformLocation(shaderProgram, "imageWidth");
        shaderHeight = glGetUniformLocation(shaderProgram, "imageHeight");
        shaderLightX = glGetUniformLocation(shaderProgram, "lightX");
        shaderLightY = glGetUniformLocation(shaderProgram, "lightY");

        rVals1Ref = glGetUniformLocation(shaderProgram, "rVals1");
        rVals2Ref = glGetUniformLocation(shaderProgram, "rVals2");

        gVals1Ref = glGetUniformLocation(shaderProgram, "gVals1");
        gVals2Ref = glGetUniformLocation(shaderProgram, "gVals2");

        bVals1Ref = glGetUniformLocation(shaderProgram, "bVals1");
        bVals2Ref = glGetUniformLocation(shaderProgram, "bVals2");

        normalsRef = glGetUniformLocation(shaderProgram, "normals");

        gainRef = glGetUniformLocation(shaderProgram, "gain");
        minGainRef = glGetUniformLocation(shaderProgram, "minGain");
        maxGainRef = glGetUniformLocation(shaderProgram, "maxGain");

        diffConstRef = glGetUniformLocation(shaderProgram, "diffConst");
        specConstRef = glGetUniformLocation(shaderProgram, "specConst");
        specExConstRef = glGetUniformLocation(shaderProgram, "specExConst");
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

        //GL20.glUseProgram(shaderProgram);
        //bindShaderVals();

        float theta = 0.0f;

        while (!glfwWindowShouldClose(window)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            glUniform1f(shaderLightX, (float)(0.5 * Math.cos(theta)));
            glUniform1f(shaderLightY, (float)(0.5 * Math.sin(theta)));

            glColor3d(1, 0, 0);
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            //GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();

            theta += 0.01;
        }
    }

    public static void main(String[] args) throws Exception {
        new HelloWorld().run();
    }

    private void cleanUp(){
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        GL20.glDeleteProgram(shaderProgram);
    }

}
