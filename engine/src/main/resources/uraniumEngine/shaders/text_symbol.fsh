
in vec2 uv;

uniform sampler2D symbol;

uniform vec4 text_color;

out vec4 out_Color;

void main () {
    out_Color = vec4(text_color.rgb, texture(symbol, uv).r * text_color.a);
}