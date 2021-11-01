package Game;

import Engine.*;
import Engine.Graphics.*;
import Engine.Graphics.lights.DirectionalLight;
import Engine.items.GameItem;
import Engine.items.SkyBox;
import Engine.items.Terrain;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/*ta class je namenjen testiranju engina in ni za uporabo igre za igro piši v DummyGame class oz .file*/

public class TestGame implements IGameLogic{

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private Camera camera;

    private float lightAngle;

    private Scene scene;

    private Hud hud;

    private float spotAngle = 0;

    private float spotInc = 1;

    /*DummyGame je placeholder za dejanski game oz. igra bo mela identično vse razen, da se bo v update dejansko dogajalo nekaj zabavnega in
    namesto samo mesh objektov bodo drugi objekti npr. enemy ,player objekti,itd. Kot predlog vse skripte za game mejta v te mapi, da bo engine ločen od same igre.*/

    public TestGame(){
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
        lightAngle = -90;
    }

    @Override
    /*Sem sodi vse, kar se izvede samo ob začetku igre*/
    public void init(WindowMangaer window) throws Exception{

        camera.setPosition(0,1,0);

        renderer.init(window);

        scene = new Scene();

        float reflectance=1f;

        float skyBoxScale = 50.0f;
        float terrainScale = 10;
        int terrainSize = 3;
        float minY = -0.1f;
        float maxY = 0.1f;
        int textInc = 40;
        Terrain terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "src/Game/resources/texture/heightmap.png", "src/Game/resources/texture/terrain.png", textInc);
        scene.setGameItems(terrain.getGameItems());


        SkyBox skyBox = new SkyBox("/Game/resources/models/skybox.obj","src/Game/resources/texture/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        setupLights();

        hud = new Hud("DEMO");

    }

    public void setupLights(){

        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        sceneLight.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));

        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(1, 0, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));

    }


    @Override
    /*Input za kamero je treba podobno narediti za player in njegov class*/
    public void input(WindowMangaer window, MouseInput mouseInput){
        //Nazdor kamere v engine
        cameraInc.set(0,0,0);

        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }

    }
    @Override
    /*sem sodi vse kar se dogaja med igro*/
    public void update(float interval,MouseInput mouseInput){
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            hud.rotateCompass(-camera.getRotation().y);
        }


        SceneLight sceneLight = scene.getSceneLight();

        DirectionalLight directionalLight = sceneLight.getDirectionalLight();

        /*odkomenteraj, če hočes videti night and day cycle*/
//        lightAngle +=1.1f;
//        if(lightAngle > 90){
//            directionalLight.setIntensity(0);
//            if(lightAngle>=360){
//                lightAngle =-90;
//            }
//            sceneLight.getAmbientLight().set(0.3f,0.3f,0.4f);
//        }
//        else if(lightAngle <= -80 || lightAngle >= 80){
//            float factor = 1 - (float)(Math.abs(lightAngle) - 80)/ 10.0f;
//            sceneLight.getAmbientLight().set(factor,factor,factor);
//            directionalLight.setIntensity(factor);
//            directionalLight.getColor().y = Math.max(factor, 0.9f);
//            directionalLight.getColor().z = Math.max(factor, 0.5f);
//
//        }
//        else{
//            directionalLight.setIntensity(1);
//            directionalLight.getColor().x = 1;
//            directionalLight.getColor().y = 1;
//            directionalLight.getColor().z = 1;
//        }
//
//        double angRad = Math.toRadians(lightAngle);
//        directionalLight.getDirection().x = (float) Math.sin(angRad);
//        directionalLight.getDirection().y = (float) Math.cos(angRad);

    }
    @Override
    public void render(WindowMangaer window){

        hud.updateSize(window);
        renderer.render(window, camera, scene, hud);

    }
    @Override
    public void cleanup(){
        renderer.cleanup();
        Map<Mesh, List<GameItem>> mapMeshes = scene.getGameMeshes();
        for(Mesh mesh : mapMeshes.keySet()){
            mesh.cleanUp();
        }
        hud.cleanup();
    }

}
