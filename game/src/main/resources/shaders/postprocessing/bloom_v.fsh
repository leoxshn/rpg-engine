in vec2 uv;

layout (binding = 0) uniform sampler2D albedo_buffer;
layout (binding = 1) uniform sampler2D blur_h_buffer;
layout (binding = 2) uniform sampler2D depth_buffer;

uniform vec2 resolution;

out vec4 out_Color;

vec4 blur13(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
    vec4 color = vec4(0.0);
    vec2 off1 = vec2(1.411764705882353) * direction;
    vec2 off2 = vec2(3.2941176470588234) * direction;
    vec2 off3 = vec2(5.176470588235294) * direction;
    color += texture2D(image, uv) * 0.1964825501511404;
    color += texture2D(image, uv + (off1 / resolution)) * 0.2969069646728344;
    color += texture2D(image, uv - (off1 / resolution)) * 0.2969069646728344;
    color += texture2D(image, uv + (off2 / resolution)) * 0.09447039785044732;
    color += texture2D(image, uv - (off2 / resolution)) * 0.09447039785044732;
    color += texture2D(image, uv + (off3 / resolution)) * 0.010381362401148057;
    color += texture2D(image, uv - (off3 / resolution)) * 0.010381362401148057;
    return color;
}

void main () {
    vec3 color = texture(albedo_buffer, uv).rgb;
    vec3 blur = blur13(blur_h_buffer, uv, resolution, vec2(0.0, 1.0)).rgb;
    float dist = length(uv * 2.0 - 1.0);
    float vignette = 1.0 - clamp(dist * dist, 0.0, 1.0);
    out_Color = vec4(color * vignette + min(blur * 1.6, .4), 1.0);
}