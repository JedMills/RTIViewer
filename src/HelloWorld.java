import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import ptmCreation.PTMObject;
import ptmCreation.PTMParser;
import utils.ShaderUtils;
import utils.Utils;

import java.nio.*;


import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


/**
 * Created by jed on 23/05/17.
 */
public class HelloWorld {

    private long window;

    private int shaderProgram, vertexShader, fragmentShader;
    private int shaderWidth, shaderHeight;
    private int shaderRCoeffs1, shaderRCoeffs2;
    private int shaderGCoeffs1, shaderGCoeffs2;
    private int shaderBCoeffs1, shaderBCoeffs2;
    private int shaderLightX, shaderLightY;

    private int imageHeight, imageWidth;

    private Utils.Vector3f lightPos;
    private int shaderArraySize;

    int[][] rVals1, rVals2, gVals1, gVals2, bVals1, bVals2;

    public void run() throws Exception{
        setupPTM();
        init();
        createShaders();
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
        glfwShowWindow(window);
    }


    private void setupPTM() throws Exception{
        PTMObject ptmObject = PTMParser.createPtmFromFile("leafTest.ptm");


        imageHeight = ptmObject.getHeight();
        imageWidth = ptmObject.getWidth();
        shaderArraySize = (imageWidth * imageHeight * 3);

        rVals1 = ptmObject.getRedVals1();
        rVals2 = ptmObject.getRedVals2();
        gVals1 = ptmObject.getGreenVals1();
        gVals2 = ptmObject.getGreenVals2();
        bVals1 = ptmObject.getBlueVals1();
        bVals2 = ptmObject.getBlueVals2();
    }



    private void createShaders(){
        shaderProgram = GL20.glCreateProgram();
        vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        String vertexShaderSource = ShaderUtils.readFromFile("src/defaultVertexShader.glsl");
        String fragmentShaderSource = ShaderUtils.readFromFile("src/defaultFragmentShader.glsl", shaderArraySize);

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

        System.out.println(glGetProgramInfoLog(shaderProgram));
        System.out.println();
        System.out.println();
        System.out.println("max = " + glGetInteger(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS));
        bindShaderReferences();
    }


    private void bindShaderReferences(){
        shaderWidth = glGetUniformLocation(shaderProgram, "imageWidth");
        shaderHeight = glGetUniformLocation(shaderProgram, "imageHeight");
        shaderLightX = glGetUniformLocation(shaderProgram, "lightX");
        shaderLightY = glGetUniformLocation(shaderProgram, "lightY");
        shaderRCoeffs1 = glGetUniformLocation(shaderProgram, "rCoeffs1");
        shaderRCoeffs2 = glGetUniformLocation(shaderProgram, "rCoeffs2");
        shaderGCoeffs1 = glGetUniformLocation(shaderProgram, "gCoeffs1");
        shaderGCoeffs2 = glGetUniformLocation(shaderProgram, "gCoeffs2");
        shaderBCoeffs1 = glGetUniformLocation(shaderProgram, "bCoeffs1");
        shaderBCoeffs2 = glGetUniformLocation(shaderProgram, "bCoeffs2");
        System.out.println("Bound shader refs");
    }


    private void bindShaderVals(){
        glUniform1i(shaderWidth, imageWidth);
        glUniform1i(shaderHeight, imageHeight);
        /*
        glUniform3iv(shaderRCoeffs1, flatten(rVals1));
        glUniform3iv(shaderRCoeffs2, flatten(rVals2));
        glUniform3iv(shaderGCoeffs1, flatten(gVals1));
        glUniform3iv(shaderGCoeffs2, flatten(gVals2));
        glUniform3iv(shaderBCoeffs1, flatten(bVals1));
        glUniform3iv(shaderBCoeffs2, flatten(bVals2));
        */
    }


    private int[] flatten(int[][] array){
        int[] out = new int[array.length * array[0].length];
        int k = 0;
        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[0].length; j++){
                out[k++] = array[i][j];
            }
        }
        return out;
    }

    private void loop(){
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);


        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        GL20.glUseProgram(shaderProgram);
        bindShaderVals();

        while (!glfwWindowShouldClose(window)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //GL20.glUseProgram(shaderProgram);
            glUniform1f(shaderLightX, 0.1f);
            glUniform1f(shaderLightY, 0.1f);

            glColor3d(1, 0, 0);
            glBegin(GL_QUADS);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            //GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();

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
