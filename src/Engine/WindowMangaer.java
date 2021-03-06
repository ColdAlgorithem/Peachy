package Engine;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;



public class WindowMangaer {

    private final String title;

    private int width;

    private int hight;

    private long windowHandle;

    private boolean resized;

    private boolean vSync;

    public WindowMangaer(String title,int width,int hight,boolean vSync){

        this.title = title;
        this.width = width;
        this.hight = hight;
        this.vSync = vSync;
        this.resized = false;


    }
    public void init(){

        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit())
            throw  new IllegalStateException("Unable to initailize GlFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE,GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE,GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,2);
        glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT,GL_TRUE);

        windowHandle = glfwCreateWindow(width,hight,title,NULL,NULL);

        if(windowHandle == NULL)
            throw  new RuntimeException("Failed to create the GLFW window");


        glfwSetFramebufferSizeCallback(windowHandle,(window,width,hight)  -> {

            this.width = width;
            this.hight = hight;
            this.setResized(true);

        });

        glfwSetKeyCallback(windowHandle,(window, key, scancode, action, mods) -> {

            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window,true);


        });

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        glfwSetWindowPos(
                windowHandle,
                (vidMode.width() -width)/2,
                (vidMode.height()-hight)/2
        );
        glfwMakeContextCurrent(windowHandle);

        if(isvSync())
            glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public long getWindowHandle(){

        return windowHandle;
    }

    public  void SetClearColor(float r,float g,float b,float alpha){

        glClearColor(r,g,b,alpha);

    }
    public boolean isKeyPressed(int keyCode){

        return glfwGetKey(windowHandle,keyCode) == GLFW_PRESS;

    }
    public boolean windowShouldClose(){

        return glfwWindowShouldClose(windowHandle);

    }
    public String getTitle(){

        return title;
    }
    public int GetWitdh(){

        return width;
    }
    public int GetHight(){

        return hight;
    }
    public boolean isResized(){

        return resized;
    }
    public void setResized(boolean resized){

        this.resized = resized;

    }
    public boolean isvSync(){

        return vSync;
    }
    public void  update(){
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();

    }

}
