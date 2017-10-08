package netwr.souls.logic;

import netwr.souls.MouseInput;
import netwr.souls.Window;

public interface IGameLogic
{
	public void init(Window window) throws Exception;
	
	public void input(Window window, MouseInput mouseInput);
	
	public void update(float interval, MouseInput mouseInput, Window window);
	
	public void render(Window window);
	
	public void cleanup();
}
