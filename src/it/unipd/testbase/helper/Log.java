package it.unipd.testbase.helper;

public class Log {
	
	public static void d(String tag, String message)
	{
		System.out.println("D - "+tag+": "+message);
	}
	
	public static void e(String tag, String message)
	{
		System.out.println("E - "+tag+": "+message);
	}
}
