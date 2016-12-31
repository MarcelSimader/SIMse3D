package com.se.simse.io.hid;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.BiConsumer;

public class SIMMouse implements MouseListener {
	
	BiConsumer<MouseEvent, SIMMouseEvent> met;

	@Override
	public void mouseClicked(MouseEvent e) {
		met.accept(e, SIMMouseEvent.CLICKED);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		met.accept(e, SIMMouseEvent.PRESSED);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		met.accept(e, SIMMouseEvent.RELEASED);
	}
	
	/**
	 * Sets up {@code SIMMouse}
	 * @param consumer {@code Consumer<MouseEvent,Integer>} that takes in the {@Code MouseEvent} and {@code SIMMouseEvent} when a mousebutton is clicked.
	 */
	public SIMMouse(BiConsumer<MouseEvent, SIMMouseEvent> consumer){
		met = consumer;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

}
