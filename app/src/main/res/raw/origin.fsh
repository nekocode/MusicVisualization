varying vec2 fragCoord;
uniform sampler2D iChannel0;

void main() {
    gl_FragColor = texture2D(iChannel0, fragCoord.xy);
}