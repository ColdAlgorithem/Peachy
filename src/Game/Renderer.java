package Game;

import Engine.Graphics.*;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;
import Engine.GameItem;
import Engine.Utils;
import Engine.WindowMangaer;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

public class Renderer {


    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transformation transformation;

    private Shaders shaderProgram;

    private  float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(WindowMangaer window) throws Exception {

        shaderProgram = new Shaders();
        shaderProgram.createVertexShader(Utils.loadResource("/resources/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/resources/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");

        shaderProgram.createMaterialUniform("material");

        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightListUniform("pointLights",MAX_POINT_LIGHTS);
        shaderProgram.createSpotLightListUniform("spotLights",MAX_SPOT_LIGHTS);
        shaderProgram.createDirectionalLightUniform("directionalLight");

    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(WindowMangaer window, Camera camera, GameItem[] gameItems, Vector3f ambientLight,
                       PointLight[] pointLightList, SpotLight[] spotLightList,DirectionalLight directionalLight) {
        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.GetWitdh(), window.GetHight());
            window.setResized(false);
        }

        shaderProgram.bind();


        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.GetWitdh(), window.GetHight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        shaderProgram.setUniform("ambientLight",ambientLight);

        shaderProgram.setUniform("specularPower",specularPower);

        //point light
        int numLights = pointLightList != null ? pointLightList.length : 0;
        for(int i= 0;i<numLights;i++) {
            PointLight currPointLight = new PointLight(pointLightList[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            shaderProgram.setUniform("pointLights", currPointLight,i);
        }

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

            shaderProgram.setUniform("spotLights", currSpotLight,i);
        }
        //global light
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(),0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x,dir.y,dir.z));
        shaderProgram.setUniform("directionalLight", currDirLight);

        shaderProgram.setUniform("texture_sampler", 0);

        for (GameItem gameItem : gameItems) {

            Mesh mesh = gameItem.getMesh();

            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem,viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

            shaderProgram.setUniform("material", mesh.getMaterial());

            mesh.render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}