package Engine.Graphics;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final int id;

    private final int width;

    private final int hight;

    public Texture(String fileName) throws Exception{

        ByteBuffer buf;

        try(MemoryStack stack = MemoryStack.stackPush()){

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load(fileName,w,h,channels,4);

            if(buf == null)
                throw new Exception("Image file ["+fileName+"] not loaded: "+stbi_failure_reason());

            width = w.get();

            hight = h.get();

        }

        this.id = createTexture(buf);

        stbi_image_free(buf);
    }

    public Texture(ByteBuffer imgeBuffer) throws Exception{

        ByteBuffer buf;

        try(MemoryStack stack = MemoryStack.stackPush()){

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buf = stbi_load_from_memory(imgeBuffer,w,h,channels,4);

            if(buf == null)
                throw new Exception("Image file not loaded: "+stbi_failure_reason());

            width = w.get();

            hight = h.get();

        }

        this.id = createTexture(buf);

        stbi_image_free(buf);
    }

    private int createTexture(ByteBuffer buf){

        int textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D,textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT,1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,hight,0,GL_RGBA,GL_UNSIGNED_BYTE,buf);

        glGenerateMipmap(GL_TEXTURE_2D);

        return textureId;

    }

    public int getId(){ return id; }

    public int getWidth() {
        return width;
    }

    public int getHight() {
        return hight;
    }

    public void bind(){ glBindTexture(GL_TEXTURE_2D,id); }

    public void cleanup(){ glDeleteTextures(id); }

}
