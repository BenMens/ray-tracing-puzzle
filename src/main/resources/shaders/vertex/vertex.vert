layout(location=0) in vec3 vertexPosition;
layout(location=1) in vec4 vertexColor;
layout(binding=3) buffer shaderBuf
{
    vec4 blendColor;
};

out highp vec4 color;

void main() {
  gl_Position = vec4(vertexPosition, 1.0);

  color = vertexColor;
}