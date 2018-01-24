// Modified from https://www.shadertoy.com/view/lsdGDH

precision highp float;

varying vec2 fragCoord;
uniform vec3 iResolution;
uniform sampler2D iChannel0;
uniform float iTime;


vec2 hash( vec2 p ) {                       // rand in [-1,1]
    p = vec2( dot(p,vec2(127.1,311.7)),
              dot(p,vec2(269.5,183.3)) );
    return -1. + 2.*fract(sin(p+20.)*53758.5453123);
}

vec4 getFreq(float f) {
	float fft  = texture2D( iChannel0, vec2(f, 0.25) ).x;
	vec3 col = vec3( fft, 4.0 * fft * (1.0 - fft), 1.0 - fft ) * fft;
    return max(vec4(col, 1.0), 0.);
}

#define k iTime * 6.
#define t iTime * 6.
#define v 2.
#define fov 90.
#define PI 3.1415

void main() {

    vec2 uv = fragCoord.xy - .5;
	vec2 muv = uv;

    float volF = getFreq(uv.x + .5).r / v;
    float volM = getFreq(.9).r / v;
    float volL = getFreq( .25).r / v;
    vec4 c = vec4(.5, 0., .2 + sin(k / 16.), 0.) / 4. - .2;

    for (float i = 8.; i > 0.; i--) {

        muv = uv * tan (radians (i * 4. + fov)/2.0);

        muv.y += sin(k + volL + muv.x * 43.) * volM / 2.;
        muv.y += sin(2.3 * k + PI + 2. + muv.x * 127.) * volM / 4.;
        muv.y += sin(PI + 3. + muv.x * 32. + k * 3.12) * volL / 4.;

    	if (muv.y < 0.02 && muv.y > -0.02) {
    		c += max(
                vec4(1. - i * .15) * (1. - abs(muv.y) * 50.),
                .0
            );
    	}

        c -= (muv.y - .5) / 24.;

    }

    uv += .5;

    c += vec4(abs(sin(uv.y * PI) / 5.)) + hash(uv * 5. + t / 100.).x / 12.;
    c *= .2 + 0.6 * pow(16.0 * uv.x * uv.y * (1.0 - uv.x) * (1.0 - uv.y), 4.);

    gl_FragColor = c;
}