package netwr.souls.rendering;

import org.joml.Vector3f;

public class Fog
{
	private boolean active;
	private Vector3f colour;
	private float density;
	public static Fog NOFOG = new Fog();
	
	public Fog()
	{
		active = false;
		this.colour = new Vector3f(0, 0, 0);
		this.density = 0;
	}
	
	public Fog(boolean active, Vector3f colour, float density)
	{
		this.colour = colour;
		this.density = density;
		this.active = active;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void isActive(boolean isActive)
	{
		active = isActive;
	}
	
	public Vector3f getColour()
	{
		return colour;
	}
	
	public void setColour(Vector3f colour)
	{
		this.colour = colour;
	}
	
	public float getDensity()
	{
		return density;
	}
	
	public void setDensity(float density)
	{
		this.density = density;
	}
}
