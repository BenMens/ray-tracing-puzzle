#version 430

layout(location=0) in vec3 vertexPosition;
layout(location=1) in vec4 vertexColor;

// Camera uniforms
layout(location=0) uniform vec3 u_cameraPosition;
layout(location=1) uniform vec3 u_cameraDirection;
layout(location=2) uniform float u_cameraFOV;
layout(location=3) uniform mat4 u_pointCameraMatrix;
layout(location=4) uniform mat3 u_vectorCameraMatrix;


layout(binding=0) buffer shaderBuf
{
    vec4 blendColor;
};

out highp vec4 color;

void main() {
  gl_Position = vec4(vertexPosition, 1.0);

  color = vertexColor;
}