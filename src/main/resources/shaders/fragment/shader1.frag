#version 430

in highp vec4 color;
layout(binding=3) buffer shaderBuf
{
    vec4 blendColor;
};

void main() {
  gl_FragColor = (color + blendColor) / 2;
}