package Engine.Graphics;

import Engine.*;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Renderer {


    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transformation transformation;

    private Shaders sceneShaderProgram;

    private Shaders hudShaderProgram;

    private float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(WindowMangaer window) throws Exception {

        setupSceneShader();
        setupHudShader();
    }

    private void setupSceneShader() throws Exception{

        sceneShaderProgram  = new Shaders();
        sceneShaderProgram .createVertexShader(Utils.loadResource("/sencilniki/vertex.vs"));
        sceneShaderProgram .createFragmentShader(Utils.loadResource("/sencilniki/fragment.fs"));
        sceneShaderProgram .link();

        sceneShaderProgram .createUniform("projectionMatrix");
        sceneShaderProgram .createUniform("modelViewMatrix");
        sceneShaderProgram .createUniform("texture_sampler");

        sceneShaderProgram .createMaterialUniform("material");

        sceneShaderProgram .createUniform("specularPower");
        sceneShaderProgram .createUniform("ambientLight");
        sceneShaderProgram .createPointLightListUniform("pointLights",MAX_POINT_LIGHTS);
        sceneShaderProgram .createSpotLightListUniform("spotLights",MAX_SPOT_LIGHTS);
        sceneShaderProgram .createDirectionalLightUniform("directionalLight");

    }

    private void setupHudShader() throws Exception {
        hudShaderProgram = new Shaders();
        hudShaderProgram.createVertexShader(Utils.loadResource("/sencilniki/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/sencilniki/hud_fragment.fs"));
        hudShaderProgram.link();

        hudShaderProgram.createUniform("projModelMatrix");
        hudShaderProgram.createUniform("colour");
        hudShaderProgram.createUniform("hasTexture");

    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(WindowMangaer window, Camera camera, GameItem[] gameItems, SceneLight sceneLight, IHud hud) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.GetWitdh(), window.GetHight());
            window.setResized(false);
        }

        renderScene(window,camera,gameItems,sceneLight);

        renderHud(window,hud);

    }

    public void renderScene(WindowMangaer window, Camera camera, GameItem[] gameItems,
                            SceneLight sceneLight){

        sceneShaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.GetWitdh(), window.GetHight(), Z_NEAR, Z_FAR);
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        renderLights(viewMatrix, sceneLight);

        sceneShaderProgram.setUniform("texture_sampler", 0);

        for (GameItem gameItem : gameItems) {

            Mesh mesh = gameItem.getMesh();

            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem,viewMatrix);
            sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            sceneShaderProgram.setUniform("material", mesh.getMaterial());


            mesh.render();
        }

        sceneShaderProgram.unbind();

    }

    public void renderLights(Matrix4f viewMatrix, SceneLight sceneLight){

        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower",specularPower);

        //point light
        PointLight[] pointLightList=sceneLight.getPointLightList();

        int numLights = pointLightList != null ? pointLightList.length : 0;
        for(int i= 0;i<numLights;i++) {
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight,i);
        }

        SpotLight[] spotLightList=sceneLight.getSpotLightList();
        //spot light
        numLights = spotLightList != null ? spotLightList.length : 0;
        for(int i= 0;i<numLights;i++) {
            SpotLight currSpotLight = new SpotLight(spotLightList[i]);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            Vector3f spotLightPos = currSpotLight.getPointLight().getPosition();
            Vector4f auxSpot = new Vector4f(spotLightPos, 1);
            auxSpot.mul(viewMatrix);
            spotLightPos.x = auxSpot.x;
            spotLightPos.y = auxSpot.y;
            spotLightPos.z = auxSpot.z;

            sceneShaderProgram.setUniform("spotLights", currSpotLight,i);
        }


        //global light
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(),0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x,dir.y,dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);


    }

    private void renderHud(WindowMangaer window,IHud hud){
        hudShaderProgram.bind();
        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.GetWitdh(), window.GetHight(), 0);
        for(GameItem gameItem:hud.getGameItems()){
            Mesh mesh = gameItem.getMesh();
            Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(gameItem,ortho);
            hudShaderProgram.setUniform("projModelMatrix",projModelMatrix);
            hudShaderProgram.setUniform("colour",gameItem.getMesh().getMaterial().getAmbientColour());
            hudShaderProgram.setUniform("hasTexture",gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);

            mesh.render();
        }
        hudShaderProgram.unbind();
    }

    public void cleanup() {
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if(hudShaderProgram != null){
            hudShaderProgram.cleanup();
        }
    }
}