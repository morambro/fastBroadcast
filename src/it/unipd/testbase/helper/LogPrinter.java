package it.unipd.testbase.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class LogPrinter {
	
	private static final String fileName = "fast_broadcast_log.txt";
	private static LogPrinter log = null;
	private File file;
	private FileOutputStream outStream;
	private boolean init = true;
	private long startTime;
	private long endTime;
	
	public static LogPrinter getInstance() {
		if(log == null)
			log = new LogPrinter();
		return log;
	}
	
	protected LogPrinter() {
		file = new File(Environment.getExternalStorageDirectory(), fileName);
		try {
			outStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void writeLine(String line) {
		try {
			line = line.concat("\n");
			outStream.write(line.getBytes());
			outStream.flush();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void writeTimedLine(String line) {
		if(init) {
			startTime = System.currentTimeMillis();
			writeLine("Starting simulation at time "+startTime);
			init = false;
		}
		endTime = System.currentTimeMillis();
		float timestamp = ((float)(endTime-startTime))/1000f;
		line = (timestamp+"s\t: "+(line));
		writeLine(line);
	}
	
	public void release() {
		try {
			outStream.close();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getResults(){
		StringBuilder results = new StringBuilder("");
		try {
			BufferedReader s = new BufferedReader(
					new FileReader(new File(Environment.getExternalStorageDirectory(), fileName)));
			String line = "";
			while((line = s.readLine()) != null){
				results.append(line).append("\n");
			}
			results.append("\nExecution Time = "+((float)(endTime-startTime))/1000f + "sec");
			s.close();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
		return results.toString();
	}
}
