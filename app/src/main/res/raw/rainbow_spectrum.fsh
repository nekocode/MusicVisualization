// Modified from https://www.shadertoy.com/view/ldX3D8

varying vec2 fragCoord;
uniform vec3 iResolution;
uniform sampler2D iChannel0;
uniform float iTime;

float bump(float x) {
	return abs(x) > 1.0 ? 0.0 : 1.0 - x * x;
}

void main()
{
	vec2 uv = fragCoord.xy;

	float c = 3.0;
	vec3 color = vec3(1.0);
	color.x = bump(c * (uv.x - 0.75));
	color.y = bump(c * (uv.x - 0.5));
	color.z = bump(c * (uv.x - 0.25));

	float line = abs(0.01 / abs(0.5-uv.y) );
	uv.y = abs( uv.y - 0.5 );

	vec4 soundWave =  texture2D( iChannel0, vec2(abs(0.5-uv.x)+0.005, 0.25) ).rrrr;
	color *= line * (1.0 - 2.0 * abs( 0.5 - uv.xxx ) + pow( soundWave.y, 10.0 ) * 30.0 );

	gl_FragColor = vec4(color, 0.0);
}