#version 420 core


float random (vec2 co) {
    highp float a = 12.9898;
    highp float b = 78.233;
    highp float c = 43758.5453;
    highp float dt = dot(co.xy, vec2(a,b));
    highp float sn = mod(dt, 3.14);
    return fract(sin(sn) * c);
}
float random (float a, float b) { return random(vec2(a, b)); }
float random (float n) { return fract(cos(n) * 114514.1919); }


vec3 rgb2hsv (vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 rgb2hsv (float r, float g, float b) { return rgb2hsv(vec3(r, g, b)); }
vec3 rgb2hsv (vec2 rg, float b) { return rgb2hsv(vec3(rg, b)); }
vec3 rgb2hsv (float r, vec2 gb) { return rgb2hsv(vec3(r, gb)); }


vec3 hsv2rgb (vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 hsv2rgb (float h, float s, float v) { return rgb2hsv(vec3(h, s, v)); }
vec3 hsv2rgb (vec2 hs, float v) { return rgb2hsv(vec3(hs, v)); }
vec3 hsv2rgb (float h, vec2 sv) { return rgb2hsv(vec3(h, sv)); }


float smooth_quantize (float value, float level_count) {
    float c = value * level_count;
    float cc = round(c);
    float f = c - cc;
    return (smoothstep(-0.5, 0.5, f) + cc) / level_count;
}
#line 1