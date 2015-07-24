package com.example.android.BluetoothChat;

import java.util.ArrayList;

import com.example.android.BluetoothChat.BluetoothChatService.ConnectedThread;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class VConnectedList extends ListActivity {
	
	public ArrayList<VirtualConnection> vconnections;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stu
		super.onCreate(savedInstanceState);
		
		
		ArrayList<ConnectedThread> connections = Globals.mChatService.connections; // (ArrayList<ConnectedThread>)
		// i.getExtras().get("connections");
	
		vconnections = new ArrayList<VirtualConnection>();
		
		for(ConnectedThread con : connections){
			vconnections.addAll(con.virtual_connections);
		}

		setListAdapter(new ArrayAdapter<VirtualConnection>(this,
				android.R.layout.simple_list_item_1, vconnections));
		
		
		 
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		 try {
			 
			 Intent in = new Intent( VConnectedList.this,
						BluetoothChat.class);

			 //Globals.mChatService.currenctConnection = Globals.mChatService.connections
				//	.get(position);
			 
			 VirtualConnection vcon = vconnections.get(position);
			 
			 Globals.mChatService.currenctConnection = vcon.getReal_connection();
			 Globals.mChatService.currenctConnection.active_virtual_connection = vcon;
			 
			 
			 finish();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	

}
