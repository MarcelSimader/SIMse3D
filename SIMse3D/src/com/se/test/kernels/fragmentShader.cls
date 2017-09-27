__kernel void render(
__constant __read_only float* x1A, 
__constant __read_only float* rA, 
__constant __read_only float* normals,
__global float* zBuffer,
__constant __read_only float* vertCount,
__constant __read_only float* camMatrix,
__constant __read_only float* lp,
__constant __read_only int* tex,
__constant __read_only float* cA, 
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
	
	for(int el=0;el<vertCount[0]*15;el+=15){
		float res = vertCount[1];
		
		float ratiox = 1280*0.5;
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
			float w = ((1.0f/x1A[el+6])*a1)+((1.0f/x1A[el+7])*a2)+((1.0f/x1A[el+8])*a3);
			
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
							
				float4 cP = ((float4)(x1A[el],x1A[el+1],x1A[el+6],0)*a1)
							+((float4)(x1A[el+2],x1A[el+3],x1A[el+7],0)*a2)
							+((float4)(x1A[el+4],x1A[el+5],x1A[el+8],0)*a3);

				
				float3 uw = (float3)(normals[el+ 9]/(x1A[el+6]),normals[el+11]/(x1A[el+7]),normals[el+13]/(x1A[el+8]));
				float3 vw = (float3)(normals[el+10]/(x1A[el+6]),normals[el+12]/(x1A[el+7]),normals[el+14]/(x1A[el+8]));
				int u = (int)(((uw.x*a1)+(uw.y*a2)+(uw.z*a3))/w);
				int v = (int)(((vw.x*a1)+(vw.y*a2)+(vw.z*a3))/w);
				
				int texture = 1;
				int normalindex = (max(min(u,64),0))+(max(min(v,64),0)*64)+(64*64*texture);
				int normaltex = tex[normalindex];
				float4 normalrgb = (float4)((normaltex>>16)&0xFF,(normaltex>>8)&0xFF,(normaltex)&0xFF,0);
				
				float4 normalmap = ((2.0f*(normalrgb/255.0f))-1.0f);
				
				float4 n1 = (float4)(normal.y,normal.x,-normal.z,0);
				float4 n2 = (float4)(-normal.x,normal.z,normal.y,0);
				
				float4 lightDir = -normalize(rP-lightPos);
				
				float ns = 0.7f;
				normal=fmax(dot(lightDir,normal),0)*normalize(normalmap.x*n1+normalmap.y*n2+normalmap.z*normal)*ns+((1.0f-ns)*normal);
				
				
				float distance = length(lightDir);
				float attenuation = 1/((0.7+0.2+0.1)*distance*distance);
				light += attenuation*(dot(lightDir,normal));
				
				float d = 1.0f;
				float p = 6.0f;
				float s = 0.005f;
				float sublight = 0.0f;
				float gsub = pow(length(-(lightDir+(normal*d))),p)*s;
				sublight += fmax(fmin(gsub,1),0);
				
				float4 vertToCam = normalize(normal-camerapos);
				
				float specPower = 70;
				float specIntens = 0.7;
				float4 lightReflect = normalize((-lightDir)-(2*dot((-lightDir),normal))*normal);
				float specS = dot(vertToCam, lightReflect);
				float speclight = 0.0f;
				if(specS>0){
					//speclight += pow(specS, specPower)*specIntens*attenuation;
				}
				
				texture = 0;
				int index = (max(min(u,64),0))+(max(min(v,64),0)*64)+(64*64*texture);
				int rgbtex = tex[index];
				int4 rgb = (int4)((rgbtex>>16)&0xFF,(rgbtex>>8)&0xFF,(rgbtex)&0xFF,0);
				
				float4 c1 = (float4)(fmin(fmax(light,0.05f),3.0f),fmin(fmax(light,0.05f),3.0f),fmin(fmax(light,0.05f),3.0f),0.0f);
				float4 c2 = (float4)(fmin(fmax(sublight,0.0f),1.0f),fmin(fmax(sublight,0.0f),1.0f),fmin(fmax(0.0f,0.0f),1.0f),0.0f)*255.0f;
				float4 c3 = (float4)(fmin(fmax(speclight,0.0f),1.0f),fmin(fmax(speclight,0.0f),1.0f),fmin(fmax(speclight,0.0f),1.0f),0.0f)*255.0f;
				
				color = (int4)((int)(fmax(fmin((c1.x*rgb.x+c2.x+c3.x),255),0)),(int)(fmax(fmin((c1.y*rgb.y+c2.y+c3.y),255),0)),(int)(fmax(fmin((c1.z*rgb.z+c2.z+c3.z),255),0)),0);
				//color = (int4)((int)(normal.x*255.0f),(int)(normal.y*255.0f),(int)(normal.z*255.0f),0);
				
				// FRAGMENT SHADER END
			}
		}
	}
	result[zBufferIndex] = ((color.x&0xFF)<<16)|((color.y&0xFF)<<8)|(color.z&0xFF);
}