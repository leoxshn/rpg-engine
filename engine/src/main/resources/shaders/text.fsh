
in vec2 uv;

uniform sampler2D font;

uniform vec2 char_uv_start;
uniform vec2 char_uv_end;
uniform vec3 text_color = vec3(1.0);

out vec4 out_Color;

void main () {
    out_Color = vec4(text_color, texture(font, mix(char_uv_start, char_uv_end, uv)).r);
}