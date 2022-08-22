layout(location=0) attribute vec3 vertexPosition;
layout(location=1) attribute vec4 vertexColor;

varying highp vec4 color;

void main() {
  gl_Position = vec4(vertexPosition, 1.0);
  color = vertexColor;
}