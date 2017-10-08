package netwr.souls.rendering.lights;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class SceneLights
{
	private List<PointLight> pointLights;
	private List<SpotLight> spotLights;
	private Vector3f skyBoxLight;
	private DirectionalLight directionalLight;
	private Vector3f ambientLight;
	
	public SceneLights(DirectionalLight directionalLight, Vector3f ambientLight, List<PointLight> pointLights, List<SpotLight> spotLights)
	{
		this.directionalLight = directionalLight;
		this.ambientLight = ambientLight;
		this.pointLights = pointLights;
		this.spotLights = spotLights;
	}
	
	public SceneLights()
	{
		pointLights = new ArrayList<PointLight>();
		spotLights = new ArrayList<SpotLight>();
	}
	
	public void addPointLight(PointLight pointLight)
	{
		pointLights.add(pointLight);
	}
	
	public void removePointLight(int index)
	{
		pointLights.remove(index);
	}
	
	public void addSpotLight(SpotLight spotLight)
	{
		spotLights.add(spotLight);
	}
	
	public void removeSpotLight(int index)
	{
		spotLights.remove(index);
	}
	
	public void setDirectionalLight(DirectionalLight directionalLight)
	{
		this.directionalLight = directionalLight;
	}
	
	public DirectionalLight getDirectionalLight()
	{
		return directionalLight;
	}
	
	public void setAmbientLight(Vector3f ambientLight)
	{
		this.ambientLight = ambientLight;
	}
	
	public Vector3f getAmbientLight()
	{
		return ambientLight;
	}
	
	public void setPointLights(List<PointLight> pointLights)
	{
		this.pointLights = pointLights;
	}
	
	public List<PointLight> getPointLights()
	{
		return pointLights;
	}
	
	public void setSpotLights(List<SpotLight> spotLights)
	{
		this.spotLights = spotLights;
	}
	
	public List<SpotLight> getSpotLights()
	{
		return spotLights;
	}
	
	public void setSkyBoxLight(Vector3f skyBoxLight)
	{
		this.skyBoxLight = skyBoxLight;
	}
	
	public Vector3f getSkyBoxLight()
	{
		return skyBoxLight;
	}
}
