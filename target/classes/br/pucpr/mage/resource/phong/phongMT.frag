#version 330

uniform vec3 uLightDir;

uniform vec3 uAmbientLight;
uniform vec3 uDiffuseLight;
uniform vec3 uSpecularLight;

uniform vec3 uAmbientMaterial;
uniform vec3 uDiffuseMaterial;
uniform vec3 uSpecularMaterial;

uniform float uSpecularPower;

uniform sampler2D uTex0;
uniform sampler2D uTex1;
uniform sampler2D uTex2;
uniform sampler2D uTex3;

in vec3 vNormal;
in vec3 vViewPath;
in vec2 vTexCoord;

in float vDepth;
in vec4 vTexWeight;

out vec4 outColor;

void main() {
    vec3 L = normalize(uLightDir);
	vec3 N = normalize(vNormal);

    vec3 ambient = uAmbientLight * uAmbientMaterial;
    
    float diffuseIntensity = max(dot(N, -L), 0.0);
    vec3 diffuse = diffuseIntensity * uDiffuseLight * uDiffuseMaterial;
       
    //Calculo do componente especular
	float specularIntensity = 0.0;
	if (uSpecularPower > 0.0) {
		vec3 V = normalize(vViewPath);
		vec3 R = reflect(L, N);
		specularIntensity = pow(max(dot(R, V), 0.0), uSpecularPower);
	}
    vec3 specular = specularIntensity * uSpecularLight * uSpecularMaterial;

    float blendFactor = clamp((vDepth - 0.99) * 100.0, 0.0, 1.0);

    vec2 farCoord = vTexCoord * 10.0;
    vec4 texelFar = texture(uTex0, farCoord) * vTexWeight.x +
                    texture(uTex1, farCoord) * vTexWeight.y +
                    texture(uTex2, farCoord) * vTexWeight.z +
                    texture(uTex3, farCoord) * vTexWeight.w;

    vec2 nearCoord = vTexCoord * 50.0;
    vec4 texelNear = texture(uTex0, nearCoord) * vTexWeight.x +
                     texture(uTex1, nearCoord) * vTexWeight.y +
                     texture(uTex2, nearCoord) * vTexWeight.z +
                     texture(uTex3, nearCoord) * vTexWeight.w;

    vec4 texel = mix(texelNear, texelFar, blendFactor);

    vec3 color = clamp(texel.rgb * (ambient + diffuse) + specular, 0.0, 1.0);
    outColor = vec4(color, texel.a);
}