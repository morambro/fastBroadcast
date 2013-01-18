package it.unipd.testbase.helper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class LogPrinter {

	private static String fileName = "";
	private static String idStr = "";
	private static LogPrinter log = null;
	private static int simulation_number = 1;
	private FileWriter file;
	private BufferedWriter writer;
	private boolean init = true;
	private long startTime;
	private long endTime;


//	private StringBuilder internalBuffer = new StringBuilder();

	public static LogPrinter getInstance() {
		if(log == null)
			log = new LogPrinter();
		return log;
	}

	public static void setup(String id) {
		idStr = id;
		String temp = new String("sim"+simulation_number+"_");
		fileName = temp+id+"_fb_log.txt";
	}
	
	protected LogPrinter() {
		try {
			file = new FileWriter(fileName);
			writer = new BufferedWriter(file);
		} catch (FileNotFoundException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeLine(String line) {
		try {
		line = line.concat("\n");
//		internalBuffer.append(line);
		writer.write(line);
		writer.flush();
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
			file.close();
			writer.close();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeResults(String res){
		writeLine("\n\n\n\t\tRESULTS:\n");
		writeLine("\nExecution Time: "+((float)(endTime-startTime))/1000f + " sec");
		writeLine(""+((float)(endTime-startTime))/1000f);
		writeLine(res);
	}

	public void reset(){
		release();
		++simulation_number;
		setup(idStr);
		log = new LogPrinter();
		++simulation_number;
		init = true;
		try {
			file = new FileWriter(fileName);
			writer = new BufferedWriter(file);
		} catch (FileNotFoundException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
