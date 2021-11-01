package Engine;

import Engine.Graphics.Mesh;
import Engine.items.GameItem;
import Engine.items.SkyBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    private Map<Mesh, List<GameItem>> meshMap;

    private SkyBox skyBox;

    public Scene(){
        meshMap = new HashMap<>();
    }


    private SceneLight sceneLight;

    public Map<Mesh, List<GameItem>> getGameMeshes() {
        return meshMap;
    }


    public void setGameItems(GameItem[] gameItems) {
        int numGamesItems = gameItems != null ? gameItems.length : 0;
        for(int i=0;i<numGamesItems; i++){
            GameItem gameItem = gameItems[i];
            Mesh mesh = gameItem.getMesh();
            List<GameItem> list = meshMap.get(mesh);
            if(list == null){
                list = new ArrayList<>();
                meshMap.put(mesh,list);
            }
            list.add(gameItem);
        }
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() { return sceneLight; }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}
