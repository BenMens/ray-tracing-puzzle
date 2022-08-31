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

// General uniforms
//uniform ivec2 u_resolution;
uniform float u_time;

// Camera uniforms
layout(location = 2) uniform float u_cameraFOV;
layout(location = 3) uniform mat4 u_pointCameraMatrix;
layout(location = 4) uniform mat3 u_vectorCameraMatrix;

// ==================================================================================================================================================
// Structs
// ==================================================================================================================================================
struct Mesh {
  int offset;
  int count;
  int textureIndex;

  vec3 center;
  float radius2;
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

// ==================================================================================================================================================
// Scene
// ==================================================================================================================================================
vec3[5] verticesPos = vec3[5](
  vec3( 0,  0,  0),  // 0
  vec3( 3,  0,  0),  // 1
  vec3( 0,  0,  3),  // 2
  vec3( 0,  3, -1),  // 3
  vec3( 3, -1,  3)   // 4
);

vec2[4] verticesST = vec2[4](
  vec2(0, 0),  // 0
  vec2(1, 0),  // 1
  vec2(0, 1),  // 2
  vec2(1, 1)   // 3
);

vec3[3] verticesN = vec3[3](
  vec3( 0,  1,  0),  // 0
  vec3( 3,  9,  3),  // 1
  vec3( 0,  3,  9)   // 2
);

int[27] indices = int[27](
  // v0, v1, v2, t0, t1, t2, n0, n1, n2
  0, 2, 1, 0, 2, 1, 0, 0, 0,  // 0
  1, 2, 4, 1, 2, 3, 1, 1, 1,  // 1
  0, 1, 3, 0, 1, 2, 2, 2, 2   // 2
);

Mesh[1] meshes = Mesh[1](
  Mesh(0, 3, 0, vec3(0), 19)
);

const Light lights[1] = Light[1](
  //Light(vec3(0, 5, 0), vec3(1, 1, 1), 130, true)
  Light(vec3(0, 1, 0), vec3(1, 1, 1), 1, true)
);


// ==================================================================================================================================================
// Get texture
// ==================================================================================================================================================
vec3 getTexture(vec2 texCoord, int index) {
  if (index == 0) {
    return vec3(1, 0, 1) * ((mod(texCoord.x * 10, 2) < 1 ^^ mod(texCoord.y * 10, 2) < 1) ? 0.5 : 1);
  }

  return vec3(0);
}


// ==================================================================================================================================================
// Intersect sphere
// ==================================================================================================================================================
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

// ==================================================================================================================================================
// Intersect triangle
// ==================================================================================================================================================
bool intersectTriangle(in vec3 origin, in vec3 direction, in vec3 v0, in vec3 v1, in vec3 v2, out float t, out float u, out float v) {
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

// ==================================================================================================================================================
// Ray
// ==================================================================================================================================================
bool ray(in vec3 origin, in vec3 direction, in float tNear, out RayHit hit) {
  bool hasHit = false;

  int textureIndex;
  int indexNear;
  float uNear, vNear;

  float t, u, v;

  // Loop through the meshes
  for(int i = 0; i < meshes.length; i++) {
    Mesh m = meshes[i];

    // If ray comes close to the mesh, check intersection with mesh
    if (intersectSphere(origin, direction, m.center, m.radius2, t) && t < tNear) {

      // Loop through all the triangles making up the mesh
      for (int j = 0; j < m.count; j++) {

        // Check for intersection with the triangle
        int index = (j + m.offset) * 9;
        if (intersectTriangle(origin, direction, 
          verticesPos[indices[index + 0]], 
          verticesPos[indices[index + 1]], 
          verticesPos[indices[index + 2]], t, u, v) && t < tNear) {
          
          // Intersection found
          indexNear = index;
          uNear = u;
          vNear = v;

          tNear = t;
          hasHit = true;
        }  
      }
    }
  }

  vec2 texCoord = 
    verticesST[indices[indexNear + 3]] * uNear +
    verticesST[indices[indexNear + 4]] * vNear +
    verticesST[indices[indexNear + 5]] * (1-uNear-vNear);

  vec3 normal = 
    verticesN[indices[indexNear + 6]] * uNear +
    verticesN[indices[indexNear + 7]] * vNear +
    verticesN[indices[indexNear + 8]] * (1-uNear-vNear);

  hit = createRayHit();
  hit.position = origin + tNear * direction;
  hit.normal = normal;
  hit.direction = direction;
  hit.albedo = getTexture(texCoord, textureIndex);
  hit.dist = tNear;

  return hasHit;
}

// ==================================================================================================================================================
// Main
// ==================================================================================================================================================
void main() {
  for (int i = 0; i < verticesN.length; i++) {
    verticesN[i] = normalize(verticesN[i]);
  }

  // ===================================================================================================================
  // Calculate origin and direction of the camera ray
  // ===================================================================================================================

  // https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays
  vec2 u_resolution = vec2(800, 800);
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

    // ========================================================================================
    // Calculate lighting
    // ========================================================================================
    for(int i = 0; i < lights.length; i++) {
      Light light = lights[i];

      vec3 origin = hit.position + hit.normal * shadow_bias;
      RayHit lightHit;

      if(light.isDistantLight) {
        // Light source is a distant light
        vec3 direction = light.position;

        if(!ray(origin, direction, maxDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor;
        }
      } else {
        // Light source is a spherical light
        vec3 direction = normalize(light.position - origin);
        float lightDist = distance(light.position, origin);

        if(!ray(origin, direction, lightDist, lightHit)) {
          vec3 diffuseColor = albedo / PI * lights[i].intensity * lights[i].color * clamp(dot(hit.normal, direction), 0, 1);
          color += diffuseColor / (4 * PI * lightDist * lightDist);
        }
      }
    }
    //color = albedo * abs(dot(-hit.direction, hit.normal));
  }

  fragColor = vec4(color, 1);
}
