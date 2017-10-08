package netwr.souls.rendering.lights;

import org.joml.Vector3f;

public class PointLight
{
	private Vector3f colour;
	private Vector3f position;
	private float intensity;
	private Attenuation attenuation;
	
	public PointLight(PointLight pointLight)
	{
		this(new Vector3f(pointLight.getColour()), new Vector3f(pointLight.getPosition()), pointLight.getIntensity(), pointLight.getAttenuation());
	}
	
	public PointLight(Vector3f colour, Vector3f position, float intensity)
	{
		this.colour = colour;
		this.position = position;
		this.intensity = intensity;
		
		attenuation = new Attenuation(1, 0, 0);
	}
	
	public PointLight(Vector3f colour, Vector3f position, float intensity, Attenuation attenuation)
	{
		this.colour = colour;
		this.position = position;
		this.intensity = intensity;
		this.attenuation = attenuation;
	}
	
	public Vector3f getColour()
	{
		return  colour;
	}
	
	public void setColour(Vector3f colour)
	{
		this.colour = colour;
	}
	
	public Vector3f getPosition()
	{
		return position;
	}
	
	public void setPosition(Vector3f position)
	{
		this.position = position;
	}
	
	public float getIntensity()
	{
		return intensity;
	}
	
	public void setIntensity(float intensity)
	{
		this.intensity = intensity;
	}
	
	public Attenuation getAttenuation()
	{
		return attenuation;
	}
	
	public void setAttenuation(Attenuation attenuation)
	{
		this.attenuation = attenuation;
	}
}
