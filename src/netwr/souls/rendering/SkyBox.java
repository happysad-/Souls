package netwr.souls.rendering;

import org.joml.Vector4f;

import netwr.souls.logic.GameItem;

public class SkyBox extends GameItem
{
	public SkyBox(String objModel, String textureFile) throws Exception
	{		
		Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
		Texture skyBoxTexture = new Texture(textureFile);
		skyBoxMesh.setMaterial(new Material(skyBoxTexture, 0.0f));
		setMesh(skyBoxMesh);
		setPosition(0, 0, 0);
	}
	
	public SkyBox(String objModel, Vector4f colour) throws Exception {
        super();
        Mesh skyBoxMesh = OBJLoader.loadMesh(objModel);
        Material material = new Material(colour, 0);
        skyBoxMesh.setMaterial(material);
        setMesh(skyBoxMesh);
        setPosition(0, 0, 0);
	}
}
