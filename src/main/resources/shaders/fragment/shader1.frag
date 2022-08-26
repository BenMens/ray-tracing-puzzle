#version 430

in highp vec4 color;

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

out vec4 out_Color;

void main() {
  out_Color = (color + blendColor) / 2;
}