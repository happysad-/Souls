package netwr.souls.rendering.lights;

import org.joml.Vector3f;

import netwr.souls.math.OrthoCoords;

public class DirectionalLight
{
	private Vector3f colour;
	private Vector3f direction;
	private float intensity;
	private float shadowPosMult;
	private OrthoCoords orthoCoords;
	
	public DirectionalLight(Vector3f colour, Vector3f direction, float intensity)
	{
		this.colour = colour;
		this.direction = direction;
		this.intensity = intensity;
		
		shadowPosMult = 1;
		orthoCoords = new OrthoCoords();
	}
	
	public DirectionalLight(DirectionalLight light)
	{
		this(new Vector3f(light.getColour()), new Vector3f(light.getDirection()), light.getIntensity());
	}
	
	public float getShadowPosMult()
	{
		return shadowPosMult;
	}
	
	public void setShadowPosMult(float shadowPosMult)
	{
		this.shadowPosMult = shadowPosMult;
	}
	
	public OrthoCoords getOrthoCoords()
	{
		return orthoCoords;
	}
	
	public void setOrthoCoords(float left, float right, float bottom, float top, float near, float far)
	{
		orthoCoords.left = left;
		orthoCoords.right = right;
		orthoCoords.bottom = bottom;
		orthoCoords.top = top;
		orthoCoords.near = near;
		orthoCoords.far = far;
	}
	
	public Vector3f getColour()
	{
		return colour;
	}
	
	public void setColour(Vector3f colour)
	{
		this.colour = colour;
	}
	
	public Vector3f getDirection()
	{
		return direction;
	}
	
	public void setDirection(Vector3f direction)
	{
		this.direction = direction;
	}
	
	public float getIntensity()
	{
		return intensity;
	}
	
	public void setIntensity(float intensity)
	{
		this.intensity = intensity;
	}
}
