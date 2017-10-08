package netwr.souls.math;

public class Box2D
{
	public float x;
	public float y;
	public float width;
	public float height;
	
	public Box2D(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(float x, float y)
	{
		return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
	}
}
