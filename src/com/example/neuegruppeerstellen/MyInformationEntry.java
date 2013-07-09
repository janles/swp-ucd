package com.example.neuegruppeerstellen;

public class MyInformationEntry {

	private String name;
	private String eta;

	public MyInformationEntry(String name, String eta) {
		this.name = name;
		this.eta = eta;
	}

	public String getEta() {
		return eta;
	}

	public void setEta(String eta) {
		this.eta = eta;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name + "\n" + "ETA: " + eta;
	}
}
