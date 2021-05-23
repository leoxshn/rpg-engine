
in vec2 uv;

uniform sampler2D sprite_sheet;

uniform vec2 tile_uv;
uniform vec2 tile_to_sheet_ratio;

out vec4 out_Color;

void main () {
    out_Color = texture(sprite_sheet, (uv + tile_uv) * tile_to_sheet_ratio);
}