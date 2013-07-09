package com.example.androidsearchcontact;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

	private String name;
	private String lookupK;
	
	public Contact (String name, String lookupK){
		this.name = name;
		this.lookupK = lookupK;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLookupK() {
		return lookupK;
	}
	public void setLookupK(String lookupK) {
		this.lookupK = lookupK;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
