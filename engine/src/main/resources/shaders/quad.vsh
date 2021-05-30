in vec2 inVertex;

uniform mat4 _engine_transofm_matix;
uniform vec2 _engine_screen;

out vec2 uv;

void main () {
    gl_Position = _engine_transofm_matix * vec4(inVertex, 0.0, 1.0);
    gl_Position.xy /= _engine_screen;
    uv = vec2(0.5 + inVertex.x / 2.0, 0.5 - inVertex.y / 2.0);
}