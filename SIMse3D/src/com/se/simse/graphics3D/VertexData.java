package com.se.simse.graphics3D;

import java.util.Random;

import com.se.simse.math.vector.Vector2f;
import com.se.simse.math.vector.Vector3f;

/**
 * Object that stores and manipulates vertex data.
 * @author Marcel Simader
 */
public class VertexData {
	
	private Vector3f[] pos;
	private Vector3f[] normal;
	private Vector3f[] vindicies;
	private Vector3f[] nindicies;
	private Vector3f[] uv;
	private Vector3f[] uvindices;
	private Vector3f position;
	private Vector3f scale;
	private Vector3f rotation;
	
	/**
	 * Instantiates a new {@code VertexData} object.
	 */
	public VertexData(){
		pos = new Vector3f[0];
		normal = new Vector3f[0];
		vindicies = new Vector3f[0];
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
	}
	
	public VertexData(Vector3f[] pos){
		this.pos = pos;
		normal = new Vector3f[0];
		vindicies = new Vector3f[0];
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
	}
	
	public VertexData(Vector3f[] pos, Vector3f[] indicies){
		this.pos = pos;
		normal = new Vector3f[0];
		this.vindicies = indicies;
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
	}
	
	public VertexData(Vector3f[] pos, Vector3f[] normal, Vector3f[] indicies){
		this.pos = pos;
		this.normal = normal;
		this.vindicies = indicies;
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
	}
	
	public VertexData(Vector3f[] pos, Vector3f[] normal, Vector3f[] indicies, Vector3f[] nindicies){
		this.pos = pos;
		this.normal = normal;
		this.vindicies = indicies;
		this.nindicies = nindicies;
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
	}
	
	public VertexData(Vector3f[] pos, Vector3f[] normal, Vector3f[] indicies, Vector3f[] nindicies, Vector3f[] uv){
		this.pos = pos;
		this.normal = normal;
		this.vindicies = indicies;
		this.nindicies = nindicies;
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
		this.uv = uv;
	}
	
	public VertexData(Vector3f[] pos, Vector3f[] normal, Vector3f[] indicies, Vector3f[] nindicies, Vector3f[] uvindicies, Vector3f[] uv){
		this.pos = pos;
		this.normal = normal;
		this.vindicies = indicies;
		this.nindicies = nindicies;
		this.position = new Vector3f();
		this.scale = new Vector3f(1,1,1);
		this.rotation = new Vector3f();
		this.uv = uv;
		this.uvindices = uvindicies;
	}

	public Vector3f[] getPos() {
		return pos;
	}

	public void setPos(Vector3f[] pos) {
		this.pos = pos;
	}

	public Vector3f[] getNormal() {
		return normal;
	}

	public void setNormal(Vector3f[] normal) {
		this.normal = normal;
	}

	public Vector3f[] getvIndicies() {
		return vindicies;
	}

	public void setvIndicies(Vector3f[] indicies) {
		this.vindicies = indicies;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f[] getnIndicies() {
		return nindicies;
	}

	public void setnIndicies(Vector3f[] nindicies) {
		this.nindicies = nindicies;
	}

	public Vector3f getScale() {
		return scale;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}
	
	public void setUV(Vector3f[] uv){
		this.uv = uv;
	}
	
	public Vector3f[] getUV(){
		return uv;
	}
	
	public void setUVIndices(Vector3f[] uvindices){
		this.uvindices = uvindices;
	}
	
	public Vector3f[] getUVIndices(){
		return uvindices;
	}
	
	/**
	 * Generates random {@code VertexData}
	 * @param numbVertices Number of generated points
	 * @param numbIndicies Number of generated triangles
	 * @param smooth Sets the number of normals / triangle to 3
	 * @return 
	 */
	public VertexData generateRandom(int numbVertices, int numbIndicies, boolean smooth){
		if(numbVertices<1||numbIndicies<1){return null;}
		Random r = new Random();
		Vector3f[] po = new Vector3f[numbVertices];
		Vector3f[] no = new Vector3f[numbVertices];
		Vector3f[] poin = new Vector3f[numbIndicies];
		Vector3f[] noin = new Vector3f[numbIndicies];
		for(int i=0;i<numbVertices;i++){
			po[i] = new Vector3f((r.nextFloat()*2f)-1f,(r.nextFloat()*2f)-1f,(r.nextFloat()*2f)-1f);
			no[i] = new Vector3f((r.nextFloat()*2f)-1f,(r.nextFloat()*2f)-1f,(r.nextFloat()*2f)-1f);
		}
		for(int i=0;i<numbIndicies;i++){
			poin[i] = new Vector3f(r.nextInt(numbVertices),r.nextInt(numbVertices),r.nextInt(numbVertices));
			if(smooth){noin[i] = new Vector3f(r.nextInt(numbVertices),r.nextInt(numbVertices),r.nextInt(numbVertices));}else{int rand = r.nextInt(numbVertices);noin[i] = new Vector3f(rand,rand,rand);}
		}
		return new VertexData(po,no,poin,noin);
	}

}
