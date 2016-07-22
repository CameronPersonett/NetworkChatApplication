package com.personett.networking;

import java.net.InetSocketAddress;

public class User {
	private String username;
	private InetSocketAddress ip;
	
	public User(String username, InetSocketAddress ip) {
		this.username = username;
		this.ip = ip;
	}
	
	public String getUsername() {
		return username;
	}
	
	public InetSocketAddress getIP() {
		return ip;
	}
}