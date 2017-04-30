package edu.csulb.android.bluetoothmessenger.router.tcp;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import edu.csulb.android.bluetoothmessenger.router.MeshNetworkManager;
import edu.csulb.android.bluetoothmessenger.router.Packet;
import edu.csulb.android.bluetoothmessenger.router.Receiver;

/**
 * Runner for dequeueing packets from packets to send, and issues the TCP connection to send them
 *
 * @author Matthew Vertescher
 * @author Peter Henderson
 *
 */
public class TcpSender {

	Socket tcpSocket = null;

	public boolean sendPacket(String ip, int port, Packet data) {
		// Try to connect, otherwise remove from table
		try {
			InetAddress serverAddr = InetAddress.getByName(ip);
			tcpSocket = new Socket();
			tcpSocket.bind(null);
			tcpSocket.connect(new InetSocketAddress(serverAddr, port), 5000);

		} catch (Exception e) {
			/*
			 * If can't connect assume that they left the chat and remove them
			 */
			MeshNetworkManager.routingTable.remove(data.getMac());
			Receiver.somebodyLeft(data.getMac());
			e.printStackTrace();
			return false;
		}

		OutputStream os = null;

		//try to send otherwise remove from table
		try {
			os = tcpSocket.getOutputStream();
			os.write(data.serialize());
			os.close();
			tcpSocket.close();

		} catch (Exception e) {
			MeshNetworkManager.routingTable.remove(data.getMac());
			Receiver.somebodyLeft(data.getMac());
			e.printStackTrace();
		}

		return true;
	}

}
