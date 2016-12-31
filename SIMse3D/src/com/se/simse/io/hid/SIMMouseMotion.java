package com.se.simse.io.hid;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.function.BiConsumer;

public class SIMMouseMotion implements MouseMotionListener {
	
	BiConsumer<MouseEvent, SIMMouseMotionEvent> met;

	@Override
	public void mouseDragged(MouseEvent e) {
		met.accept(e, SIMMouseMotionEvent.DRAGED);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		met.accept(e, SIMMouseMotionEvent.MOVED);
	}
	
	/**
	 * Sets up {@code SIMMouseMotion}
	 * @param consumer {@code Consumer<MouseEvent,Integer>} that takes in the {@Code MouseEvent} and {@code SIMMouseMotionEvent} when the mouse is moved.
	 */
	public SIMMouseMotion(BiConsumer<MouseEvent, SIMMouseMotionEvent> consumer){
		met = consumer;
	}

}
