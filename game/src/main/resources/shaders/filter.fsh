
in vec2 uv;

layout (binding = 0) uniform sampler2D albedo_buffer;
layout (binding = 1) uniform sampler2D depth_buffer;

uniform float millis;

out vec4 out_Color;

float base_color (in vec2 uv) {
    vec4 albedo = texture(albedo_buffer, uv);
    vec2 screen_size = textureSize(albedo_buffer, 0);
    float value = albedo.x;
    float lines = abs(sin(uv.x * screen_size.x + millis * 0.5)) * 4 - 2.5;
    float grain = random(uv + mod(millis, 200) - 100);
    vec3 color = vec3(0.0, 0.8, 1.0);
    vec3 c = value * (mix(vec3(0.0, 0.3, 1.0) * lines, vec3(0.0, 0.8, 1.0), value) + grain * vec3(0.0, 1.0, 1.0) * 0.4);
    float l = value * ((max(lines, 0.0) + 0.7) + grain);
    return l;
}

vec2 chromatic_aberration () {
    return vec2(
        cos(millis / mod(millis, 1000)),
        sin(millis / mod(millis, 1000))
    );
}

void main () {
    float base = base_color(uv);
    vec2 screen_size = textureSize(albedo_buffer, 0);
    float glitchiness = sin(mod(millis, 4600));
    glitchiness *= glitchiness * (length(uv - .5) * 3.0 + 2.0);
    glitchiness += 1.0 - 2.0 * abs(base - 0.5);
    vec2 aberration = chromatic_aberration() / screen_size * glitchiness;
    float r = base_color(uv + aberration);
    float g = base_color(uv - aberration);
    float b = base_color(uv - aberration) + base_color(uv + aberration);
    vec3 color = vec3(r, g, b);
    out_Color = vec4(color, 1.0);
}