#version 460

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;

out vec4 fragColor;

//razdalj od izvora luci in objekata
struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

// vir tocka svetlobe
struct PointLight
{
    vec3 colour;
    vec3 position;
    float intensity;
    Attenuation att;

};

//spot light
struct SpotLight
{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};

//sonce oz. global light
struct DirectionalLight
{
    vec3 colour;
    vec3 direction;
    float intensity;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLight;
//ohranjeno za poznejšno delo
uniform vec3 camera_pos;

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

void setupColours(Material material, vec2 textCoord)
{
    if(material.hasTexture == 1){
        ambientC = texture(texture_sampler, textCoord);
        diffuseC = ambientC;
        speculrC = ambientC;
    }
    else{
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }

}

//izračun barve za svetlobo
vec4 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColour = vec4(0, 0, 0, 0);
    vec4 specColour = vec4(0, 0, 0, 0);


    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = diffuseC * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;


    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * light_intensity  * specularFactor * material.reflectance * vec4(light_colour, 1.0);

    return (diffuseColour + specColour);
}

//izračun razdalje za svetlobo v točki
vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_colour = calcLightColour(light.colour, light.intensity, position, to_light_dir, normal);

    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}

//izračun spot light
vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec3 from_light_dir  = -to_light_dir;
    float spot_alfa = dot(from_light_dir, normalize(light.conedir));

    vec4 colour = vec4(0, 0, 0, 0);

    if ( spot_alfa > light.cutoff )
    {
        colour = calcPointLight(light.pl, position, normal);
        colour *= (1.0 - (1.0 - spot_alfa)/(1.0 - light.cutoff));
    }
    return colour;
}

//izračun za globalno svetlobo oz. "sonec"
vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
    return calcLightColour(light.colour, light.intensity, position, normalize(light.direction), normal);
}

void main()
{
    setupColours(material, outTexCoord);

    vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, mvVertexPos, mvVertexNormal);
    for (int i=0;i<MAX_POINT_LIGHTS;i++){
        if(pointLights[i].intensity > 0){
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, mvVertexNormal);
        }
    }

    for (int i=0;i<MAX_SPOT_LIGHTS;i++){
        if(spotLights[i].pl.intensity > 0){
            diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, mvVertexNormal);
        }
    }
    fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;
}