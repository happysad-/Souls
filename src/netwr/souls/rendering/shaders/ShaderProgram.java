package netwr.souls.rendering.shaders;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import netwr.souls.rendering.Fog;
import netwr.souls.rendering.Material;
import netwr.souls.rendering.lights.DirectionalLight;
import netwr.souls.rendering.lights.PointLight;
import netwr.souls.rendering.lights.SpotLight;

public class ShaderProgram
{
	private final int programId;
	
	private int vertexShaderId;
	private int fragmentShaderId;
	
	private final Map<String, Integer> uniforms;
	
	public ShaderProgram() throws Exception
	{
		programId = GL20.glCreateProgram();
		
		uniforms = new HashMap<String, Integer>();
		
		if(programId == 0)
			throw new Exception("Could not create a shader program!");
	}
	
	public void createUniform(String uniformName) throws Exception
	{
		int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
		
		if(uniformLocation < 0)
			throw new Exception("Could not find uniform: " + uniformName);
		
		uniforms.put(uniformName, uniformLocation);
		
		System.out.println("Added uniform: " + uniformName);
	}
	
	public void setUniform(String uniformName, Matrix4f value)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			FloatBuffer fb = stack.mallocFloat(16);
			value.get(fb);
			GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}
	
	public void setUniform(String uniformName, Vector3f value)
	{
		GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}
	
	public void setUniform(String uniformName, Vector4f value)
	{
		GL20.glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
	}
	
	public void setUniform(String uniformName, float value)
	{
		GL20.glUniform1f(uniforms.get(uniformName), value);
	}
	
	public void setUniform(String uniformName, int value)
	{
		GL20.glUniform1i(uniforms.get(uniformName), value);
	}
	
	public void createPointLightUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".colour");
		createUniform(uniformName + ".position");
		createUniform(uniformName + ".intensity");
		createUniform(uniformName + ".att.constant");
		createUniform(uniformName + ".att.linear");
		createUniform(uniformName + ".att.exponent");
	}
	
	public void createMaterialUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".ambient");
		createUniform(uniformName + ".diffuse");
		createUniform(uniformName + ".specular");
		createUniform(uniformName + ".hasTexture");
		createUniform(uniformName + ".reflectance");
	}
	
	public void createDirectionalLightUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".colour");
		createUniform(uniformName + ".direction");
		createUniform(uniformName + ".intensity");
	}
	
	public void createSpotLightUniform(String uniformName) throws Exception
	{
		createPointLightUniform(uniformName + ".pl");
		createUniform(uniformName + ".conedir");
		createUniform(uniformName + ".cutoff");
	}
	
	public void createPointLightListUniform(String uniformName, int size) throws Exception
	{
		for(int i = 0; i < size; i++)
			createPointLightUniform(uniformName + "[" + i + "]");
	}
	
	public void createSpotLightListUniform(String uniformName, int size) throws Exception
	{
		for(int i = 0; i < size; i++)
			createSpotLightUniform(uniformName + "[" + i + "]");
	}
	
	public void createFogUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".activeFog");
		createUniform(uniformName + ".colour");
		createUniform(uniformName + ".density");
	}
	
	public void setUniform(String uniformName, Fog fog)
	{
		setUniform(uniformName + ".activeFog", fog.isActive() ? 1 : 0);
		setUniform(uniformName + ".colour", fog.getColour());
		setUniform(uniformName + ".density", fog.getDensity());
	}
	
	public void setUniform(String uniformName, PointLight pointLight)
	{
		setUniform(uniformName + ".colour", pointLight.getColour());
		setUniform(uniformName + ".position", pointLight.getPosition());
		setUniform(uniformName + ".intensity", pointLight.getIntensity());
		setUniform(uniformName + ".att.constant", pointLight.getAttenuation().getConstant());
		setUniform(uniformName + ".att.linear", pointLight.getAttenuation().getLinear());
		setUniform(uniformName + ".att.exponent", pointLight.getAttenuation().getExponent());
	}
	
	public void setUniform(String uniformName, Material material)
	{		
		setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
	}
	
	public void setUniform(String uniformName, DirectionalLight light)
	{
		setUniform(uniformName + ".colour", light.getColour());
		setUniform(uniformName + ".direction", light.getDirection());
		setUniform(uniformName + ".intensity", light.getIntensity());
	}
	
	public void setUniform(String uniformName, SpotLight light)
	{
		setUniform(uniformName + ".pl", light.getPointLight());
		setUniform(uniformName + ".conedir", light.getConeDirection());
		setUniform(uniformName + ".cutoff", light.getCutOff());
	}
	
	public void setUniform(String uniformName, PointLight[] pointLights)
	{
	    int numLights = pointLights != null ? pointLights.length : 0;
	    
	    for (int i = 0; i < numLights; i++)
	        setUniform(uniformName, pointLights[i], i);
	}

	public void setUniform(String uniformName, PointLight pointLight, int pos)
	{
	    setUniform(uniformName + "[" + pos + "]", pointLight);
	}

	public void setUniform(String uniformName, SpotLight[] spotLights)
	{
	    int numLights = spotLights != null ? spotLights.length : 0;
	    
	    for (int i = 0; i < numLights; i++)
	        setUniform(uniformName, spotLights[i], i);
	}

	public void setUniform(String uniformName, SpotLight spotLight, int pos)
	{
	    setUniform(uniformName + "[" + pos + "]", spotLight);
	}
	
	public void setUniform(String uniformName, Matrix4f[] matrices)
	{
		try (MemoryStack stack = MemoryStack.stackPush()) {
            int length = matrices != null ? matrices.length : 0;
            FloatBuffer fb = stack.mallocFloat(16 * length);
            for (int i = 0; i < length; i++)
            {
                matrices[i].get(16 * i, fb);
            }
            GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}
	
	public void createVertexShader(String shaderCode) throws Exception
	{
		vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
	}
	
	public void createFragmentShader(String shaderCode) throws Exception
	{
		fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
	}
	
	private int createShader(String shaderCode, int shaderType) throws Exception
	{
		int shaderId = GL20.glCreateShader(shaderType);
		
		if(shaderId == 0)
			throw new Exception("Error creating shader. Code: " + shaderId);
		
		GL20.glShaderSource(shaderId, shaderCode);
		GL20.glCompileShader(shaderId);
		
		if(GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0)
			throw new Exception("Error compiling shader code. Info: " + GL20.glGetShaderInfoLog(shaderId, 1024));
		
		GL20.glAttachShader(programId, shaderId);
		
		return shaderId;
	}
	
	public void link() throws Exception
	{
		GL20.glLinkProgram(programId);
		
		if(GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0)
			throw new Exception("Error linking shader code! Info: " + GL20.glGetProgramInfoLog(programId, 1024));
	

        if (vertexShaderId != 0)
            GL20.glDetachShader(programId, vertexShaderId);
        
        if (fragmentShaderId != 0)
            GL20.glDetachShader(programId, fragmentShaderId);
        
		
		GL20.glValidateProgram(programId);
		
		if(GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0)
			System.err.println("Warning thrown validating shader code. Info: " + GL20.glGetProgramInfoLog(programId, 1024));
	}
	
	public void bind()
	{
		GL20.glUseProgram(programId);
	}
	
	public void unbind()
	{
		GL20.glUseProgram(0);
	}
	
	public void cleanup()
	{
		unbind();
		
		if(programId != 0)
		{			
			GL20.glDeleteProgram(programId);
		}
	}
}
