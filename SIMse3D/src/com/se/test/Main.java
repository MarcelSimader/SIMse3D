package com.se.test;

import static com.se.simse.graphics.DrawUtils.drawDebug;
import static com.se.simse.graphics.DrawUtils.fillScreen;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.swing.JFrame;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

import com.se.simse.SIMMain;
import com.se.simse.graphics.DrawUtils;
import com.se.simse.graphics.Texture;
import com.se.simse.graphics3D.VertexData;
import com.se.simse.gui.objects.SIMPanel;
import com.se.simse.io.KernelLoader;
import com.se.simse.io.OBJLoader;
import com.se.simse.io.hid.SIMKey;
import com.se.simse.io.hid.SIMKeyEvent;
import com.se.simse.io.hid.SIMMouse;
import com.se.simse.io.hid.SIMMouseEvent;
import com.se.simse.io.hid.SIMMouseMotion;
import com.se.simse.io.hid.SIMMouseMotionEvent;
import com.se.simse.math.Triangle;
import com.se.simse.math.matrix.Matrix4f;
import com.se.simse.math.vector.Vector2f;
import com.se.simse.math.vector.Vector3f;

public class Main {
	
	private static SIMMain sim;
	private static Matrix4f proj = new Matrix4f();
	 
	static VertexData[] vertexDataArray = new VertexData[32];
	static BufferedImage bf;
	private static float res = 2f;
	private static Cursor cursor;

	static float[] zBuffer;
	static int i = 0;
	
	private static boolean wireFrame = false;
	private static Vector2f rotation = new Vector2f();
	private static float transSpeed = 0.02f;
	private static Vector3f position = new Vector3f();
	private static Matrix4f camera,camera2;
	private static Vector3f light = new Vector3f(0,0,0);
	
	private static boolean isWDown,isADown,isSDown,isDDown,isShiftDown,isSpaceDown,isQDown,isEDown;
	
	private static CLContext context;
	private static CLPlatform platform;
	private static List<CLDevice> devices;
	private static CLCommandQueue queue;
	private static CLKernel kernel;
	private static CLProgram program;
	private final static int dimensions = 2;
	private static PointerBuffer globalWorkSize, localWorkSize;
	private static CLMem x1M,resultColorM,vtCountM,nM,cM,lpM,rM, tM, cBM;
	private static FloatBuffer x1B,nB,vtCountB,cBuffer,lpBuffer,rB, cB;
	private static IntBuffer rColorBuff, tBuffer;
	
	private static Texture tex;

	public static void main(String[] args) {
		/** setup SIMMain and frame*/
		sim = new SIMMain("Test", (Thread thread) -> update(thread), (Thread thread, Graphics graphics) -> render(thread, graphics));
		sim.setStepTimeSeconds(0.01f);
		sim.setDimension(new Vector2f(1280,720));
		JFrame f = sim.getFrame();
		f.setLocation(100, 100); 
		f.setSize(1270, 720);
		f.setTitle(sim.getID());
		
		f.addKeyListener(new SIMKey((Integer t, SIMKeyEvent k) -> kInput(t,k)));
		f.addMouseListener(new SIMMouse((MouseEvent e, SIMMouseEvent m) -> mInput(e,m)));
		f.addMouseMotionListener(new SIMMouseMotion((MouseEvent e, SIMMouseMotionEvent m) -> mmInput(e,m)));
		
		BufferedImage cursorBF = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorBF, new Point(0,0), "cursor");
		f.getContentPane().setCursor(cursor);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		/** setup GUI*/
		SIMPanel panel = new SIMPanel(100,100,1000,300,new Color(0.1f,1f,0.2f,0.3f), true);
		//sim.addGUIElement(panel);
		
		/** init OpenCL*/
		try {
			initOpenCL();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		/** load textures*/
		tex = new Texture();
		tex.importTexture("src/res/textures/testmap1.png");
		tBuffer = BufferUtils.createIntBuffer((tex.getImage().getHeight()*tex.getImage().getWidth()));
		for(int y=0;y<tex.getImage().getHeight();y++){
			for(int x=0;x<tex.getImage().getWidth();x++){
				int rgb = tex.getImage().getRGB(x, y);
				//System.out.println("bin: " +((rgb>>16)&0xFF)+"-"+((rgb>>8)&0xFF)+"-"+((rgb>>0)&0xFF));
				tBuffer.put(rgb);
			}
		}
		tBuffer.rewind();
		
		/** setup vertexData*/
		vertexDataArray[0] = OBJLoader.loadObj("src/res/torusuv.obj", true);
		vertexDataArray[0].setPosition(new Vector3f(0,0,3));
		
		vertexDataArray[1] = new VertexData(new Vector3f[]{new Vector3f(-1,0,-1),new Vector3f(-1,0,1),new Vector3f(1,0,-1),new Vector3f(1,0,1)}, 
				new Vector3f[]{new Vector3f(0,1,0)},
				new Vector3f[]{new Vector3f(0,1,3),new Vector3f(0,2,3)},
				new Vector3f[]{new Vector3f(0,0,0),new Vector3f(0,0,0)},
				new Vector3f[]{new Vector3f(0,1,3),new Vector3f(0,2,3)},
				new Vector3f[]{new Vector3f(0,0,0),new Vector3f(64,0,0),new Vector3f(0,64,0),new Vector3f(64,64,0)});
		vertexDataArray[1].setPosition(new Vector3f(0,-2,0));
		vertexDataArray[1].setScale(new Vector3f(1,1,1));
		
		vertexDataArray[2] = OBJLoader.loadObj("src/res/smooth.obj", true);
		vertexDataArray[2].setScale(new Vector3f(1,1,1));
		vertexDataArray[2].setPosition(new Vector3f(0,2,4));
		
		/**vertexDataArray[2] = OBJLoader.loadObj("src/res/sphere.obj");
		vertexDataArray[2].setPosition(new Vector3f(2,0,1));*/
		
		/**for(int i=0;i<4;i++){
			vertexDataArray[i] = OBJLoader.loadObj("src/res/smooth.obj");
			vertexDataArray[i].setPosition(new Vector3f().randomN().multN(2));
		}*/
		
		//vertexDataArray[0] = new VertexData().generateRandom(100, 6, true);
		
		/** start simse*/
		sim.start();
	}
	
	private static void initOpenCL() throws LWJGLException {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
	    CL.create();
	    platform = CLPlatform.getPlatforms().get(0); 
	    devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
	    context = CLContext.create(platform, devices, errorBuff);
	    queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuff);
	    Util.checkCLError(errorBuff.get(0)); 
	    
	    System.out.println("[Main] Device Name: " + devices.get(0).getInfoString(CL10.CL_DEVICE_NAME));
	    System.out.println("[Main] Device Platform: " + devices.get(0).getInfoInt(CL10.CL_DEVICE_PLATFORM));
	    System.out.println("[Main] Device Version: " + devices.get(0).getInfoString(CL10.CL_DEVICE_VERSION));
	    System.out.println("[Main] Device Vendor: " + devices.get(0).getInfoString(CL10.CL_DEVICE_VENDOR));
	    System.out.println("[Main] Max work group size: " + devices.get(0).getInfoSizeArray(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES)[0]+" / "
	    												  +devices.get(0).getInfoSizeArray(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES)[1]+" / "
	    											      +devices.get(0).getInfoSizeArray(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES)[2]);
	    
	    program = CL10.clCreateProgramWithSource(context, KernelLoader.loadKernel("src/com/se/test/kernels/fragmentShader.cls"), null);
	    int error = CL10.clBuildProgram(program, devices.get(0), "", null);
		Util.checkCLError(error);
		
		globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, (int)((sim.getDimension().getX())/res));
		globalWorkSize.put(1, (int)((sim.getDimension().getY())/res));
		
		localWorkSize = BufferUtils.createPointerBuffer(dimensions);
		localWorkSize.put(0, (int)((32)));
		localWorkSize.put(1, (int)((18)));
		
		resultColorM = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, (int) ((sim.getDimension().getX()*(int)sim.getDimension().getY())/res),null);
		rColorBuff = BufferUtils.createIntBuffer((int) ((sim.getDimension().getX()*(int)sim.getDimension().getY())/res));
		zBuffer = new float[(int) ((sim.getDimension().getX()*sim.getDimension().getY())/res)];
	}
	
	private static void kInput(int t, SIMKeyEvent k){
		if(k == SIMKeyEvent.RELEASED){
			if(t==KeyEvent.VK_F1){wireFrame = !wireFrame;}
			if(t==KeyEvent.VK_W){isWDown=false;}
			if(t==KeyEvent.VK_A){isADown=false;}
			if(t==KeyEvent.VK_S){isSDown=false;}
			if(t==KeyEvent.VK_D){isDDown=false;}
			if(t==KeyEvent.VK_SHIFT){isShiftDown=false;}
			if(t==KeyEvent.VK_SPACE){isSpaceDown=false;}
			if(t==KeyEvent.VK_Q){isQDown=false;}
			if(t==KeyEvent.VK_E){isEDown=false;}
		}
		if(k == SIMKeyEvent.PRESSED){
			if(t==KeyEvent.VK_W){isWDown=true;}
			if(t==KeyEvent.VK_A){isADown=true;}
			if(t==KeyEvent.VK_S){isSDown=true;}
			if(t==KeyEvent.VK_D){isDDown=true;}
			if(t==KeyEvent.VK_SHIFT){isShiftDown=true;}
			if(t==KeyEvent.VK_SPACE){isSpaceDown=true;}
			if(t==KeyEvent.VK_Q){isQDown=true;}
			if(t==KeyEvent.VK_E){isEDown=true;}
		}
	}
	
	private static void mInput(MouseEvent e, SIMMouseEvent t){
	}
	
	private static void mmInput(MouseEvent e, SIMMouseMotionEvent t){
		if(t==SIMMouseMotionEvent.MOVED){
			if(sim.getFrame().getContentPane().getCursor()!=cursor){sim.getFrame().getContentPane().setCursor(cursor);}
			try {
				Vector2f mousePosO = new Vector2f(e.getX(),e.getY());
				Vector2f cent = new Vector2f(sim.getFrame().getLocation().getX()+(sim.getDimension().getX()*0.5f),
											 sim.getFrame().getLocation().getY()+(sim.getDimension().getY()*0.5f));
				Robot r = new Robot();
				r.mouseMove((int)cent.getX(), (int)cent.getY());
				Vector2f mousePosDelta = cent.subN(mousePosO).subN(100);
				rotation.sub(mousePosDelta.multN(0.002f));
			} catch (AWTException e1) {e1.printStackTrace();}
		}else if(t==SIMMouseMotionEvent.DRAGED){
			sim.getFrame().getContentPane().setCursor(Cursor.getDefaultCursor());
		}
	}
	
	private static void update(Thread t){
		if(isWDown){position.add(new Vector3f(transSpeed*Math.sin(rotation.getX()),0,-transSpeed*Math.cos(rotation.getX())));}
		if(isSDown){position.add(new Vector3f(-transSpeed*Math.sin(rotation.getX()),0,transSpeed*Math.cos(rotation.getX())));}
		if(isADown){position.add(new Vector3f(-transSpeed*Math.cos(rotation.getX()),0,-transSpeed*Math.sin(rotation.getX())));}
		if(isDDown){position.add(new Vector3f(transSpeed*Math.cos(rotation.getX()),0,transSpeed*Math.sin(rotation.getX())));}
		if(isShiftDown){position.add(new Vector3f(0,transSpeed,0));}
		if(isSpaceDown){position.add(new Vector3f(0,-transSpeed,0));}
		if(isQDown){rotation.sub(new Vector2f(transSpeed,0));}
		if(isEDown){rotation.add(new Vector2f(transSpeed,0));}
		
		//vertexDataArray[0].setPosition(new Vector3f(Math.sin(sim.getTicks()*0.01f),0,Math.cos((sim.getTicks()*0.01f))));
		//vertexDataArray[1].setScale(new Vector3f(1f,Math.sin(sim.getTicks()*0.01f),1f));
		vertexDataArray[2].setRotation(new Vector3f(Math.sin(sim.getTicks()*0.01f),Math.cos(sim.getTicks()*0.01f),Math.sin(sim.getTicks()*0.01f)));
	}
	
	private static void render(Thread t, Graphics g){
		/** fill screen with white - set color to black*/
		fillScreen(g, sim.getDimension(), Color.black);
		
		/** setup perspective matrix*/
		proj.perspective(sim.getDimension().getX()/sim.getDimension().getY(), 70f, 1000f, 10f);
		
		/** setup camera*/
		camera = new Matrix4f();
		camera = camera.identity();
		camera2 = new Matrix4f();
		camera2 = camera2.identity();
		camera.translate(position);
		camera.rotate(new Vector3f(0,1,0),rotation.getX());
		camera2.rotate(new Vector3f(1,0,0),(float)(rotation.getY()));

		/** setup light*/
		Matrix4f lightMatrix = new Matrix4f(light);
		lightMatrix.translate(new Vector3f(1,0,0));
		Matrix4f f1 = new Matrix4f();f1=f1.identity();f1.rotate(new Vector3f(0,0,1), sim.getTicks()*0.01f);
		lightMatrix.mult(f1);
		Vector3f transLight = new Vector3f(lightMatrix.m[3+0*4], lightMatrix.m[3+1*4], lightMatrix.m[3+2*4]);
		
		int vertices = 0;
		for(int vertexIndex=vertexDataArray.length-1;vertexIndex>=0;vertexIndex--){
			VertexData vd = vertexDataArray[vertexIndex];
			if(vd!=null){
				vertices+=vd.getvIndicies().length;
			}
		}
		
		cBuffer = BufferUtils.createFloatBuffer(7);
		/**cBuffer.put(camera.m[3+0*4]);
		cBuffer.put(camera.m[3+1*4]);
		cBuffer.put(camera.m[3+2*4]);*/
		cBuffer.put(position.getX());
		cBuffer.put(position.getY());
		cBuffer.put(position.getZ());
		cBuffer.put(0);
		cBuffer.put(rotation.getX());
		cBuffer.put(rotation.getY());
		cBuffer.rewind();
		
		lpBuffer = BufferUtils.createFloatBuffer(4);
		lpBuffer.put(-transLight.getX());
		lpBuffer.put(-transLight.getY());
		lpBuffer.put(-transLight.getZ());
		lpBuffer.rewind();
		
		vtCountB = BufferUtils.createFloatBuffer(3);
		vtCountB.put(vertices);
		vtCountB.put(res);
		vtCountB.rewind();
		
		x1B = BufferUtils.createFloatBuffer(vertices*15);
		nB = BufferUtils.createFloatBuffer(vertices*15);
		rB = BufferUtils.createFloatBuffer(vertices*15);
		cB = BufferUtils.createFloatBuffer(vertices*15);
		
		for(int vertexIndex=vertexDataArray.length-1;vertexIndex>=0;vertexIndex--){
			VertexData vd = vertexDataArray[vertexIndex];
			if(vd!=null){
			
			/** apply position matrices*/
			Matrix4f[] newPositions = new Matrix4f[vd.getPos().length];
			Matrix4f[] realPositions = new Matrix4f[vd.getPos().length];
			Matrix4f[] camPositions = new Matrix4f[vd.getPos().length];
			/** apply normal matrices*/		
			Vector3f[] normT = new Vector3f[vd.getNormal().length];
			
			/** setup matrix*/
			Matrix4f rot = new Matrix4f();rot = rot.identity();
			Matrix4f rot1 = new Matrix4f();rot1 = rot1.identity();
			Matrix4f rot2 = new Matrix4f();rot2 = rot2.identity();
			rot.rotate(new Vector3f(1,0,0), vd.getRotation().getX());
			rot1.rotate(new Vector3f(0,1,0), vd.getRotation().getY());
			rot2.rotate(new Vector3f(0,0,1), vd.getRotation().getZ());
			
			Matrix4f scale = new Matrix4f(); scale = scale.identity();
			scale.scale(vd.getScale());
			
			for(i=vd.getPos().length-1;i>=0;i--){
				Vector3f f = vd.getPos()[i];
				Matrix4f ma = new Matrix4f(f);
				ma.mult(scale);
				ma.mult(rot);ma.mult(rot1);ma.mult(rot2);
				ma.translate(vd.getPosition().addN(position));
				ma.mult(camera);
				ma.mult(camera2);
				ma.mult(proj);
				
				newPositions[i] = ma;
				
				ma = new Matrix4f(f);
				ma.mult(scale);
				ma.mult(rot);ma.mult(rot1);ma.mult(rot2);
				ma.translate(vd.getPosition());
				
				realPositions[i] = ma;
				
				ma = new Matrix4f(f);
				ma.mult(scale);
				ma.mult(rot);ma.mult(rot1);ma.mult(rot2);
				ma.translate(vd.getPosition().addN(position));
				ma.mult(camera);
				ma.mult(camera2);
				
				camPositions[i] = ma;
			}
			
			for(i=vd.getNormal().length-1;i>=0;i--){
				Vector3f f = vd.getNormal()[i];
				Matrix4f mab = new Matrix4f(f);
				mab.mult(scale);
				mab.mult(rot);mab.mult(rot1);mab.mult(rot2);
				normT[i] = new Vector3f(mab.m[3+0*4], mab.m[3+1*4], mab.m[3+2*4]);
			}
			
			Triangle[] triangles = new Triangle[vd.getvIndicies().length];
			Triangle[] camtriangles = new Triangle[vd.getvIndicies().length];
			Triangle[] normaltriangles = new Triangle[vd.getvIndicies().length];
			Triangle[] realtriangles = new Triangle[vd.getvIndicies().length];
			Triangle[] textriangles = null;
			if(vd.getUV()!=null){textriangles = new Triangle[vd.getUV().length];}
			Vector3f[] wTri = new Vector3f[vd.getvIndicies().length];
			
			for(i=vd.getvIndicies().length-1;i>=0;i--){
				
				/** create triangles*/
				int x = 0,y = 0,z = 0;
				Triangle tr = null;
				if(vd.getvIndicies()!=null){
						x = (int)vd.getvIndicies()[i].getX();
						y = (int)vd.getvIndicies()[i].getY();
						z = (int)vd.getvIndicies()[i].getZ();
						
					tr = new Triangle(new Vector3f(newPositions[x].m[3+0*4],newPositions[x].m[3+1*4],newPositions[x].m[3+2*4]),
									  new Vector3f(newPositions[y].m[3+0*4],newPositions[y].m[3+1*4],newPositions[y].m[3+2*4]),
									  new Vector3f(newPositions[z].m[3+0*4],newPositions[z].m[3+1*4],newPositions[z].m[3+2*4]));
					triangles[i] = tr;
					tr = new Triangle(new Vector3f(realPositions[x].m[3+0*4],realPositions[x].m[3+1*4],realPositions[x].m[3+2*4]),
							  		  new Vector3f(realPositions[y].m[3+0*4],realPositions[y].m[3+1*4],realPositions[y].m[3+2*4]),
							  		  new Vector3f(realPositions[z].m[3+0*4],realPositions[z].m[3+1*4],realPositions[z].m[3+2*4]));
					realtriangles[i] = tr;
					tr = new Triangle(new Vector3f(camPositions[x].m[3+0*4],camPositions[x].m[3+1*4],camPositions[x].m[3+2*4]),
							   		  new Vector3f(camPositions[y].m[3+0*4],camPositions[y].m[3+1*4],camPositions[y].m[3+2*4]),
							   		  new Vector3f(camPositions[z].m[3+0*4],camPositions[z].m[3+1*4],camPositions[z].m[3+2*4]));
					camtriangles[i] = tr;
					wTri[i] = new Vector3f(newPositions[x].m[3+3*4],newPositions[y].m[3+3*4],newPositions[z].m[3+3*4]);
				}
			
				/** create normal triangles*/
				x = 0;y = 0;z = 0;
				if(vd.getnIndicies()!=null){
					x = (int)vd.getnIndicies()[i].getX();
					y = (int)vd.getnIndicies()[i].getY();
					z = (int)vd.getnIndicies()[i].getZ();
					tr = new Triangle(new Vector3f(normT[x].getX(),normT[x].getY(),normT[x].getZ()),
									  new Vector3f(normT[y].getX(),normT[y].getY(),normT[y].getZ()),
									  new Vector3f(normT[z].getX(),normT[z].getY(),normT[z].getZ()));
					normaltriangles[i] = tr;
				}else{
					normaltriangles[i] = new Triangle(new Vector3f(1,0,0),new Vector3f(0,1,0),new Vector3f(0,0,1));
				}
				
				/** create uv triangles*/
				x = 0;y = 0;z = 0;
				if(vd.getUVIndices()!=null){
					x = (int)vd.getUVIndices()[i].getX();
					y = (int)vd.getUVIndices()[i].getY();
					z = (int)vd.getUVIndices()[i].getZ();
					tr = new Triangle(new Vector3f(vd.getUV()[x].getX(),vd.getUV()[x].getY(),0),
									  new Vector3f(vd.getUV()[y].getX(),vd.getUV()[y].getY(),0),
									  new Vector3f(vd.getUV()[z].getX(),vd.getUV()[z].getY(),0));
					textriangles[i] = tr;
				}else{
					textriangles[i] = new Triangle(new Vector3f(1,0,0),new Vector3f(0,1,0),new Vector3f(0,0,1));
				}
			}
			
			/** create triangle-buffer and screen coordinates*/
			for(i=vd.getvIndicies().length-1;i>=0;i--){
				/** get screen positions*/
				Triangle triangle = triangles[i];
				Triangle ntriangle = normaltriangles[i];
				Triangle rtriangle = realtriangles[i];
				Triangle ctriangle = realtriangles[i];
				Triangle ttriangle = null;
				if(textriangles!=null){ttriangle = textriangles[i];}
				
				if(wTri[i].getX()<0&&wTri[i].getY()<0&&wTri[i].getZ()<0){	
					if(rtriangle!=null){
		
						rB.put(rtriangle.getP1().getX());
						rB.put(rtriangle.getP1().getY());
						rB.put(rtriangle.getP1().getZ());
						
						rB.put(rtriangle.getP2().getX());
						rB.put(rtriangle.getP2().getY());
						rB.put(rtriangle.getP2().getZ());
						
						rB.put(rtriangle.getP3().getX());
						rB.put(rtriangle.getP3().getY());
						rB.put(rtriangle.getP3().getZ());
						
						rB.put(0);
						rB.put(0);
						
						rB.put(0);
						rB.put(0);
						
						rB.put(0);
						rB.put(0);
					}
					
					if(ctriangle!=null){
						cB.put(ctriangle.getP1().getX());
						cB.put(ctriangle.getP1().getY());
						cB.put(ctriangle.getP1().getZ());
						
						cB.put(ctriangle.getP2().getX());
						cB.put(ctriangle.getP2().getY());
						cB.put(ctriangle.getP2().getZ());
						
						cB.put(ctriangle.getP3().getX());
						cB.put(ctriangle.getP3().getY());
						cB.put(ctriangle.getP3().getZ());
						
						cB.put(0);
						cB.put(0);
						
						cB.put(0);
						cB.put(0);
						
						cB.put(0);
						cB.put(0);
					}
				
				if(triangle!=null){		
						x1B.put(triangle.getP1().getX());
						x1B.put(triangle.getP1().getY());
						x1B.put(triangle.getP2().getX());
						
						x1B.put(triangle.getP2().getY());
						x1B.put(triangle.getP3().getX());
						x1B.put(triangle.getP3().getY());
						
						x1B.put(triangle.getP1().getZ());
						x1B.put(triangle.getP2().getZ());
						x1B.put(triangle.getP3().getZ());
						
						x1B.put(0);
						x1B.put(0);
						
						x1B.put(0);
						x1B.put(0);
						
						x1B.put(0);
						x1B.put(0);
						
						if(ntriangle!=null){
							nB.put(ntriangle.getP1().getX());
							nB.put(ntriangle.getP1().getY());
							nB.put(ntriangle.getP1().getZ());
							
							nB.put(ntriangle.getP2().getX());
							nB.put(ntriangle.getP2().getY());
							nB.put(ntriangle.getP2().getZ());
							
							nB.put(ntriangle.getP3().getX());
							nB.put(ntriangle.getP3().getY());
							nB.put(ntriangle.getP3().getZ());
							
							if(ttriangle!=null){
							
								nB.put(ttriangle.getP1().getX());
								nB.put(ttriangle.getP1().getY());
								
								nB.put(ttriangle.getP2().getX());
								nB.put(ttriangle.getP2().getY());
								
								nB.put(ttriangle.getP3().getX());
								nB.put(ttriangle.getP3().getY());
							}else{
								nB.put(-1);
								nB.put(-1);
								
								nB.put(-1);
								nB.put(-1);
								
								nB.put(-1);
								nB.put(-1);
							}
						}
					}
				}
			}
			
			/** draw wireframe*/
			if(wireFrame){
				float ratiox = (sim.getDimension().getX()*0.5f); float ratioy = (sim.getDimension().getY()*0.5f);
				for(int i=vd.getvIndicies().length-1;i>=0;i--){
					if(wTri[i].getX()<0&&wTri[i].getY()<0&&wTri[i].getZ()<0){
						Triangle triangle = triangles[i];
						int x11 = (int) (triangle.getP1().getX()*(ratiox/triangle.getP1().getZ()));
						int y11 = (int) (triangle.getP1().getY()*(ratioy/triangle.getP1().getZ()));
						int x22 = (int) (triangle.getP2().getX()*(ratiox/triangle.getP2().getZ()));
						int y22 = (int) (triangle.getP2().getY()*(ratioy/triangle.getP2().getZ()));
						int x33 = (int) (triangle.getP3().getX()*(ratiox/triangle.getP3().getZ()));
						int y33 = (int) (triangle.getP3().getY()*(ratioy/triangle.getP3().getZ()));
						x11+=ratiox;x22+=ratiox;x33+=ratiox;
						y11+=ratioy;y22+=ratioy;y33+=ratioy;
						x11*=1.33f;y11*=1.33f;x22*=1.33f;y22*=1.33f;x33*=1.33f;y33*=1.33f;
						g.setColor(new Color(1f,0f,0f,0.1f));
						g.drawLine(x11,y11,x22,y22);
						g.drawLine(x22,y22,x33,y33);
						g.drawLine(x33,y33,x11,y11);
					}
				}
			}
			}
		}
		
		x1B.rewind();
		nB.rewind();
		rB.rewind();
		cB.rewind();
		
		if(!wireFrame){
			/** set kernel data*/
			resultColorM = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, (int) ((4*((sim.getDimension().getX()*(int)sim.getDimension().getY())))/res),null);
			IntBuffer errB = BufferUtils.createIntBuffer(1);
			int memPTR = CL10.CL_MEM_COPY_HOST_PTR;
			x1M = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, x1B, errB); Util.checkCLError(errB.get(0));
			rM = CL10.clCreateBuffer(context,  CL10.CL_MEM_WRITE_ONLY | memPTR, rB, errB); Util.checkCLError(errB.get(0));
			vtCountM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, vtCountB, errB); Util.checkCLError(errB.get(0));
			nM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, nB, errB); Util.checkCLError(errB.get(0));
			cM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, cBuffer, errB); Util.checkCLError(errB.get(0));
			lpM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, lpBuffer, errB); Util.checkCLError(errB.get(0));
			tM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, tBuffer, errB); Util.checkCLError(errB.get(0));
			cBM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, cB, errB); Util.checkCLError(errB.get(0));
			CLMem tempM = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, BufferUtils.createFloatBuffer((int)(((sim.getDimension().getX()*sim.getDimension().getY())/res))), errB); Util.checkCLError(errB.get(0));
			
			kernel = CL10.clCreateKernel(program, "render", errB); Util.checkCLError(errB.get(0));
			CL10.clSetKernelArg(kernel, 0, x1M);
			CL10.clSetKernelArg(kernel, 1, rM);
			CL10.clSetKernelArg(kernel, 2, nM);
			CL10.clSetKernelArg(kernel, 3, tempM);
			CL10.clSetKernelArg(kernel, 4, vtCountM);
			CL10.clSetKernelArg(kernel, 5, cM);
			CL10.clSetKernelArg(kernel, 6, lpM);
			CL10.clSetKernelArg(kernel, 7, tM);
			CL10.clSetKernelArg(kernel, 8, cBM);
			CL10.clSetKernelArg(kernel, 9, resultColorM);
			
			int err = CL10.clEnqueueNDRangeKernel(queue, kernel, dimensions, null, globalWorkSize, localWorkSize, null, null);Util.checkCLError(err);
			err = CL10.clEnqueueReadBuffer(queue, resultColorM, CL10.CL_TRUE, 0, rColorBuff, null, null);Util.checkCLError(err);
			CL10.clFinish(queue);
			
			CL10.clReleaseKernel(kernel);
			CL10.clReleaseMemObject(x1M);
			CL10.clReleaseMemObject(vtCountM);
			CL10.clReleaseMemObject(tempM);
			CL10.clReleaseMemObject(nM);
			CL10.clReleaseMemObject(cM);
			CL10.clReleaseMemObject(lpM);
			CL10.clReleaseMemObject(rM);
			CL10.clReleaseMemObject(resultColorM);

			/** draw pixels*/
			bf = new BufferedImage((int)(sim.getDimension().getX()/res),(int)(sim.getDimension().getY()/res),BufferedImage.TYPE_INT_RGB);int rgb;
			for(int yi=0;yi<bf.getHeight();yi++){
				for(int xi=0;xi<bf.getWidth();xi++){
					rgb = rColorBuff.get(xi+yi*bf.getWidth());
					bf.setRGB(xi, yi, rgb);
				}
			}
			
			if(bf!=null){
			((Graphics2D) g).drawRenderedImage(bf, AffineTransform.getScaleInstance(
					((sim.getDimension().getX()/bf.getWidth())*1.33f), ((sim.getDimension().getY()/bf.getHeight())*1.33f)));
			}
		}

		if(wireFrame){
			float ratiox = (sim.getDimension().getX()*0.5f); float ratioy = (sim.getDimension().getY()*0.5f);
			/** draw light*/
			g.setColor(new Color(1f,0f,0f,0.5f));
			Matrix4f lightPM = new Matrix4f(transLight);
			lightPM.translate(position.multN(-1f));
			Matrix4f rotLM = new Matrix4f(); rotLM = rotLM.identity(); rotLM.rotate(new Vector3f(0,1,0), rotation.getX());
			Matrix4f rotLM1 = new Matrix4f(); rotLM1 = rotLM1.identity(); rotLM1.rotate(new Vector3f(1,0,0), rotation.getY());
			lightPM.mult(rotLM);
			lightPM.mult(rotLM1);
			lightPM.mult(proj);
			Vector3f projLight = new Vector3f(lightPM.m[3+0*4],lightPM.m[3+1*4],lightPM.m[3+2*4]);
			g.fillOval(
					(int)(((projLight.getX()*(ratiox/projLight.getZ()))+ratiox)*1.33f)-(int)(50f/(projLight.getZ())*0.5f), 
					(int)(((projLight.getY()*(ratioy/projLight.getZ()))+ratioy)*1.33f)-(int)(50f/(projLight.getZ())*0.5f), 
					(int)(50f/(projLight.getZ())), 
					(int)(50f/(projLight.getZ())));

			float coordLengthMin = 0f;
			float coordLength = 1f;

			g.setColor(new Color(1f,0f,0f,0.3f));
			DrawUtils.draw3DLine(g, new Vector3f(coordLengthMin,0,0), new Vector3f(coordLength,0,0), position, rotLM, rotLM1, proj, ratiox, ratioy);
			g.setColor(new Color(0f,1f,0f,0.3f));
			DrawUtils.draw3DLine(g, new Vector3f(0,coordLengthMin,0), new Vector3f(0,coordLength,0), position, rotLM, rotLM1, proj, ratiox, ratioy);
			g.setColor(new Color(0f,0f,1f,0.3f));
			DrawUtils.draw3DLine(g, new Vector3f(0,0,coordLengthMin), new Vector3f(0,0,coordLength), position, rotLM, rotLM1, proj, ratiox, ratioy);

			g.setColor(new Color(1f,1f,1f,0.5f));
			Matrix4f zP = new Matrix4f(new Vector3f(0,0,0));
			zP.translate(position.multN(-1f));zP.mult(rotLM);zP.mult(rotLM1);zP.mult(proj);
			if(zP.m[3+3*4]>0){
				g.fillOval((int)(((zP.m[3+0*4]*(ratiox/zP.m[3+2*4]))+ratiox)*1.33f)-(int)(0.5*(50/zP.m[3+2*4])),
						(int)(((zP.m[3+1*4]*(ratioy/zP.m[3+2*4]))+ratioy)*1.33f)-(int)(0.5*(50/zP.m[3+2*4])),
						(int)(50/zP.m[3+2*4]),(int)(50/zP.m[3+2*4]));
				((Graphics2D) g).setStroke(new BasicStroke(1));
			}
		}
		
		/** draw debug-box*/
		drawDebug(g, sim);
	}
	

}