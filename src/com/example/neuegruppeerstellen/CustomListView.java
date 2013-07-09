package com.example.neuegruppeerstellen;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomListView extends Activity {

	ListView msgList;
	ArrayList<MessageDetails> details;
	AdapterView.AdapterContextMenuInfo info;
	MessageDetails msg;

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
			ImageView image = (ImageView) v.findViewById(R.id.icon);
			TextView fromView = (TextView) v.findViewById(R.id.name);
			TextView subView = (TextView) v.findViewById(R.id.eta);
			Button button_info = (Button) v.findViewById(R.id.button_info);
			Button button_anruf = (Button) v.findViewById(R.id.button_anruf);

			msg = data.get(position);
			image.setImageResource(msg.icon);
			fromView.setText(msg.name);
			subView.setText("ETA: " + msg.eta);

			// Info-Button implementieren
			button_info.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// aktuell ausgewähltes Item holen (muss pro Klick gemacht werden)
					msg = data.get(position);

					Toast.makeText(getApplicationContext(),
							"Info für " + msg.getName(), Toast.LENGTH_SHORT)
							.show();
				}
			});

			// Anruf-Button implementieren
			button_anruf.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

				}
			});
			return v;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		msgList = (ListView) findViewById(R.id.MessageList);
		msgList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				System.out.println("Name: " + details.get(position).getName());
				String s = (String) ((TextView) v.findViewById(R.id.name))
						.getText();
				Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG)
						.show();
			}
		});

		details = new ArrayList<MessageDetails>();
		fillDummyData(); // Testdaten
		msgList.setAdapter(new CustomAdapter(details, this));
		registerForContextMenu(msgList);

	}


	private void fillDummyData() {

		MessageDetails Detail;
		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Bob");
		Detail.setEta("12:34");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Rob");
		Detail.setEta("22:22");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Mike");
		Detail.setEta("00:14");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Bike");
		Detail.setEta("16:16");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Hike");
		Detail.setEta("12:39");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Ramos");
		Detail.setEta("18:92");
		details.add(Detail);

		Detail = new MessageDetails();
		Detail.setIcon(R.drawable.ic_launcher);
		Detail.setName("Luhukay");
		Detail.setEta("18:92");
		details.add(Detail);
	}
}
