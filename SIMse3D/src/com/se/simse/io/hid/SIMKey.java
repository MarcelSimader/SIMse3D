package com.se.simse.io.hid;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.BiConsumer;

public class SIMKey implements KeyListener {
	
	private static BiConsumer<Integer,SIMKeyEvent> met;

	@Override
	public void keyTyped(KeyEvent e) {
		met.accept(e.getKeyCode(), SIMKeyEvent.TYPED);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		met.accept(e.getKeyCode(), SIMKeyEvent.PRESSED);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		met.accept(e.getKeyCode(), SIMKeyEvent.RELEASED);
	}
	
	/**
	 * Sets up {@code SIMMouse}
	 * @param consumer {@code Consumer<Integer,Integer>} that takes in the KeyCode and {@code SIMKeyEvent} when a key is pressed.
	 */
	public SIMKey(BiConsumer<Integer, SIMKeyEvent> consumer){
		met = consumer;
	}

}
