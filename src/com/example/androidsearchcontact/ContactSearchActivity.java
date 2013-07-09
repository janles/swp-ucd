package com.example.androidsearchcontact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;

import com.example.neuegruppeerstellen.R;

public class ContactSearchActivity extends ListActivity implements
		OnQueryTextListener, LoaderCallbacks<Cursor> {

	private static final String CONTACTS_FILE_NAME = "Contacts.txt";
	private SimpleCursorAdapter simpleCursorAdapter;
	private String cursorFilter;
	private List<String> contacts = new ArrayList<String>();
	private ListView listView;
	private boolean areContactsSet = false;
	private final int GREEN = Color.rgb(170, 230, 100);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kontakte_hinzufuegen);
		listView = (ListView) findViewById(android.R.id.list);

		String[] from = new String[] { Contacts.DISPLAY_NAME };

		int[] to = new int[] { android.R.id.text1};
		// ListView liste=(ListView) findViewById(R.id.list);

		simpleCursorAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_multiple_choice , null, from, to, 0);
		setListAdapter(simpleCursorAdapter);

		LoaderManager loaderManager = getLoaderManager();
		loaderManager.initLoader(0, null, this);
//		listView.setAdapter(simpleCursorAdapter);
		String raw_contacts = readFromFile(CONTACTS_FILE_NAME);
		contacts = makeContactList(raw_contacts);
		if (!contacts.isEmpty()) {
			setSelectedContacts();
		}
	}

	private void setSelectedContacts() {
		int listSize = listView.getCount();
		for (String contact : contacts) {
			for (int i = 0; i < listSize; i++) {
				Cursor cursor = (Cursor) listView.getItemAtPosition(i);
				String contactName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
				if (contactName.equalsIgnoreCase(contact)) {
					listView.setItemChecked(i, true);
				}
			}
		}
	}
	
	private ArrayList<String> makeContactList(String contactsFromFile) {
		String[] contacts = contactsFromFile.split("\n");
		ArrayList<String> contactNames = new ArrayList<String>(
				Arrays.asList(contacts));
		return contactNames;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);

		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SearchView searchView = new SearchView(ContactSearchActivity.this);
		searchView.setOnQueryTextListener(this);
		// Suchfeld weiss faerben
		searchView.setBackgroundColor(-1);
		item.setActionView(searchView);

		return true;

	}
	
	@Override
	public void onResume() {
		super.onResume();
		String rawContacts = readFromFile(CONTACTS_FILE_NAME);
		contacts = makeContactList(rawContacts);
		setSelectedContacts();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Uri baseUri;
		if (cursorFilter != null) {
			baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
					Uri.encode(cursorFilter));
		} else {
			baseUri = Contacts.CONTENT_URI;
		}

		String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
				+ Contacts.HAS_PHONE_NUMBER + "=1) AND ("
				+ Contacts.DISPLAY_NAME + " != '' ))";

		String[] projection = new String[] { Contacts._ID,
				Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS,
				Contacts.CONTACT_PRESENCE, Contacts.PHOTO_ID,
				Contacts.LOOKUP_KEY, };

		CursorLoader cursorLoader = new CursorLoader(
				ContactSearchActivity.this, baseUri, projection, select, null,
				Contacts.DISPLAY_NAME);

		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		simpleCursorAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		simpleCursorAdapter.swapCursor(null);
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		cursorFilter = !TextUtils.isEmpty(arg0) ? arg0 : null;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (!areContactsSet) {
			setSelectedContacts();
			areContactsSet = true;
		}
	}

	public String readFromFile(String filename) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream is = openFileInput(filename);
			if (is != null) {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String tmp = "";

				while ((tmp = br.readLine()) != null) {
					sb.append(tmp + "\n");
				}
				is.close();
			}
		} catch (FileNotFoundException f) {
			f.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}
		return sb.toString();
	}

	public void writeFile(String filename) {
		File dir = getFilesDir();
		File file = new File(dir, filename);
		file.delete();
		FileOutputStream fos;
		StringBuilder sb = new StringBuilder();
		try {
			fos = new FileOutputStream(file);
			for (String contact : contacts) {
				String tmp = contact + "\n";
//				fos.write(tmp.getBytes());
//				fos.flush();
				sb.append(tmp);
			}
			fos.write(sb.toString().getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveContacts(View v) {
		if (v.getId() == R.id.saveContacts) {
			contacts.clear();
			int listSize = listView.getCount();
			SparseBooleanArray listItemsChecked = listView.getCheckedItemPositions();			
			for (int i = 0; i < listSize; i++) {
				if (listItemsChecked.get(i)) {
					Cursor cursor = (Cursor) listView.getItemAtPosition(i);
					String contactName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
					if (!contacts.contains(contactName)) {	
						contacts.add(contactName);
					}
				}
			}
			writeFile(CONTACTS_FILE_NAME);
			finish();
		}
	}
}