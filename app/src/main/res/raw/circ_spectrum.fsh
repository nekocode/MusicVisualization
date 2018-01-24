// Modified from https://www.shadertoy.com/view/4djGDy

varying vec2 fragCoord;
uniform vec3 iResolution;
uniform sampler2D iChannel0;


void main()
{
	vec2	center = vec2(iResolution.x / 2.0, iResolution.y / 2.0);
	float	dist = distance(fragCoord.xy * iResolution.xy, center);

	float	radius = 1.0 + 0.4 * iResolution.y * texture2D(iChannel0, vec2(fragCoord.x, 0.25)).x;

	if (fragCoord.x > 0.25 && fragCoord.x < 0.75 && dist < (radius * 0.5))
	{
		vec3	color = vec3(1.0, 1.0, 1.0);
		float	value = dist / radius;
	    gl_FragColor = vec4((1.0 - value) * color.x, (1.0 - value) * color.y, (1.0 - value) * color.z, 1.0);
	}
	else
	{
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	}
}