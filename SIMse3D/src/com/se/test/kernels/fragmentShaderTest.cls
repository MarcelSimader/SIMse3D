
float orient2d(const float2 a, const float2 b, const float2 c){
    return (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
}

int isTopLeft(const float2 a, const float2 b){
	return sign(length(b-a));
}

__kernel void render(
__constant __read_only float* x1A, 
__constant __read_only float* rA, 
__constant __read_only float* normal,
__constant __read_only float* vertCount,
__constant __read_only float* camMatrix,
__constant __read_only float* lp,
__constant __read_only int* tex,
__write_only image2d_t result,
__global float* zBuffer){

	int index = 15*get_global_id(0);
	
	float ratiox = 1280*0.5f;
	float ratioy = 720*0.5f;
	float res = vertCount[1];
	
	float3 lightPos = (float3)(lp[0],lp[1],lp[2]);
	
	float2 v0 = ceil((float2)((x1A[index+0]*(ratiox/x1A[index+6]))+ratiox,(x1A[index+1]*(ratioy/x1A[index+6]))+ratioy));
	float2 v1 = ceil((float2)((x1A[index+2]*(ratiox/x1A[index+7]))+ratiox,(x1A[index+3]*(ratioy/x1A[index+7]))+ratioy));
	float2 v2 = ceil((float2)((x1A[index+4]*(ratiox/x1A[index+8]))+ratiox,(x1A[index+5]*(ratioy/x1A[index+8]))+ratioy));
			
	float2 x0 = v0/res;
	float2 x1 = v1/res;
	float2 x2 = v2/res;
	
	int2 sx = clamp((int2)(fmin(x0.x,fmin(x1.x,x2.x)),fmax(x0.x,fmax(x1.x,x2.x))),0,(1280/res));
	int2 sy = clamp((int2)(fmin(x0.y,fmin(x1.y,x2.y)),fmax(x0.y,fmax(x1.y,x2.y))),0,(720/res));
	
	float bias0 = isTopLeft(x1,x2) ? 0 : -1;
	float bias1 = isTopLeft(x2,x0) ? 0 : -1;
	float bias2 = isTopLeft(x0,x1) ? 0 : -1;
	
	for(float y=sy.x;y<sy.y;y+=1/res){
		for(float x=sx.x;x<sx.y;x+=1/res){	
			int zBufferIndex = (x+y*(1280/res));
			float2 p = (float2)(x,y);
			
			float w0 = orient2d(x1,x2,p)+bias0;
			float w1 = orient2d(x2,x0,p)+bias1;
			float w2 = orient2d(x0,x1,p)+bias2;
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
				
				float z = ((x1A[index+6]*a0)+(x1A[index+7]*a1)+(x1A[index+8]*a2));
				
				if(zBuffer[zBufferIndex]==0){zBuffer[zBufferIndex]=-1000;}
				if(zBuffer[zBufferIndex]<z){
					zBuffer[zBufferIndex]=z;
					float3 n = ((float3)(normal[index],normal[index+1],normal[index+2])*a0)+((float3)(normal[index+3],normal[index+4],normal[index+5])*a1)+((float3)(normal[index+6],normal[index+7],normal[index+8])*a2);
					float3 tri = ((float3)(rA[index],rA[index+1],rA[index+2])*a0)+((float3)(rA[index+3],rA[index+4],rA[index+5])*a1)+((float3)(rA[index+6],rA[index+7],rA[index+8])*a2);
					
					float light = dot(lightPos-tri,n)*0.5f;
					
					write_imagef(result,(int2)(x,y),(float4)(light,0.0f,0.0f,0.0f));
				}
			}
		}
	}
}
