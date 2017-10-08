package netwr.souls.rendering.shadows;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import netwr.souls.rendering.Texture;

public class ShadowMap
{
	public static final int SHADOW_MAP_WIDTH = 1024;
	public static final int SHADOW_MAP_HEIGHT = 1024;
	private final int depthMapFBO;
	private final Texture depthMap;
	
	public ShadowMap() throws Exception
	{
		depthMapFBO = GL30.glGenFramebuffers();
		depthMap = new Texture(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL11.GL_DEPTH_COMPONENT);
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, depthMapFBO);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthMap.getTextureId(), 0);
		
		GL11.glDrawBuffer(GL11.GL_NONE);
		GL11.glReadBuffer(GL11.GL_NONE);
		
		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
		{
			throw new Exception("Could not create a FrameBuffer!");
		}
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public Texture getDepthMapTexture()
	{
		return depthMap;
	}
	
	public int getDepthMapFBO()
	{
		return depthMapFBO;
	}
	
	public void cleanup()
	{
		GL30.glDeleteFramebuffers(depthMapFBO);
		depthMap.cleanup();
	}
}
