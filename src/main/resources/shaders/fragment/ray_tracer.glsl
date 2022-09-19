#version 430

// Settings
const float maxDist = 1200;
const float shadow_bias = 1e-1;

const vec3 skyColor = vec3(0, 0.3, 0.8);

// Constants
const float PI = 3.141592653589793;
const float TAU = 6.283185307179586;
const float EPSILON = 1e-4;

// Output
out vec4 fragColor;

// uniforms
layout(location = 2) uniform float u_cameraFOV;
layout(location = 3) uniform mat4 u_pointCameraMatrix;
layout(location = 4) uniform mat3 u_vectorCameraMatrix;
layout(location = 5) uniform ivec2 u_resolution;
layout(location = 6) uniform float u_time;


// =====================================================================================================================
// Structs
// =====================================================================================================================
struct MeshInstance {
  mat4 inverseModelMatrix;     //    0
  mat4 normalMatrix;    //   64
  vec4 center;          //  128
  float radius2;        //  144
  int facesOffset;      //  148
  int facesCount;       //  152
  int textureIndex;     //  156
  int verticesOffset;   //  160
  int normalsOffset;    //  164
  int verticesSTOffset; //  168
};


struct RayHit {
  vec3 position;
  vec3 normal;
  vec3 direction;

  vec3 albedo;

  float dist;
};

RayHit createRayHit() {
  return RayHit(vec3(0), vec3(0, 1, 0), vec3(0, -1, 0), vec3(0), 0);
}


struct Light {
  vec3 position;  // If distant light, normalize

  vec3 color;
  float intensity;

  bool isDistantLight;
};

struct Vertex {
  int indexPos;
  int indexST;
  int indexNormal;
};

// =====================================================================================================================
// SSBOs
// =====================================================================================================================

layout(std430, binding = 0) buffer verticesPosBufferLayout {
  vec4 verticesPos[];
};

layout(std430, binding = 1) buffer verticesSTBufferLayout {
  vec2 verticesST[];
};

layout(std430, binding = 2) buffer normalsBufferLayout {
  vec4 verticesN[];
};

layout(std430, binding = 3) buffer facesBufferLayout {
  Vertex faces[][3];
};

layout(std430, binding = 4) buffer meshesBufferLayout {
  MeshInstance meshInstances[];
};

// =====================================================================================================================
// Scene
// =====================================================================================================================

const Light lights[1] = Light[1](
  //Light(vec3(0, 5, 0), vec3(1, 1, 1), 130, true)
  Light(vec3(0, 1, 0), vec3(1, 1, 1), 1, true)
);


// =====================================================================================================================
// Get texture
// =====================================================================================================================
vec3 getTexture(vec2 texCoord, int index) {
  if (index == 0) {
    return vec3(1, 0, 1) * ((mod(texCoord.x * 10, 2) < 1 ^^ mod(texCoord.y * 10, 2) < 1) ? 0.5 : 1);
  }

  return vec3(0);
}


// =====================================================================================================================
// Intersect sphere
// =====================================================================================================================
bool intersectSphere(in vec3 origin, in vec3 direction, in vec3 center, in float radius2, out float t) {
  float t0, t1;

  vec3 l = center - origin;

  if(dot(l, l) < radius2) {
    t = -1;
    return true;
  }

  float tca = dot(l, direction);
  if(tca < 0)
    return false;

  float d2 = dot(l, l) - tca * tca;
  if(d2 > radius2)
    return false;

  float thc = sqrt(radius2 - d2);
  t0 = tca - thc;
  t1 = tca + thc;

  if(t0 < 0)
    t0 = t1;

  if(t1 < 0) {
    t1 = t0;

    if(t1 < 0)
      return false;
  }

  t = min(t0, t1);

  return true;
}

// =====================================================================================================================
// Intersect triangle
// =====================================================================================================================
bool intersectTriangle(
    in vec3 origin, 
    in vec3 direction, 
    in vec3 v0, 
    in vec3 v1, 
    in vec3 v2, 
    out float t, 
    out float u, 
    out float v) {

  vec3 v2v0 = v0 - v2;
  vec3 v2v1 = v1 - v2;
  vec3 pvec = cross(direction, v2v1);
  float det = dot(v2v0, pvec);

  if(det < EPSILON)
    return false;

  float invDet = 1 / det; 
 
  vec3 tvec = origin - v2; 
  u = dot(tvec, pvec) * invDet; 
  if (u < 0 || u > 1) return false; 
  vec3 qvec = cross(tvec, v2v0);
  v = dot(direction, qvec) * invDet; 
  if (v < 0 || u + v > 1) return false; 

  t = dot(v2v1, qvec) * invDet;

  return t >= 0;
}

// =====================================================================================================================
// Ray
// =====================================================================================================================
bool ray(in vec3 origin, in vec3 direction, in float tNear, out RayHit hit) {
  bool hasHit = false;

  int textureIndex;
  int indexNear;
  int meshIndexNear;
  float uNear, vNear;

  float t, u, v;

  // Loop through the meshes
  for(int i = 0; i < meshInstances.length(); i++) {
    
    MeshInstance m = meshInstances[i];

    if (m.facesCount == 0) {
      continue;
    }

    vec3 modelOrigin = (m.inverseModelMatrix * vec4(origin, 1)).xyz;
    vec3 modeldirection = (m.inverseModelMatrix * vec4(direction, 0)).xyz;

    // If ray comes close to the mesh, check intersection with mesh
    if (intersectSphere(modelOrigin, modeldirection, m.center.xyz, m.radius2, t) && t < tNear) {

      // Loop through all the triangles making up the mesh
      for (int j = 0; j < m.facesCount; j++) {

        // Check for intersection with the triangle
        if (intersectTriangle(
              modelOrigin, 
              modeldirection, 
              verticesPos[m.verticesOffset + faces[m.facesOffset + j][0].indexPos].xyz, 
              verticesPos[m.verticesOffset + faces[m.facesOffset + j][1].indexPos].xyz, 
              verticesPos[m.verticesOffset + faces[m.facesOffset + j][2].indexPos].xyz, 
              t, u, v
            ) && t < tNear) {
          
          // Intersection found
          meshIndexNear = i;
          indexNear = j;
          uNear = u;
          vNear = v;

          tNear = t;
          hasHit = true;
        }  
      }
    }
  }

  MeshInstance m = meshInstances[meshIndexNear];
  
  vec2 texCoord = 
    verticesST[m.verticesSTOffset + faces[m.facesOffset + indexNear][0].indexST] * uNear +
    verticesST[m.verticesSTOffset + faces[m.facesOffset + indexNear][1].indexST] * vNear +
    verticesST[m.verticesSTOffset + faces[m.facesOffset + indexNear][2].indexST] * (1-uNear-vNear);

  vec3 normal = (m.normalMatrix * (
    verticesN[m.normalsOffset + faces[m.facesOffset + indexNear][0].indexNormal] * uNear +
    verticesN[m.normalsOffset + faces[m.facesOffset + indexNear][1].indexNormal] * vNear +
    verticesN[m.normalsOffset + faces[m.facesOffset + indexNear][2].indexNormal] * (1-uNear-vNear))).xyz;

  hit = createRayHit();
  hit.position = origin + tNear * direction;
  hit.normal = normal;
  hit.direction = direction;
  hit.albedo = getTexture(texCoord, textureIndex);
  hit.dist = tNear;

  return hasHit;
}

// =====================================================================================================================
// Main
// =====================================================================================================================
void main() {
  
  for (int i = 0; i < verticesN.length(); i++) {
    verticesN[i].xyz = normalize(verticesN[i].xyz);
  }

  // ===================================================================================================================
  // Calculate origin and direction of the camera ray
  // ===================================================================================================================

  // https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays
  vec2 PixelCoordScreenSpace = (2 * gl_FragCoord.xy - u_resolution) / u_resolution.y;
  vec2 PixelCoordImageSpace = PixelCoordScreenSpace * tan(u_cameraFOV / 2);
  vec3 PixelCoordCameraSpace = vec3(PixelCoordImageSpace.xy, -1);

  vec3 rayOrigin = (u_pointCameraMatrix * vec4(0, 0, 0, 1)).xyz;
  vec3 rayDirection = u_vectorCameraMatrix * normalize(PixelCoordCameraSpace);

  // ===================================================================================================================
  // Calculate fragment color
  // ===================================================================================================================
  vec3 color = skyColor;
  RayHit hit;

  if(ray(rayOrigin, rayDirection, maxDist, hit)) {
    vec3 albedo = hit.albedo;
    color = vec3(0);

    // =================================================================================================================
    // Calculate lighting
    // =================================================================================================================
    for(int i = 0; i < lights.length; i++) {
      Light light = lights[i];

      vec3 origin = hit.position + hit.normal * shadow_bias;
      RayHit lightHit;

      if(light.isDistantLight) {
        // Light source is a distant light
        vec3 direction = light.position;

        if(!ray(origin, direction, maxDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * 
            lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor;
        }
      } else {
        // Light source is a spherical light
        vec3 direction = normalize(light.position - origin);
        float lightDist = distance(light.position, origin);

        if(!ray(origin, direction, lightDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * 
            lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor / (4 * PI * lightDist * lightDist);
        }
      }
    }

    // color = albedo * abs(dot(-hit.direction, hit.normal));

    color += albedo * 0.2;
  }

  fragColor = vec4(color, 1);
  //fragColor = vec4(verticesPos[0].xyz, 1);
}
