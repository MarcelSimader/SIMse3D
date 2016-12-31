package com.se.simse.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture {
	
	private BufferedImage i;
	private String path;
	
	/**
	 * Imports a {@code BufferedImage} using path.
	 * @param path Sets {@code File} path.
	 * @return true if import was succesfull.
	 */
	public boolean importTexture(String path){
		this.path = path;
		try{
			i = ImageIO.read(new File(path));
			return true;
		}catch(IOException e){System.out.println("[SIMseTexture] IOException while trying to load: '" + path + "'"); return false;}
	}
	
	public String getPath(){
		return path;
	}
	
	/**
	 * @return Imported image as {@code BufferedImage}.
	 * @throws NullPointerException if the image has not been imported.
	 * @see Texture#importTexture(String)
	 */
	public BufferedImage getImage(){
		if(i==null){throw new NullPointerException();}
		return i;
	}

}
