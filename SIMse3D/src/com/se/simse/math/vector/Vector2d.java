package com.se.simse.math.vector;

import java.util.Random;

public class Vector2d {
	
	private double x;
	private double y;
	
	/**
	 * Instantiates a new {@code Vector2d} at 0,0
	 */
	public Vector2d(){
		x = 0; y = 0;
	}
	
	public Vector2d(float x, float y){
		this.x = (double)x;
		this.y = (double)y;
	}
	
	public Vector2d(int x, int y){
		this.x = (double)x;
		this.y = (double)y;
	}
	
	public Vector2d(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Vector2d(Vector2d v){
		this.x = v.getX();
		this.y = v.getY();
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public void set(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public void random(){
		x = (new Random().nextInt()*2f)-1f;
		y = (new Random().nextInt()*2f)-1f;
	}
	
	public void randomN(){
		new Vector2d((new Random().nextDouble()*2f)-1f,
		(new Random().nextDouble()*2f)-1f);
	}
	
	public Vector2d normalized(){
		return new Vector2d(x/this.magnitude(), y/this.magnitude());
	}
	
	public void normalize(){
		x/=this.magnitude(); y/=this.magnitude();
	}
	
	public void add(Vector2d v){
		x+=v.getX();y+=v.getY();
	}
	
	public void add(double v){
		x+=v;y+=v;
	}
	public Vector2d addN(Vector2d v){
		return new Vector2d(x+v.getX(),y+v.getY());
	}
	
	public Vector2d addN(double v){
		return new Vector2d(x+v,y+v);
	}
	
	public void sub(Vector2d v){
		x-=v.getX();y-=v.getY();
	}
	
	public void sub(double v){
		x-=v;y-=v;
	}
	public Vector2d subN(Vector2d v){
		return new Vector2d(x-v.getX(),y-v.getY());
	}
	
	public Vector2d subN(double v){
		return new Vector2d(x-v,y-v);
	}
	
	public void mult(Vector2d v){
		x*=v.getX();y*=v.getY();
	}
	
	public void mult(double v){
		x*=v;y*=v;
	}
	public Vector2d multN(Vector2d v){
		return new Vector2d(x*v.getX(),y*v.getY());
	}
	
	public Vector2d multN(double v){
		return new Vector2d(x*v,y*v);
	}
	
	public void div(Vector2d v){
		x/=v.getX();y/=v.getY();
	}
	
	public void div(double v){
		x/=v;y/=v;
	}
	public Vector2d divN(Vector2d v){
		return new Vector2d(x/v.getX(),y/v.getY());
	}
	
	public Vector2d divN(double v){
		return new Vector2d(x/v,y/v);
	}
	
	public double dot(Vector2d v){
		return x*v.getX()+y*v.getY();
	}
	
	public double cross(Vector2d v){
		return (x*v.getY())-(y*v.getX());
	}
	
	public double magnitude(){
		return (double)Math.sqrt((x*x)+(y*y));
	}
	
	public double distance(Vector2d v){
		return (double)Math.sqrt(((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y)));
	}
	
	public double distanceSQ(Vector2d v){
		return (((v.getX()-x)*(v.getX()-x))+((v.getY()-y)*(v.getY()-y)));
	}
	
	public String toString(){
		return "[" + x + "|" + y + "]";
	}

}
