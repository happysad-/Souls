package netwr.souls.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import netwr.souls.rendering.Fog;
import netwr.souls.rendering.InstancedMesh;
import netwr.souls.rendering.Mesh;
import netwr.souls.rendering.SkyBox;
import netwr.souls.rendering.lights.SceneLights;
import netwr.souls.rendering.particles.IParticleEmitter;

public class Scene
{
	private SkyBox skyBox;
	private SceneLights sceneLights;
	private List<IParticleEmitter> particleEmitters;
	private Map<Mesh, List<GameItem>> meshMap;
	private final Map<InstancedMesh, List<GameItem>> instancedMeshMap;
	private Fog fog;
	private boolean shouldRenderShadows;
	
	private boolean renderingSkyBox;
	
	public Scene()
	{
		meshMap = new HashMap<Mesh, List<GameItem>>();
		instancedMeshMap = new HashMap<InstancedMesh, List<GameItem>>();
		fog = Fog.NOFOG;
	}
	
	public void setGameItems(List<GameItem> gameItems)
	{
        // Create a map of meshes to speed up rendering
        for(GameItem gameItem : gameItems)
        {
            Mesh[] meshes = gameItem.getMeshes();
            for (Mesh mesh : meshes) {
                boolean instancedMesh = mesh instanceof InstancedMesh;
                List<GameItem> list = instancedMesh ? instancedMeshMap.get(mesh) : meshMap.get(mesh);
                if (list == null) {
                    list = new ArrayList<>();
                    if (instancedMesh) {
                        instancedMeshMap.put((InstancedMesh)mesh, list);
                    } else {
                        meshMap.put(mesh, list);
                    }
                }
                list.add(gameItem);
            }
        }
	}
	
	public Map<Mesh, List<GameItem>> getMeshes()
	{
		return meshMap;
	}
	
	public void setSkyBox(SkyBox skyBox)
	{
		this.skyBox = skyBox;
	}
	
	public SkyBox getSkyBox()
	{
		return skyBox;
	}
	
	public void setSceneLights(SceneLights sceneLights)
	{
		this.sceneLights = sceneLights;
	}
	
	public SceneLights getSceneLights()
	{
		return sceneLights;
	}
	
	public Fog getFog()
	{
		return fog;
	}
	
	public void setFog(Fog fog)
	{
		this.fog = fog;
	}
	
	public void isRenderingSkyBox(boolean renderingSkyBox)
	{
		this.renderingSkyBox = renderingSkyBox;
	}
	
	public boolean isRenderingSkyBox()
	{
		return renderingSkyBox;
	}
	
	public void setParticleEmitters(List<IParticleEmitter> particleEmitters)
	{
		this.particleEmitters = particleEmitters;
	}
	
	public List<IParticleEmitter> getParticleEmitters()
	{
		return particleEmitters;
	}

	public boolean shouldRenderShadows() {
		return shouldRenderShadows;
	}

	public void shouldRenderShadows(boolean shouldRenderShadows) {
		this.shouldRenderShadows = shouldRenderShadows;
	}

	public Map<InstancedMesh, List<GameItem>> getInstancedMeshMap() {
		return instancedMeshMap;
	}
	
	public void cleanup()
	{
        for (Mesh mesh : meshMap.keySet())
        {
            mesh.cleanUp();
        }
        for (Mesh mesh : instancedMeshMap.keySet())
        {
            mesh.cleanUp();
        }
        if (particleEmitters != null)
        {
            for (IParticleEmitter particleEmitter : particleEmitters)
            {
                particleEmitter.cleanup();
            }
        }
	}
}
