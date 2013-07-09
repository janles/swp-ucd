package com.example.neuegruppeerstellen;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class Tab_List_Map extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_list_map);

		Resources ressources = getResources();
		TabHost tabHost = getTabHost();

		// Karte Tab
		Intent intentKarte = new Intent().setClass(this,
				MapOverviewActivity.class);
		TabSpec tabSpecKarte = tabHost.newTabSpec("Karte")
				.setIndicator("Karte").setContent(intentKarte);

		// Liste Tab
		Intent intentListe = new Intent()
				.setClass(this, RegisterActivity.class);
		TabSpec tabSpecListe = tabHost.newTabSpec("Liste")
				.setIndicator("Liste").setContent(intentListe);

		// alle Tabs hinzufuegen
		tabHost.addTab(tabSpecKarte);
		tabHost.addTab(tabSpecListe);

		// aktuell gewaehlten Tab setzen
		tabHost.setCurrentTabByTag("Liste");
	}
}
