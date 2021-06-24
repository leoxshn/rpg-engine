
in vec2 uv;

layout (binding = 0) uniform sampler2D albedo_buffer;
layout (binding = 1) uniform sampler2D depth_buffer;

uniform float millis;

out vec4 out_Color;

void main () {
    vec2 res = textureSize(albedo_buffer, 0);
    vec2 block = floor(uv * res / vec2(16));
    vec2 uv_noise = block / vec2(64);
    uv_noise += floor(vec2(mod(millis, 2400)) * vec2(1234.0, 3543.0)) / vec2(64);

    float block_thresh = pow(fract(mod(millis, 2400) * 1236.0453), 2.0) * 0.2;
    float line_thresh = pow(fract(mod(millis, 2400) * 2236.0453), 3.0) * 0.7;

    vec2 uv_r = uv, uv_g = uv, uv_b = uv;

    // glitch some blocks and lines
    if (random(uv_noise) < block_thresh ||
        random(vec2(uv_noise.y, 0.0)) < line_thresh) {
        vec2 dist = (fract(uv_noise) - 0.5) * 0.3;
        uv_r += dist * 0.1;
        uv_g += dist * 0.2;
        uv_b += dist * 0.125;
    }

    out_Color.r = texture(albedo_buffer, uv_r).r;
    out_Color.g = texture(albedo_buffer, uv_g).g;
    out_Color.b = texture(albedo_buffer, uv_b).b;

    // loose luma for some blocks
    if (random(uv_noise) < block_thresh)
        out_Color.rgb = out_Color.ggg;

    // discolor block lines
    if (random(vec2(uv_noise.y, 0.0)) * 3.5 < line_thresh)
        out_Color.rgb = vec3(0.0, 0.0, dot(out_Color.rgb, vec3(1.0)));
    if (random(vec2(uv_noise.y, 1.0)) * 3.5 < line_thresh) {
        float v = dot(out_Color.rgb, vec3(1.0)) * 0.5;
        out_Color.rgb = vec3(0.0, v, v);
    }
    if (random(vec2(uv_noise.y, 2.0)) * 3.5 < line_thresh)
        out_Color.rgb = vec3(dot(out_Color.rgb, vec3(1.0)), 0.0, 0.0);

    // interleave lines in some blocks
    if (random(uv_noise) * 1.5 < block_thresh ||
        random(vec2(uv_noise.y, 0.0)) * 2.5 < line_thresh) {
        float line = fract(uv.y * res.y / 3.0);
        vec3 mask = vec3(3.0, 0.0, 0.0);
        if (line > 0.333)
            mask = vec3(0.0, 3.0, 0.0);
        if (line > 0.666)
            mask = vec3(0.0, 0.0, 3.0);
        out_Color.xyz *= mask;
    }
    out_Color.a = max(out_Color.r, max(out_Color.g, out_Color.b));
}