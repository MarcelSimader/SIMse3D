
float* matrix4f(float3 f){
	float* m;
	m[0] = 0.0f;
	m[1] = 0.0f;
	m[2] = f.x;
	m[3] = 0.0f;
	m[4] = 0.0f;
	m[5] = f.y;
	m[6] = 0.0f;
	m[7] = 0.0f;
	m[8] = f.z;
	return m;
}


__kernel void setup(
__constant __read_only float* vertex,
__constant __read_only float* normal,
__constant __read_only float* vertexind,
__constant __read_only float* normalind,
__constant __read_only float* trans,
__constant __read_only float* camera,
__constant __write_only float* vertexOut,
__constant __write_only float* realOut,
__constant __write_only float* normalOut){

}