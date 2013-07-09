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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidsearchcontact.ContactSearchActivity;

public class GruppeAnzeigen extends Activity {
	private ArrayList<String> data_content = new ArrayList<String>();
	private ArrayList<String> kontakteListe = new ArrayList<String>();
	private String gruppenname;
	private String fahrtziel;
	private String ankunftszeit;
	private String contacts = "Contacts2.txt";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gruppe_anzeigen);

		TextView tv_gruppenname = (TextView) findViewById(R.id.gruppe_anzeigen_gruppenname);
		TextView tv_fahrtziel = (TextView) findViewById(R.id.gruppe_anzeigen_fahrtziel);
		TextView tv_ankunftszeit = (TextView) findViewById(R.id.tv_uhrzeit_auswahl);

		// Daten vom vorherigen Screen entgegennehmen; ausgewaehlte Grupp
		Bundle zielkorb = getIntent().getExtras();

		String group_selected = zielkorb.getString("datenpaket1");

		data_content = readFromFile(group_selected + ".txt");

		gruppenname = data_content.get(0);
		fahrtziel = data_content.get(1);
		ankunftszeit = data_content.get(2);

		tv_gruppenname.setText(gruppenname);
		tv_fahrtziel.setText(fahrtziel);
		tv_ankunftszeit.setText(ankunftszeit);

		setTitle("Übersicht - " + gruppenname);
		showContacts();
	}

	private void showContacts() {
		ArrayList<String> contactListOnly = new ArrayList<String>();
		// aktuelle Mitgliederliste auslesen
		data_content = readFromFile(gruppenname + ".txt");
		for (int i = 3; i < data_content.size(); i++) {
			contactListOnly.add(data_content.get(i));
		}

		ListAdapter listenAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, contactListOnly);
		ListView lv_contacts = (ListView) findViewById(R.id.lv_kontakte);
		lv_contacts.setAdapter(listenAdapter);
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

	// Button: "Gruppenmitglieder bearbeiten"
	public void gruppenMitgliederBearbeiten(View view) {
		Bundle data_1 = new Bundle();
		data_1.putString("datenpaket2", data_content.get(0));
		Intent in = new Intent(getApplicationContext(),
				ContactSearchActivity.class);
		in.putExtras(data_1);

		startActivity(in);
	}

	// Button: "speichern"
	public void gruppenMitgliederAktualisieren(View view) {
		File dir = getFilesDir();
		// nur wenn "contacts2.txt" angelegt wurde (also etwas an den GM
		// veraendert wurde) darf die Datei ausgelesen werden
		File contact = new File(dir, contacts);
		if (contact.exists()) {
			File gruppe = new File(dir, gruppenname + ".txt");
			gruppe.delete();

			String output = gruppenname + "\n" + fahrtziel + "\n";
			writeToFile(gruppenname + ".txt", output);

			// Contacts auslesen und in 'GruppenName.txt' einfügen
			// Kontakte müssen erneut ausgelesen werden

			kontakteListe = readFromFile(contacts);
			for (String name : kontakteListe) {
				writeToFile(gruppenname + ".txt", name + "\n");
			}

			// wenn Schreiben erfolgreich, dann wird contacts.txt gelöscht
			if (!contact.exists())
				Toast.makeText(getApplicationContext(),
						"Datei existiert nicht", Toast.LENGTH_SHORT).show();
			contact.delete();
			showContacts();
		}
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

	public void showMyContacts() {
		ListAdapter listenAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, kontakteListe);
		ListView lv_kontakte = (ListView) findViewById(R.id.lv_kontakte);
		lv_kontakte.setAdapter(listenAdapter);
	}

	public void onResume() {
		super.onResume();
		gruppenMitgliederAktualisieren(null);
	}

	public void fahrtStarten(View view) {
		Intent in = new Intent(getApplicationContext(),
				Tab_List_Map.class);
		startActivity(in);
	}
}
