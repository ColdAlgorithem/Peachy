package Engine.Graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shaders {

    private final  int programId;

    private int verexShaderId;

    private int fragmentShaderId;

    private final Map<String,Integer> uniforms;

    public Shaders() throws Exception{
        programId = glCreateProgram();
        if(programId == 0){
            throw new Exception("Colud not create Shader");

        }
        uniforms = new HashMap<>();
    }
    public void createUniform(String uniformName) throws Exception{

        int uniformLocation = glGetUniformLocation(programId,uniformName);

        if(uniformLocation<0)
            throw new Exception("Colud not find uniform:"+uniformName);

        uniforms.put(uniformName,uniformLocation);


    }

    public void createPointLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) createPointLightUniform(uniformName + "[" + i + "]");
    }

    public void createPointLightUniform(String uniformName) throws Exception{
        createUniform(uniformName+".colour");
        createUniform(uniformName+".position");
        createUniform(uniformName+".intensity");
        createUniform(uniformName+".att.constant");
        createUniform(uniformName+".att.linear");
        createUniform(uniformName+".att.exponent");
    }

    public void createSpotLightListUniform(String uniformName, int size) throws Exception {
        for (int i = 0; i < size; i++) createSpotLightUniform(uniformName + "[" + i + "]");
    }

    public void createSpotLightUniform(String uniformName) throws Exception{
        createPointLightUniform(uniformName+".pl");
        createUniform(uniformName+".conedir");
        createUniform(uniformName+".cutoff");
    }

    public void createDirectionalLightUniform(String uniformName) throws Exception{
        createUniform(uniformName+".colour");
        createUniform(uniformName+".direction");
        createUniform(uniformName+".intensity");
    }

    public void createMaterialUniform(String uniformName) throws Exception{
        createUniform(uniformName+".ambient");
        createUniform(uniformName+".diffuse");
        createUniform(uniformName+".specular");
        createUniform(uniformName+".hasTexture");
        createUniform(uniformName+".reflectance");

    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            FloatBuffer fb = stack.mallocFloat(16);

            value.get(fb);

            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, Vector3f value){

        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);

    }

    public void setUniform(String uniformName, Vector4f value){

        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z,value.w);

    }

    public  void setUniform(String uniformsName,int value){
        glUniform1i(uniforms.get(uniformsName),value);


    }

    public  void setUniform(String uniformsName,float value){
        glUniform1f(uniforms.get(uniformsName),value);


    }

    public void setUniform(String uniformName, SpotLight spotLight,int pos){
        setUniform(uniformName+"["+pos+"]",spotLight);
    }

    public void setUniform(String uniformName, PointLight pointLight,int pos){
        setUniform(uniformName+"["+pos+"]",pointLight);
    }

    public void setUniform(String uniformName, PointLight[] pointLights){
        int numLights = pointLights != null ? pointLights.length : 0;
        for(int i=0;i<numLights;i++) setUniform(uniformName, pointLights[i],i);
    }

    public void setUniform(String uniformName, SpotLight[] spotLight){
        int numLights = spotLight != null ? spotLight.length : 0;
        for(int i=0;i<numLights;i++) setUniform(uniformName, spotLight[i],i);
    }


    public  void setUniform(String uniformsName,DirectionalLight dirLight){
        setUniform(uniformsName+".colour",dirLight.getColor());
        setUniform(uniformsName+".direction",dirLight.getDirection());
        setUniform(uniformsName+".intensity",dirLight.getIntensity());
    }

    public void setUniform(String uniformName, PointLight pointLight){
        setUniform(uniformName+".colour",pointLight.getColor());
        setUniform(uniformName+".position",pointLight.getPosition());
        setUniform(uniformName+".intensity",pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName+".att.constant",att.getConstant());
        setUniform(uniformName+".att.linear",att.getLinear());
        setUniform(uniformName+".att.exponent",att.getExponent());


    }

    public void setUniform(String uniformName, Material material){
        setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    public void setUniform(String uniformName, SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".conedir", spotLight.getConeDirection());
        setUniform(uniformName + ".cutoff", spotLight.getCutOff());
    }


    public void createVertexShader(String shaderCode) throws Exception{

        verexShaderId = createShader(shaderCode,GL_VERTEX_SHADER);

    }
    public void createFragmentShader(String shaderCode) throws Exception{

        fragmentShaderId=createShader(shaderCode,GL_FRAGMENT_SHADER);

    }
    protected int createShader(String shaderCode,int shaderType) throws Exception{

        int shaderId = glCreateShader(shaderType);
        if(shaderId == 0)
            throw new Exception("Error creating shader. Type: "+shaderType);

        glShaderSource(shaderId,shaderCode);
        glCompileShader(shaderId);

        if(glGetShaderi(shaderId,GL_COMPILE_STATUS) == 0)
            throw new Exception("Error compiling Shader code: "+glGetShaderInfoLog(shaderId,1024));

        glAttachShader(programId,shaderId);

        return shaderId;

    }
    public void link() throws Exception{
        glLinkProgram(programId);
        if(glGetProgrami(programId,GL_LINK_STATUS) == 0)
            throw new Exception("Error linking shader code: "+glGetProgramInfoLog(programId,1024));

        if(verexShaderId != 0)
            glDetachShader(programId,verexShaderId);

        if(fragmentShaderId !=0)
            glDetachShader(programId,fragmentShaderId);

        glValidateProgram(programId);
        if(glGetProgrami(programId,GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating Shader code: "+glGetProgramInfoLog(programId,1024));


    }

    public void bind(){
        glUseProgram(programId);

    }
    public void unbind(){
        glUseProgram(0);

    }

    public void cleanup(){
        unbind();
        if(programId != 0)
            glDeleteProgram(programId);


    }

}
