package com.se.simse.math.vector;

import java.util.Random;

public class Vector2f {
	
	private float x;
	private float y;
	
	/**
	 * Instantiates a new {@code Vector2f} at 0,0
	 */
	public Vector2f(){
		x = 0; y = 0;
	}
	
	public Vector2f(double x, double y){
		this.x = (float)x;
		this.y = (float)y;
	}
	
	public Vector2f(int x, int y){
		this.x = (float)x;
		this.y = (float)y;
	}
	
	public Vector2f(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public Vector2f(Vector2f v){
		this.x = v.getX();
		this.y = v.getY();
	}
	
	public Vector2f(Vector3f v){
		this.x = v.getX();
		this.y = v.getY();
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public void setX(float x){
		this.x = x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public void random(){
		x = (new Random().nextFloat()*2f)-1f;
		y = (new Random().nextFloat()*2f)-1f;
	}
	
	public void randomN(){
		new Vector2f((new Random().nextFloat()*2f)-1f,
		(new Random().nextFloat()*2f)-1f);
	}
	
	public Vector2f normalized(){
		return new Vector2f(x/this.magnitude(), y/this.magnitude());
	}
	
	public void normalize(){
		x/=this.magnitude(); y/=this.magnitude();
	}
	
	public void add(Vector2f v){
		x+=v.getX();y+=v.getY();
	}
	
	public void add(double v){
		x+=v;y+=v;
	}
	public Vector2f addN(Vector2f v){
		return new Vector2f(x+v.getX(),y+v.getY());
	}
	
	public Vector2f addN(double v){
		return new Vector2f(x+v,y+v);
	}
	
	public void sub(Vector2f v){
		x-=v.getX();y-=v.getY();
	}
	
	public void sub(double v){
		x-=v;y-=v;
	}
	public Vector2f subN(Vector2f v){
		return new Vector2f(x-v.getX(),y-v.getY());
	}
	
	public Vector2f subN(double v){
		return new Vector2f(x-v,y-v);
	}
	
	public void mult(Vector2f v){
		x*=v.getX();y*=v.getY();
	}
	
	public void mult(double v){
		x*=v;y*=v;
	}
	public Vector2f multN(Vector2f v){
		return new Vector2f(x*v.getX(),y*v.getY());
	}
	
	public Vector2f multN(double v){
		return new Vector2f(x*v,y*v);
	}
	
	public void div(Vector2f v){
		x/=v.getX();y/=v.getY();
	}
	
	public void div(double v){
		x/=v;y/=v;
	}
	public Vector2f divN(Vector2f v){
		return new Vector2f(x/v.getX(),y/v.getY());
	}
	
	public Vector2f divN(double v){
		return new Vector2f(x/v,y/v);
	}
	
	public float dot(Vector2f v){
		return x*v.getX()+y*v.getY();
	}
	
	public float cross(Vector2f v){
		return (x*v.getY())-(y*v.getX());
	}
	
	public float magnitude(){
		return (float)Math.sqrt((x*x)+(y*y));
	}
	
	public float distance(Vector2f v){
		return (float)Math.sqrt(((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y)));
	}
	
	public float distanceSQ(Vector2f v){
		return (((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y)));
	}
	
	/**public boolean isPointInTriangle(Vector2f a, Vector2f b, Vector2f c){
		float as_x = x-a.getX();
		float as_y = y-a.getY();
		
		boolean s_ab = (b.getX()-a.getX())*as_y-(b.getY()-a.getY())*as_x > 1;
		if((c.getX()-a.getX())*as_y-(c.getY()-a.getY())*as_x>1 == s_ab) { return false;}
		if((c.getX()-b.getX())*(y-b.getY())-(c.getY()-b.getY())*(x-b.getX()) > 1 != s_ab) { return false;}
		return true;
	}*/
	
	public boolean isPointInTriangle(Vector2f a, Vector2f b, Vector2f c){
		Vector2f v0 = c.subN(a);
		Vector2f v1 = b.subN(a);
		Vector2f v2 = subN(a);
		
		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);
		
		float invDenom = 1f/(dot00*dot11-dot01*dot01);
		float u = (dot11*dot02-dot01*dot12)*invDenom;
		float v = (dot00*dot12-dot01*dot02)*invDenom;
		
		return (u>=0)&&(v>=0)&&(u+v<=1);
	}
	
	public String toString(){
		return "[" + x + "|" + y + "]";
	}

}
