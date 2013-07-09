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
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MeineGruppen extends Activity implements OnItemClickListener {
	private ArrayList<String> gruppenListe = new ArrayList<String>();
	private Button buttonAustreten, buttonAnzeigen;
	private TextView tv_keine_gruppen;
	private String group_selected = ""; // ausgewaehlte Gruppe fuer
										// "Gruppe anzeigen"

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meine_gruppen);

		// Initialisierung der Activity
		showMyGroups();
		String readData = readFromFile(NeueGruppeErstellen.allGroups);

		ListView lv_groups = (ListView) findViewById(R.id.lv_group);
		lv_groups.setOnItemClickListener(this);

		buttonAustreten = (Button) findViewById(R.id.buttonAustreten);
		buttonAnzeigen = (Button) findViewById(R.id.buttonAnzeigen);
		tv_keine_gruppen = (TextView) findViewById(R.id.tv_KeineGruppen);

		noGroupsTest(readData);
	}

	/**
	 * Wenn keine Gruppen vorhanden sind, soll eine entsprechende TextView angezeigt werden
	 * 
	 * @param data
	 *            Dateiinhalt
	 */
	private void noGroupsTest(String data) {
		if (data.length() <= 0) {
			tv_keine_gruppen.setVisibility(View.VISIBLE);
		} else {
			tv_keine_gruppen.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * aktuell eingetragene Gruppen in einer ListView anzeigen
	 */
	public void showMyGroups() {
		ListAdapter listenAdapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_multiple_choice, gruppenListe);
		ListView lv_groups = (ListView) findViewById(R.id.lv_group);
		lv_groups.setAdapter(listenAdapter);
	}

	private String readFromFile(String filename) {
		String TAG = MeineGruppen.class.getName();
		String result = "";
		// Liste leeren, damit die Gruppen nicht nur angehangen werden
		gruppenListe.clear();
		try {
			InputStream inputStream = openFileInput(filename);
			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				showMyGroups();
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
					// hier wird jede Zeile eingefügt
					gruppenListe.add(receiveString);
				}
				inputStream.close();
				result = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e(TAG, "Can not read file: " + e.toString());
		}
		return result;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ListView lv_groups = (ListView) findViewById(R.id.lv_group);

		// zaehlt alle ausgewaehlten Items aus der Liste
		int countCheckedItems = 0;
		int len = lv_groups.getCount();
		SparseBooleanArray checked = lv_groups.getCheckedItemPositions();
		for (int i = 0; i < len; i++) {
			if (checked.get(i)) {
				countCheckedItems++;
				// hier wird immer das zuletzt angetippte Item gespeichert
				group_selected = gruppenListe.get(i);
			}
		}

		// nichts ausgewaehlt ==> keine Button anzeigen
		if (countCheckedItems <= 0) {
			buttonAnzeigen.setVisibility(View.INVISIBLE);
			buttonAustreten.setVisibility(View.INVISIBLE);
		}
		// bei genau einem ausgewaehltem Item beide Button anzeigen
		else if (countCheckedItems == 1) {
			buttonAnzeigen.setVisibility(View.VISIBLE);
			buttonAustreten.setVisibility(View.VISIBLE);

		}
		// wenn mehrere Items ausgewaehlt sind, wird nur der "austreten"-Button
		// angezeigt
		else if (countCheckedItems > 1) {
			buttonAnzeigen.setVisibility(View.INVISIBLE);
			buttonAustreten.setVisibility(View.VISIBLE);
		}
	}

	// Button "anzeigen"
	public void gruppeAnzeigen(View view) {
		// dem neuen Intent muss die aktuell ausgewaehlte Gruppe mitgegeben werden (group_selected)
		Bundle data_1 = new Bundle();
		data_1.putString("datenpaket1", group_selected);

		Intent in = new Intent(MeineGruppen.this, GruppeAnzeigen.class);
		in.putExtras(data_1);

		startActivity(in);
	}
	
	// Button "Neue Gruppe erstellen"
		public void neueGruppeErstellen(View view) {
			startActivity(new Intent(MeineGruppen.this, NeueGruppeErstellen.class));
		}

	// Button "austreten"
	public void gruppeAustreten(View view) {
		// alle angeklickten (d.h. zu loeschenden) Gruppen in einer Liste
		// speichern
		ArrayList<String> deleteItems = new ArrayList<String>();
		ListView lv_groups = (ListView) findViewById(R.id.lv_group);
		SparseBooleanArray checkedItems = lv_groups.getCheckedItemPositions();
		if (checkedItems != null) {
			for (int i = 0; i < checkedItems.size(); i++) {
				// Item nur in die Liste nehmen, wenn es angeklickt (true) ist
				if (checkedItems.valueAt(i)) {
					int keyAt = checkedItems.keyAt(i);
					deleteItems.add(lv_groups.getItemAtPosition(keyAt) + "");
				}
			}
		}

		// ausgewaehlte Dateien loeschen...
		deleteGroups(deleteItems);
		if (gruppenListe.size() <= 0) {
			tv_keine_gruppen.setVisibility(View.VISIBLE);
		}
		buttonAnzeigen.setVisibility(View.INVISIBLE);
		buttonAustreten.setVisibility(View.INVISIBLE);
	}

	// loescht alle Dateien, die sich in deleteItems befinden
	private void deleteGroups(ArrayList<String> deleteItems) {
		for (String filename : deleteItems) {
			File dir = getFilesDir();
			File file = new File(dir, filename + ".txt");

			if (file.delete()) {
				// Toast.makeText(this, file.getName() + " wurde gelöscht!",
				// Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Löschoperation ist fehlgeschlagen!",
						Toast.LENGTH_SHORT).show();
			}

			// in "allGroups.txt" Gruppennamen loeschen
			deleteGroupsFromAllGroups(deleteItems);
		}
	}

	private void deleteGroupsFromAllGroups(ArrayList<String> deleteItems) {
		for (String group : deleteItems) {
			gruppenListe.remove(group);
		}

		// allGroups.txt loeschen
		File dir = getFilesDir();
		File file = new File(dir, "allGroups.txt");
		if (file.delete()) {
			// Toast.makeText(this, file.getName() + " wurde gelöscht!",
			// Toast.LENGTH_SHORT).show();
		} else {
			// Toast.makeText(this, "Löschoperation ist fehlgeschlagen!",
			// Toast.LENGTH_SHORT).show();
		}

		// allGroups.txt ohne die geloeschten Gruppen schreiben
		for (String group : gruppenListe) {
			writeToFile("allGroups.txt", group + "\n");
		}
		showMyGroups();
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
	
	public void onResume() {
		super.onResume();
		readFromFile(NeueGruppeErstellen.allGroups);
		showMyGroups();
		buttonAnzeigen.setVisibility(View.INVISIBLE);
		buttonAustreten.setVisibility(View.INVISIBLE);
	}
}
