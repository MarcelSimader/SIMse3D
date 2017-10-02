package com.se.simse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JFrame;

import com.se.simse.gui.SIMGUIElement;
import com.se.simse.gui.SIMGUIElementType;
import com.se.simse.math.vector.Vector2f;

/**
 * Main class of SIMse
 * @author Marcel Simader
 * 
 */
public class SIMMain {

	private String ID;
	private Thread t;
	private Thread t1;
	private Consumer<Thread> updateMethod;
	private boolean isRunning;
	
	private JFrame frame;
	private BiConsumer<Thread, Graphics> renderMethod;
	private Graphics g;
	
	private long currentTime, lastTime, delta, timePassed;
	private float stepTime;
	private int ticks, seconds;
	private double FPS, lastTimeFPS, currentTimeFPS, avgFPS;
	private int rendTicks;
	
	private Vector2f dim;
	
	private ArrayList<SIMGUIElement> simguilist = new ArrayList<SIMGUIElement>();
	
	/** Main init */

	private void init() {
		isRunning = false;
		
		/** Update Thread*/
		t = new Thread(()->{
			currentTime = System.nanoTime();
			while(isRunning){
				while(timePassed>=stepTime){
					updateMethod.accept(t);
					timePassed-=stepTime;
					ticks++;
					if(ticks%(1000000000l/stepTime)==0){
						seconds++;
					}
				}
				lastTime = currentTime;
				currentTime = System.nanoTime();
				delta = currentTime-lastTime;
				timePassed+=delta;
			}
			System.out.println("[SIMMain] Stopped Update Thread: '" + t.getName()+ "'");
		});
		t.setName(ID+"update");
		
		/** Render Thread*/
		t1 = new Thread(()->{
			lastTimeFPS = System.nanoTime();
			while(isRunning){
				BufferStrategy bs = frame.getBufferStrategy();
				if(bs==null){frame.createBufferStrategy(2);}else{
					g = bs.getDrawGraphics();
					renderMethod.accept(t, g);
					drawGUI(g);
					g.dispose();
					bs.show();
				}
				lastTimeFPS = currentTimeFPS;
				currentTimeFPS = System.nanoTime();
				if(rendTicks<5){avgFPS += 1000000000d/(currentTimeFPS-lastTimeFPS);
				}else{FPS=avgFPS/5.0d;avgFPS = 0d;rendTicks=0;}
				rendTicks++;
			}
			System.out.println("[SIMMain] Stopped Render Thread: '" + t1.getName()+ "'");
		});
		t1.setName(ID+"render");
		
		System.out.println("[SIMMain] Initiated '" + this.getClass().toString() + "': '" + ID + "'");
	}
	
	/**
	 * Starts all current threads of this instance.
	 * @throws NullPointerException if either {@link SIMMain#updateMethod} or {@link SIMMain#renderMethod} is null.
	 */
	public void start(){
		isRunning = true;
		
		if(updateMethod!=null){
			t.start();
			System.out.println("[SIMMain] Started Update Thread: '" + t.getName()+ "'");
		}else{
			System.out.println("[SIMMain] Could not start Thread '" + t.getName()+ "'");
			throw new NullPointerException();
		}
		
		if(renderMethod!=null){
			t1.start();
			System.out.println("[SIMMain] Started Render Thread: '" + t1.getName()+ "'");
		}else{
			System.out.println("[SIMMain] Could not start Thread '" + t1.getName()+ "'");
			throw new NullPointerException();
		}
	}
	
	/**
	 * Stops all current threads of this instance.
	 */
	public void stop(){
		isRunning = false;
	}
	
	/**Instantiate a new SIMMain instance.
	*  @param  id  Sets the ID and name of the current instance.
	*  @param  updateMethod  Sets the function that gets called every x updates.
	*  @param  renderMethod  Sets the function that gets called as often as possible
	*  @see com.se.simse.SIMMain#setStepTime(float)
	*  @see com.se.simse.SIMMain#setStepTimeMillis(float)
	*  @see com.se.simse.SIMMain#setStepTimeSeconds(float)
	**/
	public SIMMain(String id, Consumer<Thread> updateMethod, BiConsumer<Thread, Graphics> renderMethod){
		ID = id;
		this.updateMethod = updateMethod;
		this.renderMethod = renderMethod;
		
		if(updateMethod==null||renderMethod==null){
			System.err.println("[SIMMain] updateMethod or renderMethod is null");
		}
		
		frame = new JFrame();
		
		init();
	}
	
	private void drawGUI(Graphics g){
		for(int i=simguilist.size()-1;i>=0;i--){
			SIMGUIElement t = simguilist.get(i);
			switch(t.getType()){
				case PANEL: 
					if(t.isVisible()){
						g.setColor(t.getColor());
						g.fillRect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
					}
				break;
			}
		}
	}
	
	/** setters and getters */
	
	public void setGUIElement(int i, SIMGUIElement t){
		simguilist.set(i, t);
	}
	
	public void addGUIElement(SIMGUIElement t){
		simguilist.add(t);
	}
	
	public ArrayList<SIMGUIElement> getSimguilist() {
		return simguilist;
	}

	public void setSimguilist(ArrayList<SIMGUIElement> simguilist) {
		this.simguilist = simguilist;
	}
	
	public void setDimension(Vector2f d){
		dim = d;
	}

	public Vector2f getDimension(){
		return dim;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public float getStepTimeMillis() {
		return stepTime/1000000;
	}
	
	/**
	 * Sets the stepTime in nanoseconds.
	 * @param nanos steptime in nanoseconds.
	 */
	public void setStepTime(float nanos) {
		this.stepTime = nanos;
	}
	
	/**
	 * Sets the stepTime in milliseconds.
	 * @param millis steptime in milliseconds.
	 */
	public void setStepTimeMillis(float millis) {
		this.stepTime = millis*1000000f;
	}
	
	/**
	 * Sets the stepTime in seconds.
	 * @param seconds steptime in seconds.
	 */
	public void setStepTimeSeconds(float seconds){
		this.stepTime = seconds*1000000000f;
	}

	public long getDelta() {
		return delta;
	}

	public int getTicks() {
		return ticks;
	}
	
	public int getSeconds(){
		return seconds;
	}
	
	public double getFPS(){
		return FPS;
	}

}
