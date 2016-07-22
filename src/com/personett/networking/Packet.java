package com.personett.networking;

public class Packet 
{
	public static class Packet00LoginRequest { String username; }
	public static class Packet01LoginAnswer { boolean accepted = false; }
	public static class Packet02Message { String message; }
	public static class Packet03Command { String command; }
}