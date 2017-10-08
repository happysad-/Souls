#version 300 es

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 vertexNormal;

out vec2 outTexCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

uniform float textureXOffset;
uniform float textureYOffset;
uniform int numCols;
uniform int numRows;

void main()
{
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
	
	float x = (texCoord.x / float(numCols) + textureXOffset);
	float y = (texCoord.y / float(numRows) + textureYOffset);
	
	outTexCoord = vec2(x, y);
}