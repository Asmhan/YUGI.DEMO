/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.example.android.BluetoothChat.BluetoothChatService.ConnectedThread;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message Type Sent to/from other device
	public static final int TEXT = 1;
	public static final int IMAGE = 2;
	public static final int PROFILE = 3;
	public static final int CONNECTIONS = 4;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	private static final int REQUEST_FILE_SELECT = 3;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	protected static final int REQUEST_CAMERA = 4;
	protected static final int SELECT_FILE = 5;

	// Layout Views
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private Button button_attach;
	private ImageView attachImage;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	public BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public BluetoothChatService mChatService = Globals.mChatService;

	// Mac address for the connected device

	private String address;
	private BluetoothDevice device;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.main);

		if (Globals.mChatService != null
				&& Globals.mChatService.currenctConnection != null)
			Globals.mChatService.setState(3);
		// setStatus(3);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;

		}
		Button connectedDeviceButton = (Button) findViewById(R.id.connectedDevice);
		connectedDeviceButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(BluetoothChat.this,
						DeviceConnectedList.class);

				// i.putExtra("connections", Globals.mChatService.connections);
				startActivity(i);

			}
		});

		Button vconnectedDeviceButton = (Button) findViewById(R.id.vconnectedDevice);
		vconnectedDeviceButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(BluetoothChat.this, VConnectedList.class);

				// i.putExtra("connections", Globals.mChatService.connections);
				startActivity(i);

			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (Globals.mChatService == null) {
				setupChat();

			}

			// Initialize the send button with a listener that for click events
			mSendButton = (Button) findViewById(R.id.button_send);
			mSendButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Send a message using content of the edit text widget
					TextView view = (TextView) findViewById(R.id.edit_text_out);
					String message = view.getText().toString();
					sendMessage(message);
				}
			});

			if (mConversationArrayAdapter != null) {

				mConversationArrayAdapter.clear();

				if (Globals.mChatService != null
						&& Globals.mChatService.currenctConnection != null) {

					if (Globals.mChatService.currenctConnection.active_virtual_connection == null) {
						for (String m : Globals.mChatService.currenctConnection.messages) {
							mConversationArrayAdapter.add(m);
						}
					} else {
						for (String m : Globals.mChatService.currenctConnection.active_virtual_connection.messages) {
							mConversationArrayAdapter.add(m);
						}
					}

				}
			}
		}

		ActionBar actionBar = getActionBar();
		if (Globals.mChatService.currenctConnection != null) {
			actionBar.setSubtitle("Connected to : "
					+ Globals.mChatService.currenctConnection);

		} else {
			actionBar.setSubtitle("");

		}

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (Globals.mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (Globals.mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				Globals.mChatService.start();
			}
		}

	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the BluetoothChatService to perform bluetooth connections
		Globals.mChatService = new BluetoothChatService(this, mHandler);
		mChatService = Globals.mChatService;

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

		button_attach = (Button) findViewById(R.id.button_attach);
		button_attach.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				sendImage();

			}
		});

		attachImage = (ImageView) findViewById(R.id.attachImage);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (Globals.mChatService != null)
			Globals.mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {

		sendConnections();

		// Check that we're actually connected before trying anything
		if (Globals.mChatService.currenctConnection == null) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] message_bytes = message.getBytes();
			Globals.mChatService.write(message_bytes, TEXT);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			mOutEditText.setText(mOutStringBuffer);
		}
	}

	/**
	 * Send Image
	 */

	private void sendImage() {
		if (Globals.mChatService.currenctConnection == null) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Bitmap bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.app_icon);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] image_bytes = baos.toByteArray();
		Globals.mChatService.write(image_bytes, IMAGE);

		mOutStringBuffer.setLength(0);
		// mOutEditText.setText(mOutStringBuffer);
	}

	private void sendConnections() {
		if (Globals.mChatService.currenctConnection == null) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		String connections__addresses = "";
		for (ConnectedThread con : Globals.mChatService.connections) {
			if (!con.device.getAddress()
					.equalsIgnoreCase(
							Globals.mChatService.currenctConnection.device
									.getAddress())) {
				connections__addresses += con.device.getAddress() + ","
						+ con.device.getName() + ";";

			}
		}
		// connections__addresses = connections__addresses.substring(0,
		// connections__addresses.length() - 2);

		byte[] connections_addresses_bytes = connections__addresses.getBytes();
		Globals.mChatService.write(connections_addresses_bytes, CONNECTIONS);

		mOutStringBuffer.setLength(0);

	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
		actionBar.setSubtitle("");
		if (Globals.mChatService != null
				&& Globals.mChatService.currenctConnection != null) {
			actionBar.setSubtitle("Connected to : "
					+ Globals.mChatService.currenctConnection);
		}
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
		actionBar.setSubtitle("");
		if (Globals.mChatService != null
				&& Globals.mChatService.currenctConnection != null) {
			actionBar.setSubtitle("Connected to : "
					+ Globals.mChatService.currenctConnection);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:

				if (msg.obj instanceof DataObject) {

					final DataObject dataObject = (DataObject) msg.obj;
					switch (dataObject.getNumber()) {

					case IMAGE: {
						byte[] buffer = dataObject.getData();
						Bitmap bm1 = BitmapFactory.decodeByteArray(buffer, 0,
								buffer.length);

						attachImage.setImageBitmap(bm1);
						break;
					}

					case TEXT: {
						byte[] writeBuf = dataObject.getData(); // (byte[])
																// msg.obj;
						// construct a string from the buffer
						String writeMessage = new String(writeBuf);
						String m = "Me:  " + writeMessage;

						if (Globals.mChatService.currenctConnection.active_virtual_connection == null) {
							Globals.mChatService.currenctConnection.messages
									.add(m);
						} else {
							Globals.mChatService.currenctConnection.active_virtual_connection.messages
									.add(m);

						}

						mConversationArrayAdapter.add(m);
						break;
					}
					}

					break;
				}
			case MESSAGE_READ:

				if (msg.obj instanceof DataObject) {
					final DataObject dataObject = (DataObject) msg.obj;

					switch (dataObject.getNumber()) {

					case IMAGE: {
						byte[] buffer = dataObject.getData();
						Bitmap bm1 = BitmapFactory.decodeByteArray(buffer, 0,
								buffer.length);

						attachImage.setImageBitmap(bm1);
						break;
					}
					case TEXT: {

						byte[] readBuf = dataObject.getData();// (byte[])
																// msg.obj;
						// construct a string from the valid bytes in the buffer

						if (!mBluetoothAdapter.getAddress().equalsIgnoreCase(
								dataObject.receiver_address)) {
							for (ConnectedThread con : Globals.mChatService.connections) {
								if (con.device.getAddress().equalsIgnoreCase(
										dataObject.receiver_address)) {

									con.write(dataObject.getData(), TEXT,
											dataObject.sender_address);

									break;
								}
							}

							break;

						}

						String readMessage = new String(readBuf);
						String m = "";

						if (dataObject.sender_address
								.equalsIgnoreCase(Globals.mChatService.currenctConnection.device
										.getAddress())) {
							m = Globals.mChatService.currenctConnection + ":  "
									+ readMessage;
						}

						boolean indirect_sent = true;
						for (ConnectedThread con : Globals.mChatService.connections) {
							if (con.device.getAddress().equalsIgnoreCase(
									dataObject.sender_address)) {
								m = con + ":  " + readMessage;
								con.messages.add(m);
								indirect_sent = false;
							}
						}

						String vcon_address = null;
						if (indirect_sent) {
							for (ConnectedThread con : Globals.mChatService.connections) {
								for (VirtualConnection vcon : con.virtual_connections) {
									if (vcon.getAddress().equalsIgnoreCase(
											dataObject.sender_address)) {
										m = vcon + ":  " + readMessage;
										vcon.messages.add(m);
										vcon_address = vcon.getAddress();
									}
								}

							}
						}

						if (dataObject.sender_address
								.equalsIgnoreCase(Globals.mChatService.currenctConnection.device
										.getAddress())) {
							mConversationArrayAdapter.add(m);
						}

						if (vcon_address != null
								&& Globals.mChatService.currenctConnection.active_virtual_connection != null
								&& Globals.mChatService.currenctConnection.active_virtual_connection
										.getAddress().equalsIgnoreCase(
												vcon_address)) {
							mConversationArrayAdapter.add(m);
						}

						break;
					}
					case CONNECTIONS: {

						byte[] virtual_connections_bytes = dataObject.getData();

						String virtual_connections_string = new String(
								virtual_connections_bytes);

						if (virtual_connections_string.isEmpty()) {
							break;
						}

						String[] virtual_connecions = virtual_connections_string
								.split(";");

						for (ConnectedThread con : Globals.mChatService.connections) {
							if (con.device.getAddress().equalsIgnoreCase(
									dataObject.sender_address)) {
								con.virtual_connections.clear();
								for (String vcon : virtual_connecions) {
									String[] vcon_details = vcon.split(",");
									VirtualConnection vc = new VirtualConnection();
									vc.setAddress(vcon_details[0]);
									vc.setName(vcon_details[1]);
									vc.setReal_connection(con);
									con.virtual_connections.add(vc);
								}
							}
						}

						break;
					}
					}
					break;
				}

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		File file = null;
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;

		// you would then listen for the selected file's uri in
		// onActivityResult()
		case REQUEST_FILE_SELECT:
			if (requestCode == REQUEST_FILE_SELECT) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				Log.d(TAG, "File Uri: " + uri.toString());
				// Get the path
				String path = null;
				try {
					path = FileUtils.getPath(this, uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				Log.d(TAG, "File Path: " + path);
				// Get the file instance
				File mFile = new File(path);
				// Initiate the upload

				// to solve the problem of only option "gallery " and no camera
				// to make user choose like camera you need to do this
				/*
				 * you will need to query the packageManger with
				 * PackageManager.queryIntentActivities()
				 */
				List<Intent> targetedShareIntents = new ArrayList<Intent>();
				Intent shareIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				shareIntent.setType("*/*");
				List<ResolveInfo> resInfo = getPackageManager()
						.queryIntentActivities(shareIntent, 0);
				if (!resInfo.isEmpty()) {
					for (ResolveInfo resolveInfo : resInfo) {
						String packageName = resolveInfo.activityInfo.packageName;
						Intent targetedShareIntent = new Intent(
								android.content.Intent.ACTION_SEND);
						targetedShareIntent.setType("*/*");
						shareIntent.putExtra(Intent.EXTRA_STREAM,
								Uri.fromFile(mFile));
						targetedShareIntent.setPackage(packageName);
						targetedShareIntents.add(targetedShareIntent);

					}
					Intent chooserIntent = Intent.createChooser(
							targetedShareIntents.remove(0),
							"Select app to share");
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
							targetedShareIntents.toArray(new Parcelable[] {}));
					startActivity(Intent.createChooser(shareIntent,
							"Share File"));
				}
			}
			break;
		}
		// now we need return back to our activity with proper result . so add
		// " onActivityResult()"
		// method into Main activity class

		if (resultCode == RESULT_OK) {

			if (requestCode == REQUEST_CAMERA) {
				// determine uri of camera image to save
				File f = new File(Environment.getExternalStorageDirectory()
						.toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {

					Bitmap bm;
					BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

					bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
							btmapOptions);

					// bm = Bitmap.createScaledBitmap(bm, 70, 70, true);
					// Globals.mChatService.sendMessage(bm);
					String path = android.os.Environment
							.getExternalStorageDirectory()
							+ File.separator
							+ "Phoenix" + File.separator + "default";
					f.delete();
					OutputStream fOut = null;
					file = new File(path, String.valueOf(System
							.currentTimeMillis()) + ".jpg");
					try {
						fOut = new FileOutputStream(file);
						bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();
						fOut.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == SELECT_FILE) {
				if (data != null) {

					Uri selectedImageUri = data.getData();
					String selectedImagePath = getPath(selectedImageUri, this);
					BitmapFactory.Options options = new BitmapFactory.Options();
					// options.inJustDecodeBounds = true;==
					Bitmap btemp = BitmapFactory.decodeFile(selectedImagePath,
							options);

					// / use btemp Image file
					/*
					 * You can write data to bytes and then create a file in
					 * sdcard folder with whatever name and extension you want
					 * and then write the bytes to that file. This will save
					 * bitmap to sdcard.
					 */

					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					btemp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
					byte[] image = bytes.toByteArray();

					Globals.mChatService.write(image, IMAGE);
				}

			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// method related to image to show the path

	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);

		// Get the BluetoothDevice object
		device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		Globals.mChatService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// if you want the user to be able to choose any file in the system ,
	// you will need to include your own file manager

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					REQUEST_FILE_SELECT);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	// to choose you want such as (scan,share,image ,discoverable)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.share:
			// Start a chat session with a paired device
			showFileChooser();
			return true;
		case R.id.send_image:
			selectImage();
			return true;
		}
		return false;
	}

	// new method into Main activity file called "selectImage () "
	// it is used for to choose any one option from camera,gallery and cancel in
	// alert dialog box
	// allow the user to choose camera or gallery
	// by using Action pick activity
	// started by using the ACTION_GET_CONTENT intent, and that works well for
	// getting to the gallery.

	private void selectImage() {
		final CharSequence[] items = { "Take Photo", "Choose from Library",
				"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(
				BluetoothChat.this);
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Take Photo")) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File f = new File(android.os.Environment
							.getExternalStorageDirectory(),
							"/bluetoothchat/temp.jpg");
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, REQUEST_CAMERA);
				} else if (items[item].equals("Choose from Library")) {
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent, "Select File"),
							SELECT_FILE);
				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();

	}

}
