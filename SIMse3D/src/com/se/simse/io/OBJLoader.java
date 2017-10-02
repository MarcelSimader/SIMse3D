package com.se.simse.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Synthesizer;

import com.se.simse.graphics3D.VertexData;
import com.se.simse.math.vector.Vector3f;

public class OBJLoader {
	
	public static VertexData loadObj(String fileName, boolean importUV){
		FileReader fr = null;
		try {
			fr = new FileReader(new File(fileName));
		} catch (FileNotFoundException e) {System.err.println("[OBJLoader]: IOException at FileReader (" + fileName + ")");e.printStackTrace();}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Vector3f> indices = new ArrayList<Vector3f>();
		List<Vector3f> nindices = new ArrayList<Vector3f>();
		List<Vector3f> uv = new ArrayList<Vector3f>();
		List<Vector3f> uvindices = new ArrayList<Vector3f>();
		Vector3f[] verticesArray = null;
		Vector3f[] normalsArray = null;
		Vector3f[] indicesArray = null;
		Vector3f[] nindicesArray = null;
		Vector3f[] uvArray = null;
		Vector3f[] uvindicesArray = null;
		
		try{
			while(true){
				line = reader.readLine();
				if(line==null){
					break;
				}
				String[] currentLine = line.split(" ");
				String[] currentLine1 = line.split(" ");
				if(line.startsWith("v ")){
					Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),Float.parseFloat(currentLine[2]),Float.parseFloat(currentLine[3]));
					vertices.add(vertex);
				}else if(line.startsWith("vn ")){
					Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),Float.parseFloat(currentLine[2]),Float.parseFloat(currentLine[3]));
					normals.add(vertex);
				}else if(line.startsWith("f ")){
					Vector3f indice = new Vector3f(Float.parseFloat(currentLine1[1].split("/")[0]),Float.parseFloat(currentLine1[2].split("/")[0]),Float.parseFloat(currentLine1[3].split("/")[0])); 
					indices.add(indice);
					if(importUV){Vector3f uvindice = new Vector3f(Float.parseFloat(currentLine1[1].split("/")[1]),Float.parseFloat(currentLine1[2].split("/")[1]),Float.parseFloat(currentLine1[3].split("/")[1]));  
					uvindices.add(uvindice);}
					Vector3f nindice = new Vector3f(Float.parseFloat(currentLine1[1].split("/")[2]),Float.parseFloat(currentLine1[2].split("/")[2]),Float.parseFloat(currentLine1[3].split("/")[2]));  
					nindices.add(nindice);
				}else if(line.startsWith("vt ") && importUV){
					float u = Float.parseFloat(currentLine1[1].split(" ")[0]);
					float v = Float.parseFloat(currentLine1[2].split(" ")[0]);
					/**while(u<0f){u+=1f;}
					while(u>1f){u-=1f;}
					while(v<0f){v+=1f;}
					while(v>1f){v-=1f;}*/
					Vector3f uvCoord = new Vector3f(u,v,0); 
					uv.add(uvCoord);
				}
			}
			reader.close();
		}catch(Exception e){System.err.println("[OBJLoader]: Error loading file (" + fileName + ".obj)");e.printStackTrace();}
		
		verticesArray = new Vector3f[vertices.size()];
		normalsArray = new Vector3f[normals.size()];
		indicesArray = new Vector3f[indices.size()];
		nindicesArray = new Vector3f[nindices.size()];
		uvArray = new Vector3f[uv.size()];
		uvindicesArray = new Vector3f[uvindices.size()];
		
		int vP = 0;
		for(Vector3f vertex : vertices){
			verticesArray[vP] = vertex;
			vP++;
		}
		
		vP = 0;
		for(Vector3f normal : normals){
			normalsArray[vP] = normal;
			vP++;
		}
		
		vP = 0;
		for(Vector3f indice : indices){
			indicesArray[vP] = indice.subN(1f);
			vP++;
		}
		
		vP = 0;
		for(Vector3f nindice : nindices){
			nindicesArray[vP] = nindice.subN(1f);
			vP++;
		}
		
		if(importUV){
			vP = 0;
			for(Vector3f uvCoord : uv){
				uvArray[vP] = (uvCoord.multN(64f));
				float u = Math.min(Math.max(64-uvArray[vP].getX(),0),64);
				float v = Math.min(Math.max(64-uvArray[vP].getY(),0),64);
				uvArray[vP] = new Vector3f(u,v,0);
				vP++;
			}
			vP = 0;
			for(Vector3f uvindiceCoord : uvindices){
				uvindicesArray[vP] = uvindiceCoord.subN(1f);
				vP++;
			}
		}
		
		if(importUV){
			return new VertexData(verticesArray, normalsArray, indicesArray, nindicesArray, uvindicesArray, uvArray);
		}else{
			return new VertexData(verticesArray, normalsArray, indicesArray, nindicesArray);
		}
	}

}
