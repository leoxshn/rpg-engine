
in vec2 uv;

uniform float millis;

out vec4 out_Color;

void main () {
    float center = 1.0 - 2 * length(uv - 0.5);
    out_Color = vec4(center, 0.0, 0.0, center);
}