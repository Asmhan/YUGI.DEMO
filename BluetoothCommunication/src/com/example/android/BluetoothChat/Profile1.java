package com.example.android.BluetoothChat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Profile1 extends Activity{

	Button returnChat;
	TextView name;
	TextView job;
	TextView mail;
	ImageView photo;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		returnChat =(Button)findViewById(R.id.go_to_chat);
		name=(TextView)findViewById(R.id.name_details);
		job=(TextView)findViewById(R.id.info_1);
		mail=(TextView)findViewById(R.id.info_2); 
		photo=(ImageView)findViewById(R.id.profile_photo);
		//photo.setImageResource(R.drawable.yugi);
		
		name.setText("Nmae : Asmahan");
		job.setText("career : android developer ");
		mail.setText("E-MAIL :loveallah@yahoo.com");
		
	
		
	}

}
