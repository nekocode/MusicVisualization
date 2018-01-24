// Modified from https://www.shadertoy.com/view/Xds3Rr

varying vec2 fragCoord;
uniform vec3 iResolution;
uniform sampler2D iChannel0;

void main() {
    // first row is frequency data
    float fft = texture2D(iChannel0, vec2(fragCoord.x, 0.25)).x;

    // second row is the sound wave, one texel is one mono sample
    float wave = texture2D(iChannel0, vec2(fragCoord.x, 0.75)).x;

    // convert frequency to colors
    vec3 col = vec3(fft, 4.0 * fft * (1.0 - fft), 1.0 - fft) * fft;

    // add wave form on top
    col += 1.0 - smoothstep(0.0, 0.02, abs(wave - fragCoord.y));

    // output final color
    gl_FragColor = vec4(col, 1.0);
}