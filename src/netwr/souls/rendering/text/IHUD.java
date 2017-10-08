package netwr.souls.rendering.text;

import java.util.List;

import netwr.souls.logic.GameItem;

public interface IHUD
{
	public List<GameItem> getGameItems();
	
	default void cleanup()
	{
		List<GameItem> gameItems = getGameItems();
		
		for(GameItem gameItem : gameItems)
			gameItem.getMesh().cleanUp();
	}
}
