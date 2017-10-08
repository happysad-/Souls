package netwr.souls.game;

import org.joml.Vector3f;

import netwr.souls.rendering.lights.DirectionalLight;

public class Sun
{
	private DirectionalLight light;
	private float lightAngle = 0F;
	
	public Sun()
	{
		light = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), 1F);
	}
	
	public void updatePosition()
	{
		lightAngle += 1.1F;
		
		if(lightAngle > 90)
		{
			light.setIntensity(0);
			
			if(lightAngle >= 360)
				lightAngle = -90;
		}
		else if(lightAngle <= -80 || lightAngle >= 80)
		{
			float factor = 1 - (float)(Math.abs(lightAngle) - 80) / 10.0F;
			light.setIntensity(factor);
			light.getColour().y = Math.max(factor, 0.9F);
			light.getColour().z = Math.max(factor, 0.5F);
		}
		else
		{
			light.setIntensity(1);
			light.getColour().x = 1;
			light.getColour().y = 1;
			light.getColour().z = 1;
		}
		
		double angRad = Math.toRadians(lightAngle);
		light.getDirection().x = (float)(Math.sin(angRad));
		light.getDirection().y = (float)(Math.cos(angRad));
	}
	
	public DirectionalLight getLight()
	{
		return light;
	}
}
