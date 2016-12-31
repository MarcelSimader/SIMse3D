package com.se.simse.math;

import com.se.simse.SIMMain;
import com.se.simse.math.vector.Vector2f;
import com.se.simse.math.vector.Vector3f;

public class Triangle {
	
	private Vector3f p1;
	private Vector3f p2;
	private Vector3f p3;
	
	public Triangle(){}
	
	public Triangle(Vector3f p1,Vector3f p2,Vector3f p3){
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	
	public Vector3f getP(int index){
		switch(index){
		case 0: return p1;
		case 1: return p2;
		case 2: return p3;
		default: return p1;
		}
	}
	
	public void setP(int index, Vector3f d){
		switch(index){
		case 0: p1=d;
		case 1: p2=d;
		case 2: p3=d;
		default: p1=d;
		}
	}

	public Vector3f getP1() {
		return p1;
	}

	public void setP1(Vector3f p1) {
		this.p1 = p1;
	}

	public Vector3f getP2() {
		return p2;
	}

	public void setP2(Vector3f p2) {
		this.p2 = p2;
	}

	public Vector3f getP3() {
		return p3;
	}

	public void setP3(Vector3f p3) {
		this.p3 = p3;
	}
	
	public Vector3f getAVG(){
		return new Vector3f(getAVGX(), getAVGY(), getAVGZ());
	}
	
	public float getAVGX(){
		return (p1.getX()+p2.getX()+p3.getX())/3f;
	}
	
	public float getAVGY(){
		return (p1.getY()+p2.getY()+p3.getY())/3f;
	}
	
	public float getAVGZ(){
		return (p1.getZ()+p2.getZ()+p3.getZ())/3f;
	}
	
	public float getMaxX(){
		return Math.max(Math.max(p1.getX(), p2.getX()), p3.getX());
	}
	
	public float getMinX(){
		return Math.min(Math.min(p1.getX(), p2.getX()), p3.getX());
	}
	
	public float getMaxY(){
		return Math.max(Math.max(p1.getY(), p2.getY()), p3.getY());
	}
	
	public float getMinY(){
		return Math.min(Math.min(p1.getY(), p2.getY()), p3.getY());
	}
	
	public float getMaxZ(){
		return Math.max(Math.max(p1.getZ(), p2.getZ()), p3.getZ());
	}
	
	public float getMinZ(){
		return Math.min(Math.min(p1.getZ(), p2.getZ()), p3.getZ());
	}
	
	public String toString(){
		return "p1: " + p1.toString() + "\n" + "p2: " + p2.toString() + "\n" + "p3: " + p3.toString() + "\n";
	}
	
	public static Triangle cohenClipTri(Triangle tr, Vector2f min, Vector2f max){
		int width = (int) max.getX();
		int height = (int) max.getY();
		int minx = (int) min.getX();
		int miny = (int) min.getY();
		
		Triangle result = new Triangle();
		
		Vector2f[] ind = {new Vector2f(0,1),new Vector2f(1,2),new Vector2f(2,0)};
		for(int i=0;i<3;i++){
			float x11 = tr.getP((int)ind[i].getX()).getX();
			float x22 = tr.getP((int)ind[i].getY()).getX();
			float y11 = tr.getP((int)ind[i].getX()).getY();
			float y22 = tr.getP((int)ind[i].getY()).getY();
			int k1=-1,k2=-1;
			float dx = x22-x11;
			float dy = y22-y11;
			
			if(y11<miny){k1=0;};
			if(x11>width){k1=1;};
			if(y11>height){k1=2;};
			if(x11<minx){k1=3;};
			
			if(y22<miny){k2=0;};
			if(x22>width){k2=1;};
			if(y22>height){k2=2;};
			if(x22<minx){k2=3;};
			
			while(k1!=-1||k2!=-1){
				if(k1==k2){break;}
				
				if(k1==3){
					y11+=(minx-x11)*dy/dx;
					x11=minx;
				}
				if(k1==1){
					y11+=(width-x11)*dy/dx;
					x11=width;
				}
				if(k1==0){
					x11+=(miny-y11)*dx/dy;
					y11=miny;
				}
				if(k1==2){
					x11+=(height-y11)*dx/dy;
					y11=height;
				}
				
				k1=-1;
				if(y11<miny){k1=0;};
				if(x11>width){k1=1;};
				if(y11>height){k1=2;};
				if(x11<minx){k1=3;};
				if(k1==k2){break;}
				
				if(k2==3){
					y22+=(minx-x22)*dy/dx;
					x22=minx;
				}
				if(k2==1){
					y22+=(width-x22)*dy/dx;
					x22=width;
				}
				if(k2==0){
					x22+=(miny-y22)*dx/dy;
					y22=miny;
				}
				if(k2==2){
					x22+=(height-y22)*dx/dy;
					y22=height;
				}
				
				k2=-1;
				if(y22<miny){k2=0;};
				if(x22>width){k2=1;};
				if(y22>height){k2=2;};
				if(x22<minx){k2=3;};
				if(k1==k2){break;}
			}
			
			result.setP((int)ind[i].getX(), new Vector3f(x11,y11,tr.getP((int)ind[i].getX()).getZ()));
			result.setP((int)ind[i].getY(), new Vector3f(x22,y22,tr.getP((int)ind[i].getY()).getZ()));
		}
		return result;
	}

}
