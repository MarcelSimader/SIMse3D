package com.se.simse.math.vector;

import java.util.Random;

public class Vector3d {
	
	private double x;
	private double y;
	private double z;
	
	/**
	 * Instantiates a new {@code Vector3d} at 0,0,0
	 */
	public Vector3d(){
		x = 0; y = 0; z = 0;
	}
	
	public Vector3d(float x, float y, float z){
		this.x = (double)x;
		this.y = (double)y;
		this.z = (double)z;
	}
	
	public Vector3d(int x, int y, int z){
		this.x = (double)x;
		this.y = (double)y;
		this.z = (double)z;
	}
	
	public Vector3d(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3d(Vector3d v){
		this.x = v.getX();
		this.y = v.getY();
		this.z = v.getZ();
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getZ(){
		return z;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public void setZ(double z){
		this.z = z;
	}
	
	public void set(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void random(){
		x = (new Random().nextDouble()*2f)-1f;
		y = (new Random().nextDouble()*2f)-1f;
		z = (new Random().nextDouble()*2f)-1f;
	}
	
	public Vector3d randomN(){
		return new Vector3d((new Random().nextDouble()*2f)-1f,
		(new Random().nextDouble()*2f)-1f,
		(new Random().nextDouble()*2f)-1f);
	}
	
	public Vector3d normalized(){
		return new Vector3d(x/this.magnitude(), y/this.magnitude(), z/this.magnitude());
	}
	
	public void normalize(){
		x/=this.magnitude(); y/=this.magnitude(); z/=this.magnitude();
	}
	
	public void add(Vector3d v){
		x+=v.getX();y+=v.getY();z+=v.getZ();
	}
	
	public void add(double v){
		x+=v;y+=v;z+=v;
	}
	public Vector3d addN(Vector3d v){
		return new Vector3d(x+v.getX(),y+v.getY(),z+v.getZ());
	}
	
	public Vector3d addN(double v){
		return new Vector3d(x+v,y+v,z+v);
	}
	
	public void sub(Vector3d v){
		x-=v.getX();y-=v.getY();z-=v.getZ();
	}
	
	public void sub(double v){
		x-=v;y-=v;z-=v;
	}
	public Vector3d subN(Vector3d v){
		return new Vector3d(x-v.getX(),y-v.getY(),z-v.getZ());
	}
	
	public Vector3d subN(double v){
		return new Vector3d(x-v,y-v,z-v);
	}
	
	public void mult(Vector3d v){
		x*=v.getX();y*=v.getY();z*=v.getZ();
	}
	
	public void mult(double v){
		x*=v;y*=v;z*=v;
	}
	public Vector3d multN(Vector3d v){
		return new Vector3d(x*v.getX(),y*v.getY(),z*v.getZ());
	}
	
	public Vector3d multN(double v){
		return new Vector3d(x*v,y*v,z*v);
	}
	
	public void div(Vector3d v){
		x/=v.getX();y/=v.getY();z/=v.getZ();
	}
	
	public void div(double v){
		x/=v;y/=v;z/=v;
	}
	public Vector3d divN(Vector3d v){
		return new Vector3d(x/v.getX(),y/v.getY(),z/v.getZ());
	}
	
	public Vector3d divN(double v){
		return new Vector3d(x/v,y/v,z/v);
	}
	
	public double dot(Vector3d v){
		return x*v.getX()+y*v.getY()*z*v.getZ();
	}
	
	public double cross(Vector3d v){
		return (x*v.getY())-(y*v.getX()-(z*v.getZ()));
	}
	
	public double magnitude(){
		return (double)Math.sqrt((x*x)+(y*y)+(z*z));
	}
	
	public double distance(Vector3d v){
		return (double)Math.sqrt(((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y))+((v.getZ()-z)*(v.getZ()-z)));
	}
	
	public double distanceSQ(Vector3d v){
		return (((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y))+((v.getZ()-z)*(v.getZ()-z)));
	}
	
	public String toString(){
		return "[" + x + "|" + y + "|" + z + "]";
	}

}
