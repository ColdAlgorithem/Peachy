package Game;

import Engine.GameItem;
import Engine.Graphics.FontTexture;
import Engine.Graphics.Material;
import Engine.Graphics.Mesh;
import Engine.Graphics.OBJLoader;
import Engine.IHud;
import Engine.TextItem;
import Engine.WindowMangaer;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.awt.*;

/*Sem se dodajajo vsi elementi hud npr. (health bar,preostali čas do boss,itd.)
* hud elementi se naredijo isto ko 3d objekti le z to razliko, da so definirani znotraj hud
* in spremenjeni v update v dummyGame. Glej implementacijo kompasa kot primer*/

public class Hud implements IHud {

    private static final Font FONT = new Font("Arial",Font.PLAIN,20);

    private static final String CHARSET = "ISO-8859-1";

    //odkomenteraj ko je texture za tekst narejen
    //private static final String FONT_TEXTURE = "src/Game/resources/texture/font_texture.png";

    private final GameItem[] gameItems;

    //TextItem so vsi itemi, ki vsebujejo niz
    private final TextItem statusTextItem;
    //GameItem so ne textovni deli hud
    private final GameItem kompasItem;

    public Hud(String statusText) throws Exception{

        FontTexture fontTexture = new FontTexture(FONT,CHARSET);
        this.statusTextItem = new TextItem(statusText,fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 1, 1, 1));

        Mesh mesh = OBJLoader.loadMesh("/Game/resources/models/kompas.obj");
        Material material = new Material();
        material.setAmbientColour(new Vector4f(1, 0, 0, 1));
        mesh.setMaterial(material);
        kompasItem = new GameItem(mesh);
        kompasItem.setScale(40.0f);

        kompasItem.setRotation(0f, 0f, 180f);

        gameItems = new GameItem[]{statusTextItem, kompasItem};

    }
    public void setStatusTextItem(String statusText){
        this.statusTextItem.setText(statusText);
    }

    public void rotateCompass(float angle){
        this.kompasItem.setRotation(0,0,180+angle);
    }

    @Override
    public GameItem[] getGameItems(){
        return gameItems;
    }

    public void updateSize(WindowMangaer window){
        this.statusTextItem.setPosition(10f, window.GetHight() - 50f, 0);
        this.kompasItem.setPosition(window.GetWitdh() - 40f, 50f, 0);
    }

}
