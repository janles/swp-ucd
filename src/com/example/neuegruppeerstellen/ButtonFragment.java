package com.example.neuegruppeerstellen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neuegruppeerstellen.R;

public class ButtonFragment extends Fragment {

	 @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_buttons,
	        container, false);
	    return view;
	  }	
}
