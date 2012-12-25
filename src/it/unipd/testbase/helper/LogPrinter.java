package it.unipd.testbase.helper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class LogPrinter {

	private static final String fileName = "fast_broadcast_log.txt";
	private static LogPrinter log = null;
	private FileWriter file;
	private BufferedWriter writer;

	public static LogPrinter getInstance() {
		if(log == null)
			log = new LogPrinter();
		return log;
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
			writer.write(line);
			writer.flush();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeTimedLine(String line) {
		line = (new String("Time "+System.currentTimeMillis()+": ").concat(line));
		writeLine(line);
	}

	public void release() {
		try {
			writer.close();
		} catch (IOException e) {
			Log.e("LogPrinter", e.getMessage());
			e.printStackTrace();
		}
	}
}
