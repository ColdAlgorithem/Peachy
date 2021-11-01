package Engine.items;

import Engine.Graphics.Material;
import Engine.Graphics.Mesh;
import Engine.Graphics.OBJLoader;
import Engine.Graphics.Texture;

public class SkyBox extends GameItem{
    public SkyBox(String objModel,String textureFile) throws Exception{

        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Texture skyBoxTexture = new Texture(textureFile);
        skyBoxMesh.setMaterial(new Material(skyBoxTexture,0.0f));
        setMesh(skyBoxMesh);
        setPosition(0,0,0);

    }
}
