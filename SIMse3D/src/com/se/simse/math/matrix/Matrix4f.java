package com.se.simse.math.matrix;

import com.se.simse.math.vector.Vector3f;

public class Matrix4f {
	
	private int size = 4;
	public float[] m = new float[size*size];
	
	/**
	 * Instantiates a new empty {@code Matrix4f} object.
	 */
	public Matrix4f(){
		for(int i=0;i<size*size;i++){
			m[i] = 0f;
		}
	}
	
	/**
	 * Instantiates a new {@code Matrix4f} object from a {@code Vector3f}.
	 * @param v Vector data in m[3][0] m[3][1] m[3][2]
	 */
	public Matrix4f(Vector3f v){
		for(int i=0;i<size*size;i++){
			m[i] = 0f;
		}
		m[3+2*size] = v.getZ();
		m[3+1*size] = v.getY();
		m[3+0*size] = v.getX();
	}
	
	/**
	 * Generates an identity matrix array.
	 */
	public Matrix4f identity(){
		Matrix4f ide = new Matrix4f();
		ide.m[0+0*size] = 1;
		ide.m[1+1*size] = 1;
		ide.m[2+2*size] = 1;
		ide.m[3+3*size] = 1;
		return ide;
	}
	
	public void add(Matrix4f a){
		for(int i=0;i<size*size;i++){
			m[i]+=a.m[i];
		}
	}
	
	public void sub(Matrix4f a){
		for(int i=0;i<size*size;i++){
			m[i]-=a.m[i];
		}
	}
	
	public void mult(Matrix4f a){
		Matrix4f result = new Matrix4f();
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				for(int k=0;k<size;k++){
					result.m[i+j*size] += m[i+k*size]*a.m[k+j*size];
				}
			}
		}
		m = result.m;
	}
	
	/**
	 * Creates an orthographic projection matrix.
	 * @param r Right clip
	 * @param l Left clip
	 * @param t Top clip
	 * @param b Bottom clip
	 * @param f Far clip plane
	 * @param n Near clip plane
	 */
	public void orthographic(float r, float l, float t, float b, float f, float n){
		Matrix4f res = identity();
		res.m[0+0*size] = 2f/(r-l);
		res.m[1+1*size] = 2f/(t-b);
		res.m[2+2*size] = -2f/(f-n);
		res.m[3+2*size] = -((f+n)/(f-n));
		res.m[3+1*size] = -((t+b)/(t-b));
		res.m[3+0*size] = -((r+l)-(r-l));
		m = res.m;
	}
	
	/**
	 * Creates a perspective projection matrix.
	 * @param aspect Aspect ratio of screen.
	 * @param fov Field Of View
	 * @param far Far clip plane
	 * @param near Near clip plane
	 */
	public void perspective(float aspect, float fov, float far, float near){
		float c = (float)Math.tan(Math.toRadians(fov)*0.5f);
		Matrix4f res = identity();
		res.m[0+0*size] = 1f/(aspect*c);
		res.m[1+1*size] = 1f/(c);
		res.m[2+2*size] = -((far+near)/(far-near));
		res.m[2+3*size] = -1f; 
		res.m[3+3*size] = 0f; 
		res.m[3+2*size] = -((2f*far*near)/(far-near));
		m = res.m;
	}
	
	/**
	 * Translates the {@code Matrix4f}
	 * @param v offset
	 */
	public void translate(Vector3f v){
		m[3+0*size] += v.getX();
		m[3+1*size] += v.getY();
		m[3+2*size] += v.getZ();
	}
	
	/**
	 * Scales the {@code Matrix4f}
	 * @param v scale factor
	 */
	public void scale(Vector3f v){
		m[0+0*size] = v.getX();
		m[1+1*size] = v.getY();
		m[2+2*size] = v.getZ();
	}
	
	/**
	 * Rotates the {@code Matrix4f}
	 * @param v The rotation-axis. (1,0,0) -> X-Axis rotation
	 * @param angle The angle of the rotation.
	 */
	public void rotate(Vector3f v, float angle){
		if(v.getX()==1){
			m[1+1*size] = (float) Math.cos(angle);
			m[1+2*size] = (float) -Math.sin(angle);
			m[2+2*size] = (float) Math.cos(angle);
			m[2+1*size] = (float) Math.sin(angle);
		}
		
		if(v.getY()==1){
			m[0+0*size] = (float) Math.cos(angle);
			m[2+0*size] = (float) Math.sin(angle);
			m[2+2*size] = (float) Math.cos(angle);
			m[0+2*size] = (float) -Math.sin(angle);
		}
		
		if(v.getZ()==1){
			m[0+0*size] = (float) Math.cos(angle);
			m[0+1*size] = (float) Math.sin(angle);
			m[1+0*size] = (float) -Math.sin(angle);
			m[1+1*size] = (float) Math.cos(angle);
		}
	}
	
	public String toString(){
		return "|" + m[0+0*size] + ","  + m[1+0*size] + "," + m[2+0*size] +  "," + m[3+0*size] +"|\n"+
			   "|" + m[0+1*size] + ","  + m[1+1*size] + "," + m[2+1*size] +  "," + m[3+1*size] +"|\n"+
			   "|" + m[0+2*size] + ","  + m[1+2*size] + "," + m[2+2*size] +  "," + m[3+2*size] +"|\n"+
			   "|" + m[0+3*size] + ","  + m[1+3*size] + "," + m[2+3*size] +  "," + m[3+3*size] +"|\n";
	}

}
