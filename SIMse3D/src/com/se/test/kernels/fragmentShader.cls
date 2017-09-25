__kernel void render(
__constant __read_only float* x1A, 
__constant __read_only float* rA, 
__constant __read_only float* normals,
__global float* zBuffer,
__constant __read_only float* vertCount,
__constant __read_only float* camMatrix,
__constant __read_only float* lp,
__global __write_only int* result){

	int x = get_global_id(0);
	int y = get_global_id(1);
	
	int zBufferIndex = (x+y*get_global_size(0));
	
	int4 color = (int4)(0,0,0,0);
	float4 lightPos = (float4)(lp[0],lp[1],lp[2],0);
	float4 p = (float4)(x,y,0,0);
	float4 camerapos = (float4)(camMatrix[0],camMatrix[1],camMatrix[2],0.0f);
	float4 camerarot = (float4)(camMatrix[4],camMatrix[5],0.0f,0.0f);
	
	if(zBuffer[zBufferIndex]==0){zBuffer[zBufferIndex]=-1000;}
	
	for(int el=0;el<vertCount[0]*9;el+=9){
		float res = vertCount[1];
		
		float ratiox = 1270*0.5;
		float ratioy = 720*0.5;
		float x11 = (x1A[el+0]*(ratiox/x1A[el+6]))+ratiox;
		float y11 = (x1A[el+1]*(ratioy/x1A[el+6]))+ratioy;
		float x22 = (x1A[el+2]*(ratiox/x1A[el+7]))+ratiox;
		float y22 = (x1A[el+3]*(ratioy/x1A[el+7]))+ratioy;
		float x33 = (x1A[el+4]*(ratiox/x1A[el+8]))+ratiox;
		float y33 = (x1A[el+5]*(ratioy/x1A[el+8]))+ratioy;
		
		float4 x1 = (float4)(x11/res,y11/res,0,0);
		float4 x2 = (float4)(x22/res,y22/res,0,0);
		float4 x3 = (float4)(x33/res,y33/res,0,0);
		
		float f = ((x2.y-x3.y)*(x1.x-x3.x)+(x3.x-x2.x)*(x1.y-x3.y));
		float alpha = ((x2.y-x3.y)*(p.x-x3.x)+(x3.x-x2.x)*(p.y-x3.y))/f;
		float beta = ((x3.y-x1.y)*(p.x-x3.x)+(x1.x-x3.x)*(p.y-x3.y))/f;
		float gamma = 1-alpha-beta;
		
		if(alpha>=0&&beta>=0&&gamma>=0){
	
			float4 f1 = x1 - p;
			float4 f2 = x2 - p;
			float4 f3 = x3 - p;
			float a = cross(x1-x2, x2-x3).z;
			float a1 = fabs((cross(f2,f3)/a).z);
			float a2 = fabs((cross(f3,f1)/a).z);
			float a3 = fabs((cross(f1,f2)/a).z);
			
			float z = (x1A[el+6]*a1)+(x1A[el+7]*a2)+(x1A[el+8]*a3);
			
			float4 nP = (float4)((float)(x)/(float)(get_global_size(0)),(float)(y)/(float)(get_global_size(1)),z,0);
			
			if(zBuffer[zBufferIndex]<z){
				zBuffer[zBufferIndex] = z;
				
				// FRAGMENT SHADER
				
				float light = 0;

				float4 normal = ((float4)(normals[el],normals[el+1],normals[el+2],0)*a1)
								+((float4)(normals[el+3],normals[el+4],normals[el+5],0)*a2)
								+((float4)(normals[el+6],normals[el+7],normals[el+8],0)*a3);
								
				float4 rP = ((float4)(rA[el],rA[el+1],rA[el+2],0)*a1)
							+((float4)(rA[el+3],rA[el+4],rA[el+5],0)*a2)
							+((float4)(rA[el+6],rA[el+7],rA[el+8],0)*a3);

				float4 lightDir = -normalize(rP-lightPos);
				float distance = length(lightDir);
				float attenuation = 1/((0.7+0.2+0.1)*distance*distance);
				light += attenuation*(dot(lightDir,normal));
				
				float4 vertToCam = -normalize(normal-camerapos);
				
				float d = 0.0f;
				float p = 3.5f;
				float s = 3.1f;
				float sublight = 0.0f;
				float gsub = pow(dot(vertToCam,(lightDir+(normal*d))),p)*s;
				sublight += fmax(fmin(gsub,1),0);
				
				float specPower = 17;
				float specIntens = 2;
				float4 lightReflect = normalize((-lightDir)-(2*dot((-lightDir),normal))*normal);
				float specS = dot(-vertToCam, lightReflect);
				if(specS>0){
					light += pow(specS, specPower)*specIntens*attenuation;
				}
				
				int4 c1 = (int4)((int)(fmin(fmax(sublight*90+light*185,0.0f),255.0f)),(int)(fmin(fmax(sublight*60+light*198,0.0f),255.0f)),(int)(fmin(fmax(light*234,0.0f),255.0f)),0);
				color = c1;
				//color = (int4)(vertToCam.x*150,vertToCam.y*0,vertToCam.z*150 ,0);
				
				// FRAGMENT SHADER END
			}
		}
	}
	result[zBufferIndex] = ((color.x&0x0FF)<<16)|((color.y&0x0FF)<<8)|(color.z&0x0FF);
}