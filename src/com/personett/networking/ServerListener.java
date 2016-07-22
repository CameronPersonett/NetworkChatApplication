package com.personett.networking;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ServerListener extends Listener {
	ChatServer chatServer;
	
	public ServerListener(ChatServer chatServer) {
		this.chatServer = chatServer;
	}
	
	public void connected(Connection connection) {
		chatServer.connected(connection);
	}
	
	public void disconnected(Connection connection) {
		chatServer.disconnected();
	}
	
	public void received(Connection currentConnection, Object packet) {
		chatServer.handlePacket(currentConnection, packet);
	}
}