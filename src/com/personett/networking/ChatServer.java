package com.personett.networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.personett.networking.Packet.*;

public class ChatServer {
	private Server server;
	Map<Connection, User> users;
	
	public static void main(String[] args) {
		try {
			new ChatServer();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ChatServer() throws IOException {
		server = new Server();
		users = new HashMap<>();
		registerPackets();
		ServerListener serverListener = new ServerListener(this);
		server.addListener(serverListener);
		server.bind(25566, 25567);
		server.start();
	}
	
	private void registerPackets() {
		Kryo kryo = server.getKryo();
		kryo.register(char[].class);
		kryo.register(String.class);
		kryo.register(Packet00LoginRequest.class);
		kryo.register(Packet01LoginAnswer.class);
		kryo.register(Packet02Message.class);
		kryo.register(Packet03Command.class);
	}
	
	public void connected(Connection connection) {
		System.out.println("Connection from " + connection.getRemoteAddressTCP());
	}
	
	public void disconnected() {
		ArrayList<Connection> currentConnections = getCurrentConnectionsArrayList();
		Connection disconnectedUser = null;
		for(Connection previousConnection : users.keySet()) {
			if(!currentConnections.contains(previousConnection)) {
				System.out.println(users.get(previousConnection).getIP() + " disconnected.");
				Packet02Message logoutMessagePacket = new Packet02Message();
				logoutMessagePacket.message = users.get(previousConnection).getUsername() + " has logged out.";
				server.sendToAllTCP(logoutMessagePacket);
				disconnectedUser = previousConnection;
			}
		}
		users.remove(disconnectedUser);
	}
	
	public ArrayList<Connection> getCurrentConnectionsArrayList() {
		Connection[] connectionsArray = server.getConnections();
		ArrayList<Connection> connectionsArrayList = new ArrayList<>();
		Collections.addAll(connectionsArrayList, connectionsArray);
		return connectionsArrayList;
	}
	
	public void handlePacket(Connection currentConnection, Object packet) {
		if(packet instanceof Packet00LoginRequest) {
			handleLoginRequest(currentConnection, packet);
		} else if(packet instanceof Packet02Message) {
			handleMessage(currentConnection, packet);
		} else if(packet instanceof Packet03Command) {
			handleCommand(currentConnection, packet);
		}
	}
	
	public void handleLoginRequest(Connection currentConnection, Object packet) {
		Packet00LoginRequest loginRequestPacket = (Packet00LoginRequest)packet;
		addUser(currentConnection, loginRequestPacket);
		sendLoginConfirmation(currentConnection);
		sendLoginNotification(currentConnection, loginRequestPacket);
	}
	
	public void addUser(Connection currentConnection, Packet00LoginRequest loginRequestPacket) {
		User newUser = new User(loginRequestPacket.username, currentConnection.getRemoteAddressTCP());
		users.put(currentConnection, newUser);
	}
	
	public void sendLoginConfirmation(Connection currentConnection) {
		Packet01LoginAnswer loginAnswer = new Packet01LoginAnswer();
		loginAnswer.accepted = true;
		currentConnection.sendTCP(loginAnswer);
	}
	
	public void sendLoginNotification(Connection currentConnection, Packet00LoginRequest loginRequestPacket) {
		Packet02Message loginMessagePacket = new Packet02Message();
		loginMessagePacket.message = loginRequestPacket.username + " has logged in.";
		server.sendToAllExceptTCP(currentConnection.getID(), loginMessagePacket);
	}
	
	public void handleMessage(Connection currentConnection, Object packet) {
		String message = ((Packet02Message)packet).message;
		String username = users.get(currentConnection).getUsername();
		Packet02Message messagePacket = new Packet02Message();
		messagePacket.message = username + ": " + message;
		server.sendToAllTCP(messagePacket);
	}
	
	public void handleCommand(Connection currentConnection, Object packet) {
		String command = ((Packet03Command)packet).command;
		if(command.equalsIgnoreCase("users")) {
			sendUsersList(currentConnection);
		} else {
			sendErrorMessage(currentConnection);
		}
	}
	
	public void sendUsersList(Connection currentConnection) {
		String usersString = getUsersString();
		Packet02Message usersMessagePacket = new Packet02Message();
		usersMessagePacket.message = usersString;
		currentConnection.sendTCP(usersMessagePacket);
	}
	
	public String getUsersString() {
		String usersString = "";
		for(User user : users.values()) {
			usersString += user.getUsername() + "\n";
		}
		return usersString;
	}
	
	public void sendErrorMessage(Connection currentConnection) {
		Packet02Message errorMessagePacket = new Packet02Message();
		errorMessagePacket.message = "Unknown command.";
		currentConnection.sendTCP(errorMessagePacket);
	}
}