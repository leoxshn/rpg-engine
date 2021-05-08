
in vec2 uv;

uniform sampler2D sprite_sheet;

uniform vec2 frame;
uniform vec2 frame_to_sheet_ratio;

out vec4 out_Color;

void main () {
    out_Color = texture(sprite_sheet, uv * frame_to_sheet_ratio + frame * frame_to_sheet_ratio);
}