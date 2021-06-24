in vec2 uv;

layout (binding = 0) uniform sampler2D albedo_buffer;
layout (binding = 1) uniform sampler2D depth_buffer;

uniform float millis;

out vec4 out_Color;

float base_color (in vec2 uv) {
    vec4 albedo = texture(albedo_buffer, uv);
    vec2 screen_size = textureSize(albedo_buffer, 0);
    float value = albedo.r;
    float glitch = albedo.g;
    float lines = abs(sin(uv.x * screen_size.x + millis * 0.5)) * 4 - 2.5;
    float grain = random(uv + mod(millis, 200) - 100);
    float l = value * ((max(lines, 0.0) + 0.7) + grain) + glitch * grain * 3.0;
    return l;
}

vec2 chromatic_aberration () {
    return vec2(
        cos(millis / mod(millis, 1000)),
        sin(millis / mod(millis, 1000))
    );
}

float sparkle (in vec2 uv) {
    vec2 resolution = textureSize(albedo_buffer, 0);
    vec2 specBase = vec2(random(uv), random(uv + 1.0)) - 0.5;
    vec2 offset = specBase / 4 * vec2(1.0, resolution.x / resolution.y);
    float color = base_color(uv + offset) * pow(max(1.0 - length(specBase) * 2.0, 0.0), 3.0) * 0.2;
    return color;
}

float base_full (in vec2 uv) {
    return base_color(uv) + sparkle(uv);
}

void main () {
    float individual_glitch = texture(albedo_buffer, uv).g;
    float base = base_full(uv) * (1.0 + individual_glitch * .5);
    vec2 screen_size = textureSize(albedo_buffer, 0);
    float glitchiness = sin(mod(millis, 4600));
    glitchiness *= glitchiness * (length(uv - .5) * 4.0 + 1.0);
    glitchiness += 1.0 - 2.0 * abs(base - 0.5);
    glitchiness += individual_glitch * 12.0;
    vec2 aberration = chromatic_aberration() / screen_size * glitchiness;
    float r = base_full(uv + aberration) * (1.0 - individual_glitch * 0.5);
    float g = base_full(uv - aberration) * (1.0 - individual_glitch);
    float b = base_full(uv - aberration) + base_full(uv + aberration) * (1.0 + individual_glitch);
    vec3 color = vec3(r, g, b);
    out_Color = vec4(color, 1.0);
}