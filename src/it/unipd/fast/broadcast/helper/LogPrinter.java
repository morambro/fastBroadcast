package it.unipd.fast.broadcast.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class LogPrinter {
	
	private static final String fileName = "fast_broadcast_log.txt";
	private static LogPrinter log = null;
	private File file;
	private FileOutputStream outStream;
	
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
		line = (new String("Time "+System.currentTimeMillis()+": ").concat(line));
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
}
