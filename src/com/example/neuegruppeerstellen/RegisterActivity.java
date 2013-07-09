package com.example.neuegruppeerstellen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neuegruppeerstellen.messageEndpoint.MessageEndpoint;
import com.example.neuegruppeerstellen.messageEndpoint.model.CollectionResponseMessageData;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;

public class RegisterActivity extends Activity {

	enum State {
		REGISTERED, REGISTERING, UNREGISTERED, UNREGISTERING
	}

	private State curState = State.UNREGISTERED;
	private OnTouchListener registerListener = null;
	private OnTouchListener unregisterListener = null;
	private MessageEndpoint messageEndpoint = null;

	ListView msgList;
	ArrayList<MessageDetails> details;
	AdapterView.AdapterContextMenuInfo info;
	MessageDetails msg;

	public void showMessage() {
		ListAdapter listenAdapter = new ArrayAdapter<MessageDetails>(this,
				R.layout.list_view_main, R.layout.list_item_message, details);
		ListView lv_groups = (ListView) findViewById(R.id.MessageList);
		lv_groups.setAdapter(listenAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view_main);
		msgList = (ListView) findViewById(R.id.MessageList);

		details = new ArrayList<MessageDetails>();
		// fillDummyData();

		// TESTWEISE HARD KODIERTE DATEN
		// TODO: Bei fillDummyData() stürzt die App nach einer Zeit ab
		// obwohl die Liste mit korrekten Kontakten gefüllt wird
		MessageDetails msgDetails = new MessageDetails();
		msgDetails.setIcon(R.drawable.ic_launcher);
		msgDetails.setName("Marie");
		msgDetails.setEta("12:12");
		msgDetails.setDistToGoal("110 km");
		msgDetails.setDistToMe("25 km");
		msgDetails.setAverageSpeed("164 kmh");
		msgDetails.setSpeed("121 kmh");
		details.add(msgDetails);

		msgDetails = new MessageDetails();
		msgDetails.setIcon(R.drawable.ic_launcher);
		msgDetails.setName("Wayne");
		msgDetails.setEta("21:21");
		msgDetails.setDistToGoal("56 km");
		msgDetails.setDistToMe("12 km");
		msgDetails.setAverageSpeed("234 kmh");
		msgDetails.setSpeed("123 kmh");
		details.add(msgDetails);
		// TESTWEISE HARD KODIERTE DATEN

		msgList.setAdapter(new CustomAdapter(details, this));
		registerForContextMenu(msgList);

		if (GCMIntentService.PROJECT_NUMBER == null
				|| GCMIntentService.PROJECT_NUMBER.length() == 0) {
			showDialog("Unable to register for Google Cloud Messaging. "
					+ "Your application's PROJECT_NUMBER field is unset! You can change "
					+ "it in GCMIntentService.java");
		} else {
			try {
				GCMIntentService.register(getApplicationContext());
			} catch (Exception e) {
				Log.e(RegisterActivity.class.getName(),
						"Exception received when attempting to register for Google Cloud "
								+ "Messaging. Perhaps you need to set your virtual device's "
								+ " target to Google APIs? "
								+ "See https://developers.google.com/eclipse/docs/cloud_endpoints_android"
								+ " for more information.", e);
				showDialog("There was a problem when attempting to register for "
						+ "Google Cloud Messaging. If you're running in the emulator, "
						+ "is the target of your virtual device set to 'Google APIs?' "
						+ "See the Android log for more details.");
			}
		}

		/*
		 * build the messaging endpoint so we can access old messages via an endpoint call
		 */
		MessageEndpoint.Builder endpointBuilder = new MessageEndpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
				new HttpRequestInitializer() {
					public void initialize(HttpRequest httpRequest) {
					}
				});

		messageEndpoint = CloudEndpointUtils.updateBuilder(endpointBuilder)
				.build();

		// Phonestate Listener
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		/*
		 * If we are dealing with an intent generated by the GCMIntentService class, then display
		 * the provided message.
		 */
		if (intent.getBooleanExtra("gcmIntentServiceMessage", false)) {
			showDialog(intent.getStringExtra("message"));
			if (intent.getBooleanExtra("registrationMessage", false)) {
			} else {
				/*
				 * if we didn't get a registration/unregistration message then go get the last 5
				 * messages from app-engine
				 */
				new QueryMessagesTask(this, messageEndpoint).execute();
			}
		}
	}

	private void showDialog(String message) {
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						}).show();
	}

	/*
	 * Need to run this in background so we don't hold up the UI thread, this task will ask the App
	 * Engine backend for the last 5 messages sent to it
	 */
	private class QueryMessagesTask extends
			AsyncTask<Void, Void, CollectionResponseMessageData> {
		Exception exceptionThrown = null;
		Activity activity;
		MessageEndpoint messageEndpoint;

		public QueryMessagesTask(Activity activity,
				MessageEndpoint messageEndpoint) {
			this.activity = activity;
			this.messageEndpoint = messageEndpoint;
		}

		@Override
		protected CollectionResponseMessageData doInBackground(Void... params) {
			try {
				CollectionResponseMessageData messages = messageEndpoint
						.listMessages().setLimit(20).execute();
				return messages;
			} catch (IOException e) {
				exceptionThrown = e;
				return null;
				// Handle exception in PostExecute
			}
		}

		protected void onPostExecute(CollectionResponseMessageData messages) {
			// Check if exception was thrown
			if (exceptionThrown != null) {
				Log.e(RegisterActivity.class.getName(),
						"Exception when listing Messages", exceptionThrown);
				showDialog("Failed to retrieve the last 5 messages from "
						+ "the endpoint at " + messageEndpoint.getBaseUrl()
						+ ", check log for details");
			} else {
				// Nachricht empfangen
				String message = messages.getItems().get(0).getMessage();
				String[] temp = message.split(" ");
				String name = temp[0];
				String eta = temp[1];

				Toast.makeText(getApplicationContext(),
						"toast mit message " + message, Toast.LENGTH_LONG)
						.show();

				MessageDetails detail = new MessageDetails();
				detail.setIcon(R.drawable.ic_launcher);
				detail.setName(name);
				detail.setEta(eta);

				int indexOfEntry = -1;
				for (int i = 0; i < details.size(); i++) {
					if (details.get(i).getName().equals(name)) {
						indexOfEntry = i;

						break;
					}
				}
				if (indexOfEntry != -1) {
					details.set(indexOfEntry, detail);
				}
				// showMessage();
				msgList.setAdapter(new CustomAdapter(details, activity));
			}
		}
	}

	// enthält die Logik der ListView
	public class CustomAdapter extends BaseAdapter {
		private ArrayList<MessageDetails> data;
		Context context;

		CustomAdapter(ArrayList<MessageDetails> data, Context c) {
			this.data = data;
			this.context = c;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item_message, null);
			}

			// alle Komponenten des "list_item_message.xml" Layouts holen
			ImageView imageView = (ImageView) v.findViewById(R.id.icon);
			TextView nameView = (TextView) v.findViewById(R.id.name);
			TextView etaView = (TextView) v.findViewById(R.id.eta);
			Button button_info = (Button) v.findViewById(R.id.button_info);

			ImageButton button_anruf = (ImageButton) v
					.findViewById(R.id.button_anruf);

			msg = data.get(position);
			imageView.setImageResource(msg.icon);
			nameView.setText(msg.name);
			etaView.setText("ETA: " + msg.eta);

			// Info-Button implementieren
			button_info.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// aktuell ausgewähltes Item holen (muss pro Klick gemacht
					// werden)
					msg = data.get(position);

					Toast.makeText(getApplicationContext(),
							"Info für " + msg.getName(), Toast.LENGTH_SHORT)
							.show();
					showPersonInfo(msg);
				}

				// Popup
				private void showPersonInfo(MessageDetails msg) {
					String name = msg.getName();
					String averageSpeed = msg.getAverageSpeed();
					String speed = msg.getSpeed();
					String distToGoal = msg.getDistToGoal();
					String distToMe = msg.getDistToMe();

					String completeMessage = "Speed: " + speed + "\n"
							+ "Average Speed: " + averageSpeed + "\n"
							+ "Distance to Goal: " + distToGoal + "\n"
							+ "Distance to Me: " + distToMe;

					AlertDialog.Builder helpBuilder = new AlertDialog.Builder(
							context);
					helpBuilder.setTitle("Infos für " + name);
					helpBuilder.setMessage(completeMessage);
					helpBuilder.setPositiveButton("",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// Popup bei Klick schließen
								}
							});
					AlertDialog helpDialog = helpBuilder.create();
					helpDialog.show();
				}
			});

			// Anruf-Button implementieren
			button_anruf.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:000111222333"));
					startActivity(callIntent);
				}
			});
			return v;
		}
	}

	// monitor phone call activities
	private class PhoneCallListener extends PhoneStateListener {


		private boolean isPhoneCalling = false;

		String LOG_TAG = "LOGGING 123";

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {


			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// phone ringing
				Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
			}


			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				Log.i(LOG_TAG, "OFFHOOK");

				isPhoneCalling = true;
			}


			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended,
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(LOG_TAG, "IDLE");

				if (isPhoneCalling) {



					Log.i(LOG_TAG, "restart app");

					// restart app
					// Intent i = getBaseContext().getPackageManager()
					// .getLaunchIntentForPackage(
					// getBaseContext().getPackageName());
					// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					//
					// startActivity(i);



					Log.i(LOG_TAG, "restart app");

					// restart app
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

					isPhoneCalling = false;
				}

			}
		}
	}

	private void fillDummyData() {
		ArrayList<String> readFromFile = readFromFile("tempGroupName.txt");

		// tempDatei leeren
		File dir = getFilesDir();
		File file = new File(dir, "tempGroupName.txt");

		if (file.delete()) {
			Toast.makeText(this, file.getName() + " wurde gelöscht!",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Löschoperation ist fehlgeschlagen!",
					Toast.LENGTH_SHORT).show();
		}// tempDatei leeren ende

		String group_selected = readFromFile.get(0);
		ArrayList<String> data_content = readFromFile(group_selected + ".txt");

		// showContacts(data_content);
	}

	private void showContacts(ArrayList<String> data_content) {
		ArrayList<String> contactListOnly = new ArrayList<String>();

		// aktuelle Mitgliederliste auslesen und ArrayList fuellen
		for (int i = 3; i < data_content.size(); i++) {
			MessageDetails messageDetails = new MessageDetails();
			messageDetails.setName(data_content.get(i));
			messageDetails.setEta("10:00");
			messageDetails.setIcon(R.drawable.ic_launcher);
			details.add(messageDetails);
		}
	}

	private ArrayList<String> readFromFile(String filename) {
		String TAG = MeineGruppen.class.getName();

		ArrayList<String> result = new ArrayList<String>();
		try {
			InputStream inputStream = openFileInput(filename);
			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";

				while ((receiveString = bufferedReader.readLine()) != null) {
					if (receiveString.length() != 0)
						result.add(receiveString);
				}
				inputStream.close();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e(TAG, "Can not read file: " + e.toString());
		}
		return result;
	}

	public void writeToFile(String filename, String data) {
		String TAG = MeineGruppen.class.getName();
		try {
			FileOutputStream fos = openFileOutput(filename, Context.MODE_APPEND);
			fos.write(data.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			Log.e(TAG, "File write failed: " + e.toString());
		}
	}

}