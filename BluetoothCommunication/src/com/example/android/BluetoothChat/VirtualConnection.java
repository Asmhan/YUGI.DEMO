package com.example.android.BluetoothChat;

import java.util.ArrayList;

import com.example.android.BluetoothChat.BluetoothChatService.ConnectedThread;

public class VirtualConnection {

	private String name;
	private String address;
	private ConnectedThread real_connection;
	public ArrayList<String> messages = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public ConnectedThread getReal_connection() {
		return real_connection;
	}
	public void setReal_connection(ConnectedThread real_connection) {
		this.real_connection = real_connection;
	}
	
	public String toString(){
		return this.name;
	}
	
	
	
}
