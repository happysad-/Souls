package netwr.souls.rendering;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import netwr.souls.Utils;
import netwr.souls.Window;
import netwr.souls.logic.GameItem;
import netwr.souls.logic.Scene;
import netwr.souls.math.OrthoCoords;
import netwr.souls.rendering.animation.AnimGameItem;
import netwr.souls.rendering.animation.AnimatedFrame;
import netwr.souls.rendering.filters.FrustumCullingFilter;
import netwr.souls.rendering.lights.DirectionalLight;
import netwr.souls.rendering.lights.PointLight;
import netwr.souls.rendering.lights.SceneLights;
import netwr.souls.rendering.lights.SpotLight;
import netwr.souls.rendering.particles.IParticleEmitter;
import netwr.souls.rendering.shaders.ShaderProgram;
import netwr.souls.rendering.shadows.ShadowMap;

public class Renderer {

    private static final int MAX_POINT_LIGHTS = 5;

    private static final int MAX_SPOT_LIGHTS = 5;

    private final Transformation transformation;

    private ShadowMap shadowMap;

    private ShaderProgram depthShaderProgram;

    private ShaderProgram sceneShaderProgram;

    private ShaderProgram skyBoxShaderProgram;

    private ShaderProgram particlesShaderProgram;

    private final float specularPower;

    private final FrustumCullingFilter frustumFilter;

    private final List<GameItem> filteredItems;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
        frustumFilter = new FrustumCullingFilter();
        filteredItems = new ArrayList<>();
    }

    public void init(Window window) throws Exception {
        shadowMap = new ShadowMap();

        setupDepthShader();
        setupSkyBoxShader();
        setupSceneShader();
        setupParticlesShader();
    }

    public void render(Window window, Camera camera, Scene scene) {
        clear();

        if (window.getOptions().frustumCulling) {
            frustumFilter.updateFrustum(window.getProjectionMatrix(), camera.getViewMatrix());
            frustumFilter.filter(scene.getMeshes());
            frustumFilter.filter(scene.getInstancedMeshMap());
        }

        // Render depth map before view ports has been set up
        renderDepthMap(window, camera, scene);

        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Update projection matrix once per render cycle
        window.updateProjectionMatrix();

        renderScene(window, camera, scene);
        renderSkyBox(window, camera, scene);
        
        if(scene.getParticleEmitters() != null && scene.getParticleEmitters().size() > 0)
        	renderParticles(window, camera, scene);

        //renderAxes(camera);
        renderCrossHair(window);
    }

    private void setupParticlesShader() throws Exception {
        particlesShaderProgram = new ShaderProgram();
        particlesShaderProgram.createVertexShader(Utils.loadResource("shaders/particle_vertex.vs"));
        particlesShaderProgram.createFragmentShader(Utils.loadResource("shaders/particle_fragment.fs"));
        particlesShaderProgram.link();

        particlesShaderProgram.createUniform("projectionMatrix");
        particlesShaderProgram.createUniform("texture_sampler");

        particlesShaderProgram.createUniform("numCols");
        particlesShaderProgram.createUniform("numRows");
    }

    private void setupDepthShader() throws Exception {
        depthShaderProgram = new ShaderProgram();
        depthShaderProgram.createVertexShader(Utils.loadResource("shaders/depth_vertex.vs"));
        depthShaderProgram.createFragmentShader(Utils.loadResource("shaders/depth_fragment.fs"));
        depthShaderProgram.link();

        depthShaderProgram.createUniform("isInstanced");
        depthShaderProgram.createUniform("jointsMatrix");
        depthShaderProgram.createUniform("modelLightViewNonInstancedMatrix");
        depthShaderProgram.createUniform("orthoProjectionMatrix");
    }

    private void setupSkyBoxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("shaders/skybox_vertex.vs"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("shaders/skybox_fragment.fs"));
        skyBoxShaderProgram.link();

        // Create uniforms for projection matrix
        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
        skyBoxShaderProgram.createUniform("colour");
        skyBoxShaderProgram.createUniform("hasTexture");
    }

    private void setupSceneShader() throws Exception {
        // Create shader
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(Utils.loadResource("shaders/scene_vertex.vs"));
        sceneShaderProgram.createFragmentShader(Utils.loadResource("shaders/scene_fragment.fs"));
        sceneShaderProgram.link();

        // Create uniforms for modelView and projection matrices
        sceneShaderProgram.createUniform("projectionMatrix");
        sceneShaderProgram.createUniform("modelViewNonInstancedMatrix");
        sceneShaderProgram.createUniform("texture_sampler");
        sceneShaderProgram.createUniform("normalMap");
        // Create uniform for material
        sceneShaderProgram.createMaterialUniform("material");
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
        sceneShaderProgram.createUniform("modelLightViewNonInstancedMatrix");
        sceneShaderProgram.createUniform("renderShadow");

        // Create uniform for joint matrices
        sceneShaderProgram.createUniform("jointsMatrix");

        sceneShaderProgram.createUniform("isInstanced");
        sceneShaderProgram.createUniform("numCols");
        sceneShaderProgram.createUniform("numRows");

//        sceneShaderProgram.createUniform("selectedNonInstanced");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    private void renderParticles(Window window, Camera camera, Scene scene) {
        particlesShaderProgram.bind();

        particlesShaderProgram.setUniform("texture_sampler", 0);
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        particlesShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = camera.getViewMatrix();
        List<IParticleEmitter> emitters = scene.getParticleEmitters();

        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

//        Matrix3f aux = new Matrix3f();
        for(IParticleEmitter particleEmitter : emitters)
        {
            IParticleEmitter emitter = particleEmitter;
            InstancedMesh mesh = (InstancedMesh) emitter.getParentParticle().getMesh();

            Texture text = mesh.getMaterial().getTexture();
            particlesShaderProgram.setUniform("numCols", text.getNumCols());
            particlesShaderProgram.setUniform("numRows", text.getNumRows());

            mesh.renderListInstanced(emitter.getParticles(), true, transformation, viewMatrix, null);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(true);

        particlesShaderProgram.unbind();
    }

    private void renderDepthMap(Window window, Camera camera, Scene scene) {
        if (scene.shouldRenderShadows()) {
            // Setup view port to match the texture size
            glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
            glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
            glClear(GL_DEPTH_BUFFER_BIT);

            depthShaderProgram.bind();

            DirectionalLight light = scene.getSceneLights().getDirectionalLight();
            Vector3f lightDirection = light.getDirection();

            float lightAngleX = (float) Math.toDegrees(Math.acos(lightDirection.z));
            float lightAngleY = (float) Math.toDegrees(Math.asin(lightDirection.x));
            float lightAngleZ = 0;
            Matrix4f lightViewMatrix = transformation.updateLightViewMatrix(new Vector3f(lightDirection).mul(light.getShadowPosMult()), new Vector3f(lightAngleX, lightAngleY, lightAngleZ));
            OrthoCoords orthCoords = light.getOrthoCoords();
            Matrix4f orthoProjMatrix = transformation.updateOrthoProjectionMatrix(orthCoords.left, orthCoords.right, orthCoords.bottom, orthCoords.top, orthCoords.near, orthCoords.far);

            depthShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);

            renderNonInstancedMeshes(scene, depthShaderProgram, null, lightViewMatrix);

            renderInstancedMeshes(scene, depthShaderProgram, null, lightViewMatrix);

            // Unbind
            depthShaderProgram.unbind();
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    private void renderSkyBox(Window window, Camera camera, Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox != null) {
            skyBoxShaderProgram.bind();

            skyBoxShaderProgram.setUniform("texture_sampler", 0);

            Matrix4f projectionMatrix = window.getProjectionMatrix();
            skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
            Matrix4f viewMatrix = camera.getViewMatrix();
            float m30 = viewMatrix.m30();
            viewMatrix.m30(0);
            float m31 = viewMatrix.m31();
            viewMatrix.m31(0);
            float m32 = viewMatrix.m32();
            viewMatrix.m32(0);

            Mesh mesh = skyBox.getMesh();
            Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
            skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLights().getSkyBoxLight());
            skyBoxShaderProgram.setUniform("colour", mesh.getMaterial().getAmbientColour());
            skyBoxShaderProgram.setUniform("hasTexture", mesh.getMaterial().isTextured() ? 1 : 0);

            mesh.render();

            viewMatrix.m30(m30);
            viewMatrix.m31(m31);
            viewMatrix.m32(m32);
            skyBoxShaderProgram.unbind();
        }
    }

    public void renderScene(Window window, Camera camera, Scene scene) {
        sceneShaderProgram.bind();

        Matrix4f projectionMatrix = window.getProjectionMatrix();
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f orthoProjMatrix = transformation.getOrthoProjectionMatrix();
        sceneShaderProgram.setUniform("orthoProjectionMatrix", orthoProjMatrix);
        Matrix4f lightViewMatrix = transformation.getLightViewMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();

        SceneLights SceneLights = scene.getSceneLights();
        renderLights(viewMatrix, SceneLights);

        sceneShaderProgram.setUniform("fog", scene.getFog());
        sceneShaderProgram.setUniform("texture_sampler", 0);
        sceneShaderProgram.setUniform("normalMap", 1);
        sceneShaderProgram.setUniform("shadowMap", 2);
        sceneShaderProgram.setUniform("renderShadow", scene.shouldRenderShadows() ? 1 : 0);

        renderNonInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        renderInstancedMeshes(scene, sceneShaderProgram, viewMatrix, lightViewMatrix);

        sceneShaderProgram.unbind();
    }

    private void renderNonInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        sceneShaderProgram.setUniform("isInstanced", 0);

        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = scene.getMeshes();
        for (Mesh mesh : mapMeshes.keySet()) {
            if (viewMatrix != null) {
                shader.setUniform("material", mesh.getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getTextureId());
            }

            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }

            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
//                sceneShaderProgram.setUniform("selectedNonInstanced", gameItem.isSelected() ? 1.0f : 0.0f);
                Matrix4f modelMatrix = transformation.buildModelMatrix(gameItem);
                if (viewMatrix != null) {
                    Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                    sceneShaderProgram.setUniform("modelViewNonInstancedMatrix", modelViewMatrix);
                }
                Matrix4f modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                sceneShaderProgram.setUniform("modelLightViewNonInstancedMatrix", modelLightViewMatrix);

                if (gameItem instanceof AnimGameItem) {
                    AnimGameItem animGameItem = (AnimGameItem) gameItem;
                    AnimatedFrame frame = animGameItem.getCurrentFrame();
                    shader.setUniform("jointsMatrix", frame.getJointMatrices());
                }
            }
            );
        }
    }

    private void renderInstancedMeshes(Scene scene, ShaderProgram shader, Matrix4f viewMatrix, Matrix4f lightViewMatrix) {
        shader.setUniform("isInstanced", 1);

        // Render each mesh with the associated game Items
        Map<InstancedMesh, List<GameItem>> mapMeshes = scene.getInstancedMeshMap();
        for (InstancedMesh mesh : mapMeshes.keySet()) {
            Texture text = mesh.getMaterial().getTexture();
            if (text != null) {
                sceneShaderProgram.setUniform("numCols", text.getNumCols());
                sceneShaderProgram.setUniform("numRows", text.getNumRows());
            }

            if (viewMatrix != null) {
                shader.setUniform("material", mesh.getMaterial());
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, shadowMap.getDepthMapTexture().getTextureId());
            }

            filteredItems.clear();
            for (GameItem gameItem : mapMeshes.get(mesh)) {
                if (gameItem.isInsideFrustum()) {
                    filteredItems.add(gameItem);
                }
            }
            mesh.renderListInstanced(filteredItems, transformation, viewMatrix, lightViewMatrix);
        }
    }

    private void renderLights(Matrix4f viewMatrix, SceneLights SceneLights) {

        sceneShaderProgram.setUniform("ambientLight", SceneLights.getAmbientLight());
        sceneShaderProgram.setUniform("specularPower", specularPower);
        
        int count = 0;

        // Process Point Lights
        List<PointLight> pointLightList = SceneLights.getPointLights();
        for(PointLight pointLight : pointLightList)
        {
            // Get a copy of the point light object and transform its position to view coordinates
            PointLight currPointLight = new PointLight(pointLight);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            sceneShaderProgram.setUniform("pointLights", currPointLight, count);
            count++;
        }
        
        count = 0;

        // Process Spot Ligths
        List<SpotLight> spotLightList = SceneLights.getSpotLights();
        for(SpotLight spotLight : spotLightList)
        {
            // Get a copy of the spot light object and transform its position and cone direction to view coordinates
            SpotLight currSpotLight = new SpotLight(spotLight);
            Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
            dir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

            Vector3f lightPos = currSpotLight.getPointLight().getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;

            sceneShaderProgram.setUniform("spotLights", currSpotLight, count);
            count++;            
        }

        // Get a copy of the directional light object and transform its position to view coordinates
        DirectionalLight currDirLight = new DirectionalLight(SceneLights.getDirectionalLight());
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        sceneShaderProgram.setUniform("directionalLight", currDirLight);
    }

    private void renderCrossHair(Window window) {
        if (window.getWindowOptions().compatibleProfile) {
            glPushMatrix();
            glLoadIdentity();

            float inc = 0.05f;
            glLineWidth(2.0f);

            glBegin(GL_LINES);

            glColor3f(1.0f, 1.0f, 1.0f);

            // Horizontal line
            glVertex3f(-inc, 0.0f, 0.0f);
            glVertex3f(+inc, 0.0f, 0.0f);
            glEnd();

            // Vertical line
            glBegin(GL_LINES);
            glVertex3f(0.0f, -inc, 0.0f);
            glVertex3f(0.0f, +inc, 0.0f);
            glEnd();

            glPopMatrix();
        }
    }

    /**
     * Renders the three axis in space (For debugging purposes only
     *
     * @param camera
     */
    @SuppressWarnings("unused")
	private void renderAxes(Window window, Camera camera) {
        Window.WindowOptions opts = window.getWindowOptions();
        if (opts.compatibleProfile) {
            glPushMatrix();
            glLoadIdentity();
            float rotX = camera.getRotation().x;
            float rotY = camera.getRotation().y;
            float rotZ = 0;
            glRotatef(rotX, 1.0f, 0.0f, 0.0f);
            glRotatef(rotY, 0.0f, 1.0f, 0.0f);
            glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
            glLineWidth(2.0f);

            glBegin(GL_LINES);
            // X Axis
            glColor3f(1.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(1.0f, 0.0f, 0.0f);
            // Y Axis
            glColor3f(0.0f, 1.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 1.0f, 0.0f);
            // Z Axis
            glColor3f(1.0f, 1.0f, 1.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, 1.0f);
            glEnd();

            glPopMatrix();
        }
    }

    public void cleanup() {
        if (shadowMap != null) {
            shadowMap.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
        if (skyBoxShaderProgram != null) {
            skyBoxShaderProgram.cleanup();
        }
        if (sceneShaderProgram != null) {
            sceneShaderProgram.cleanup();
        }
        if (particlesShaderProgram != null) {
            particlesShaderProgram.cleanup();
        }
    }
}
