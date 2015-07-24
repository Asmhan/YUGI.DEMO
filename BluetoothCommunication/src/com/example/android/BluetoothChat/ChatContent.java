package com.example.android.BluetoothChat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ChatContent extends Activity {
	
TextView my_info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listcontent);
		Intent data=this.getIntent();
		
		//String info=data.getExtras().getString("name");
		TextView my_info=(TextView)findViewById(R.id.name_1);
		
		my_info.setText(Globals.mChatService.currenctConnection.toString());
		
		Toast.makeText(this,"Hi",Toast.LENGTH_LONG).show();
		
		
		

}

}
