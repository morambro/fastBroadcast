package it.unipd.fast.broadcast.wifi_connection.transmissionmanager;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import android.util.Log;

public class TcpTransmissionManager implements ITranmissionManager {
	private static final String TAG = "it.unipd.fast.broadcast";
	private static final int port = 8888;
	
	@Override
	public void send(String ID, IMessage msg) {

		Socket socket = new Socket();
		byte buf[]  = new byte[1024];
		int len = buf.length;
		try {
			/**
			 * Create a client socket with the ID,
			 * port, and timeout information.
			 */
			socket.bind(null);
			socket.connect((new InetSocketAddress(ID, port)));

			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = new ByteArrayInputStream(msg.getMessage());
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
			Log.d(TAG, this.getClass().getSimpleName()+": Data Sent to "+ID+" via TCP");
		} catch (Exception e) {
			Log.d(TAG, this.getClass().getSimpleName()+": "+e.getMessage());
		}


		/**
		 * Clean up any open sockets when done
		 * transferring or if an exception occurred.
		 */
		finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void send(List<String> IDs, IMessage msg) {
		// TODO Auto-generated method stub

	}

}
