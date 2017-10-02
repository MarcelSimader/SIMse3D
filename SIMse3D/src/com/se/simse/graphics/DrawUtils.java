package com.se.simse.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import javax.swing.JFrame;

import com.se.simse.SIMMain;
import com.se.simse.math.matrix.Matrix4f;
import com.se.simse.math.vector.Vector2d;
import com.se.simse.math.vector.Vector2f;
import com.se.simse.math.vector.Vector3f;

public class DrawUtils {
	
	/**
	 * Draws a line using a {@code Vector2d} object.
	 * @param g {@code Graphics} object of the {@code JPanel}
	 * @param origin Origin point of the {@code Vector2d}.
	 * @param v The {@code Vector2d} to be drawn.
	 * @see DrawUtils#drawVector(Graphics, Vector2i, Vector2f)
	 * @see DrawUtils#drawVector(Graphics, Vector2i, Vector2i)
	 */
	public static void drawVector(Graphics g, Vector2f origin, Vector2d v){
		g.drawLine((int)origin.getX(), (int)origin.getY(), (int)origin.getX()+(int)v.getX(), (int)origin.getY()+(int)v.getY());
	}
	
	/**
	 * Draws a line using a {@code Vector2f} object.
	 * @param g {@code Graphics} object of the {@code JPanel}
	 * @param origin Origin point of the {@code Vector2f}.
	 * @param v The {@code Vector2f} to be drawn.
	 * @see DrawUtils#drawVector(Graphics, Vector2i, Vector2d)
	 * @see DrawUtils#drawVector(Graphics, Vector2i, Vector2i)
	 */
	public static void drawVector(Graphics g, Vector2f origin, Vector2f v){
		g.drawLine((int)origin.getX(), (int)origin.getY(), (int)origin.getX()+(int)v.getX(), (int)origin.getY()+(int)v.getY());
	}
	
	/**
	 * Fills the screen with a solid color.
	 * @param g {@code Graphics} object of the {@code JPanel}
	 * @param f A {@code Vector2f} for the width/height.
	 * @param c The {@code Color} to fill the screen with.
	 */
	public static void fillScreen(Graphics g, Vector2f f, Color c){
		g.setColor(c);
		g.fillRect(0, 0, (int)f.getX(), (int)f.getY());
	}
	
	/**
	 * Draws a debug-box on the screen.
	 * @param g {@code Graphics} object of the {@code JPanel}
	 * @param s {@code SIMMain} object used to get debug data.
	 */
	public static void drawDebug(Graphics g, SIMMain s){
		g.setColor(new Color(0,0,0,100));
		g.fillRect((int)s.getDimension().getX()-200, 0, 200, 85);
		g.setColor(Color.white);
		DecimalFormat df = new DecimalFormat("#.##");
		g.drawString("FPS: " + df.format(s.getFPS()), (int)s.getDimension().getX()-190, 55);
		g.drawString("TICKS/S: " + 1000f/s.getStepTimeMillis(), (int)s.getDimension().getX()-190, 75);
	}
	
	public static void draw3DLine(Graphics g, Vector3f min, Vector3f max, Vector3f position, Matrix4f rot, Matrix4f rot2,Matrix4f proj, float ratiox, float ratioy){
		((Graphics2D) g).setStroke(new BasicStroke(3));
		Matrix4f p1 = new Matrix4f(min);Matrix4f p2 = new Matrix4f(max);
		p1.translate(position.multN(-1f));p2.translate(position.multN(-1f));
		p1.mult(rot);p2.mult(rot);p1.mult(rot2);p2.mult(rot2);
		p1.mult(proj);p2.mult(proj); 
		if(p1.m[3+3*4]>0&&p2.m[3+3*4]>0){
			g.drawLine((int)(((p1.m[3+0*4]*(ratiox/p1.m[3+2*4]))+ratiox)*1.33f),
			(int)(((p1.m[3+1*4]*(ratioy/p1.m[3+2*4]))+ratioy)*1.33f),
			(int)(((p2.m[3+0*4]*(ratiox/p2.m[3+2*4]))+ratiox)*1.33f),
			(int)(((p2.m[3+1*4]*(ratioy/p2.m[3+2*4]))+ratioy)*1.33f));
			((Graphics2D) g).setStroke(new BasicStroke(1));
		}
	}

}
