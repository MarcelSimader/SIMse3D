package com.se.simse.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import com.se.test.Main;

public class KernelLoader {
	public static String loadKernel(String name) {
		if(!name.endsWith(".cls")) {
			name += ".cls";
		}
		BufferedReader br = null;
		String resultString = null;
		try {
			File clSourceFile = new File(name);
			br = new BufferedReader(new FileReader(clSourceFile));
			String line = null;
			StringBuilder result = new StringBuilder();
			while((line = br.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			resultString = result.toString();
		} catch(NullPointerException npe) {
			System.err.println("[KernelLoader]: Error retrieving OpenCL source file: ");
			npe.printStackTrace();
		} catch(IOException ioe) {
			System.err.println("[KernelLoader]: Error reading OpenCL source file: ");
			ioe.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException ex) {
				System.err.println("[KernelLoader]: Error closing OpenCL source file");
				ex.printStackTrace();
			}
		}

		return resultString;
	}
}
