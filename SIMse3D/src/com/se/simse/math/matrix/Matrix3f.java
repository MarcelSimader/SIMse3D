package com.se.simse.math.matrix;

import com.se.simse.math.vector.Vector3d;
import com.se.simse.math.vector.Vector3f;

public class Matrix3f {
	
	private int size = 3;
	public float[] m = new float[size*size];
	
	/**
	 * Instantiates a new empty {@code Matrix3f} object.
	 */
	public Matrix3f(){
		for(int i=0;i<size*size;i++){
			m[i] = 0f;
		}
	}
	
	/**
	 * Instantiates a new {@code Matrix3f} object from a {@code Vector3f}.
	 * @param v Vector data in m[2][2] m[2][1] m[2][0]
	 */
	public Matrix3f(Vector3f v){
		for(int i=0;i<size*size;i++){
			m[i] = 0f;
		}
		m[2+2*size] = v.getZ();
		m[2+1*size] = v.getY();
		m[2+0*size] = v.getX();
	}
	
	/**
	 * Generates an identity matrix array.
	 */
	public Matrix3f identity(){
		Matrix3f ide = new Matrix3f();
		ide.m[0+0*size] = 1;
		ide.m[1+1*size] = 1;
		ide.m[2+2*size] = 1;
		return ide;
	}
	
	public float determinant(Matrix3f a){
		return (a.m[0+0*size]*(a.m[1+1*size]*a.m[2+2*size]-a.m[2+1*size]*a.m[1+2*size]))-
			   (a.m[1+0*size]*(a.m[0+1*size]*a.m[2+2*size]-a.m[2+1*size]*a.m[0+2*size]))-
			   (a.m[2+0*size]*(a.m[0+1*size]*a.m[1+2*size]-a.m[1+1*size]*a.m[0+2*size]));
	}
	
	public float determinant(){
			return (m[0+0*size]*(m[1+1*size]*m[2+2*size]-m[2+1*size]*m[1+2*size]))-
				   (m[1+0*size]*(m[0+1*size]*m[2+2*size]-m[2+1*size]*m[0+2*size]))-
				   (m[2+0*size]*(m[0+1*size]*m[1+2*size]-m[1+1*size]*m[0+2*size]));
	}
	
	public void add(Matrix3f a){
		for(int i=0;i<size*size;i++){
			m[i]+=a.m[i];
		}
	}
	
	public void sub(Matrix3f a){
		for(int i=0;i<size*size;i++){
			m[i]-=a.m[i];
		}
	}
	
	public void mult(Matrix3f a){
		Matrix3f result = new Matrix3f();
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				for(int k=0;k<size;k++){
					result.m[i+j*size] += m[i+k*size]*a.m[k+j*size];
				}
			}
		}
		m = result.m;
	}
	
	public String toString(){
		return "|" + m[0+0*3] + ","  + m[1+0*3] + "," + m[2+0*3] + "|\n"+
			   "|" + m[0+1*3] + ","  + m[1+1*3] + "," + m[2+1*3] + "|\n"+
			   "|" + m[0+2*3] + ","  + m[1+2*3] + "," + m[2+2*3] + "|\n";
	}

}
