package com.se.simse.math.vector;

import java.util.Random;

public class Vector3f {
	
	private float x;
	private float y;
	private float z;
	
	/**
	 * Instantiates a new {@code Vector3f} at 0,0,0
	 */
	public Vector3f(){
		x = 0; y = 0; z = 0;
	}
	
	public Vector3f(double x, double y, double z){
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
	}
	
	public Vector3f(int x, int y, int z){
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
	}
	
	public Vector3f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3f(Vector3f v){
		this.x = v.getX();
		this.y = v.getY();
		this.z = v.getZ();
	}
	
	public float get(int i){
		switch(i){
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: return x;
		}
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public float getZ(){
		return z;
	}
	
	public void setX(float x){
		this.x = x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public void setZ(float z){
		this.z = z;
	}
	
	public void set(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void random(){
		x = (new Random().nextFloat()*2f)-1f;
		y = (new Random().nextFloat()*2f)-1f;
		z = (new Random().nextFloat()*2f)-1f;
	}
	
	public Vector3f randomN(){
		return new Vector3f((new Random().nextFloat()*2f)-1f,
		(new Random().nextFloat()*2f)-1f,
		(new Random().nextFloat()*2f)-1f);
	}
	
	public Vector3f normalized(){
		return new Vector3f(x/this.magnitude(), y/this.magnitude(), z/this.magnitude());
	}
	
	public void normalize(){
		x/=this.magnitude(); y/=this.magnitude(); z/=this.magnitude();
	}
	
	public void add(Vector3f v){
		x+=v.getX();y+=v.getY();z+=v.getZ();
	}
	
	public void add(float v){
		x+=v;y+=v;z+=v;
	}
	public Vector3f addN(Vector3f v){
		return new Vector3f(x+v.getX(),y+v.getY(),z+v.getZ());
	}
	
	public Vector3f addN(float v){
		return new Vector3f(x+v,y+v,z+v);
	}
	
	public void sub(Vector3f v){
		x-=v.getX();y-=v.getY();z-=v.getZ();
	}
	
	public void sub(float v){
		x-=v;y-=v;z-=v;
	}
	public Vector3f subN(Vector3f v){
		return new Vector3f(x-v.getX(),y-v.getY(),z-v.getZ());
	}
	
	public Vector3f subN(float v){
		return new Vector3f(x-v,y-v,z-v);
	}
	
	public void mult(Vector3f v){
		x*=v.getX();y*=v.getY();z*=v.getZ();
	}
	
	public void mult(float v){
		x*=v;y*=v;z*=v;
	}
	public Vector3f multN(Vector3f v){
		return new Vector3f(x*v.getX(),y*v.getY(),z*v.getZ());
	}
	
	public Vector3f multN(float v){
		return new Vector3f(x*v,y*v,z*v);
	}
	
	public void div(Vector3f v){
		x/=v.getX();y/=v.getY();z/=v.getZ();
	}
	
	public void div(float v){
		x/=v;y/=v;z/=v;
	}
	public Vector3f divN(Vector3f v){
		return new Vector3f(x/v.getX(),y/v.getY(),z/v.getZ());
	}
	
	public Vector3f divN(float v){
		return new Vector3f(x/v,y/v,z/v);
	}
	
	public float dot(Vector3f v){
		return x*v.getX()+y*v.getY()+z*v.getZ();
	}
	
	public float cross(Vector3f v){
		return (x*v.getY())-(y*v.getX()-(z*v.getZ()));
	}
	
	public float magnitude(){
		return (float)Math.sqrt((x*x)+(y*y)+(z*z));
	}
	
	public float distance(Vector3f v){
		return (float)Math.sqrt(((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y))+((v.getZ()-z)*(v.getZ()-z)));
	}
	
	public float distanceSQ(Vector3f v){
		return (((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y))+((v.getZ()-z)*(v.getZ()-z)));
	}
	
	public Vector3f reflect(Vector3f v){
		return new Vector3f(subN(dot(v.normalized().multN(2f).multN(v.normalized()))));
	}
	
	public String toString(){
		return "[" + x + "|" + y + "|" + z + "]";
	}

}
