package it.unipd.vanets.framework.wificonnection.receiver.transport;

import it.unipd.vanets.framework.eventdispatcher.EventDispatcher;
import it.unipd.vanets.framework.eventdispatcher.event.message.MessageReceivedEvent;
import it.unipd.vanets.framework.helper.DebugLogger;
import it.unipd.vanets.framework.wificonnection.message.MessageBuilder;
import it.unipd.vanets.framework.wificonnection.receiver.AbstractPacketReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPPacketReceiver extends AbstractPacketReceiver{
	protected final String TAG = "it.unipd.vanets.framework";
	private ServerSocket serverSocket;
	
	DebugLogger logger = new DebugLogger(TCPPacketReceiver.class);
	
	@Override
	public void run() {
		try{
			// Create a server socket and wait for client connections. This
			// call blocks until a connection is accepted from a client
			logger.d("Tiro su un socket TCP terminated = ");
			serverSocket = new ServerSocket(TCP_PORT);
			while(!terminated){
				logger.d("Attendo una connessione");
				// Waits for an incoming connection
				Socket client = serverSocket.accept();
				logger.d("Connessione in ingresso");
				handleConnection(client);
			}
			serverSocket.close();
		}catch(IOException e){
			logger.e(e);
		}
		// close the TCP socket before exiting
		disconnectSocket();
	}
	
	/**
	 * Method called to close the opened TCP socket if different to null and opened
	 * 
	 */
	private void disconnectSocket(){
		if(serverSocket != null && !serverSocket.isClosed()){
			try{
				serverSocket.close();
				logger.d("Ho chiuso il socket");
			}catch(Exception e){
				logger.d("Impossibile chiudere il socket");
				logger.e(e);
			}
		}
	}
	
	private void handleConnection(final Socket socket){
		new Thread() {
			
			@Override
			public void run() {
				logger.d("Gestione connessione in nuovo thread");
				// Read from the input stream
				try {
					InputStreamReader is = new InputStreamReader(socket.getInputStream());
					StringBuilder sb = new StringBuilder();
					BufferedReader br = new BufferedReader(is);
					String read = br.readLine();

					while(read != null) {
						sb.append(read);
						read =br.readLine();
					}
					
					String xmlMsg = new String(sb.toString());

					EventDispatcher.getInstance().triggerEvent(new MessageReceivedEvent(
							MessageBuilder.getInstance().getMessage(xmlMsg),
							socket.getInetAddress().getHostAddress()
					));
					
					socket.close();
				} catch (IOException e) {
					logger.e(e);
				}
			}
		}.start();
	}
	
	@Override
	public void terminate() {
		this.terminated = true;
		disconnectSocket();
	}
}
