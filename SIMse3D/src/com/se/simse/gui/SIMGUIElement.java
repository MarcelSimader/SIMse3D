package com.se.simse.gui;

import java.awt.Color;

import com.se.simse.math.vector.Vector3f;

public abstract class SIMGUIElement {
	
	protected int x,y;
	protected int width,height;
	protected Color color;
	protected SIMGUIElementType type;
	protected boolean isVisible;
	
	public SIMGUIElementType getType(){
		return type;
	}
	
	public abstract int getX();
	public abstract int getY();
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract Color getColor();
	public abstract boolean isVisible();
}
