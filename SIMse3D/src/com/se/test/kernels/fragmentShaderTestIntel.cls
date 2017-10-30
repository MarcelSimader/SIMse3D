
float orient2d(const float2 a, const float2 b, const float2 c){
    return (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
}

float area(const float2 a, const float2 b, const float2 c){
    return 0.5f*((a.x*b.y)+(a.y*c.x)+(b.x*c.y)-(c.x*b.y)-(c.y*a.x)-(b.x*a.y));
}

int isTopLeft(const float2 a, const float2 b){
	return sign(length(b-a));
}

float3 vecMult(float* m, float3 v){
	return (float3)(
	m[0]*v.x+m[1]*v.y+m[2]*v.z,
	m[3]*v.x+m[4]*v.y+m[5]*v.z,
	m[6]*v.x+m[7]*v.y+m[8]*v.z
	);
}

void matMult(float* m2, float* m, float* m1){
	int w = 3;
	m2[0]= m[0+0*w]*m1[0+0*w]+ m[1+0*w]*m1[0+1*w]+ m[2+0*w]*m1[0+2*w];
	m2[1]= m[0+0*w]*m1[1+0*w]+ m[1+0*w]*m1[1+1*w]+ m[2+0*w]*m1[1+2*w];
	m2[2]= m[0+0*w]*m1[2+0*w]+ m[1+0*w]*m1[2+1*w]+ m[2+0*w]*m1[2+2*w];
	
	m2[3]= m[0+1*w]*m1[0+0*w]+ m[1+1*w]*m1[0+1*w]+ m[2+1*w]*m1[0+2*w];
	m2[4]= m[0+1*w]*m1[1+0*w]+ m[1+1*w]*m1[1+1*w]+ m[2+1*w]*m1[1+2*w];
	m2[5]= m[0+1*w]*m1[2+0*w]+ m[1+1*w]*m1[2+1*w]+ m[2+1*w]*m1[2+2*w];
	
	m2[6]= m[0+2*w]*m1[0+0*w]+ m[1+2*w]*m1[0+1*w]+ m[2+2*w]*m1[0+2*w];
	m2[7]= m[0+2*w]*m1[1+0*w]+ m[1+2*w]*m1[1+1*w]+ m[2+2*w]*m1[1+2*w];
	m2[8]= m[0+2*w]*m1[2+0*w]+ m[1+2*w]*m1[2+1*w]+ m[2+2*w]*m1[2+2*w];
}

__kernel void render(
__global __read_only float* x1A, 
__global __read_only float* rA, 
__constant __read_only float* normal,
__constant __read_only float* camMatrix,
__constant __read_only float* lp,
__constant __read_only int* textures,
__write_only image2d_t result,
__global float* zBuffer){

	int index = 15*get_global_id(0);
	
	float ratiox = 1280*0.5f;
	float ratioy = 720*0.5f;
	float res = camMatrix[5];
	float2 ratio = (float2)(1280/res, 720/res);
	
	float3 lightPos = (float3)(lp[0],lp[1],lp[2]);
	float3 cameraPos = (float3)(-camMatrix[0],-camMatrix[1],-camMatrix[2]);
	float2 camreaRot = (float2)(camMatrix[3],camMatrix[4]);
	
	float2 v0 = ceil((float2)((x1A[index+0]*(ratiox/x1A[index+6]))+ratiox,(x1A[index+1]*(ratioy/x1A[index+6]))+ratioy));
	float2 v1 = ceil((float2)((x1A[index+2]*(ratiox/x1A[index+7]))+ratiox,(x1A[index+3]*(ratioy/x1A[index+7]))+ratioy));
	float2 v2 = ceil((float2)((x1A[index+4]*(ratiox/x1A[index+8]))+ratiox,(x1A[index+5]*(ratioy/x1A[index+8]))+ratioy));
			
	float2 x0 = v0/res;
	float2 x1 = v1/res;
	float2 x2 = v2/res;
	
	float A01 = (x0.y - x1.y)/res; float B01 = (x1.x - x0.x)/res;
	float A12 = (x1.y - x2.y)/res; float B12 = (x2.x - x1.x)/res;
	float A20 = (x2.y - x0.y)/res; float B20 = (x0.x - x2.x)/res;
	
	int2 sx = clamp((int2)(fmin(x0.x,fmin(x1.x,x2.x)),fmax(x0.x,fmax(x1.x,x2.x))),0,(1280/res));
	int2 sy = clamp((int2)(fmin(x0.y,fmin(x1.y,x2.y)),fmax(x0.y,fmax(x1.y,x2.y))),0,(720/res));
	
	float bias0 = isTopLeft(x1,x2) ? 0 : -1;
	float bias1 = isTopLeft(x2,x0) ? 0 : -1;
	float bias2 = isTopLeft(x0,x1) ? 0 : -1;
	
	float2 p = (float2)(sx.x,sy.x);
	
	float w0_row = orient2d(x1,x2,p)+bias0;
	float w1_row = orient2d(x2,x0,p)+bias1;
	float w2_row = orient2d(x0,x1,p)+bias2;
	
	if(!(x0.x<0 && x1.x<0 && x2.x<0 && x0.y<0 && x1.y<0 && x2.y<0)&&!(x0.x>ratio.x && x1.x>ratio.x && x2.x>ratio.x && x0.y>ratio.y && x1.y>ratio.y && x2.y>ratio.y)){
	for(float j=sy.x;j<sy.y;j+=1/res){
		float w0 = w0_row;
		float w1 = w1_row;
		float w2 = w2_row;
		
		for(float i=sx.x;i<sx.y;i+=1/res){	
	
			//start lighting
			if(w0<=0.0f && w1<=0.0f && w2<=0.0f){
				int zBufferIndex = (int)(floor(i)+floor(j)*(1280/res));
				p = (float2)(i,j);
				
				float a = area(x0,x1,x2);
				float a0 = area(p,x1,x2)/a;
				float a1 = area(x0,p,x2)/a;
				float a2 = area(x0,x1,p)/a;
				float z = ((x1A[index+6]*a0)+(x1A[index+7]*a1)+(x1A[index+8]*a2));
				float w = (((1.0f/x1A[index+6])*a0)+((1.0f/x1A[index+7])*a1)+((1.0f/x1A[index+8])*a2));

				if(zBuffer[zBufferIndex]==0){zBuffer[zBufferIndex]=-40.0f;}
				if(zBuffer[zBufferIndex]<=z){
					zBuffer[zBufferIndex]=z;
					float3 n = ((float3)(normal[index  ],normal[index+1],normal[index+2])*a0)
							  +((float3)(normal[index+3],normal[index+4],normal[index+5])*a1)
							  +((float3)(normal[index+6],normal[index+7],normal[index+8])*a2);
					float3 tri = ((float3)(rA[index  ],rA[index+1],rA[index+2])*a0)
								+((float3)(rA[index+3],rA[index+4],rA[index+5])*a1)
								+((float3)(rA[index+6],rA[index+7],rA[index+8])*a2);
					
					//float viewDistance = fast_length(cameraPos-tri);
					//float fogF = fmin(exp(-0.1*viewDistance),1);
					
					//______________________________________UV___________________________________________________________
					float3 uw = (float3)((rA[index +9])/x1A[index+6],(rA[index+11])/x1A[index+7],(rA[index+13])/x1A[index+8]);
					float3 vw = (float3)((rA[index+10])/x1A[index+6],(rA[index+12])/x1A[index+7],(rA[index+14])/x1A[index+8]);
					int u = (int)((((uw.x*a0)+(uw.y*a1)+(uw.z*a2))/(w)));
					int v = (int)((((vw.x*a0)+(vw.y*a1)+(vw.z*a2))/(w)));
					
					//________________________________NORMAL___________________________________________________________
					
					int texNIndex = (max(min(u,256),0))+(max(min(v,256),0)*256)+(256*256*2);
					int texN = textures[texNIndex];
					float3 texNRGB = ((float3)((texN>>16)&0xFF,(texN>>8)&0xFF,(texN)&0xFF))/255.0f;
					texNRGB=(2.0f*texNRGB)-1.0f;
					
					float3 deltaPos1 = (float3)(rA[index+3],rA[index+4],rA[index+5])-(float3)(rA[index],rA[index+1],rA[index+2]);
					float3 deltaPos2 = (float3)(rA[index+6],rA[index+7],rA[index+8])-(float3)(rA[index],rA[index+1],rA[index+2]);
					float2 deltaUV1 = (float2)(rA[index+11],rA[index+12])-(float2)(rA[index+9],rA[index+10]);
					float2 deltaUV2 = (float2)(rA[index+13],rA[index+14])-(float2)(rA[index+9],rA[index+10]);
					
					float r = 1.0f/(deltaUV1.x*deltaUV2.y-deltaUV1.y*deltaUV2.x);
					float3 tangent = normalize((deltaPos1*deltaUV2.y-deltaPos2*deltaUV1.y)*r);
					
					float rot1[9];
					rot1[0]=1;	rot1[1]=0;					rot1[2]=0;
					rot1[3]=0;	rot1[4]=cos(camreaRot.x);	rot1[5]=-sin(camreaRot.x);
					rot1[6]=0;	rot1[7]=sin(camreaRot.x);	rot1[8]=cos(camreaRot.x);
					
					float rot2[9];
					rot2[0]=cos(camreaRot.y);	rot2[1]=0;	rot2[2]=sin(camreaRot.y);
					rot2[3]=0;					rot2[4]=1;	rot2[5]=0;
					rot2[6]=-sin(camreaRot.y);	rot2[7]=0;	rot2[8]=cos(camreaRot.y);
					
					matMult(rot1, rot1, rot2);
					
					float viewSpace[9];
					viewSpace[0]=1;	viewSpace[1]=0;	viewSpace[2]=cameraPos.x;
					viewSpace[3]=0;	viewSpace[4]=1;	viewSpace[5]=cameraPos.y;
					viewSpace[6]=0;	viewSpace[7]=0;	viewSpace[8]=cameraPos.z;
					
					matMult(viewSpace, viewSpace, rot1);
					
					tangent = vecMult(viewSpace, tangent);
					float3 bitangent = cross(n,tangent);
					
					float tangentSpace[9];
					tangentSpace[0]=tangent.x;	tangentSpace[1]=bitangent.x;	tangentSpace[2]=n.x;
					tangentSpace[3]=tangent.y;	tangentSpace[4]=bitangent.y;	tangentSpace[5]=n.y;
					tangentSpace[6]=tangent.z;	tangentSpace[7]=bitangent.z;	tangentSpace[8]=n.z;
					
					//n = texNRGB;
					
					//_________________________________DIFFUSE___________________________________________________________
					//float3 toCam = vecMult(tangentSpace,normalize(cameraPos-tri));
					float3 toCam = normalize(cameraPos-tri);
					
					//float3 toLight = vecMult(tangentSpace, normalize(lightPos-tri));
					float3 toLight = normalize(lightPos-tri);
					float lightDistance = fmax(fast_length(lightPos-tri),0);
					float attenuation = 1.0f/((lightDistance*lightDistance*0.1f)+(0.01f*lightDistance)+1.0f);
					float light = 0.02f+fmax(attenuation*dot(toLight,n),0);
					
					
					//_____________________________SPECULAR___________________________________________________________
					float3 reflect = -toLight-(2*dot(-toLight,n)*n);
					float specF = fmin(fmax(dot(toCam,reflect),0),1);
					float spec = attenuation*0.5f*pow(specF,15);
					
					//___________________________________TEXTURES___________________________________________________________
					int texIndex = (max(min(u,256),0))+(max(min(v,256),0)*256)+(256*256*0);
					int tex = textures[texIndex];
					float4 texRGB = ((float4)((tex>>16)&0xFF,(tex>>8)&0xFF,(tex)&0xFF,0))/255.0f;
					
					int texSIndex = (max(min(u,256),0))+(max(min(v,256),0)*256)+(256*256*1);
					int texS = textures[texSIndex];
					float4 texSRGB = ((float4)((texS>>16)&0xFF,(texS>>8)&0xFF,(texS)&0xFF,0))/255.0f;
					
					//_________________________________________WRITE_IMAGEF___________________________________________________________
					write_imagef(result,(int2)(i,j),(float4)(texRGB.z*light+(spec*texSRGB.x),texRGB.y*light+(spec*texSRGB.x),texRGB.x*light+(spec*texSRGB.x),0));
					//write_imagef(result,(int2)(i,j),(float4)((n.z+1)/2,(n.y+1)/2,(n.x+1)/2,0));
					//write_imagef(result,(int2)(i,j),(float4)(n.z,n.y,n.x,0));
				}
			}
			
			//end lighting
			
			w0 += A12;
			w1 += A20;
			w2 += A01;
		}
		
		w0_row += B12;
		w1_row += B20;
		w2_row += B01;
	}
	}
}
