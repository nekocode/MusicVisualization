// Modified from https://www.shadertoy.com/view/Xdl3W2

varying vec2 fragCoord;
uniform vec3 iResolution;
uniform sampler2D iChannel0;
uniform float iTime;

float sp1 = 0.0;
float timesum = 0.0;
float timesum2 = 0.0;

float intersect( vec3 ro, vec3 rd, vec4 sphere, float reps )
{
	float r = sphere.w + sp1 * 3.0;

	vec3 sc = ro - sphere.xyz;

	float t = ( -ro.z + sphere.z ) / rd.z;
	vec3 pt = ro + t * rd;
	vec3 diff = pt - sphere.xyz;
	float wave1 = sin( atan(diff.y,diff.x) * reps + sp1 + timesum ) * 1.4;
	float wave2 = sin( atan(diff.y,diff.x) + sp1 + iTime ) * 10.0;
	r+= ( wave1 + texture2D( iChannel0, vec2( wave2 * 0.5, 0.0 ) ).x ) * 0.4;
	r+= sp1 * 2.0;
	float sum = 0.0;
	float test1 = sphere.w + r - length(diff);
	float test2 = sphere.w * 0.25 + r - length(diff);
	sum -= ( test1 ) > 4.0 ? ( test2 ) < 4.0 ? 3.0 : 0.0 : 0.0;
	return sum;
}

vec3 rotateY( vec3 v, float a )
{
	return vec3( sin(a)*v.x-cos(a)*v.z, v.y, cos(a)*v.x+sin(a)*v.z );
}

float color( vec2 uv2 )
{
	vec3 ro = vec3( 0.0, 0.0, 10.0 );
	ro = rotateY( ro, cos( timesum2 * 0.05 ) + 1.57 );
	vec3 rd = -normalize( vec3( uv2, 1.0 ) );
	rd = rotateY( rd, cos( timesum2 * 0.05 ) + 1.57 );

	float res = 0.0;

	// shapes
	res += intersect( ro, rd, vec4(  0.0, 0.0,  0.0, 4.0 ), 10.0 );
	res += intersect( ro, rd, vec4(  8.0, 0.0, -5.0, 1.0 ), 3.0  );
	res += intersect( ro, rd, vec4( -8.0, 0.0, -5.0, 1.0 ), 3.0  );
	if ( res < 0.0 )
	{
		res = abs(res*1.0);
	}
	else
	{
		res = 0.0;
	}

	return res;
}

void main()
{
	sp1 = texture2D( iChannel0, vec2( .0, 0.0) ).x;
	vec2 uv = fragCoord.xy;

	timesum  = ( -iTime + abs(sp1 * 1.0) ) * 30.0;
	timesum2 = (  iTime + abs(sp1 * 0.5) ) * 30.0;

	vec2 uv2 = uv * 2.0 - 1.0;
	uv2.x *= 1.78;
	float sh1 = texture2D( iChannel0, vec2( uv.y, 		 .5 ) ).x - 0.25;
	float sh2 = texture2D( iChannel0, vec2( uv.y * 0.5,  .5 ) ).x - 0.25;
	float sh3 = texture2D( iChannel0, vec2( uv.y * 0.75, .5 ) ).x - 0.25;
	float camult1 = 0.1;
	float camult2 = 0.2;
	float camult3 = 0.3;
	vec3 c = vec3( color( uv2 + vec2( camult1 * sh1, 0.0 ) ),
				   color( uv2 + vec2( camult2 * sh2, 0.0 ) ),
				   color( uv2 + vec2( camult3 * sh3, 0.0 ) ) );

	float tanargmul = 0.5;
	vec3 bg = vec3(( 1.0 - pow( abs( tan( uv2.x * tanargmul ) * tan( uv2.y * tanargmul ) ), 2.0 ) ) * 0.5);
	bg.r *= 0.5;
	bg.g *= 0.7;
	gl_FragColor = vec4( clamp( c + bg, 0.0, 1.0 ), 1.0 );
}