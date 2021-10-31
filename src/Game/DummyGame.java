package Game;


import Engine.*;
import Engine.Graphics.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic{

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;

    private GameItem[] gameItems;

    private final Renderer renderer;

    private Camera camera;

    private float lightAngle;

    private SceneLight sceneLight;

    private Hud hud;

    private float spotAngle = 0;

    private float spotInc = 1;

    /*DummyGame je placeholder za dejanski game oz. igra bo mela identično vse razen, da se bo v update dejansko dogajalo nekaj zabavnega in
    namesto samo mesh objektov bodo drugi objekti npr. enemy ,player objekti,itd. Kot predlog vse skripte za game mejta v te mapi, da bo engine ločen od same igre.*/

    public DummyGame(){
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
        lightAngle = -90;
    }

    @Override
    /*Sem sodi vse, kar se izvede samo ob začetku igre*/
    public void init(WindowMangaer window) throws Exception{

        renderer.init(window);

        float reflectance=1f;

        Mesh mesh = OBJLoader.loadMesh("/Game/resources/models/cube.obj");
        Texture texture = new Texture("src/Game/resources/texture/grassblock.png");
        Material material = new Material(texture,reflectance);

        mesh.setMaterial(material);
        GameItem gameItem = new GameItem(mesh);
        gameItem.setScale(0.5f);
        gameItem.setPosition(0,0,-2);
        gameItems = new GameItem[]{gameItem};
        //Point light
        sceneLight = new SceneLight();

        sceneLight.setAmbientLight(new Vector3f(0.3f,0.3f,0.3f));

        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        PointLight pointLight = new PointLight(lightColour,lightPosition,lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f,0.0f,1.0f);
        pointLight.setAttenuation(att);
        sceneLight.setPointLightList(new PointLight[]{pointLight});

        //spot light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        PointLight sl_pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        sl_pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(sl_pointLight, coneDir, cutoff);
        sceneLight.setSpotLightList(new SpotLight[]{spotLight,new SpotLight(spotLight)});

        //"sonce" oz. global light
        lightPosition = new Vector3f(-1,0,0);
        lightColour = new Vector3f(1, 1, 1);
        sceneLight.setDirectionalLight(new DirectionalLight(lightColour, lightPosition, lightIntensity));

        hud = new Hud("DEMO");
    }


    @Override
    /*Input za kamero je treba podobno narediti za player in njegov class*/
    public void input(WindowMangaer window, MouseInput mouseInput){
        //Nazdor kamere v engine
        /*Pozneje po treba kamero narediti bolj dinamično ampak za
        * zdej se mi zdi da je okej oz. sam spremenit bi moral v senčilniku izračun pos kamere oz.
        * sam premakni kamero na mesto ko se bo igra pognala oz. kjer igralec stoji kdor koli bo to delal*/

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
        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        float lightPos = spotLightList[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            spotLightList[0].getPointLight().getPosition().z = lightPos - 0.1f;
        }

    }
    @Override
    /*sem sodi vse kar se dogaja med igro*/
    public void update(float interval,MouseInput mouseInput){

        SpotLight[] spotLightList = sceneLight.getSpotLightList();
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            hud.rotateCompass(-camera.getRotation().y);
        }

        //spotAngle += spotInc * 0.05f;
        if (spotAngle > 2) {
            spotInc = -1;
        }
        else if (spotAngle < -2) {
            spotInc = 1;
        }
        double spotAngleRad = Math.toRadians(spotAngle);
        Vector3f coneDir = spotLightList[0].getConeDirection();
        coneDir.y = (float) Math.sin(spotAngleRad);

        lightAngle +=1.1f;
        if(lightAngle > 90){
            directionalLight.setIntensity(0);
            if(lightAngle>=360){
                lightAngle =-90;
            }
        }
        else if(lightAngle <= -80 || lightAngle >= 80){
            float factor = 1 - (float)(Math.abs(lightAngle) - 80)/ 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);

        }
        else{
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }

        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);

    }
    @Override
    public void render(WindowMangaer window){

        hud.updateSize(window);
        renderer.render(window, camera, gameItems, sceneLight, hud);

    }
    @Override
    public void cleanup(){
        for(GameItem gameItem : gameItems){
            gameItem.getMesh().cleanUp();
        }
        hud.cleanup();
    }

}
