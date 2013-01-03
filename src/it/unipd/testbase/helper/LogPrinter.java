package it.unipd.testbase.helper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class LogPrinter {

	private static String fileName = "fast_broadcast_log.txt";
	private static LogPrinter log = null;
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
		fileName = fileName.concat(id);
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
//			writer.write(internalBuffer.toString()+"\n\nok");
			writer.close();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeResults(String res){
		writeLine("\n\n\n\t\tRESULTS:\n");
		writeLine("\nExecution Time:\t"+((float)(endTime-startTime))/1000f + "sec");
		writeLine(res);
	}

	public void reset(){
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
