package com.example.neuegruppeerstellen;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// falls die contacte Liste noch da ist, wird sie gelöscht
		File dir = getFilesDir();
		File contact = new File(dir, "Contacts.txt");
		contact.delete();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// bei Klick auf "Neue Gruppe erstellen" wird eine neue Activity gestartet
	public void gruppeErstellen(View view) {
		startActivity(new Intent(MainActivity.this, NeueGruppeErstellen.class));
	}

	// bei Klick auf "Meine Gruppen" wird eine neue Activity gestartet
	public void meineGruppen(View view) {
		startActivity(new Intent(MainActivity.this, MeineGruppen.class));
	}

}
