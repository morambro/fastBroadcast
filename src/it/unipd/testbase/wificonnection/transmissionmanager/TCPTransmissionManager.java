package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.wificonnection.message.IMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class TCPTransmissionManager implements ITranmissionManager {
	
	DebugLogger logger = new DebugLogger(TCPTransmissionManager.class);
	
	protected static final String TAG = "it.unipd.testbase";
	
	@Override
	public void send(final String ID,final IMessage msg) {

		new Thread(){
			public void run(){
				Socket socket = new Socket();
				byte buf[]  = new byte[1024];
				int len = buf.length;
				try {
					/**
					 * Create a client socket with the ID,
					 * port, and timeout information.
					 */
					socket.bind(null);
					socket.connect((new InetSocketAddress(ID, TCP_PORT)));

					OutputStream outputStream = socket.getOutputStream();
					InputStream inputStream = new ByteArrayInputStream(msg.getMessage());
					while ((len = inputStream.read(buf)) != -1) {
						outputStream.write(buf, 0, len);
					}
					outputStream.close();
					inputStream.close();
					logger.d("Data Sent to "+ID+" via TCP");
				} catch (Exception e) {
					logger.d(e.getMessage());
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
		}.start();	}

	@Override
	public void send(List<String> IDs, IMessage msg) {
		for(String ID : IDs){
			send(ID,msg);
		}
	}
	

}
