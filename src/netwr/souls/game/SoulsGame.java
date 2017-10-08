package netwr.souls.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import de.matthiasmann.twl.utils.PNGDecoder;
import netwr.souls.MouseInput;
import netwr.souls.Window;
import netwr.souls.logic.GameItem;
import netwr.souls.logic.IGameLogic;
import netwr.souls.logic.Scene;
import netwr.souls.rendering.Camera;
import netwr.souls.rendering.Fog;
import netwr.souls.rendering.Material;
import netwr.souls.rendering.Mesh;
import netwr.souls.rendering.OBJLoader;
import netwr.souls.rendering.Renderer;
import netwr.souls.rendering.SkyBox;
import netwr.souls.rendering.Texture;
import netwr.souls.rendering.lights.DirectionalLight;
import netwr.souls.rendering.lights.SceneLights;
import netwr.souls.terrain.HeightMapMesh;
import netwr.souls.terrain.Terrain;

public class SoulsGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private HUD hud;

    private static final float CAMERA_POS_STEP = 0.10f;

    private Terrain terrain;

    private float angleInc;

    private float lightAngle;

//    private FlowParticleEmitter particleEmitter;

    private float skyBoxScale = 100.0f;

    public SoulsGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 45;
    }
    
    @Override
    public void init(Window window) throws Exception
    {
    	renderer.init(window);
    	
    	scene = new Scene();
    	
    	float reflectance = 1f;
    	
    	Mesh cubeMesh = OBJLoader.loadMesh("models/cube.obj");
    	Texture cubeTexture = new Texture("textures/terrain_textures.png", 2, 1);
    	Material cubeMaterial = new Material(cubeTexture, reflectance);
    	cubeMesh.setMaterial(cubeMaterial);
    	
    	GameItem cubeItem = new GameItem(cubeMesh);
    	cubeItem.setPosition(0, 0, 0);
    	
    	List<GameItem> gameItemList = new ArrayList<GameItem>();
    	
    	gameItemList.add(cubeItem);
    	
    	scene.setGameItems(gameItemList);
    	
    	float blockScale = 0.5f;
      float skyBoxScale = 100.0f;
      float extension = 2.0f;

      float startx = extension * (-skyBoxScale + blockScale);
      float startz = extension * (skyBoxScale - blockScale);
      float starty = -1.0f;
      float inc = blockScale * 2;

      float posx = startx;
      float posz = startz;
      float incy = 0.0f;

      PNGDecoder decoder = new PNGDecoder(new FileInputStream("textures/heightmap.png"));
      int height = decoder.getHeight();
      int width = decoder.getWidth();
      ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
      decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
      buf.flip();

      int instances = height * width;
      Mesh mesh = OBJLoader.loadMesh("models/cube.obj", instances);
      Texture texture = new Texture("textures/terrain_textures.png", 2, 1);
      Material material = new Material(texture, reflectance);
      mesh.setMaterial(material);
      List<GameItem> gameItems = new ArrayList<GameItem>(instances);
      for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
              GameItem gameItem = new GameItem(mesh);
              gameItem.setScale(blockScale);
              int rgb = HeightMapMesh.getRGB(i, j, width, buf);
              incy = rgb / (10 * 255 * 255);
              gameItem.setPosition(posx, starty + incy, posz);
              int textPos = Math.random() > 0.5f ? 0 : 1;
              gameItem.setTextPos(textPos);
              gameItems.add(gameItem);

              posx += inc;
          }
          posx = startx;
          posz -= inc;
      }
      scene.setGameItems(gameItems);
    	
    	// Shadows
      scene.shouldRenderShadows(false);

      // Fog
      Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
      scene.setFog(new Fog(true, fogColour, 0.02f));

      // Setup  SkyBox
      SkyBox skyBox = new SkyBox("models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
      skyBox.setScale(skyBoxScale);
      scene.setSkyBox(skyBox);

      // Setup Lights
      setupLights();

      camera.getPosition().x = 0.25f;
      camera.getPosition().y = 6.5f;
      camera.getPosition().z = 6.5f;
      camera.getRotation().x = 25;
      camera.getRotation().y = -1;

      hud = new HUD("DEMO");
    }

//    @Override
//    public void init(Window window) throws Exception {
//        renderer.init(window);
//
//        scene = new Scene();
//
//        float reflectance = 1f;
//
//        float blockScale = 0.5f;
//        float skyBoxScale = 100.0f;
//        float extension = 2.0f;
//
//        float startx = extension * (-skyBoxScale + blockScale);
//        float startz = extension * (skyBoxScale - blockScale);
//        float starty = -1.0f;
//        float inc = blockScale * 2;
//
//        float posx = startx;
//        float posz = startz;
//        float incy = 0.0f;
//
//        PNGDecoder decoder = new PNGDecoder(new FileInputStream("textures/heightmap.png"));
//        int height = decoder.getHeight();
//        int width = decoder.getWidth();
//        ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
//        decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
//        buf.flip();
//
//        int instances = height * width;
//        Mesh mesh = OBJLoader.loadMesh("models/cube.obj", instances);
//        Texture texture = new Texture("textures/terrain_textures.png", 2, 1);
//        Material material = new Material(texture, reflectance);
//        mesh.setMaterial(material);
//        List<GameItem> gameItems = new ArrayList<GameItem>(instances);
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                GameItem gameItem = new GameItem(mesh);
//                gameItem.setScale(blockScale);
//                int rgb = HeightMapMesh.getRGB(i, j, width, buf);
//                incy = rgb / (10 * 255 * 255);
//                gameItem.setPosition(posx, starty + incy, posz);
//                int textPos = Math.random() > 0.5f ? 0 : 1;
//                gameItem.setTextPos(textPos);
//                gameItems.add(gameItem);
//
//                posx += inc;
//            }
//            posx = startx;
//            posz -= inc;
//        }
//        scene.setGameItems(gameItems);
//
//        // Particles
//        int maxParticles = 200;
//        Vector3f particleSpeed = new Vector3f(0, 1, 0);
//        particleSpeed.mul(2.5f);
//        long ttl = 4000;
//        long creationPeriodMillis = 300;
//        float range = 0.2f;
//        float scale = 0.2f;
//        Mesh partMesh = OBJLoader.loadMesh("models/particle.obj", maxParticles);
//        Texture particleTexture = new Texture("textures/particle_anim.png", 4, 4);
//        Material partMaterial = new Material(particleTexture, reflectance);
//        partMesh.setMaterial(partMaterial);
//        Particle particle = new Particle(partMesh, particleSpeed, ttl, 100);
//        particle.setScale(scale);
//        particleEmitter = new FlowParticleEmitter(particle, maxParticles, creationPeriodMillis);
//        particleEmitter.setActive(true);
//        particleEmitter.setPositionRndRange(range);
//        particleEmitter.setSpeedRndRange(range);
//        particleEmitter.setAnimRange(10);
//        
//        List<IParticleEmitter> particleEmitters = new ArrayList<IParticleEmitter>();
//        
//        particleEmitters.add(particleEmitter);
//        
//        this.scene.setParticleEmitters(particleEmitters);
//
//        // Shadows
//        scene.shouldRenderShadows(false);
//
//        // Fog
//        Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
//        scene.setFog(new Fog(true, fogColour, 0.02f));
//
//        // Setup  SkyBox
//        SkyBox skyBox = new SkyBox("models/skybox.obj", new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
//        skyBox.setScale(skyBoxScale);
//        scene.setSkyBox(skyBox);
//
//        // Setup Lights
//        setupLights();
//
//        camera.getPosition().x = 0.25f;
//        camera.getPosition().y = 6.5f;
//        camera.getPosition().z = 6.5f;
//        camera.getRotation().x = 25;
//        camera.getRotation().y = -1;
//
//        hud = new HUD("DEMO");
//        
//        
//    }

    private void setupLights() {
        SceneLights sceneLight = new SceneLights();
        scene.setSceneLights(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        directionalLight.setShadowPosMult(10);
        directionalLight.setOrthoCoords(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            cameraInc.y = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            angleInc -= 0.05f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            angleInc += 0.05f;
        } else {
            angleInc = 0;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput, Window window) {
        // Update camera based on mouse            
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update camera position
        Vector3f prevPos = new Vector3f(camera.getPosition());
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        // Check if there has been a collision. If true, set the y position to
        // the maximum height
        float height = terrain != null ? terrain.getHeight(camera.getPosition()) : -Float.MAX_VALUE;
        if (camera.getPosition().y <= height) {
            camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }

        lightAngle += angleInc;
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLights().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();
        
        System.out.println("CAMERA[" + camera.getPosition().x + " | " + camera.getPosition().y + " | " + camera.getPosition().z + "]");

//        particleEmitter.update((long) (interval * 1000));
    }

    @Override
    public void render(Window window) {
        if (hud != null) {
            hud.updateSize(window);
        }
        renderer.render(window, camera, scene);
//        hud.render(window);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        scene.cleanup();
        if (hud != null) {
            hud.cleanup();
        }
    }
}
