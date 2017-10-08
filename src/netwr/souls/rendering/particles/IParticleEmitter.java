package netwr.souls.rendering.particles;

import java.util.List;

import netwr.souls.logic.GameItem;

public interface IParticleEmitter
{
	public void cleanup();
	
	public Particle getParentParticle();
	
	public List<GameItem> getParticles();
}
