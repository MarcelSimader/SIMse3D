package com.se.simse.gui.objects;

import java.awt.Color;

import com.se.simse.gui.SIMGUIElement;
import com.se.simse.gui.SIMGUIElementType;
import com.se.simse.math.vector.Vector3f;

public class SIMPanel extends SIMGUIElement {
	
	private boolean isVis;
	
	public SIMPanel(int x, int y, int width, int height, Color color, boolean isVis){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.isVis = isVis;
		this.type = SIMGUIElementType.PANEL;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public boolean isVisible() {
		return isVis;
	}

}
