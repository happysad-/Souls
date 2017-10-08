package netwr.souls.rendering;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import netwr.souls.Utils;
import netwr.souls.Window;
import netwr.souls.logic.GameItem;
import netwr.souls.logic.Scene;
import netwr.souls.math.OrthoCoords;
import netwr.souls.rendering.animation.AnimGameItem;
import netwr.souls.rendering.animation.AnimatedFrame;
import netwr.souls.rendering.lights.DirectionalLight;
import netwr.souls.rendering.lights.PointLight;
import netwr.souls.rendering.lights.SceneLights;
import netwr.souls.rendering.lights.SpotLight;
import netwr.souls.rendering.particles.IParticleEmitter;
import netwr.souls.rendering.shaders.ShaderProgram;
import netwr.souls.rendering.shadows.ShadowMap;
import netwr.souls.rendering.text.IHUD;

public class RendererOLD {

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transformation transformation;

    private ShaderProgram sceneShaderProgram;

    private ShaderProgram hudShaderProgram;

    private ShaderProgram skyBoxShaderProgram;
    
    private ShaderProgram depthMapShaderProgram;
    
    private ShaderProgram particleShaderProgram;
    
    private ShadowMap shadowMap;

    private final float specularPower;

    public RendererOLD()
    {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(Window window) throws Exception
    {
    	shadowMap = new ShadowMap();
    	
        setupSkyBoxShader();
        setupSceneShader();
        setupHudShader();
        setupDepthMapShader();
        setupParticleShader();
        
        window.setResized(true);
    }

    private void setupSkyBoxShader() throws Exception
    {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("shaders/skybox_vertex.vs"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("shaders/skybox_fragment.fs"));
        skyBoxShaderProgram.link();

        // Create uniforms for projection matrix
        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
    }

    private void setupSceneShader() throws Exception
    {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        // Create uniforms for modelView and projection matrices and texture
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
        sceneShaderProgram.createUniform("normalMap");
        // Create lighting related uniforms
        sceneShaderProgram.createUniform("specularPower");
        sceneShaderProgram.createUniform("ambientLight");
        sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        sceneShaderProgram.createDirectionalLightUniform("directionalLight");
        sceneShaderProgram.createFogUniform("fog");
        
     // Create uniforms for shadow mapping
        sceneShaderProgram.createUniform("shadowMap");
        sceneShaderProgram.createUniform("orthoProjectionMatrix");
        sceneShaderProgram.createUniform("modelLightViewMatrix");
        
        // For Animations
        sceneShaderProgram.createUniform("jointsMatrix");
    }

    private void setupHudShader() throws Exception
    {
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("shaders/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("shaders/hud_fragment.fs"));
        hudShaderProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base colour
        hudShaderProgram.createUniform("projModelMatrix");
        hudShaderProgram.createUniform("colour");
        hudShaderProgram.createUniform("hasTexture");
    }
    
    private void setupDepthMapShader() throws Exception
    {
    	depthMapShaderProgram = new ShaderProgram();
    	depthMapShaderProgram.createVertexShader(Utils.loadResource("shaders/depth_vertex.vs"));
    	depthMapShaderProgram.createFragmentShader(Utils.loadResource("shaders/depth_fragment.fs"));
    	depthMapShaderProgram.link();
    	
    	depthMapShaderProgram.createUniform("orthoProjectionMatrix");
    	depthMapShaderProgram.createUniform("modelLightViewMatrix");
    	
    	depthMapShaderProgram.createUniform("jointsMatrix");
    }
    
    public void setupParticleShader() throws Exception
    {
    	particleShaderProgram = new ShaderProgram();
    	particleShaderProgram.createVertexShader(Utils.loadResource("shaders/particle_vertex.vs"));
    	particleShaderProgram.createFragmentShader(Utils.loadResource("shaders/particle_fragment.fs"));
    	particleShaderProgram.link();
    	
    	particleShaderProgram.createUniform("projectionMatrix");
    	particleShaderProgram.createUniform("modelViewMatrix");
    	particleShaderProgram.createUniform("texture_sampler");
    	particleShaderProgram.createUniform("textureXOffset");
    	particleShaderProgram.createUniform("textureYOffset");
    	particleShaderProgram.createUniform("numCols");
    	particleShaderProgram.createUniform("numRows");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, Scene scene, IHUD hud) {
        clear();
        
        renderDepthMap(window, camera, scene);
        
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection and view atrices once per render cycle
        transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        transformation.updateViewMatrix(camera);

        renderScene(window, camera, scene);

        if(scene.isRenderingSkyBox())
        	renderSkyBox(window, camera, scene);
        
        renderParticles(window, camera, scene);

        renderHud(window, hud);
    }

    private void renderSkyBox(Window window, Camera camera, Scene scene) {
        skyBoxShaderProgram.bind();

        skyBoxShaderProgram.setUniform("texture_sampler", 0);

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        SkyBox skyBox = scene.getSkyBox();
        Matrix4f viewMatrix = transformation.getViewMatrix();
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
        skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLights().getSkyBoxLight());

        scene.getSkyBox().getMesh().render();

        skyBoxShaderProgram.unbind();
    }

    public void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
        sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        Matrix4f lightViewMatrix = transformation.getLightViewMatrix();

        Matrix4f viewMatrix = transformation.getViewMatrix();
        
        sceneShaderProgram.setUniform("fog", scene.getFog());
        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("normalMap", 1);
        sceneShaderProgram.setUniform("shadowMap", 2);

        SceneLights sceneLight = scene.getSceneLights();
        renderLights(viewMatrix, sceneLight);

//        sceneShaderProgram.setUniform("texture_sampler", 0);
        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getMeshes();
        for (Mesh mesh : mapMeshes.keySet())
        {
            sceneShaderProgram.setUniform("material", mesh.getMaterial());
            
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getTextureId());
            
            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(gameItem, viewMatrix);
                sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(gameItem, lightViewMatrix);
                sceneShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                
                if(gameItem instanceof AnimGameItem)
                {
                	AnimGameItem animGameItem = (AnimGameItem)gameItem;
                	AnimatedFrame frame = animGameItem.getCurrentFrame();
                	sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            }
            );
}

        sceneShaderProgram.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, SceneLights sceneLight) {

        sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);

        // Process Point Lights
        List<PointLight> pointLightList = sceneLight.getPointLights();
        int numLights = pointLightList != null ? pointLightList.size() : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = pointLightList.get(i);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, i);
        }

        // Process Spot Ligths
        List<SpotLight> spotLightList = sceneLight.getSpotLights();
        numLights = spotLightList != null ? spotLightList.size() : 0;
        for (int i = 0; i < numLights; i++) {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = spotLightList.get(i);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            sceneShaderProgram.setUniform("spotLights", currSpotLight, i);
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }

    private void renderHud(Window window, IHUD hud) {
        hudShaderProgram.bind();

        Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
        for (GameItem gameItem : hud.getGameItems()) {
            Mesh mesh = gameItem.getMesh();
            // Set ortohtaphic and model matrix for this HUD item
            Matrix4f projModelMatrix = transformation.buildOrthoProjModelMatrix(gameItem, ortho);
            hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudShaderProgram.setUniform("colour", gameItem.getMesh().getMaterial().getAmbientColour());
            hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);

            // Render the mesh for this HUD item
            mesh.render();
        }

        hudShaderProgram.unbind();
    }
    
    private void renderDepthMap(Window window, Camera camera, Scene scene)
    {
    	GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
    	GL11.glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
    	
    	GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    	depthMapShaderProgram.bind();
    	
    	DirectionalLight light = scene.getSceneLights().getDirectionalLight();
    	Vector3f lightDirection = light.getDirection();
    	OrthoCoords orthoCoords = light.getOrthoCoords();
    	
    	float lightAngleX = (float)Math.toDegrees(Math.acos(lightDirection.z));
    	float lightAngleY = (float)Math.toDegrees(Math.asin(lightDirection.x));
    	float lightAngleZ = 0;
    	
    	Matrix4f lightViewMatrix = transformation.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX, lightAngleY, lightAngleZ));
    	Matrix4f orthoProjMatrix = transformation.updateOrthoProjectionMatrix(orthoCoords.left, orthoCoords.right, orthoCoords.bottom, orthoCoords.top, orthoCoords.near, orthoCoords.far);
    	
    	depthMapShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
    	
    	Map<Mesh, List<GameItem>> mapMeshes = scene.getMeshes();
    	
    	for(Mesh mesh : mapMeshes.keySet())
    	{
    		mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
    			Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(gameItem, lightViewMatrix);
    			depthMapShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
    		
    			
    			if(gameItem instanceof AnimGameItem)
    			{
    				AnimGameItem animGameItem = (AnimGameItem)gameItem;
                	AnimatedFrame frame = animGameItem.getCurrentFrame();
                	sceneShaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());
    			}
    		});
    	}
    	
    	depthMapShaderProgram.unbind();
    	GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
    
    public void renderParticles(Window window, Camera camera, Scene scene)
    {
    	GL11.glDepthMask(false);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    	particleShaderProgram.bind();
    	
    	particleShaderProgram.setUniform("texture_sampler", 0);
    	
    	Matrix4f projectionMatrix = transformation.getProjectionMatrix();
    	particleShaderProgram.setUniform("projectionMatrix", projectionMatrix);
    	
    	Matrix4f viewMatrix = transformation.getViewMatrix();
    	
    	List<IParticleEmitter> emitters = scene.getParticleEmitters();
    	
    	for(IParticleEmitter emitter : emitters)
    	{
    		Mesh mesh = emitter.getParentParticle().getMesh();
    		
    		mesh.renderList((emitter.getParticles()), (GameItem gameItem) -> {
    			
    			Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
    			
    			viewMatrix.transpose3x3(modelMatrix);
    			
    			Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
    			particleShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
    			
    			Texture texture = mesh.getMaterial().getTexture();
    			int col = gameItem.getTextPos() % texture.getNumCols();
    			int row = gameItem.getTextPos() / texture.getNumRows();
    			float textureXOffset = (float) col / texture.getNumCols();
    			float textureYOffset = (float) row / texture.getNumRows();
    			
    			particleShaderProgram.setUniform("textureXOffset", textureXOffset);
    			particleShaderProgram.setUniform("textureYOffset", textureYOffset);
    			particleShaderProgram.setUniform("numCols", texture.getNumCols());
    			particleShaderProgram.setUniform("numRows", texture.getNumRows());
    		});
    	}
    	
    	particleShaderProgram.unbind();
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    	GL11.glDepthMask(true);
    }

    public void cleanup() {
    	
        if (skyBoxShaderProgram != null)
            skyBoxShaderProgram.cleanup();
        
        if (sceneShaderProgram != null)
            sceneShaderProgram.cleanup();
            
        if (hudShaderProgram != null)
            hudShaderProgram.cleanup();
        
        if(depthMapShaderProgram != null)
        	depthMapShaderProgram.cleanup();
        
        if(particleShaderProgram != null)
        	particleShaderProgram.cleanup();
    }
}
