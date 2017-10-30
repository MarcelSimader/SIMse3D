package com.se.test;

import static com.se.simse.graphics.DrawUtils.drawDebug;
import static com.se.simse.graphics.DrawUtils.fillScreen;

import java.awt.AWTException;
import java.awt.AlphaComposite;
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
import java.awt.image.Raster;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.swing.JFrame;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.api.CLImageFormat;
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
	 
	static VertexData[] vertexDataArray = new VertexData[64];
	static BufferedImage bf, wbf;
	private static float res = 1f;
	private static Cursor cursor;

	static int i = 0;
	
	private static boolean wireFrame = false;
	private static boolean wireFrameOnly = false;
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
	private static CLKernel[] kernel = new CLKernel[2];
	private static String[] programLoc = new String[2];
	private static CLProgram[] program = new CLProgram[2];
	private final static int dimensions = 1;
	private static PointerBuffer globalWorkSize;
	private static CLMem viewMem,resultColorM,normalMem,infoMem,lightPositionMem,triangleMem, textureMem;
	private static FloatBuffer viewBuffer,normalBuffer,infoBuffer,lightPosBuffer,triangleBuffer;
	private static IntBuffer resultColorBuffer, textureBuffer;
	
	private static Texture[] tex;
	
	private static long dt, dt1;
	private static int amount, amount1;

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
		//SIMPanel panel = new SIMPanel(100,100,1000,300,new Color(0.1f,1f,0.2f,0.3f), true);
		//sim.addGUIElement(panel);
		
		/** init OpenCL*/
		try {
			initOpenCL();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		/** load textures*/
		tex = new Texture[3];
		tex[0] = new Texture();
		tex[1] = new Texture();
		tex[2] = new Texture();
		tex[0].importTexture("src/res/textures/testres/test256.png");
		tex[1].importTexture("src/res/textures/testres/test256S.png");
		tex[2].importTexture("src/res/textures/testres/test256N.png");
		textureBuffer = BufferUtils.createIntBuffer(tex.length+((tex.length)*(tex[0].getImage().getHeight()*tex[0].getImage().getWidth())));
		for(int j=0; j<tex.length;j++){
			for(int y=0;y<tex[j].getImage().getHeight();y++){
				for(int x=0;x<tex[j].getImage().getWidth();x++){
					int rgb = tex[j].getImage().getRGB(x, y);
					textureBuffer.put(rgb);
				}
			}
			textureBuffer.put(0);
		}
		textureBuffer.rewind();
		
		/** setup vertexData*/
		vertexDataArray[0] = OBJLoader.loadObj("src/res/torusuv.obj", true);
		vertexDataArray[0].setScale(new Vector3f(3,3,3));
		vertexDataArray[0].setPosition(new Vector3f(0,-3.5,0));
		
		/**vertexDataArray[1] = new VertexData(new Vector3f[]{new Vector3f(-1,0,-1),new Vector3f(-1,0,1),new Vector3f(1,0,-1),new Vector3f(1,0,1)}, 
				new Vector3f[]{new Vector3f(0,1,0)},
				new Vector3f[]{new Vector3f(0,1,3),new Vector3f(0,2,3)},
				new Vector3f[]{new Vector3f(0,0,0),new Vector3f(0,0,0)},
				new Vector3f[]{new Vector3f(0,1,3),new Vector3f(0,2,3)},
				new Vector3f[]{new Vector3f(0,0,0),new Vector3f(64,0,0),new Vector3f(0,64,0),new Vector3f(64,64,0)});
		vertexDataArray[1].setPosition(new Vector3f(0,-2,0));
		vertexDataArray[1].setScale(new Vector3f(1,1,1));*/
		
		vertexDataArray[1] = OBJLoader.loadObj("src/res/teapot.obj", false);
		//vertexDataArray[1].setScale(new Vector3f(1,1,1));
		//vertexDataArray[1].setPosition(new Vector3f(0,-0.5,2));
		
		/**vertexDataArray[2] = OBJLoader.loadObj("src/res/sphere.obj");
		vertexDataArray[2].setPosition(new Vector3f(2,0,1));*/
		
		/**for(int i=0;i<16;i++){
			vertexDataArray[i] = OBJLoader.loadObj("src/res/smooth.obj", true);
			vertexDataArray[i].setPosition(new Vector3f().randomN().multN(4));
		}*/
		
		//vertexDataArray[0] = new VertexData().generateRandom(100, 6, true);
		
		/** setup perspective matrix*/
		proj.perspective(sim.getDimension().getX()/sim.getDimension().getY(), 70f, 1000f, 10f);
		//proj.orthographic(-10,-10, -10, 10, 100, 0.1f);
		
		/** start simse*/
		sim.start();
	}
	
	private static void initOpenCL() throws Exception {
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
	    
	    programLoc[0] = "src/com/se/test/kernels/fragmentShaderTestIntel.cls";
	    programLoc[1] = "src/com/se/test/kernels/vertexShader.cls";

	    boolean err = false;
	    for(int i=0;i<1;i++){
		    program[i] = CL10.clCreateProgramWithSource(context, KernelLoader.loadKernel(programLoc[i]), null);
		    CL10.clBuildProgram(program[i], devices.get(0), "", null);
		    System.out.println("s");
		    String log = program[i].getBuildInfoString(devices.get(0), CL10.CL_PROGRAM_BUILD_LOG);
		    if(log.length()>1){
			    System.err.println("\n---------------------------------------------BUILD LOG--------------------------------------------------------");
			    System.err.print("Kernel: [" +programLoc[i].substring("src/com/se/test/kernels/".length(), programLoc[i].length())+ "]\n\n");
				System.err.println(log);
				System.err.println("----------------------------------------------------------------------------------------------------------------\n");
				//err=true;
		    }
	    }
	    if(err){
	    	throw new Exception("Build Program Failed");
	    }
		
		CLImageFormat format = new CLImageFormat(CL10.CL_INTENSITY, CL10.CL_UNORM_INT8);
		resultColorM = CLMem.createImage2D(context, CL10.CL_MEM_READ_ONLY | CL10.CL_MEM_COPY_HOST_PTR, format, (long)(sim.getDimension().getX()/res), (long)(sim.getDimension().getY()/res), (long)(0), null, null);
		resultColorBuffer = BufferUtils.createIntBuffer((int) ((sim.getDimension().getX()/res)*(sim.getDimension().getY()/res)));
	}
	
	private static void kInput(int t, SIMKeyEvent k){
		if(k == SIMKeyEvent.RELEASED){
			if(t==KeyEvent.VK_F1){wireFrame = !wireFrame;if(wireFrameOnly&&wireFrame){wireFrameOnly=false;}}
			if(t==KeyEvent.VK_F2){wireFrameOnly = !wireFrameOnly;if(wireFrameOnly&&wireFrame){wireFrame=false;}}
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
		//vertexDataArray[0].setScale(new Vector3f(1f,Math.sin(sim.getTicks()*0.01f),1f));
		//vertexDataArray[0].setRotation(new Vector3f(Math.sin(sim.getTicks()*0.01f),Math.cos(sim.getTicks()*0.01f),Math.sin(sim.getTicks()*0.01f)));
	}
	
	private static void render(Thread t, Graphics g){
		
		/** fill screen with white - set color to black*/
		fillScreen(g, sim.getDimension(), Color.black);
		
		/** setup camera*/
		camera = new Matrix4f();
		camera = camera.identity();
		camera2 = new Matrix4f();
		camera2 = camera2.identity();
		camera.translate(position);
		camera.rotate(new Vector3f(0,1,0),rotation.getX());
		camera2.rotate(new Vector3f(1,0,0),(float)(rotation.getY()));
		camera.mult(camera2);

		/** setup light*/
		Matrix4f lightMatrix = new Matrix4f(light);
		lightMatrix.translate(new Vector3f(1,-1,0));
		Matrix4f f1 = new Matrix4f();f1=f1.identity();f1.rotate(new Vector3f(0,1,0), sim.getTicks()*0.01f);
		lightMatrix.mult(f1);
		Vector3f transLight = new Vector3f(lightMatrix.m[3+0*4], lightMatrix.m[3+1*4], lightMatrix.m[3+2*4]);
		
		int vertices = 0;
		for(int vertexIndex=vertexDataArray.length-1;vertexIndex>=0;vertexIndex--){
			VertexData vd = vertexDataArray[vertexIndex];
			if(vd!=null){
				vertices+=vd.getvIndicies().length;
			}
		}
		
		//SETUP BUFFERS AND TRIANGLES________________________________________________________________________________________________________
		
		globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, (int)(vertices));
		
		infoBuffer = BufferUtils.createFloatBuffer(6);
		infoBuffer.put(position.getX());
		infoBuffer.put(position.getY());
		infoBuffer.put(position.getZ());
		infoBuffer.put(rotation.getX());
		infoBuffer.put(rotation.getY());
		infoBuffer.put(res);
		infoBuffer.rewind();
		
		lightPosBuffer = BufferUtils.createFloatBuffer(3);
		lightPosBuffer.put(-transLight.getX());
		lightPosBuffer.put(-transLight.getY());
		lightPosBuffer.put(-transLight.getZ());
		lightPosBuffer.rewind();

		viewBuffer = BufferUtils.createFloatBuffer(vertices*15);
		normalBuffer = BufferUtils.createFloatBuffer(vertices*15);
		triangleBuffer = BufferUtils.createFloatBuffer(vertices*15);
		
		wbf = new BufferedImage((int)(sim.getDimension().getX()),(int)(sim.getDimension().getY()),BufferedImage.TYPE_INT_RGB);
		Graphics wg = wbf.getGraphics();
		wg.setColor(new Color(1f,0f,0f));
		
		long t3 = System.currentTimeMillis();
		
		for(int vertexIndex=vertexDataArray.length-1;vertexIndex>=0;vertexIndex--){
			VertexData vd = vertexDataArray[vertexIndex];
			if(vd!=null){
				
				//APPLY TRANSFORMATIONS________________________________________________________________________________________________________
					
				/** apply position matrices*/
				Vector3f[] newPositions = new Vector3f[vd.getPos().length];
				Vector3f[] realPositions = new Vector3f[vd.getPos().length];
				float[] w = new float[vd.getPos().length];
				
				/** apply normal matrices*/		
				Vector3f[] normT = new Vector3f[vd.getNormal().length];
				
				/** setup matrix*/
				Matrix4f rot = new Matrix4f();rot = rot.identity();
				Matrix4f rot1 = new Matrix4f();rot1 = rot1.identity();
				Matrix4f rot2 = new Matrix4f();rot2 = rot2.identity();
				rot.rotate(new Vector3f(1,0,0), vd.getRotation().getX());
				rot1.rotate(new Vector3f(0,1,0), vd.getRotation().getY());
				rot2.rotate(new Vector3f(0,0,1), vd.getRotation().getZ());
				rot1.mult(rot2);
				rot.mult(rot1);
				
				Matrix4f scale = new Matrix4f(); scale = scale.identity();
				scale.scale(vd.getScale());
				
				for(i=vd.getPos().length-1;i>=0;i--){
					Vector3f f = vd.getPos()[i];
					Matrix4f ma = new Matrix4f(f);
					ma.mult(scale);
					ma.mult(rot);
					ma.translate(vd.getPosition().addN(position));
					ma.mult(camera);
					ma.mult(proj);
					
					newPositions[i] = new Vector3f(ma.m[3+0*4],ma.m[3+1*4],ma.m[3+2*4]);
					w[i] = ma.m[3+3*4];
					
					ma = new Matrix4f(f);
					ma.mult(scale);
					ma.mult(rot);
					ma.translate(vd.getPosition());
					
					realPositions[i] = new Vector3f(ma.m[3+0*4],ma.m[3+1*4],ma.m[3+2*4]);
				}
				for(i=vd.getNormal().length-1;i>=0;i--){
					Vector3f f = vd.getNormal()[i];
					Matrix4f mab = new Matrix4f(f);
					mab.mult(rot);
					normT[i] = new Vector3f(mab.m[3+0*4], mab.m[3+1*4], mab.m[3+2*4]);
				}
				//CREATE TRIANGLES________________________________________________________________________________________________________
				
				for(i=vd.getvIndicies().length-1;i>=0;i--){
					/** create triangles*/
					int x = 0,y = 0,z = 0;
					boolean bCam = false;
					Triangle tr = null;
					if(vd.getvIndicies()!=null){
						x = (int)vd.getvIndicies()[i].getX();
						y = (int)vd.getvIndicies()[i].getY();
						z = (int)vd.getvIndicies()[i].getZ();
							
						if(w[x]<0&&w[y]<0&&w[z]<0){
							tr = new Triangle(newPositions[x],newPositions[y],newPositions[z]);
							viewBuffer.put(tr.getP1().getX());
							viewBuffer.put(tr.getP1().getY());
							
							viewBuffer.put(tr.getP2().getX());
							viewBuffer.put(tr.getP2().getY());
							
							viewBuffer.put(tr.getP3().getX());
							viewBuffer.put(tr.getP3().getY());
							
							viewBuffer.put(tr.getP1().getZ());
							viewBuffer.put(tr.getP2().getZ());
							viewBuffer.put(tr.getP3().getZ());
							
							viewBuffer.put(0);
							viewBuffer.put(0);
							
							viewBuffer.put(0);
							viewBuffer.put(0);
							
							viewBuffer.put(0);
							viewBuffer.put(0);
							
							if(wireFrame||wireFrameOnly){
								float ratiox = (sim.getDimension().getX()*0.5f); float ratioy = (sim.getDimension().getY()*0.5f);
								int x11 = (int) (tr.getP1().getX()*(ratiox/tr.getP1().getZ()));
								int y11 = (int) (tr.getP1().getY()*(ratioy/tr.getP1().getZ()));
								int x22 = (int) (tr.getP2().getX()*(ratiox/tr.getP2().getZ()));
								int y22 = (int) (tr.getP2().getY()*(ratioy/tr.getP2().getZ()));
								int x33 = (int) (tr.getP3().getX()*(ratiox/tr.getP3().getZ()));
								int y33 = (int) (tr.getP3().getY()*(ratioy/tr.getP3().getZ()));
								x11+=ratiox;x22+=ratiox;x33+=ratiox;
								y11+=ratioy;y22+=ratioy;y33+=ratioy;
								x11*=1.33f;y11*=1.33f;x22*=1.33f;y22*=1.33f;x33*=1.33f;y33*=1.33f;
								wg.drawLine(x11,y11,x22,y22);
								wg.drawLine(x22,y22,x33,y33);
								wg.drawLine(x33,y33,x11,y11);
							}
							
							tr = new Triangle(realPositions[x],realPositions[y],realPositions[z]);
							triangleBuffer.put(tr.getP1().getX());
							triangleBuffer.put(tr.getP1().getY());
							triangleBuffer.put(tr.getP1().getZ());
							
							triangleBuffer.put(tr.getP2().getX());
							triangleBuffer.put(tr.getP2().getY());
							triangleBuffer.put(tr.getP2().getZ());
							
							triangleBuffer.put(tr.getP3().getX());
							triangleBuffer.put(tr.getP3().getY());
							triangleBuffer.put(tr.getP3().getZ());
							bCam = true;
						}
					}
				
					
					/** create normal triangles*/
					x = 0;y = 0;z = 0;
					if(bCam){
						if(vd.getnIndicies()!=null){
							x = (int)vd.getnIndicies()[i].getX();
							y = (int)vd.getnIndicies()[i].getY();
							z = (int)vd.getnIndicies()[i].getZ();
							tr = new Triangle(normT[x],normT[y],normT[z]);
							
							normalBuffer.put(tr.getP1().getX());
							normalBuffer.put(tr.getP1().getY());
							normalBuffer.put(tr.getP1().getZ());
								
							normalBuffer.put(tr.getP2().getX());
							normalBuffer.put(tr.getP2().getY());
							normalBuffer.put(tr.getP2().getZ());
								
							normalBuffer.put(tr.getP3().getX());
							normalBuffer.put(tr.getP3().getY());
							normalBuffer.put(tr.getP3().getZ());
								
							normalBuffer.put(0);
							normalBuffer.put(0);
								
							normalBuffer.put(0);
							normalBuffer.put(0);
								
							normalBuffer.put(0);
							normalBuffer.put(0);
						}
					}
					
					/** create uv triangles*/
					x = 0;y = 0;z = 0;
					if(bCam){
						if(vd.getUVIndices()!=null){
							x = (int)vd.getUVIndices()[i].getX();
							y = (int)vd.getUVIndices()[i].getY();
							z = (int)vd.getUVIndices()[i].getZ();
							tr = new Triangle(vd.getUV()[x],vd.getUV()[y],vd.getUV()[z]);
							
							triangleBuffer.put(tr.getP1().getX());
							triangleBuffer.put(tr.getP1().getY());
							
							triangleBuffer.put(tr.getP2().getX());
							triangleBuffer.put(tr.getP2().getY());
							
							triangleBuffer.put(tr.getP3().getX());
							triangleBuffer.put(tr.getP3().getY());
							
						}else{
							triangleBuffer.put(-1);
							triangleBuffer.put(-1);
							
							triangleBuffer.put(-1);
							triangleBuffer.put(-1);
							
							triangleBuffer.put(-1);
							triangleBuffer.put(-1);
						}
					}
				}
			}
		}
		
		viewBuffer.rewind();
		normalBuffer.rewind();
		triangleBuffer.rewind();
		long t4 = System.currentTimeMillis();
		//System.out.println(t4-t3);

		/** set kernel data*/
		
		if(!wireFrameOnly){
				
			CLImageFormat format = new CLImageFormat(CL10.CL_RGBA, CL10.CL_UNORM_INT8);
	
			IntBuffer errB = BufferUtils.createIntBuffer(1);
			int memPTR = CL10.CL_MEM_COPY_HOST_PTR;
	
			viewMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, viewBuffer, errB); Util.checkCLError(errB.get(0));
			triangleMem = CL10.clCreateBuffer(context,  CL10.CL_MEM_WRITE_ONLY | memPTR, triangleBuffer, errB); Util.checkCLError(errB.get(0));
			normalMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, normalBuffer, errB); Util.checkCLError(errB.get(0));
			infoMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, infoBuffer, errB); Util.checkCLError(errB.get(0));
			lightPositionMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, lightPosBuffer, errB); Util.checkCLError(errB.get(0));
			textureMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, textureBuffer, errB); Util.checkCLError(errB.get(0));
			resultColorM = CLMem.createImage2D(context, CL10.CL_MEM_WRITE_ONLY, format, (long)(sim.getDimension().getX()/res), (long)(sim.getDimension().getY()/res), (long)(0), null, errB);Util.checkCLError(errB.get(0));
			CLMem zBufferMem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | memPTR, BufferUtils.createFloatBuffer((int)((sim.getDimension().getX()/res)*(sim.getDimension().getY()/res))), errB); Util.checkCLError(errB.get(0));
	
			kernel[0] = CL10.clCreateKernel(program[0], "render", errB); Util.checkCLError(errB.get(0));
	
			CL10.clSetKernelArg(kernel[0], 0, viewMem);
			CL10.clSetKernelArg(kernel[0], 1, triangleMem);
			CL10.clSetKernelArg(kernel[0], 2, normalMem);
			CL10.clSetKernelArg(kernel[0], 3, infoMem);
			CL10.clSetKernelArg(kernel[0], 4, lightPositionMem);
			CL10.clSetKernelArg(kernel[0], 5, textureMem);
			CL10.clSetKernelArg(kernel[0], 6, resultColorM);
			CL10.clSetKernelArg(kernel[0], 7, zBufferMem);
	
			long t1 = System.currentTimeMillis();
	
			int err = CL10.clEnqueueNDRangeKernel(queue, kernel[0], dimensions, null, globalWorkSize, null, null, null);Util.checkCLError(err);
			CL10.clFinish(queue);
	
			long t2 = System.currentTimeMillis();
			dt+=t2-t1;
			amount++;
			if(sim.getTicks()%10==0){
				System.out.println("render: " + dt/amount+"ms");dt=0;amount=0;
			}
	
			PointerBuffer origin = new PointerBuffer(3);
			origin.put(0l);origin.put(0l);origin.put(0l);
			origin.rewind();
			PointerBuffer region = new PointerBuffer(3);
			region.put((long)(sim.getDimension().getX()/res));region.put((long)(sim.getDimension().getY()/res));region.put(1l);
			region.rewind();
	
			err = CL10.clEnqueueReadImage(queue, resultColorM, CL10.CL_TRUE, origin, region, (long)(0), (long)(0), resultColorBuffer, null, null);Util.checkCLError(err);
			CL10.clFinish(queue);
	
	
			CL10.clReleaseMemObject(triangleMem);
			CL10.clReleaseMemObject(viewMem);
			CL10.clReleaseMemObject(normalMem);
			CL10.clReleaseMemObject(infoMem);
			CL10.clReleaseMemObject(lightPositionMem);
			CL10.clReleaseMemObject(textureMem);
			CL10.clReleaseMemObject(zBufferMem);
			CL10.clReleaseMemObject(resultColorM);
			CL10.clReleaseKernel(kernel[0]);
	
			t1 = System.currentTimeMillis();
	
			/** draw pixels*/
			bf = new BufferedImage((int)(sim.getDimension().getX()/res),(int)(sim.getDimension().getY()/res),BufferedImage.TYPE_INT_RGB);
			Raster b = bf.getRaster();
			for(int yi=0;yi<bf.getHeight();yi++){
				for(int xi=0;xi<bf.getWidth();xi++){
					int i = xi+yi*bf.getWidth();
					b.getDataBuffer().setElem(i, resultColorBuffer.get(i));
				}
			}
			bf.getRaster().setDataElements(0, 0, b);
	
			if(bf!=null){
				((Graphics2D) g).drawRenderedImage(bf, AffineTransform.getScaleInstance(
						((sim.getDimension().getX()/bf.getWidth())*1.33f), ((sim.getDimension().getY()/bf.getHeight())*1.33f)));
			}
	
			t2 = System.currentTimeMillis();
			dt1+=t2-t1;
			amount1++;
			if(sim.getTicks()%10==0){
				//	System.out.println("draw image: " + dt1/amount1 +"ms");dt1=0;amount1=0;
			}
		
		}

		if(wireFrame||wireFrameOnly){
			if(wireFrame){
				((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
			}
			g.drawImage(wbf, 0, 0, null);
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			
			float ratiox = (sim.getDimension().getX()*0.5f); float ratioy = (sim.getDimension().getY()*0.5f);
			/** draw light*/
			float a = 0.3f;
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
			float coordLength = -1.5f;

			g.setColor(new Color(1f,0f,0f,a));
			DrawUtils.draw3DLine(g, new Vector3f(coordLengthMin,0,0), new Vector3f(coordLength,0,0), position, rotLM, rotLM1, proj, ratiox, ratioy);
			g.setColor(new Color(0f,1f,0f,a));
			DrawUtils.draw3DLine(g, new Vector3f(0,coordLengthMin,0), new Vector3f(0,coordLength,0), position, rotLM, rotLM1, proj, ratiox, ratioy);
			g.setColor(new Color(0f,0f,1f,a));
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