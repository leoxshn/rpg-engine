in vec2 uv;

uniform bool is_chosen = false;

const float border_width = .03;
const float border_width_selected = .08;
const float aspect = 1.0;

out vec4 out_Color;

void main () {
    float b = is_chosen ? border_width_selected : border_width;
    float maxX = 1.0 - b;
    float minX = b;
    float maxY = maxX / aspect;
    float minY = minX / aspect;

    if (uv.x < maxX && uv.x > minX &&
        uv.y < maxY && uv.y > minY) {
    } else {
        out_Color = vec4(vec3(1.0), is_chosen ? 1. : .6);
    }
}