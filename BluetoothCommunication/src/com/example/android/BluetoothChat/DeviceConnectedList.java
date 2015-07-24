package com.example.android.BluetoothChat;

import java.util.ArrayList;

import com.example.android.BluetoothChat.BluetoothChatService.ConnectedThread;

import android.app.Application;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

//import com.example.android.BluetoothChat.BluetoothChatService;

public class DeviceConnectedList extends ListActivity {

	static final String MAIN_MENU[] = { "Asmahan", "Yasmin", "Nourhan",
			"Ibrahim" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent i = this.getIntent();

		ArrayList<ConnectedThread> connections = Globals.mChatService.connections; 
		
		for(ConnectedThread con : connections){
			con.active_virtual_connection = null;
		}
		
		setListAdapter(new ArrayAdapter<ConnectedThread>(this,
				android.R.layout.simple_list_item_1, connections));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		// sdString selection=MAIN_MENU[position];
		try {
			// Class
			// selectionClass=Class.forName("com.example.fakechat."+selection);

			Intent in = new Intent(DeviceConnectedList.this,
					BluetoothChat.class);

			// in.putExtra("name", MAIN_MENU[position]);
			Globals.mChatService.currenctConnection = Globals.mChatService.connections
					.get(position);
			// Globals.mChatService.setState(3);
			// startActivity(in);
			finish();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
