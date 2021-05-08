in vec2 inVertex;

uniform vec2 _engine_quad_position;
uniform vec2 _engine_quad_size;

out vec2 uv;

void main () {
    gl_Position = vec4(inVertex * _engine_quad_size + _engine_quad_position, 0.0, 1.0);
    uv = vec2(0.5 + inVertex.x / 2.0, 0.5 - inVertex.y / 2.0);
}