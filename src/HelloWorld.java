import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;


import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


/**
 * Created by jed on 23/05/17.
 */
public class HelloWorld {

    private long window;
    private int shaderProgram, vertexShader, fragmentShader;
    private int shaderValue;

    public void run(){
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

        window = glfwCreateWindow(300, 300, "Hello LWJGL!", NULL, NULL);
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


    private void createShaders(){
        shaderProgram = GL20.glCreateProgram();
        vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        String vertexShaderSource = Utils.readFromFile("src/defaultVertexShader.glsl");
        String fragmentShaderSource = Utils.readFromFile("src/defaultFragmentShader.glsl");

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

        shaderValue = glGetUniformLocation(shaderProgram, "value");
    }


    private void loop(){
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        double theta = 0;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        while (!glfwWindowShouldClose(window)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            GL20.glUseProgram(shaderProgram);
            glUniform1f(shaderValue, (float)Math.abs(Math.sin(theta)));

            glColor3d(1, 0, 0);
            glBegin(GL_QUADS);
                glVertex2d(-1, -1);
                glVertex2d(1, -1);
                glVertex2d(1, 1);
                glVertex2d(-1, 1);
            glEnd();

            GL20.glUseProgram(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
            theta += 0.1;
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

    private void cleanUp(){
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        GL20.glDeleteProgram(shaderProgram);
    }

}
