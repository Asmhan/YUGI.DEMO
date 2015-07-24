package com.example.android.BluetoothChat;

import java.io.Serializable;

import android.bluetooth.BluetoothAdapter;

public class DataObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6164935091291749854L;
	
	

	public String sender_address;
	public String receiver_address;
	private String profileName;
	private int number;
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	/*public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = "";
	}*/

	private byte[] data;
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	/*public boolean isHasAttach() {
		return hasAttach;
	}*/

	/*public void setHasAttach(boolean hasAttach) {
		this.hasAttach = hasAttach;
	}*/

	//private boolean hasAttach;

	public DataObject(byte[] d, int number) {
		this.data = d;
		this.number = number;
	}

}
