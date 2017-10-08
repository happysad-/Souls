package netwr.souls;

import netwr.souls.Window.WindowOptions;
import netwr.souls.game.SoulsGame;
import netwr.souls.logic.IGameLogic;

public class Start
{
	public static final int WIDTH = 600;
	public static final int HEIGHT = 480;
	public static void main(String[] args)
	{
		try
		{
			boolean useVSync = false;
			
			if(args.length > 0)
				if(args[0].equals("useVSync"))
					useVSync = true;
			
			System.out.println("Current Dir: " + System.getProperty("user.dir"));
			System.out.println("LWJGL Lib Path: " + System.getProperty("org.lwjgl.librarypath"));
			System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
			
			if(System.getProperty("os.name").startsWith("Mac"))
			{
				System.setProperty("java.awt.headless", "true");
			}
			
			WindowOptions options = new WindowOptions();
			options.antialiasing = true;
			options.compatibleProfile = false;
			options.cullFace = true;
			options.frustumCulling = true;
			
			IGameLogic gameLogic = new SoulsGame();
			GameEngine gameEngine = new GameEngine("Souls", 600, 480, useVSync, options, gameLogic);
			
			gameEngine.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
