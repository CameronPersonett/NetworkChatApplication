package com.personett.networking;

import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.personett.networking.Packet.*;

public class ChatClient {
	private Client client;
	private String username;
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	public ChatClient() {
		initialize();
		tryConnecting();
		pollInput();
	}
	
	public void initialize() {
		username = JOptionPane.showInputDialog("Please enter a username.");
		client = new Client(25566, 25567);
		register();
		ClientListener listener = new ClientListener(this);
		client.addListener(listener);
		client.start();
	}
	
	public void register() {
		Kryo kryo = client.getKryo();
		kryo.register(char[].class);
		kryo.register(String.class);
		kryo.register(Packet00LoginRequest.class);
		kryo.register(Packet01LoginAnswer.class);
		kryo.register(Packet02Message.class);
		kryo.register(Packet03Command.class);
	}
	
	public void tryConnecting() {
		try {
			client.connect(5000, "localhost", 25566, 25567);
			Packet00LoginRequest login = new Packet00LoginRequest();
			login.username = username;
			client.sendTCP(login);
		} catch(IOException e) {
			System.out.println("Could not connect.");
			e.printStackTrace();
			client.stop();
		}
	}
	
	public void connected() {
		System.out.println("You have connected to the server.");
		System.out.println("Type /users for a list of users currently logged in.");
	}
	
	public void disconnected() {
		System.out.println("You have disconnected from the server.");
		client.close();
	}
	
	public void pollInput() {
		Scanner scanner = new Scanner(System.in);
		while(scanner.hasNextLine()) {
			String input = scanner.nextLine();
			if(input.charAt(0) == '/') {
				sendCommand(input.substring(1));
			} else {
				sendMessage(input);
			}
		}
		scanner.close();
	}
	
	public void sendMessage(String message) {
		Packet02Message messagePacket = new Packet02Message();
		messagePacket.message = message;
		client.sendTCP(messagePacket);
	}
	
	public void sendCommand(String command) {
		Packet03Command commandPacket = new Packet03Command();
		commandPacket.command = command;
		client.sendTCP(commandPacket);
	}
	
	public void handlePacket(Connection connection, Object packet) {
		if(packet instanceof Packet01LoginAnswer) {
			if(((Packet01LoginAnswer)packet).accepted) {
				System.out.println("Login authenticated.");
			} else {
				connection.close();
			}
		} else if(packet instanceof Packet02Message) {
			System.out.println(((Packet02Message)packet).message);
		}
	}
}