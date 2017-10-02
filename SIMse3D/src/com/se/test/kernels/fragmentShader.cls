
float orient2d(const float2 a, const float2 b, const float2 c)
{
    return (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
}

__kernel void render(
__constant __read_only float* x1A, 
__constant __read_only float* rA, 
__constant __read_only float* normal,
__constant __read_only float* vertCount,
__constant __read_only float* camMatrix,
__constant __read_only float* lp,
__constant __read_only int* tex,
__write_only image2d_t result){

	int x = get_global_id(0);
	int y = get_global_id(1);
	
	int4 color = (int4)(0,0,0,0);
	float3 lightPos = (float3)(lp[0],lp[1],lp[2]);
	float2 p = (float2)(x,y);
	float4 camerapos = (float4)(camMatrix[0],camMatrix[1],camMatrix[2],0.0f);
	float4 camerarot = (float4)(camMatrix[4],camMatrix[5],0.0f,0.0f);
	
	float res = vertCount[1];
	float ratiox = 1280*0.5f;
	float ratioy = 720*0.5f;
	
	for(int el=0;el<vertCount[0]*15;el+=15){
		
		float2 v0 = (float2)((x1A[el+0]*(ratiox/x1A[el+6]))+ratiox,(x1A[el+1]*(ratioy/x1A[el+6]))+ratioy);
		float2 v1 = (float2)((x1A[el+2]*(ratiox/x1A[el+7]))+ratiox,(x1A[el+3]*(ratioy/x1A[el+7]))+ratioy);
		float2 v2 = (float2)((x1A[el+4]*(ratiox/x1A[el+8]))+ratiox,(x1A[el+5]*(ratioy/x1A[el+8]))+ratioy);
		
		float2 x0 = v0/res;
		float2 x1 = v1/res;
		float2 x2 = v2/res;
		
		float w0 = orient2d(x1,x2,p);
		float w1 = orient2d(x2,x0,p);
		float w2 = orient2d(x0,x1,p);
		
		if(p.x>fmin(x0.x,fmin(x1.x,x2.x))&&p.y>fmin(x0.y,fmin(x1.y,x2.y))&&p.x<fmax(x0.x,fmax(x1.x,x2.x))&&p.y<fmax(x0.y,fmax(x1.y,x2.y))){
		
		if(w0<=0.0f && w1<=0.0f && w2<=0.0f){
			float3 x0 = (float3)(x0.x,x0.y,0);
			float3 x1 = (float3)(x1.x,x1.y,0);
			float3 x2 = (float3)(x2.x,x2.y,0);
			float3 p = (float3)(p.x,p.y,0);
			
			float3 f0 = x0 - p;
			float3 f1 = x1 - p;
			float3 f2 = x2 - p;

			float a = cross(x0-x1, x1-x2).z;
			float a0 = fabs((cross(f1,f2)/a).z);
			float a1 = fabs((cross(f2,f0)/a).z);
			float a2 = fabs((cross(f0,f1)/a).z);
			
			float z = ((x1A[el+6]*a0)+(x1A[el+7]*a1)+(x1A[el+8]*a2));

			float3 n = ((float3)(normal[el],normal[el+1],normal[el+2])*a0)+((float3)(normal[el+3],normal[el+4],normal[el+5])*a1)+((float3)(normal[el+6],normal[el+7],normal[el+8])*a2);
									
			float3 tri = ((float3)(rA[el],rA[el+1],rA[el+2])*a0)+((float3)(rA[el+3],rA[el+4],rA[el+5])*a1)+((float3)(rA[el+6],rA[el+7],rA[el+8])*a2);
									
			color = (int4)(5*dot(lightPos-tri, n),30,30,0);
			
		}
		}
	}
	write_imagef(result,(int2)(x,y),(float4)(color.z/255.0f,color.y/255.0f,color.x/255.0f,0.0f));
}
