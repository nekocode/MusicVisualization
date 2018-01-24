attribute vec2  vPosition;
attribute vec2  vTexCoord;
varying vec2    fragCoord;

void main() {
    fragCoord = vTexCoord;
    gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );
}