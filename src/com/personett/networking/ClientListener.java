package com.personett.networking;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class ClientListener extends Listener
{
	private ChatClient chatClient;
	
	public ClientListener(ChatClient testClient) {
		this.chatClient = testClient;
	}
	
	public void connected(Connection connection) {
		chatClient.connected();
	}
	
	public void disconnected(Connection connection) {
		chatClient.disconnected();
	}
	
	public void received(Connection connection, Object packet) {
		chatClient.handlePacket(connection, packet);
	}
}